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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import eu.qualimaster.coordination.CoordinationManager;
import eu.qualimaster.coordination.INameMapping;
import eu.qualimaster.easy.extension.internal.SubTopologyScalingHelper;
import eu.qualimaster.infrastructure.IScalingDescriptor;
import eu.qualimaster.monitoring.events.SubTopologyMonitoringEvent;

/**
 * Tests the sub-topology scaling helper.
 * 
 * @author Holger Eichelberger
 */
public class SubTopologyScalingHelperTest {

    /**
     * A scaling descriptor just for testing.
     * 
     * @author Holger Eichelberger
     */
    private static class MyScalingDescriptor implements IScalingDescriptor {

        private static final long serialVersionUID = 6577750542128073160L;
        private String prefix = "prefix";

        /**
         * Creates a scaling descriptor.
         * 
         * @param prefix the prefix used to create the sub-topology
         */
        private MyScalingDescriptor(String prefix) {
            this.prefix = prefix;
        }
        
        @Override
        public Map<String, Integer> getScalingResult(double factor, boolean executors) {
            Map<String, Integer> result = new HashMap<String, Integer>();
            result.put(prefix + "processor1", (int) (1 * factor)); 
            return result;
        }

        @Override
        public Map<String, Integer> getScalingResult(int oldExecutors, int newExecutors, boolean diffs) {
            Map<String, Integer> result = new HashMap<String, Integer>();
            if (diffs) {
                result.put(prefix + "processor1", newExecutors - oldExecutors); 
            } else {
                result.put(prefix + "processor1", newExecutors); 
            }
            return result;
        }
        
    }
    
    /**
     * Tests the scaling helper.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testSubTopologyScalingHelper() throws IOException {
        INameMapping mapping = NameMappingHelperTest.loadNameMapping("randomPipMapping.xml", "RandomPip");
        Map<String, IScalingDescriptor> descriptors = new HashMap<String, IScalingDescriptor>();
        descriptors.put("RandomProcessor1", new MyScalingDescriptor("RandomProcessor1")); // -> implementation
        Map<String, List<String>> structure = new HashMap<String, List<String>>();
        List<String> sub = new ArrayList<String>();
        sub.add("RandomProcessor1processor1;eu.qualimaster.algorithms.Process1Bolt");
        structure.put("RandomProcessor1", sub);
        SubTopologyMonitoringEvent evt = new SubTopologyMonitoringEvent("RandomPip", structure, descriptors);
        mapping.considerSubStructures(evt);
        CoordinationManager.registerTestMapping(mapping);
        
        Map<String, Integer> result = SubTopologyScalingHelper.getSubTopologyScaling(
            "RandomPip", "RandomProcessor1", 2, true).toMappedMap();
        Assert.assertNotNull(result);
        Assert.assertTrue(result.get("RandomProcessor1processor1") > 0);
        result = SubTopologyScalingHelper.getSubTopologyScaling(
            "RandomPip", "RandomProcessor1", 2, false).toMappedMap();
        Assert.assertNotNull(result);
        Assert.assertTrue(result.get("RandomProcessor1processor1") > 0);
        result = SubTopologyScalingHelper.getSubTopologyScaling(
            "RandomPip", "RandomProcessor1", 1, 2, true).toMappedMap();
        Assert.assertNotNull(result);
        Assert.assertTrue(result.get("RandomProcessor1processor1") > 0);
        
        CoordinationManager.unregisterNameMapping(mapping);
    }
    
}
