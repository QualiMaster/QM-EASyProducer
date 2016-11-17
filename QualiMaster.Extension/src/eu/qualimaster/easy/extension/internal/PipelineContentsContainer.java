/*
 * Copyright 2016 University of Hildesheim, Software Systems Engineering
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.qualimaster.easy.extension.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.qualimaster.coordination.RepositoryConnector;
import eu.qualimaster.coordination.RuntimeVariableMapping;
import eu.qualimaster.coordination.RepositoryConnector.Models;
import eu.qualimaster.coordination.RepositoryConnector.Phase;
import eu.qualimaster.easy.extension.QmConstants;
import net.ssehub.easy.varModel.confModel.Configuration;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.model.values.ContainerValue;
import net.ssehub.easy.varModel.model.values.ReferenceValue;

/**
 * Stores information of a pipeline, which is relevant for the {@link IvmlElementIdentifier}.
 * @author El-Sharkawy
 *
 */
class PipelineContentsContainer {
    
    /**
     * Denotes which kind of model element was mapped / shall be returned. 
     * @author El-Sharkawy
     *
     */
    public static enum MappedInstanceType {
        SOURCE, ALGORITHM, SINK;
    }
    
    private static Set<IDecisionVariable> allMappedVariables = new HashSet<>();
    
    // Element structure of pipeline (collected through visiting)
    private List<IDecisionVariable> sources = new ArrayList<>();
    private List<IDecisionVariable> familyElements = new ArrayList<>();
    private List<IDecisionVariable> dataManagementElements = new ArrayList<>();
    private List<IDecisionVariable> sinks = new ArrayList<>();
   
    // Mapped runtime elements (collected when init() is called for the first time)
    private Map<String, IDecisionVariable> algorithmMapping = new HashMap<>();
    private Map<String, IDecisionVariable> sourceMapping = new HashMap<>();
    private Map<String, IDecisionVariable> sinkMapping = new HashMap<>();
    
    private Models phase = null;
    
    /**
     * Adds a source of the pipeline.
     * @param source A source of the pipeline.
     */
    void addSource(IDecisionVariable source) {
        sources.add(source);
    }
    
    /**
     * Adds a sink of the pipeline.
     * @param sink A sink of the pipeline.
     */
    void addSink(IDecisionVariable sink) {
        sinks.add(sink);
    }
    
    /**
     * Adds a family element.
     * @param familyElement A family element of the pipeline.
     */
    void addFamilyElement(IDecisionVariable familyElement) {
        familyElements.add(familyElement);
    }
    
    /**
     * Adds a DataManagement element of the pipeline.
     * @param dataManagement A DataManagement element of the pipeline.
     */
    void addDataManagementElement(IDecisionVariable dataManagement) {
        dataManagementElements.add(dataManagement);
    }
    
    /**
     * Returns whether the given {@link IDecisionVariable} is a mapped runtime counterpart of a model element.
     * @param var The {@link IDecisionVariable} to test.
     * @return <tt>true</tt> if it is a runtime variable, <tt>false</tt> if it is an element of the originaly
     *     defined model.
     */
    public static boolean isMappingVariable(IDecisionVariable var) {
        return allMappedVariables.contains(var);
    }
    
    /**
     * Creates the algorithm structure, this includes the original algorithms
     * as well as the mapped runtime counterparts.
     */
    private void gatherAlgorithms() {
        for (int i = 0, end = familyElements.size(); i < end; i++) {
            IDecisionVariable familyElement = familyElements.get(i);
            List<IDecisionVariable> runtimeAglrotihms = getMappedMembers(familyElement);
            IDecisionVariable familySlot = familyElement.getNestedElement(QmConstants.SLOT_FAMILYELEMENT_FAMILY);
            if (null != familySlot) {
                Configuration config = familySlot.getConfiguration();
                ReferenceValue familyRef = (ReferenceValue) familySlot.getValue();
                IDecisionVariable orgFamily = PipelineVisitor.extractVar(familyRef, config);
                
                ContainerValue referencedOrgAlgos = null;
                if (null != orgFamily) {
                    referencedOrgAlgos = (ContainerValue) orgFamily.getNestedElement(
                            QmConstants.SLOT_FAMILY_MEMBERS).getValue(); 
                }
                
                collectAlgorithmFromFamily(config, referencedOrgAlgos, runtimeAglrotihms);
            }
        }
    }

