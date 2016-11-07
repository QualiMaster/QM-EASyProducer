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
package test.eu.qualimaster.easy.extension.internal;

import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;

import eu.qualimaster.easy.extension.internal.WeightingSelector;
import eu.qualimaster.observables.IObservable;
import eu.qualimaster.observables.ResourceUsage;
import eu.qualimaster.observables.Scalability;
import eu.qualimaster.observables.TimeBehavior;
import net.ssehub.easy.instantiation.core.model.common.VilException;
import net.ssehub.easy.instantiation.core.model.vilTypes.Map;
import net.ssehub.easy.instantiation.core.model.vilTypes.TypeDescriptor;
import net.ssehub.easy.instantiation.rt.core.model.rtVil.types.RtVilTypeRegistry;

/**
 * Tests the optimizers/selectors.
 * 
 * @author Holger Eichelberger
 */
public class OptimizerTest {
    
    /**
     * Tests the weighting selector.
     */
    @Test
    public void weightingSelectorTest() {
        Assert.assertNull(WeightingSelector.weightingSelection(null, null));

        java.util.Map<String, java.util.Map<IObservable, Double>> predictions = new HashMap<>();
        HashMap<IObservable, Double> test = new HashMap<>();
        test.put(ResourceUsage.CAPACITY, 0.5);
        test.put(Scalability.ITEMS, 2.0);
        predictions.put("alg1", test);
        test = new HashMap<>();
        test.put(ResourceUsage.CAPACITY, 0.6);
        test.put(Scalability.ITEMS, 4.0);
        predictions.put("alg2", test);
        java.util.Map<IObservable, Double> weighting = new HashMap<>();
        weighting.put(ResourceUsage.CAPACITY, 1.0);
        weighting.put(ResourceUsage.TASKS, 1.0);
        weighting.put(ResourceUsage.EXECUTORS, 1.0);
        weighting.put(Scalability.ITEMS, 2.0);
        
        String result = weightingSelection(predictions, weighting);
        Assert.assertEquals("alg2", result);
    }
    
    /**
     * Performs a weighting selection based on pure Java maps turned internally into VIL maps.
     * 
     * @param predictions the predictions
     * @param weighting the weights
     * @return the weighting selection
     */
    private static String weightingSelection(java.util.Map<String, java.util.Map<IObservable, Double>> predictions, 
        java.util.Map<IObservable, Double> weighting) {
        Map<String, Map<IObservable, Double>> vilPredictions = createPredictions(predictions);
        Map<IObservable, Double> vilWeighting = createWeighting(weighting);
        return WeightingSelector.weightingSelection(vilPredictions, vilWeighting);
    }
    
    /**
     * Tests the weighting selector on LATENCY.
     */
    @Test
    public void weightingLatencySelectorTest() {
        java.util.Map<String, java.util.Map<IObservable, Double>> predictions = new HashMap<>();
        HashMap<IObservable, Double> test = new HashMap<>();
        test.put(TimeBehavior.LATENCY, 200.0);
        predictions.put("alg1", test);
        test = new HashMap<>();
        test.put(TimeBehavior.LATENCY, 300.0);
        predictions.put("alg2", test);
        java.util.Map<IObservable, Double> weighting = new HashMap<>();
        weighting.put(TimeBehavior.LATENCY, -1.0); // invert by max
        
        String result = weightingSelection(predictions, weighting);
        Assert.assertEquals("alg1", result);
    }

    /**
     * Creates a valid VIL map for predictions.
     * 
     * @param preds the predictions in pure Java maps
     * @return the corresponding VIL map
     */
    public static Map<String, Map<IObservable, Double>> createPredictions(java.util.Map<String, 
        java.util.Map<IObservable, Double>> preds) {
        TypeDescriptor<?>[] predTypesInner = TypeDescriptor.createArray(2);
        predTypesInner[0] = RtVilTypeRegistry.INSTANCE.getType(IObservable.class);
        predTypesInner[1] = RtVilTypeRegistry.realType();
        TypeDescriptor<?>[] predTypes = TypeDescriptor.createArray(2);
        predTypes[0] = RtVilTypeRegistry.stringType();
        try {
            predTypes[1] = RtVilTypeRegistry.getMapType(predTypesInner);
        } catch (VilException e) {
            predTypes[1] = RtVilTypeRegistry.anyType();
        }
        
        java.util.Map<Object, Object> predictions = new HashMap<>();
        for (java.util.Map.Entry<String, java.util.Map<IObservable, Double>> oEnt : preds.entrySet()) {
            HashMap<Object, Object> test = new HashMap<>();
            for (java.util.Map.Entry<IObservable, Double> iEnt : oEnt.getValue().entrySet()) {
                test.put(iEnt.getKey(), iEnt.getValue());
            }
            Object innerMap;
            if (predictions.size() % 2 == 0) { // alternate just for testing!
                innerMap = test;
            } else {
                innerMap = new Map<IObservable, Double>(test, predTypesInner);
            }
            predictions.put(oEnt.getKey(), innerMap);
        }

        return new Map<String, Map<IObservable, Double>>(predictions, predTypes);
    }

    /**
     * Creates a valid VIL map for weights.
     * 
     * @param weights the weights in pure Java maps
     * @return the corresponding VIL map
     */
    public static Map<IObservable, Double> createWeighting(java.util.Map<IObservable, Double> weights) {
        TypeDescriptor<?>[] weightingTypes = TypeDescriptor.createArray(2);
        weightingTypes[0] = RtVilTypeRegistry.INSTANCE.getType(IObservable.class);
        weightingTypes[1] = RtVilTypeRegistry.realType();

        java.util.Map<Object, Object> weighting = new HashMap<>();
        weighting.putAll(weights);
        
        return new Map<IObservable, Double>(weighting, weightingTypes);
    }

}
