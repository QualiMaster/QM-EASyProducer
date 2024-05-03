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

import java.util.ArrayList;
import java.util.List;

import eu.qualimaster.adaptation.AdaptationManager;
import eu.qualimaster.coordination.INameMapping;
import eu.qualimaster.coordination.INameMapping.Algorithm;
import eu.qualimaster.coordination.INameMapping.Component;
import net.ssehub.easy.instantiation.core.model.vilTypes.IVilType;
import net.ssehub.easy.instantiation.core.model.vilTypes.Instantiator;
import net.ssehub.easy.instantiation.core.model.vilTypes.ListSequence;
import net.ssehub.easy.instantiation.core.model.vilTypes.ReturnGenerics;
import net.ssehub.easy.instantiation.core.model.vilTypes.Sequence;
import net.ssehub.easy.instantiation.core.model.vilTypes.UnmodifiableSequence;

/**
 * An instantiator for accessing the components of manually implemented sub-topologies (mapped into at runtime).
 * 
 * @author Holger Eichelberger
 */
@Instantiator("getSubTopologyComponents")
public class SubTopologyComponentsHelper implements IVilType {
    
    /**
     * Returns the components implementing the given algorithm in terms of a manual sub-topology.
     * 
     * @param pipeline the pipeline name
     * @param algorithm the algorithm name
     * @return the names of the sub-topology components, may be empty if there are none
     */
    @ReturnGenerics({String.class})
    public static Sequence<String> getSubTopologyComponents(String pipeline, String algorithm) {
        INameMapping mapping = AdaptationManager.getNameMapping(pipeline);
        Algorithm alg = mapping.getAlgorithm(algorithm);
        List<String> tmp = new ArrayList<String>();
        if (null != alg) {
            for (Component c : alg.getComponents()) {
                tmp.add(c.getName());
            }
        }
        return new UnmodifiableSequence<>(new ListSequence<String>(tmp, String.class));
    }
    
}
