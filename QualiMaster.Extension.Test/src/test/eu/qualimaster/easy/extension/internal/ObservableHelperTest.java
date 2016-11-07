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

import org.junit.Assert;
import org.junit.Test;

import eu.qualimaster.easy.extension.internal.ObservableHelper;

/**
 * Tests the observable helper.
 * 
 * @author Holger Eichelberger
 */
public class ObservableHelperTest {

    /**
     * Tests the observable type names used in the model.
     */
    @Test
    public void testSingleObservable() {
        String[] typeNames = {"Latency", "Throughput_Items", "Throughput_Volume", "Enactment_Delay", "Used_Memory", 
            "Used_Machines", "Available_Machines", "Bandwidth", "Capacity", "Executors", "Tasks", "Accuracy_Confidence",
            "Accuracy_Error_Rate", "Believability", "Relevancy", "Completeness", "Volume", "Velocity", "Volatility",
            "Variety", "Items"};
        for (String typeName : typeNames) {
            Assert.assertNotNull(ObservableHelper.mapObservable(typeName));
        }
    }
    
}
