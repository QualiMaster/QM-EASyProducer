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
        TypeDescriptor<?>[] weightingTypes = TypeDescriptor.createArray(2);
        weightingTypes[0] = RtVilTypeRegistry.INSTANCE.getType(IObservable.class);
        weightingTypes[1] = RtVilTypeRegistry.realType();

        java.util.Map<Object, Object> predictions = new HashMap<>();
        HashMap<Object, Object> test = new HashMap<>();
        test.put(ResourceUsage.CAPACITY, 0.5);
        test.put(Scalability.ITEMS, 2.0);
        predictions.put("alg1", test);
        test = new HashMap<>();
        test.put(ResourceUsage.CAPACITY, 0.6);
        test.put(Scalability.ITEMS, 4.0);
        predictions.put("alg2", new Map<IObservable, Double>(test, predTypesInner));
        java.util.Map<Object, Object> weighting = new HashMap<>();
        weighting.put(ResourceUsage.CAPACITY, 1.0);
        weighting.put(ResourceUsage.TASKS, 1.0);
        weighting.put(ResourceUsage.EXECUTORS, 1.0);
        weighting.put(Scalability.ITEMS, 2.0);
        
        Map<String, Map<IObservable, Double>> vilPredictions 
            = new Map<String, Map<IObservable, Double>>(predictions, predTypes);
        Map<IObservable, Double> vilWeighting = new Map<IObservable, Double>(weighting, weightingTypes);

        String result = WeightingSelector.weightingSelection(vilPredictions, vilWeighting);
        Assert.assertEquals("alg2", result);
    }

}
