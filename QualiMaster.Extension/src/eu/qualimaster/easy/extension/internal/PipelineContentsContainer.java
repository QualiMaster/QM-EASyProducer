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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.qualimaster.adaptation.AdaptationManager;
import eu.qualimaster.coordination.INameMapping;
import eu.qualimaster.coordination.RepositoryConnector;
import eu.qualimaster.coordination.RepositoryConnector.IPhase;
import eu.qualimaster.coordination.RepositoryConnector.Models;
import eu.qualimaster.coordination.RepositoryConnector.Phase;
import eu.qualimaster.easy.extension.QmConstants;
import eu.qualimaster.monitoring.MonitoringManager;
import net.ssehub.easy.varModel.confModel.Configuration;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.model.values.ContainerValue;
import net.ssehub.easy.varModel.model.values.ReferenceValue;

/**
 * Stores information of a pipeline, which is relevant for the {@link IvmlElementIdentifier}.
 * 
 * @author El-Sharkawy
 * @author Holger Eichelberger
 */
public class PipelineContentsContainer {
    
    /**
     * Denotes which kind of model element was mapped / shall be returned. 
     * @author El-Sharkawy
     *
     */
    public static enum MappedInstanceType {
        SOURCE, ALGORITHM, SINK;
    }
    
    private static Set<IDecisionVariable> allMappedVariables = new HashSet<>();
    
    private String pipelineName;
    
    // Element structure of pipeline (collected through visiting)
    private List<IDecisionVariable> sources = new ArrayList<>();
    private List<IDecisionVariable> familyElements = new ArrayList<>();
    private List<IDecisionVariable> dataManagementElements = new ArrayList<>();
    private List<IDecisionVariable> sinks = new ArrayList<>();
    private List<IDecisionVariable> replaySinks = new ArrayList<>();
   
    // Mapped runtime elements (collected when init() is called for the first time)
    private Map<String, IDecisionVariable> algorithmMapping = new HashMap<>();
    private Map<String, IDecisionVariable> sourceMapping = new HashMap<>();
    private Map<String, IDecisionVariable> sinkMapping = new HashMap<>();
    
    private Models models = null;
    
    /**
     * Default constructor.
     */
    public PipelineContentsContainer() {
        this("<Unknown Pipeline>");
    }
    
    /**
     * Constructor for debugging purpose.
     * @param pipelineName The name of the pipeline. Is only used for debugging purpose.
     */
    public PipelineContentsContainer(String pipelineName) {
        this.pipelineName = pipelineName;
    }
    
    /**
     * Adds a source of the pipeline.
     * @param source A source of the pipeline.
     */
    void addSource(IDecisionVariable source) {
        sources.add(source);
    }
    
    /**
     * Adds a non ReplaySink of the pipeline.
     * @param sink A non ReplaySink of the pipeline.
     */
    void addSink(IDecisionVariable sink) {
        sinks.add(sink);
    }
    