    /**
     * Part of {@link #gatherAlgorithms()}, collects one (original) algorithm together with its mapped counterpart.
     * @param config The top level configuration, needed for querying {@link IDecisionVariable}s.
     * @param referencedOrgAlgos A container value containing reference values to algorithms.
     * @param runtimeAglrotihms A list of already mapped counterparts. Maybe <tt>null</tt>,
     *     in this case no mapping will be created.
     */
    private void collectAlgorithmFromFamily(Configuration config, ContainerValue referencedOrgAlgos,
        List<IDecisionVariable> runtimeAglrotihms) {
        int lastIndex = null != referencedOrgAlgos ? referencedOrgAlgos.getElementSize() : 0;
        
        for (int i = 0; i < lastIndex; i++) {
            ReferenceValue orgRef = (ReferenceValue) referencedOrgAlgos.getElement(i);
            IDecisionVariable orgAlgorithm = PipelineVisitor.extractVar(orgRef, config);
            String orgName = orgAlgorithm.getNestedElement(QmConstants.SLOT_NAME).getValue().getValue().toString();
            
            if (null != runtimeAglrotihms) {
                IDecisionVariable mappedAlgorithm = null;
                for (int j = 0, end = runtimeAglrotihms.size(); j < end && null == mappedAlgorithm; j++) {
                    IDecisionVariable tmpAlgo = runtimeAglrotihms.get(j);
                    if (tmpAlgo.getNestedElement(QmConstants.SLOT_NAME).getValue().getValue().equals(orgName)) {
                        mappedAlgorithm = tmpAlgo;
                        runtimeAglrotihms.remove(j);
                    }
                }
                
                if (null != mappedAlgorithm) {
                    algorithmMapping.put(orgName, mappedAlgorithm);
                    allMappedVariables.add(mappedAlgorithm);
                }
            }
        }
    }
    
    /**
     * Returns all mapped (runtime/adaptation) variables belonging to the given pipeline element.
     * @param originalVariable The original element of the pipeline
     * @return All mapped (runtime/adaptation) variables belonging to the given pipeline element
     *     (<b>null</b> if there is none)
     */
    private List<IDecisionVariable> getMappedMembers(IDecisionVariable originalVariable) {
        List<IDecisionVariable> mappedVariable = null;
        if (phase != null) {
            mappedVariable = phase.getVariableMapping().getMappedVariables(originalVariable);
        } else {
            Models tmpPhase = RepositoryConnector.getModels(Phase.ADAPTATION);
            RuntimeVariableMapping mapping = tmpPhase.getVariableMapping();
            if (null != mapping) {
                mappedVariable = mapping.getMappedVariables(originalVariable);
                if (null != mappedVariable) {
                    phase = tmpPhase;
                }
            }
            if (null == mappedVariable) {
                tmpPhase = RepositoryConnector.getModels(Phase.MONITORING);
                mapping = tmpPhase.getVariableMapping();
                if (null != mapping) {
                    mappedVariable = mapping.getMappedVariables(originalVariable);
                    if (null != mappedVariable) {
                        phase = tmpPhase;
                    }
                }
            }
        }
        
        return mappedVariable;
    }
    
//    /**
//     * Creates the sources structure, this includes the original sources
//     * as well as the mapped runtime counterparts.
//     */
//    private void gatherSources() {
//        for (int i = 0, end = sources.size(); i < end; i++) {
//            IDecisionVariable sourceElement = sources.get(i);
//            List<IDecisionVariable> runtimesources = getMappedMembers(sourceElement);
//            IDecisionVariable sourceSlot = sourceElement.getNestedElement(QmConstants.SLOT_SOURCE_SOURCE);
//            if (null != sourceSlot && null != runtimesources && !runtimesources.isEmpty()) {
//                Configuration config = sourceSlot.getConfiguration();
//                ReferenceValue sourceRef = (ReferenceValue) sourceSlot.getValue();
//                IDecisionVariable orgSource = PipelineVisitor.extractVar(sourceRef, config);
//                
//                if (null != orgSource) {
//                    String orgName = orgSource.getNestedElement(QmConstants.SLOT_NAME).getValue()
//                            .getValue().toString();
//                    sourceMapping.put(orgName, runtimesources.get(0));
//                    allMappedVariables.add(runtimesources.get(0));
//                }
//            }
//        }
//    }
    
