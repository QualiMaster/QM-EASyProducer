/*
 * Copyright 2009-2016 University of Hildesheim, Software Systems Engineering
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

import eu.qualimaster.adaptation.AdaptationManager;
import eu.qualimaster.coordination.INameMapping;
import eu.qualimaster.coordination.INameMapping.Component;
import net.ssehub.easy.instantiation.core.model.vilTypes.IVilType;
import net.ssehub.easy.instantiation.core.model.vilTypes.Instantiator;

/**
 * Accesses names from the pipeline mapping. Please use only during adaptation.
 * 
 * @author Holger Eichelberger
 */
@Instantiator("getImplementationName")
public class NameMappingHelper implements IVilType {

    /**
     * Returns the implementation/component name of the given pipeline node.
     * 
     * @param pipelineName the name of the pipeline
     * @param pipelineNodeName the name of the pipeline node
     * @return the mapped name or <code>pipelineNodeName</code> 
     */
    public static String getImplementationName(String pipelineName, String pipelineNodeName) {
        String result = pipelineNodeName;
        INameMapping mapping = AdaptationManager.getNameMapping(pipelineName);
        if (null != mapping) {
            Component cmp = mapping.getPipelineNodeComponent(pipelineNodeName);
            if (null != cmp) {
                result = cmp.getName();
            }
        }
        return result;
    }
    
}
