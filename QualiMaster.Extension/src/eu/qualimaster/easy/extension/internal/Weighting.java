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
import net.ssehub.easy.instantiation.core.model.vilTypes.ParameterMeta;
import net.ssehub.easy.instantiation.core.model.vilTypes.ReturnGenerics;
import net.ssehub.easy.instantiation.core.model.vilTypes.Set;
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
     * Implements a weighting for mass predictions.
     * 
     * @param predictions the predictions given as name-observable-prediction mapping, may be <b>null</b>, entries 
     *     may be <b>null</b>
     * @param weighting the weighting of the observables, negative weights invert the value by subtracting from the 
     *     respective maximum
     * @return the "best" solution in terms of the name as maximum of weighted average sums
     */
    @ReturnGenerics({String.class, Double.class})
    public static Map<String, Double> weightAll(
        @ParameterMeta(generics = {String.class, Map.class, IObservable.class, Double.class}) 
        Map<String, Map<IObservable, Double>> predictions, 
        @ParameterMeta(generics = {IObservable.class, Double.class}) 
        Map<IObservable, Double> weighting) {
        return weightAll(predictions, weighting, null);
    }
    
    /**
     * Implements a weighting for mass predictions.
     * 
     * @param predictions the predictions given as name-observable-prediction mapping, may be <b>null</b>, entries 
     *     may be <b>null</b>
     * @param weighting the weighting of the observables, negative weights invert the value by subtracting from the 
     *     respective maximum
     * @param costs the costs, i.e., observables to be counted negative
     * @return the "best" solution in terms of the name as maximum of weighted average sums
     */
    @ReturnGenerics({String.class, Double.class})
    public static Map<String, Double> weightAll(
        @ParameterMeta(generics = {String.class, Map.class, IObservable.class, Double.class}) 
        Map<String, Map<IObservable, Double>> predictions, 
        @ParameterMeta(generics = {IObservable.class, Double.class}) 
        Map<IObservable, Double> weighting, 
        @ParameterMeta(generics = {IObservable.class})
        Set<IObservable> costs) {
        TypeDescriptor<?>[] types = TypeDescriptor.createArray(2);
        types[0] = TypeRegistry.stringType();
        types[1] = TypeRegistry.realType();
        return new Map<String, Double>(new HashMap<Object, Object>(weightAllImpl(predictions, weighting, costs)), 
            types);
    }
    
    /**
     * Implements a weighting of mass predictions.
     * 
     * @param predictions the predictions given as name-observable-prediction mapping, may be <b>null</b>, entries 
     *     may be <b>null</b>
     * @param weighting the weighting of the observables, negative weights invert the value by subtracting from the 
     *     respective maximum
     * @param costs the costs, i.e., observables to be counted negative (may be <b>null</b> for no costs)
     * @return the "best" solution in terms of the name as the maximum the average weighted value if no 
     *     <code>costs</code>, the maximum weighted sum if <code>costs</code>
     */
    @Invisible
    public static java.util.Map<String, Double> weightAllImpl(
        @ParameterMeta(generics = {String.class, Map.class, IObservable.class, Double.class}) 
        Map<String, Map<IObservable, Double>> predictions, 
        @ParameterMeta(generics = {IObservable.class, Double.class}) 
        Map<IObservable, Double> weighting,
        @ParameterMeta(generics = {IObservable.class})
        Set<IObservable> costs) {
 
        HashMap<String, Double> result = new HashMap<String, Double>();
        if (null != predictions && null != weighting) {
            MaxProcessor maxProcessor = new MaxProcessor();
            processPredictions(predictions, weighting, maxProcessor);

            UpdateProcessor updateProcessor = new UpdateProcessor(maxProcessor, weighting, result, costs);
            processPredictions(predictions, weighting, updateProcessor);
        }
        return result;
    }
    
    /**
     * A prediction processor.
     * 
     * @author Holger Eichelberger
     */
    private interface IPredictionProcessor {
        
        /**
         * Processes a single prediction.
         * 
         * @param obs the observable
         * @param prediction the prediction
         */
        public void process(IObservable obs, Double prediction);
        
        /**
         * Postprocesses a named entry for multiple predictions.
         * 
         * @param name the name
         */
        public void postProcess(String name);
    }

    /**
     * Processes predictions.
     * 
     * @param predictions the predictions
     * @param weighting the weighting, negative weights invert the value by subtracting from the 
     *     respective maximum
     * @param processor the prediction processor
     */
    @SuppressWarnings("unchecked")
    private static void processPredictions(Map<String, Map<IObservable, Double>> predictions, 
        Map<IObservable, Double> weighting, IPredictionProcessor processor) {
        for (String name : predictions.keys()) {
            Object map = predictions.get(name);
            if (map instanceof Map) {
                Map<IObservable, Double> algPredictions = (Map<IObservable, Double>) map;
                for (IObservable obs : weighting.keys()) {
                    if (null != obs) {
                        processor.process(obs, algPredictions.get(obs));
                    }
                }
            } else if (map instanceof java.util.Map) {
                java.util.Map<IObservable, Double> algPredictions = (java.util.Map<IObservable, Double>) map;
                for (IObservable obs : weighting.keys()) {
                    if (null != obs) {
                        processor.process(obs, algPredictions.get(obs));
                    }
                }
            }
            processor.postProcess(name);
        }
    }

    /**
     * Implements a maximum prediction processor, i.e., collects the maximum values of the predictions.
     * 
     * @author Holger Eichelberger
     */
    private static class MaxProcessor implements IPredictionProcessor {

        private HashMap<IObservable, Double> max = new HashMap<IObservable, Double>();

        /**
         * Returns the result.
         * 
         * @return the result, a mapping of observables to maximum values
         */
        public HashMap<IObservable, Double> getResult() {
            return max;
        }
        
        @Override
        public void process(IObservable obs, Double prediction) {
            Double v = max.get(obs);
            if (null != prediction) {
                if (null == v) {
                    max.put(obs, prediction);
                } else {
                    max.put(obs, Math.max(prediction, v));
                }
            }
        }

        @Override
        public void postProcess(String name) {
        }
        
    };

    /**
     * Implements an update processor calculating the weighted predictions.
     * 
     * @author Holger Eichelberger
     */
    private static class UpdateProcessor implements IPredictionProcessor {

        private HashMap<IObservable, Double> max;
        private Map<IObservable, Double> weighting;
        private HashMap<String, Double> result;
        private Set<IObservable> costs;
        private double sum = 0;
        private double weights = 0;

        /**
         * Creates the processor.
         * 
         * @param max contains the maximum predictions
         * @param weighting contains the weightings, negative weights invert the value by subtracting from the 
     *     respective maximum
         * @param result filled with results (changed as a side effect)
         * @param costs weights to be counted negative
         */
        private UpdateProcessor(MaxProcessor max, Map<IObservable, Double> weighting, HashMap<String, Double> result, 
            Set<IObservable> costs) {
            this.max = max.getResult();
            this.weighting = weighting;
            this.result = result;
            this.costs = costs;
        }

        @Override
        public void process(IObservable obs, Double prediction) {
            Double weight = weighting.get(obs);
            if (null != weight) {
                if (null != prediction) {
                    double p = prediction;
                    if (weight < 0) {
                        Double m = max.get(obs);
                        if (null != m) {
                            p = m - p;
                        }
                        weight *= -1;
                    }
                    sum += p * weight;
                }
                if (null != costs && costs.includes(obs)) {
                    weights *= -1;
                }
                weights += weight;
            }
        }

        @Override
        public void postProcess(String name) {
            double algVal;
            if (null != costs) {
                if (weights != 0) {
                    algVal = sum / weights;
                } else {
                    algVal = 0;
                }
            } else {
                algVal = sum;
            }
            result.put(name, algVal);
            
            // and clear
            sum = 0;
            weights = 0;
        }

    };

}
