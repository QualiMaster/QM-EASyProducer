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

import java.util.HashMap;

import eu.qualimaster.adaptation.AdaptationManager;
import eu.qualimaster.coordination.INameMapping;
import eu.qualimaster.coordination.INameMapping.Algorithm;
import eu.qualimaster.infrastructure.IScalingDescriptor;
import net.ssehub.easy.instantiation.core.model.vilTypes.IVilType;
import net.ssehub.easy.instantiation.core.model.vilTypes.Instantiator;
import net.ssehub.easy.instantiation.core.model.vilTypes.Map;
import net.ssehub.easy.instantiation.core.model.vilTypes.OperationMeta;
import net.ssehub.easy.instantiation.core.model.vilTypes.TypeDescriptor;
import net.ssehub.easy.instantiation.rt.core.model.rtVil.types.RtVilTypeRegistry;

/**
 * An instantiator for accessing the expected sub-topology scaling of manually implemented sub-topologies.
 * The request is passed throught to the scalability descriptor of the implementing topology.
 * 
 * @author Holger Eichelberger
 */
@Instantiator("getSubTopologyScaling")
public class SubTopologyScalingHelper implements IVilType {
    
    /**
     * Returns the sub-topology scaling for a given algorithm, i.e., if the algorithm is implemented by a (manual) 
     * sub-topology.
     * 
     * @param pipeline the pipeline name
     * @param algorithm the algorithm name
     * @param factor the scaling factor
     * @param executors scale the executors or the tasks
     * @return the expected scaling (no scaling if empty)
     */
    @OperationMeta(returnGenerics = {String.class, Integer.class})
    public static Map<String, Integer> getSubTopologyScaling(String pipeline, String algorithm, double factor, 
        boolean executors) {
        java.util.Map<Object, Object> tmp = new HashMap<Object, Object>(); 
        INameMapping mapping = AdaptationManager.getNameMapping(pipeline);
        Algorithm alg = mapping.getAlgorithm(algorithm);
        if (null != alg) {
            IScalingDescriptor desc = alg.getScalingDescriptor();
            if (null != desc) {
                tmp.putAll(desc.getScalingResult(factor, executors));
            }
        }
        TypeDescriptor<?>[] types = TypeDescriptor.createArray(2);
        types[0] = RtVilTypeRegistry.stringType();
        types[1] = RtVilTypeRegistry.integerType();
        return new Map<String, Integer>(tmp, types);
    }

    /**
     * Returns the sub-topology scaling for a given algorithm, i.e., if the algorithm is implemented by a (manual) 
     * sub-topology.
     * 
     * @param pipeline the pipeline name
     * @param algorithm the algorithm name
     * @param oldExecutors the (actual) number of overall executors
     * @param newExecutors the new number of overall executors
     * @param diffs return the differences (<code>true</code>) or the absolute values (<code>false</code>)
     * @return the expected scaling (no scaling if empty)
     */
    @OperationMeta(returnGenerics = {String.class, Integer.class})
    public static Map<String, Integer> getSubTopologyScaling(String pipeline, String algorithm, int oldExecutors, 
        int newExecutors, boolean diffs) {
        java.util.Map<Object, Object> tmp = new HashMap<Object, Object>(); 
        INameMapping mapping = AdaptationManager.getNameMapping(pipeline);
        Algorithm alg = mapping.getAlgorithm(algorithm);
        if (null != alg) {
            IScalingDescriptor desc = alg.getScalingDescriptor();
            if (null != desc) {
                tmp.putAll(desc.getScalingResult(oldExecutors, newExecutors, diffs));
            }
        }
        TypeDescriptor<?>[] types = TypeDescriptor.createArray(2);
        types[0] = RtVilTypeRegistry.stringType();
        types[1] = RtVilTypeRegistry.integerType();
        return new Map<String, Integer>(tmp, types);
    }
    
}
