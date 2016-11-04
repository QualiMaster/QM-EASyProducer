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

import eu.qualimaster.observables.IObservable;
import net.ssehub.easy.instantiation.core.model.vilTypes.IVilType;
import net.ssehub.easy.instantiation.core.model.vilTypes.Instantiator;
import net.ssehub.easy.instantiation.core.model.vilTypes.Map;
import net.ssehub.easy.instantiation.core.model.vilTypes.ParameterMeta;

/**
 * A simple selection of the "best" alternative via weighted values/predictions.
 * 
 * @author Holger Eichelberger
 */
@Instantiator("weightingSelection")
public class WeightingSelector implements IVilType {
    
    /**
     * Implements a simple weighting of mass predictions.
     * 
     * @param predictions the predictions given as name-observable-prediction mapping, may be <b>null</b>, entries 
     *     may be <b>null</b>
     * @param weighting the weighting of the observables
     * @return the "best" solution in terms of the name
     */
    public static String weightingSelection(
        @ParameterMeta(generics = {String.class, Map.class, IObservable.class, Double.class}) 
        Map<String, Map<IObservable, Double>> predictions, 
        @ParameterMeta(generics = {IObservable.class, Double.class}) 
        Map<IObservable, Double> weighting) {
        
        java.util.Map<String, Double> weighted = Weighting.weightingImpl(predictions, weighting);
        String best = null;
        double bestVal = 0;
        for (java.util.Map.Entry<String, Double> ent : weighted.entrySet()) {
            String name = ent.getKey();
            Double algVal = ent.getValue();
            if (null == best || (null != algVal && algVal > bestVal)) {
                best = name;
            }
        }
        return best;
    }
    
}