    /**
     * Adds a ReplaySink of the pipeline.
     * @param replaySink A ReplaySink of the pipeline.
     */
    void addReplaySink(IDecisionVariable replaySink) {
        replaySinks.add(replaySink);
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
     * @return <code>true</code> if it is a runtime variable, <code>false</code> if it is an element of the originally
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
                IDecisionVariable orgFamily = Utils.extractVariable(familyRef, config);
                
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
     * @param runtimeAlgorithms A list of already mapped counterparts. Maybe <code>null</code>,
     *     in this case no mapping will be created.
     */
    private void collectAlgorithmFromFamily(Configuration config, ContainerValue referencedOrgAlgos,
        List<IDecisionVariable> runtimeAlgorithms) {
        int lastIndex = null != referencedOrgAlgos ? referencedOrgAlgos.getElementSize() : 0;
        
        INameMapping nMapping = getNameMapping();
        for (int i = 0; i < lastIndex; i++) {
            ReferenceValue orgRef = (ReferenceValue) referencedOrgAlgos.getElement(i);
            IDecisionVariable orgAlgorithm = Utils.extractVariable(orgRef, config);
            String orgName = orgAlgorithm.getNestedElement(QmConstants.SLOT_NAME).getValue().getValue().toString();
            
            if (null != runtimeAlgorithms) {
                IDecisionVariable mappedAlgorithm = null;
                for (int j = 0; j < runtimeAlgorithms.size() && null == mappedAlgorithm; j++) { // due to deletion
                    IDecisionVariable tmpAlgo = runtimeAlgorithms.get(j);
                    if (tmpAlgo.getNestedElement(QmConstants.SLOT_NAME).getValue().getValue().equals(orgName)) {
                        mappedAlgorithm = tmpAlgo;
                        runtimeAlgorithms.remove(j);
                    }
                }
                
                if (null != mappedAlgorithm) {
                    algorithmMapping.put(orgName, mappedAlgorithm);
                    String impl = NameMappingHelper.getAlgorithmImplName(nMapping, orgName);
                    if (null != impl && !impl.equals(orgName)) {
                        algorithmMapping.put(impl, mappedAlgorithm);
                    }
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
        Models models = getModels();
        if (null != models) {
            mappedVariable = models.getVariableMapping().getMappedVariables(originalVariable);
        }
        return mappedVariable;
    }
    
    /**
     * Creates the mapping structure for mapping runtime variables. This does not work for algorithms of a family,
     * because they have a different nesting structure.
     * @param orignalVariables The list of original variables for which the mapped variables shall be retrieved.
     *   One of <code>sources, sinks</code>
     * @param slotName The slot name of the pipeline element, which is pointing to the selected element. 
     * @param mapping The mapping to be filled, e.g., (original source name, mapped source instance).
     */
    private void gatherMappedNonFamilyElement(List<IDecisionVariable> orignalVariables, String slotName,
        Map<String, IDecisionVariable> mapping) {
        
        INameMapping nMapping = getNameMapping();
        for (int i = 0, end = orignalVariables.size(); i < end; i++) {
            IDecisionVariable orignalVariable = orignalVariables.get(i);
            List<IDecisionVariable> mappedRuntimeVariables = getMappedMembers(orignalVariable);
            IDecisionVariable pointerVariable = orignalVariable.getNestedElement(slotName);
            if (null != pointerVariable && null != mappedRuntimeVariables && !mappedRuntimeVariables.isEmpty()) {
                Configuration config = orignalVariable.getConfiguration();
                ReferenceValue referencedValue = (ReferenceValue) pointerVariable.getValue();
                IDecisionVariable orgReferencedVariable = Utils.extractVariable(referencedValue, config);
                
                if (null != orgReferencedVariable) {
                    String orgName = orgReferencedVariable.getNestedElement(QmConstants.SLOT_NAME).getValue()
                        .getValue().toString();
                    IDecisionVariable var = mappedRuntimeVariables.get(0);
                    mapping.put(orgName, var);
                    String impl = NameMappingHelper.getPipelineElementImplName(nMapping, orgName);
                    if (null != impl && !impl.equals(orgName)) {
                        mapping.put(impl, var);
                    }
                    // sources and sinks may currently occur as "algorithms" - this is ok as respective signals
                    // are sent. Indicate a mapping without variable so that no warnings are issued. mapping to var
                    // can lead to accidental overriding of values
                    algorithmMapping.put(orgName, null);
                    impl = NameMappingHelper.getAlgorithmImplName(nMapping, orgName);
                    if (null != impl && !impl.equals(orgName)) {
                        algorithmMapping.put(impl, null);
                    }
                    allMappedVariables.add(mappedRuntimeVariables.get(0));
                }
            }
        }
    }
    
    /**
     * Returns (and initializes lazily) the underlying models instance.
     * 
     * @return the models
     */
    private Models getModels() {
        if (null == models) {
            // try it via the phase set before VIL execution
            IPhase phase = RepositoryConnector.getPhase(Thread.currentThread());
            if (null != phase) {
                models = RepositoryConnector.getModels(phase);
            }
            if (null == models) { // fallback of original code, assumes that monitoring called first
                Models tmpModels = RepositoryConnector.getModels(Phase.ADAPTATION);
                if (null != tmpModels && null != tmpModels.getVariableMapping()) {
                    models = tmpModels;
                }
                if (null == models) {
                    tmpModels = RepositoryConnector.getModels(Phase.MONITORING);
                    if (null != tmpModels && null != tmpModels.getVariableMapping()) {
                        models = tmpModels;
                    }
                }
            }
        }
        return models;
    }
    
    /**
     * Returns the responsible name mapping.
     * 
     * @return the name mapping
     */
    private INameMapping getNameMapping() {
        INameMapping result = null;
        Models models = getModels();
        // just to keep layer separation!
        if (null != models) {
            if (Phase.ADAPTATION == models.getPhase()) {
                AdaptationManager.getNameMapping(pipelineName);
            } else if (Phase.MONITORING == models.getPhase()) {
                MonitoringManager.getNameMapping(pipelineName);
            }
        }
        return result;
    }
    
    /**
     * Creates structured information for dependent information, which is not collected directly during visiting.
     */
    void init() {
        gatherAlgorithms();
        gatherMappedNonFamilyElement(sources, QmConstants.SLOT_SOURCE_SOURCE, sourceMapping);
        gatherMappedNonFamilyElement(sinks, QmConstants.SLOT_SINK_SINK, sinkMapping);
        gatherMappedNonFamilyElement(replaySinks, QmConstants.SLOT_REPLAYSINK_SINK, sinkMapping);
    }

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

    /**
     * Returns whether there is a mapping (not necessarily a mapped instance) for the given (configured) item.
     * @param type Specifies for which kind of pipeline element the mapped element shall be returned.
     * @param orgName The user defined name of the variable.
     * @return <code>true</code> for mapping, <code>false</code> else
     */
    public boolean hasMapping(MappedInstanceType type, String orgName) {
        boolean result = false;
        
        if (null != type) {
            switch (type) {
            case SOURCE:
                result = sourceMapping.containsKey(orgName);
                break;
            case ALGORITHM:
                result = algorithmMapping.containsKey(orgName);
                break;
            case SINK:
                result = sinkMapping.containsKey(orgName);
                break;
            default:
                Bundle.getLogger(PipelineContentsContainer.class).error("Undefined type passed: " + type.name());
                break;
            }
        }
        
        return result;
    }
    
    /**
     * Returns a list of all member sources of the visited pipeline.
     * @return A unmodifiable list of source elements of the pipeline, won't be <code>null</code>.
     */
    public List<IDecisionVariable> getSources() {
        return Collections.unmodifiableList(new ArrayList<>(sources));
    }
    
    /**
     * Returns a list of all member non replay sinks of the visited pipeline.
     * @return A unmodifiable list of non replay sink elements of the pipeline, won't be <code>null</code>.
     */
    public List<IDecisionVariable> getSinks() {
        return Collections.unmodifiableList(new ArrayList<>(sinks));
    }
    
    /**
     * Returns a list of all member replay sinks of the visited pipeline.
     * @return A unmodifiable list of replay sink elements of the pipeline, won't be <code>null</code>.
     */
    public List<IDecisionVariable> getReplaySinks() {
        return Collections.unmodifiableList(new ArrayList<>(replaySinks));
    }
    
    /**
     * Returns a list of all member family elements of the visited pipeline.
     * @return A unmodifiable list of family elements of the pipeline, won't be <code>null</code>.
     */
    public List<IDecisionVariable> getFamilyElements() {
        return Collections.unmodifiableList(new ArrayList<>(familyElements));
    }
    
    /**
     * Returns a list of all member data management elements of the visited pipeline.
     * @return A unmodifiable list of data management elements of the pipeline, won't be <code>null</code>.
     */
    public List<IDecisionVariable> getDataManagementElements() {
        return Collections.unmodifiableList(new ArrayList<>(dataManagementElements));
    }
    
    @Override
    public String toString() {
        return "Sources: " + sources
            + "\nFamilyElements: " + familyElements
            + "\nDataManagementElement: " + dataManagementElements
            + "\nReplaySinks: " + replaySinks
            + "\nSinks: " + sinks;
    }
    
}
