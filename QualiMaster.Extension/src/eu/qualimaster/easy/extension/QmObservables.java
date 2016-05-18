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
package eu.qualimaster.easy.extension;

import java.util.ArrayList;

import eu.qualimaster.observables.IObservable;
import eu.qualimaster.observables.Observables;
import eu.qualimaster.observables.ResourceUsage;
import eu.qualimaster.observables.Scalability;
import eu.qualimaster.observables.TimeBehavior;

/**
 * Maps the QM observables into names. Due to OSGi problems, we do not use the observables themselves.
 * 
 * @author Holger Eichelberger
 */
public class QmObservables {

    public static final String PART_INFRASTRUCTURE = "Infrastructure";
    
    // link to Monitoring layer :o
    public static final String PREFIX_MACHINE = "machine:";
    public static final String PREFIX_HWNODE = "hwNode:";
    
    public static final String RESOURCEUSAGE_USED_MACHINES = ResourceUsage.USED_MACHINES.name();
    public static final String RESOURCEUSAGE_USED_DFES = ResourceUsage.USED_DFES.name();

    public static final String TIMEBEHAVIOR_THROUGHPUT_ITEMS = TimeBehavior.THROUGHPUT_ITEMS.name();
    public static final String TIMEBEHAVIOR_LATENCY = TimeBehavior.LATENCY.name();

    public static final String SCALABILITY_ITEMS = Scalability.ITEMS.name();
    
    public static final IObservable[] OBSERVABLES_LIST = Observables.OBSERVABLES;
    
    /**
     * Get a list of all supported QMObservalbes.
     * @return observaböeNames List of observalbes.
     */
    public static ArrayList<String> getAllObservables() {
        
        ArrayList<String> observableNames = new ArrayList<String>();
        
        for (int i = 0; i < OBSERVABLES_LIST.length; i++) {
            String observableName = OBSERVABLES_LIST[i].name();
            observableNames.add(observableName);
        }
        
        return observableNames;
    }
}
