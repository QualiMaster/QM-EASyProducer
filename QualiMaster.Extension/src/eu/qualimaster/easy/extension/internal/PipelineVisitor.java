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

import java.util.HashSet;
import java.util.Set;

import eu.qualimaster.easy.extension.QmConstants;
import net.ssehub.easy.varModel.confModel.Configuration;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.model.values.ContainerValue;
import net.ssehub.easy.varModel.model.values.ReferenceValue;
import net.ssehub.easy.varModel.model.values.Value;

/**
 * Visits a pipeline variable and gathers {@link PipelineContentsContainer}.
 * @author El-Sharkawy
 *
 */
public class PipelineVisitor {

    private PipelineContentsContainer container;
    private Configuration config;
    private boolean containerInitialized;
    private boolean initializeRuntimeMapping;
    private Set<IDecisionVariable> done = new HashSet<IDecisionVariable>();
    private IVariableMapper mapper;

    /**
     * Performs optional variable mapping.
     * 
     * @author Holger Eichelberger
     */
    interface IVariableMapper {

        /**
         * Maps original and copied variables.
         * 
         * @param original the original variable
         * @param copy the copied variable
         */
        public void map(IDecisionVariable original, IDecisionVariable copy);

    }
    
    /**
     * Constructor which will map runtime clones.
     * 
     * @param pipeline The pipeline for which the information should be extracted.
     * @param mapper optional variable mapper instance
     */
    PipelineVisitor(IDecisionVariable pipeline, IVariableMapper mapper) {
        this(pipeline, true, mapper);
    }
    
    /**
     * Constructor for deciding whether runtime instances shall be mapped or whether this is not needed. No mapper.
     * 
     * @param pipeline The pipeline for which the information should be extracted.
     * @param initializeRuntimeMapping <code>true</code> runtime variables will be mapped, <code>false</code> mapping 
     * is not needed. Mapping (<code>true</code>) is only needed during adaptation, no mapping (<code>false</code>) 
     * is needed in QM-Iconf application.
     */
    public PipelineVisitor(IDecisionVariable pipeline, boolean initializeRuntimeMapping) {
        this(pipeline, initializeRuntimeMapping, null);
    }

    /**
     * Constructor for deciding whether runtime instances shall be mapped or whether this is not needed. 
     * 
     * @param pipeline The pipeline for which the information should be extracted.
     * @param initializeRuntimeMapping <code>true</code> runtime variables will be mapped, <code>false</code> mapping 
     * is not needed. Mapping (<code>true</code>) is only needed during adaptation, no mapping (<code>false</code>) is 
     * needed in QM-Iconf application.
     * @param mapper variable mapper instance to link original and copied variables
     */
    public PipelineVisitor(IDecisionVariable pipeline, boolean initializeRuntimeMapping, IVariableMapper mapper) {
        this.mapper = mapper;
        this.initializeRuntimeMapping = initializeRuntimeMapping;
        String pipelineName = VariableHelper.getName(pipeline);
        if (null == pipelineName) {
            pipelineName = pipeline.getDeclaration().getName();
        }
        
        container = new PipelineContentsContainer(pipelineName);
        containerInitialized = false;
        this.config = pipeline.getConfiguration();
        ContainerValue sources = (ContainerValue) pipeline
            .getNestedElement(QmConstants.SLOT_PIPELINE_SOURCES).getValue();
        
        visitContainerValue(sources);
    }
    
    /**
     * Returns the gathered information (directly available after calling the constructor).
     * @return The referenced variables of the pipeline.
     */
    public PipelineContentsContainer getPipelineContents() {
        if (!containerInitialized && initializeRuntimeMapping) {
            containerInitialized = true;
            container.init();
        }
        return container;
    }
    
    /**
     * Responsible for visiting the next element and storing relevant information.
     * @param pipelineElement The currently visited element (node or flow) of the pipeline.
     */
    private void visitPipelineElement(IDecisionVariable pipelineElement) {
        if (null != pipelineElement && !done.contains(pipelineElement)) {
            done.add(pipelineElement);
            String typeName = pipelineElement.getDeclaration().getType().getName();
            
            if (QmConstants.TYPE_SOURCE.equals(typeName)) {
                // Visit source
                container.addSource(pipelineElement);
                
                IDecisionVariable nextVars = pipelineElement.getNestedElement(QmConstants.SLOT_SOURCE_OUTPUT);
                if (null == nextVars) {
                    // Special case for test case (simplified model)
                    nextVars = pipelineElement.getNestedElement("next");
                }
                if (null != nextVars) {
                    ContainerValue referencedVariables = (ContainerValue) nextVars.getValue();
                    visitContainerValue(referencedVariables);
                }
                visitAvailable(pipelineElement, pipelineElement.getNestedElement(QmConstants.SLOT_SOURCE_SOURCE));
            } else if (QmConstants.TYPE_FLOW.equals(typeName)) {
                // Visit flow (do not gather information, but continue visiting)
                IDecisionVariable nextVar = pipelineElement.getNestedElement(QmConstants.SLOT_FLOW_DESTINATION);
                if (null != nextVar) {
                    ReferenceValue refValue = (ReferenceValue) nextVar.getValue();
                    IDecisionVariable referencedVariable = extractVar(refValue);
                    visitPipelineElement(referencedVariable);
                }
            } else if (QmConstants.TYPE_FAMILYELEMENT.equals(typeName)) {
                container.addFamilyElement(pipelineElement);
                visitProcessingElement(pipelineElement);
                IDecisionVariable family = Configuration.dereference(
                    pipelineElement.getNestedElement(QmConstants.SLOT_FAMILYELEMENT_FAMILY));
                IDecisionVariable members = family.getNestedElement(QmConstants.SLOT_FAMILY_MEMBERS);
                visitAvailable(pipelineElement, members); // family.members
            } else if (QmConstants.TYPE_DATAMANAGEMENTELEMENT.equals(typeName)) {
                container.addDataManagementElement(pipelineElement);
                visitProcessingElement(pipelineElement);
            } else if (QmConstants.TYPE_REPLAYSINK.equals(typeName)) {
                container.addReplaySink(pipelineElement);
                visitAvailable(pipelineElement, pipelineElement.getNestedElement(QmConstants.SLOT_SINK_SINK));
                // End visiting
            } else if (QmConstants.TYPE_SINK.equals(typeName)) {
                container.addSink(pipelineElement);
                visitAvailable(pipelineElement, pipelineElement.getNestedElement(QmConstants.SLOT_SINK_SINK));
                // End visiting
            } else {
                // Probably some kind of processing element.
                visitProcessingElement(pipelineElement);
            }
        }
    }

