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
import eu.qualimaster.common.QMInternal;
import eu.qualimaster.coordination.INameMapping;
import eu.qualimaster.coordination.INameMapping.Algorithm;
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
     * @param pipelineNodeName the (configured/implementation) name of the pipeline node
     * @return the mapped name or <code>pipelineNodeName</code> 
     */
    public static String getImplementationName(String pipelineName, String pipelineNodeName) {
        return getPipelineElementImplName(getNameMapping(pipelineName), pipelineNodeName);
    }

    /**
     * Returns the name mapping for <code>pipelineName</code>.
     * 
     * @param pipelineName the pipeline name
     * @return the name mapping
     */
    @QMInternal
    public static INameMapping getNameMapping(String pipelineName) {
        return AdaptationManager.getNameMapping(pipelineName);
    }

    /**
     * Returns the implementation/component name of the given pipeline node from a give name mapping.
     * 
     * @param mapping the name mapping to use (may be <b>null</b> but then no mapping takes place)
     * @param pipelineElementName the (configured/implementation) name of the pipeline node
     * @return the implementation name or <code>pipelineNodeName</code> 
     */
    @QMInternal
    public static String getPipelineElementImplName(INameMapping mapping, String pipelineElementName) {
        String result = pipelineElementName;
        if (null != mapping) {
            Component cmp = mapping.getPipelineNodeComponent(pipelineElementName);
            if (null != cmp) {
                result = cmp.getName();
            }
        }
        return result;
    }

    /**
     * Returns the implementation/component name of the given algorithm from a given name mapping.
     * 
     * @param mapping the name mapping to use (may be <b>null</b> but then no mapping takes place)
     * @param algorithmName the configured name of the algorithm
     * @return the implementation name or <code>algorithmName</code> 
     */
    public static String getAlgorithmImplName(INameMapping mapping, String algorithmName) {
        String result = algorithmName;
        if (null != mapping) {
            Algorithm alg = mapping.getAlgorithm(algorithmName);
            if (null != alg) {
                result = alg.getImplName();
            }
        }
        return result;
    }

    /**
     * Returns the configured name of the given pipeline node from a give name mapping.
     * 
     * @param mapping the name mapping to use (may be <b>null</b> but then no mapping takes place)
     * @param pipelineElementName the (configured/implementation) name of the pipeline node
     * @return the mapped name or <code>pipelineNodeName</code> 
     */
    public static String mapPipelineElementName(INameMapping mapping, String pipelineElementName) {
        String result = pipelineElementName;
        if (null != mapping) {
            String tmp = mapping.getPipelineNodeByImplName(pipelineElementName);
            if (null != tmp) {
                Component cmp = mapping.getPipelineNodeComponent(tmp);
                if (null != cmp) {
                    result = cmp.getName();
                }
            }
        }
        return result;
    }
    
    /**
     * Returns the configured name of the given pipeline node from a give name mapping.
     * 
     * @param mapping the name mapping to use (may be <b>null</b> but then no mapping takes place)
     * @param algorithmName the (configured/implementation) name of the algorithm
     * @return the mapped name or <code>pipelineNodeName</code> 
     */
    public static String mapAlgorithmName(INameMapping mapping, String algorithmName) {
        String result = algorithmName;
        if (null != mapping) {
            Algorithm alg = mapping.getAlgorithmByImplName(algorithmName);
            if (null != alg) {
                result = alg.getName();
            }
        }
        return result;
    }
    
}