    /**
     * Creates the mapping structure for mapping runtime variables. This does not work for algorithms of a family,
     * because they have a different nesting structure.
     * @param orignalVariables The list of original variables for which the mapped variables shall be retrieved.
     *   One of <tt>sources, sinks</tt>
     * @param slotName The slot name of the pipeline element, which is pointing to the selected element. 
     * @param mapping The mapping to be filled, e.g., (original source name, mapped source instance).
     */
    private void gatherMappedNonFamilyElement(List<IDecisionVariable> orignalVariables, String slotName,
        Map<String, IDecisionVariable> mapping) {
        
        for (int i = 0, end = orignalVariables.size(); i < end; i++) {
            IDecisionVariable orignalVariable = orignalVariables.get(i);
            List<IDecisionVariable> mappedRuntimeVariables = getMappedMembers(orignalVariable);
            IDecisionVariable pointerVariable = orignalVariable.getNestedElement(slotName);
            if (null != pointerVariable && null != mappedRuntimeVariables && !mappedRuntimeVariables.isEmpty()) {
                Configuration config = orignalVariable.getConfiguration();
                ReferenceValue referencedValue = (ReferenceValue) pointerVariable.getValue();
                IDecisionVariable orgReferencedVariable = PipelineVisitor.extractVar(referencedValue, config);
                
                if (null != orgReferencedVariable) {
                    String orgName = orgReferencedVariable.getNestedElement(QmConstants.SLOT_NAME).getValue()
                        .getValue().toString();
                    mapping.put(orgName, mappedRuntimeVariables.get(0));
                    allMappedVariables.add(mappedRuntimeVariables.get(0));
                }
            }
        }
    }
    
    /**
     * Creates structured information for dependent information, which is not collected directly during visiting.
     */
    void init() {
        gatherAlgorithms();
        gatherMappedNonFamilyElement(sources, QmConstants.SLOT_SOURCE_SOURCE, sourceMapping);
        gatherMappedNonFamilyElement(sinks, QmConstants.SLOT_SINK_SINK, sinkMapping);
        // TODO SE: ReplaySinks
    }
    
//    /**
//     * Returns the mapped algorithm instance for the given (configured) algorithm.
//     * @param originalAlgorithmName The user defined name of the algorithm.
//     * @return The configured name of the original algorithm from the model.
//     */
//    public IDecisionVariable getMappedAlgorithm(String originalAlgorithmName) {
//        return algorithmMapping.get(originalAlgorithmName);
//    }
//    
    /**
     * Returns the mapped instance for the given (configured) item.
     * @param type Specifies for which kind of pipeline element the mapped element shall be returned.
     * @param orgName The user defined name of the variable.
     * @return The configured name of the original algorithm from the model.
     */
    public IDecisionVariable getMappedInstance(MappedInstanceType type, String orgName) {
        IDecisionVariable result = null;
        
        if (null != type) {
            switch (type) {
            case SOURCE:
                result = sourceMapping.get(orgName);
                break;
            case ALGORITHM:
                result = algorithmMapping.get(orgName);
                break;
            case SINK:
                result = sinkMapping.get(orgName);
                break;
            default:
                Bundle.getLogger(PipelineContentsContainer.class).error("Undefined type passed: " + type.name());
                break;
            }
        }
        
        return result;
    }
    
    @Override
    public String toString() {
        return "Sources: " + sources + "\nFamilyElements: " + familyElements + "\nDataManagementElement: "
            + dataManagementElements + "\nSinks: " + sinks;
    }

}
