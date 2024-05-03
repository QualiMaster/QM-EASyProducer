/*
 * Copyright 2016 University of Hildesheim, Software Systems Engineering
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

import java.util.HashMap;
import java.util.Map;

import eu.qualimaster.monitoring.ObservableMapper;
import eu.qualimaster.monitoring.observations.ObservationFactory;
import eu.qualimaster.monitoring.parts.PartType;
import eu.qualimaster.observables.AnalysisObservables;
import eu.qualimaster.observables.IObservable;

/**
 * A static mapping between implementing classes of observables and variables of the model.
 * @author El-Sharkawy
 */
public class ObservableMapping {
    
    /**
     * Mapping for observables of algorithms.
     */
    private static final Map<String, String> ALGORITHM_OBSERVABLES = new HashMap<String, String>();
    
    /**
     * Reverse mapping for {@link #ALGORITHM_OBSERVABLES}.
     */
    private static final Map<String, String> REVERSE_ALGORITHM_MAPPING = new HashMap<String, String>();
    
    /**
     * Mapping for observables, do not have an own mapping (currently, only algorithms have an own mapping).
     */
    private static final Map<String, String> GENERAL_OBSERVABLES = new HashMap<String, String>();
    
    /**
     * Reverse mapping for {@link #GENERAL_OBSERVABLES}.
     */
    private static final Map<String, String> REVERSE_GENERAL_MAPPING = new HashMap<String, String>();

    /**
     * Part of the static block, adds a mapping between class name of an obervable and the algorithm item
     * to the map.
     * @param observable The implementing obervable enumeration.
     * @param variableName The name of the model element.
     */
    private static void putAlgorithmMapping(IObservable observable, String variableName) {
        ALGORITHM_OBSERVABLES.put(observable.name(), variableName);
        REVERSE_ALGORITHM_MAPPING.put(variableName, observable.name());
    }
    /**
     * Part of the static block, adds a mapping between class name of an obervable and the model item to the two
     * maps. Not suitable for algorithms as they have different slot names for the same observables as the other
     * elements.
     * @param observable The implementing observable enumeration.
     * @param variableName The name of the model element.
     */
    private static void put(IObservable observable, String variableName) {
        GENERAL_OBSERVABLES.put(observable.name(), variableName);
        REVERSE_GENERAL_MAPPING.put(variableName, observable.name());
    }
    
    static {
        Map<IObservable, String> reverse = ObservableMapper.getReverseNameMapping();
        for (PartType part : PartType.values()) {
            for (IObservable obs : ObservationFactory.getObservations(part)) {
                if (AnalysisObservables.IS_ENACTING != obs) { // handled via rt-VIL
                    String name = reverse.get(obs);
                    if (null != name) {
                        // ignore multi registrations for now
                        if (PartType.ALGORITHM == part) {
                            putAlgorithmMapping(obs, name);
                        } else {
                            put(obs, name);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Returns the variable name for an {@link IObservable} of an algorithm.
     * @param implementingObservableName The name of an {@link IObservable} implementation.
     * @return the variable name of the IVML model, or <code>null</code> if there is no such {@link IObservable}.
     */
    public static String mapAlgorithmObservable(String implementingObservableName) {
        return ALGORITHM_OBSERVABLES.get(implementingObservableName);
    }
    
    /**
     * Returns {@link IObservable#name()} for a given runtime variable of the IVML model of an algorithm.
     * @param variableObservableName a runtime variable of an algorithm
     * @return {@link IObservable#name()} or <code>null</code> if there exist no such {@link IObservable} for the given
     *     variable name. This must be an variable name and not a display name! 
     */
    public static String mapReverseAlgorithmObservable(String variableObservableName) {
        return REVERSE_ALGORITHM_MAPPING.get(variableObservableName);
    }
    
    /**
     * Returns the variable name for an {@link IObservable} (not useable for algorithms).
     * @param implementingObservableName The name of an {@link IObservable} implementation.
     * @return the variable name of the IVML model, or <code>null</code> if there is no such {@link IObservable}.
     */
    public static String mapGeneralObservable(String implementingObservableName) {
        return GENERAL_OBSERVABLES.get(implementingObservableName);
    }
    
    /**
     * Returns {@link IObservable#name()} for a given runtime variable of the IVML model (not useable for algorithms).
     * @param variableObservableName a runtime variable
     * @return {@link IObservable#name()} or <code>null</code> if there exist no such {@link IObservable} for the given
     *     variable name. This must be an variable name and not a display name! 
     */
    public static String mapReverseGeneralObservable(String variableObservableName) {
        return REVERSE_GENERAL_MAPPING.get(variableObservableName);
    }
    
}
