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

import java.util.Collection;

import eu.qualimaster.adaptation.AdaptationManager;
import eu.qualimaster.common.QMInternal;
import eu.qualimaster.coordination.INameMapping;
import eu.qualimaster.coordination.INameMapping.ISubPipeline;
import net.ssehub.easy.instantiation.core.model.vilTypes.IVilType;
import net.ssehub.easy.instantiation.core.model.vilTypes.Instantiator;

/**
 * Provides access to sub-pipeline information. This is required as loose sub-pipeline 
 * information is (currently) an implementation style / property.
 * 
 * @author Holger Eichelberger
 */
@Instantiator("isSubPipeline")
public class SubPipelineHelper implements IVilType {
    
    /**
     * Returns whether a given <code>subPipelineName</code> indicates a sub-pipeline of <code>pipelineName</code>.
     * 
     * @param pipelineName the name of the pipeline
     * @param subPipelineName the name of the sub-pipeline
     * @return <code>true</code> if <code>subPipelineName</code> is a sub-pipeline of <code>pipelineName</code>
     */
    public static boolean isSubPipeline(String pipelineName, String subPipelineName) {
        return isSubPipeline(AdaptationManager.getNameMapping(pipelineName), pipelineName, subPipelineName);
    }

    /**
     * Returns whether a given <code>subPipelineName</code> indicates a sub-pipeline of <code>pipelineName</code>.
     *
     * @param mapping the name mapping to use
     * @param pipelineName the name of the pipeline
     * @param subPipelineName the name of the sub-pipeline
     * @return <code>true</code> if <code>subPipelineName</code> is a sub-pipeline of <code>pipelineName</code>
     */
    @QMInternal
    public static boolean isSubPipeline(INameMapping mapping, String pipelineName, String subPipelineName) {
        boolean result = false;
        if (null != mapping) {
            Collection<ISubPipeline> sub = mapping.getSubPipelines();
            if (null != sub) {
                for (ISubPipeline s : sub) {
                    if (s.getName().equals(subPipelineName)) {
                        result = true;
                        break;
                    }
                }
            }
        }
        return result;
    }

}