    /**
     * Visits the available collection in {@code pipelineElement} and links original and copied variables.
     * 
     * @param pipelineElement the element to visit
     * @param oVar the original data variable
     */
    private void visitAvailable(IDecisionVariable pipelineElement, IDecisionVariable oVar) {
        if (null != mapper) {
            // TODO slot and container
            IDecisionVariable aVar = pipelineElement.getNestedElement(QmConstants.SLOT_AVAILABLE);
            if (null == oVar) {
                Bundle.getLogger(PipelineVisitor.class).warn("No original slot found in " 
                    + pipelineElement.getQualifiedName());
            } else if (null == aVar) {
                Bundle.getLogger(PipelineVisitor.class).warn("No slot " + QmConstants.SLOT_AVAILABLE 
                    + " found in " + pipelineElement.getQualifiedName());
            } else {
                visitAvailable(oVar.getValue(), aVar.getValue(), pipelineElement.getQualifiedName());
            }
        }
    }

    /**
     * Visits the available collection in {@code pipelineElement} and links original and copied variables.
     * 
     * @param oValue the original value (may be variable or collection)
     * @param aValue the copied (available) value
     * @param eltName the element name
     */
    private void visitAvailable(Value oValue, Value aValue, String eltName) {
        if (null == oValue) {
            Bundle.getLogger(PipelineVisitor.class).warn("Original value of is null " + eltName);
        } else if (!(aValue instanceof ContainerValue)) {
            Bundle.getLogger(PipelineVisitor.class).warn("Value of " + QmConstants.SLOT_AVAILABLE 
                + " is not a container " + eltName);
        } else {
            ContainerValue aCnt = (ContainerValue) aValue;
            if (oValue instanceof ContainerValue) {
                ContainerValue oCnt = (ContainerValue) oValue;
                int max = Math.max(oCnt.getElementSize(), aCnt.getElementSize());
                for (int i = 0; i < max; i++) {
                    Value ov = oCnt.getElement(i);
                    Value av = aCnt.getElement(i);
                    if (ov instanceof ReferenceValue && av instanceof ReferenceValue) {
                        IDecisionVariable o = extractVar((ReferenceValue) ov);
                        IDecisionVariable a = extractVar((ReferenceValue) av);
                        mapper.map(o, a);
                    }
                }
            } else {
                if (aCnt.getElementSize() > 0 && oValue instanceof ReferenceValue) {
                    IDecisionVariable o = extractVar((ReferenceValue) oValue);
                    Value av = aCnt.getElement(0);
                    if (av instanceof ReferenceValue) {
                        IDecisionVariable a = extractVar((ReferenceValue) av);
                        mapper.map(o, a);
                    }
                }
                
            }
        }
    }

    /**
     * General part of the visitation method for pipeline elements. Visits the outgoing flow of a
     * processing element (Datamanagement or FamilyElement).
     * @param pipelineElement A Datamanagement or FamilyElement
     */
    private void visitProcessingElement(IDecisionVariable pipelineElement) {
        IDecisionVariable nextVars = pipelineElement.getNestedElement(QmConstants.SLOT_OUTPUT);
        if (null != nextVars) {
            ContainerValue referencedVariables = (ContainerValue) nextVars.getValue();
            visitContainerValue(referencedVariables);
        }
    }
    
    /**
     * Iterates through a container, extracts all referenced variables, and calls the visit method.
     * @param containerValue A container of references.
     */
    private void visitContainerValue(ContainerValue containerValue) {
        if (null != containerValue) {
            for (int i = 0, end = containerValue.getElementSize(); i < end; i++) {
                ReferenceValue refValue = (ReferenceValue) containerValue.getElement(i);
                IDecisionVariable referencedVariable = extractVar(refValue);
                visitPipelineElement(referencedVariable);
            }
        }
    }
    
    /**
     * Extracts an {@link IDecisionVariable} from the given {@link ReferenceValue}.
     * @param refValue A value pointing to an element of a pipeline.
     * @return The referenced {@link IDecisionVariable} or in case of any errors <code>null</code>.
     */
    private IDecisionVariable extractVar(ReferenceValue refValue) {
        return Utils.extractVariable(refValue, config);
    }
}
