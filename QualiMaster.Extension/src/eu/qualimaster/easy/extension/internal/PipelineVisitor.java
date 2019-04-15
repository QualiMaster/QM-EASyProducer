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
    
    /**
     * Constructor which will map runtime clones.
     * @param pipeline The pipeline for which the information should be extracted.
     */
    PipelineVisitor(IDecisionVariable pipeline) {
        this(pipeline, true);
    }
    
    /**
     * Constructor for deciding whether runtime instances shall be mapped or whether this is not needed. 
     * @param pipeline The pipeline for which the information should be extracted.
     * @param initializeRuntimeMapping <tt>true</tt> runtime variables will be mapped, <tt>false</tt> mapping is not
     * needed. Mapping (<tt>true</tt>) is only needed during adaptation, no mapping (<tt>false</tt>) is needed
     * in QM-Iconf application.
     */
    public PipelineVisitor(IDecisionVariable pipeline, boolean initializeRuntimeMapping) {
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
            } else if (QmConstants.TYPE_DATAMANAGEMENTELEMENT.equals(typeName)) {
                container.addDataManagementElement(pipelineElement);
                visitProcessingElement(pipelineElement);
            } else if (QmConstants.TYPE_REPLAYSINK.equals(typeName)) {
                container.addReplaySink(pipelineElement);
                // End visiting
            } else if (QmConstants.TYPE_SINK.equals(typeName)) {
                container.addSink(pipelineElement);
                // End visiting
            } else {
                // Probably some kind of processing element.
                visitProcessingElement(pipelineElement);
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
     * @return The referenced {@link IDecisionVariable} or in case of any errors <tt>null</tt>.
     */
    private IDecisionVariable extractVar(ReferenceValue refValue) {
        return Utils.extractVariable(refValue, config);
    }
}
