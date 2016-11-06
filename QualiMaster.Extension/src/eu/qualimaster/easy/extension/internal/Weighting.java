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

import eu.qualimaster.observables.IObservable;
import net.ssehub.easy.instantiation.core.model.vilTypes.IVilType;
import net.ssehub.easy.instantiation.core.model.vilTypes.Instantiator;
import net.ssehub.easy.instantiation.core.model.vilTypes.Invisible;
import net.ssehub.easy.instantiation.core.model.vilTypes.Map;
import net.ssehub.easy.instantiation.core.model.vilTypes.OperationMeta;
import net.ssehub.easy.instantiation.core.model.vilTypes.ParameterMeta;
import net.ssehub.easy.instantiation.core.model.vilTypes.TypeDescriptor;
import net.ssehub.easy.instantiation.core.model.vilTypes.TypeRegistry;

/**
 * Weighting functions.
 * 
 * @author Holger Eichelberger
 */
@Instantiator("weightAll")
public class Weighting implements IVilType {

    /**
     * Implements a weighting of mass predictions.
     * 
     * @param predictions the predictions given as name-observable-prediction mapping, may be <b>null</b>, entries 
     *     may be <b>null</b>
     * @param weighting the weighting of the observables
     * @return the "best" solution in terms of the name
     */
    @OperationMeta(returnGenerics = {String.class, Double.class})
    public static Map<String, Double> weightAll(
        @ParameterMeta(generics = {String.class, Map.class, IObservable.class, Double.class}) 
        Map<String, Map<IObservable, Double>> predictions, 
        @ParameterMeta(generics = {IObservable.class, Double.class}) 
        Map<IObservable, Double> weighting) {
        TypeDescriptor<?>[] types = TypeDescriptor.createArray(2);
        types[0] = TypeRegistry.stringType();
        types[1] = TypeRegistry.realType();
        return new Map<String, Double>(new HashMap<Object, Object>(weightAllImpl(predictions, weighting)), types);
    }
    
    /**
     * Implements a weighting of mass predictions.
     * 
     * @param predictions the predictions given as name-observable-prediction mapping, may be <b>null</b>, entries 
     *     may be <b>null</b>
     * @param weighting the weighting of the observables
     * @return the "best" solution in terms of the name
     */
    @Invisible
    @SuppressWarnings("unchecked")
    public static java.util.Map<String, Double> weightAllImpl(
        @ParameterMeta(generics = {String.class, Map.class, IObservable.class, Double.class}) 
        Map<String, Map<IObservable, Double>> predictions, 
        @ParameterMeta(generics = {IObservable.class, Double.class}) 
        Map<IObservable, Double> weighting) {
        HashMap<String, Double> result = new HashMap<String, Double>();
        if (null != predictions) {
            double[] results = new double[2];
            for (String name : predictions.keys()) {
                double algVal = 0;
                results[0] = 0; // sum
                results[1] = 0; // weights
                Object map = predictions.get(name);
                if (map instanceof Map) {
                    Map<IObservable, Double> algPredictions = (Map<IObservable, Double>) map;
                    for (IObservable obs : weighting.keys()) {
                        if (null != obs) {
                            update(obs, weighting, algPredictions.get(obs), results);
                        }
                    }
                } else if (map instanceof java.util.Map) {
                    java.util.Map<IObservable, Double> algPredictions = (java.util.Map<IObservable, Double>) map;
                    for (IObservable obs : weighting.keys()) {
                        if (null != obs) {
                            update(obs, weighting, algPredictions.get(obs), results);
                        }
                    }
                }
                if (results[1] != 0) {
                    algVal = results[0] / results[1];
                } else {
                    algVal = 0;
                }
                result.put(name, algVal);
            }
        }
        return result;
    }

    /**
     * Updates the sum/weight in <code>result</code>.
     * 
     * @param obs the actual observable
     * @param weighting the weighting
     * @param predicted the predicted value
     * @param results the results to be updated
     */
    private static void update(IObservable obs, Map<IObservable, Double> weighting, Double predicted, 
        double[] results) {
        Double weight = weighting.get(obs);
        if (null != weight) {
            if (null != predicted) {
                results[0] += predicted * weight;
            }
            results[1] += weight;
        }
    }
    
}
