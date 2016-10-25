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

import eu.qualimaster.adaptation.events.SourceVolumeAdaptationEvent;
import eu.qualimaster.monitoring.events.ConstraintViolationAdaptationEvent;
import eu.qualimaster.monitoring.events.FrozenSystemState;
import eu.qualimaster.monitoring.events.ViolatingClause;
import eu.qualimaster.observables.ResourceUsage;
import eu.qualimaster.observables.Scalability;
import net.ssehub.easy.instantiation.core.model.vilTypes.IVilType;
import net.ssehub.easy.instantiation.core.model.vilTypes.Instantiator;

import static eu.qualimaster.easy.extension.QmConstants.*;

/**
 * Converts specific events into constraint violations. This shall only be available for
 * certain event types, i.e., the respective constructors are not available in rt-VIL.
 * 
 * @author Holger Eichelberger
 */
@Instantiator("toConstraintViolation")
public class ConstraintViolationConverter implements IVilType {

    /**
     * Turns a source volume adaptation event into a ITEMS constraint violation. Just for internal use!!
     * 
     * @param event the source volume adaptation event
     * @return the converted event
     */
    public static ConstraintViolationAdaptationEvent toConstraintViolation(SourceVolumeAdaptationEvent event) {
        List<ViolatingClause> violating = new ArrayList<ViolatingClause>();
        violating.add(new ViolatingClause(Scalability.ITEMS, SLOT_SOURCE_ITEMS, ">", event.getAllDeviations(), 
            event.getAverageDeviations()));
        ConstraintViolationAdaptationEvent result = new ConstraintViolationAdaptationEvent(violating, 
            new FrozenSystemState()); // use the actual state!
        return result;
    }

    /**
     * Turns a source volume adaptation event into a CAPACITY constraint violation. Just for internal use!!
     * 
     * @param capacity the violating capacity value
     * @param capacityDeviation the deviation
     * @return the converted event (may be <b>null</b> if the parameters are not valid)
     */
    public static ConstraintViolationAdaptationEvent toConstraintViolation(double capacity, double capacityDeviation) {
        List<ViolatingClause> violating = new ArrayList<ViolatingClause>();
        ConstraintViolationAdaptationEvent result = null;
        if (0 <= capacity && capacity <= 1) {
            violating.add(new ViolatingClause(ResourceUsage.CAPACITY, SLOT_SOURCE_CAPACITY, ">", capacity, 
                capacityDeviation));
            result = new ConstraintViolationAdaptationEvent(violating, 
                new FrozenSystemState()); // use the actual state!
        }
        return result;
    }

}
