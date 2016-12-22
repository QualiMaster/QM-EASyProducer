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

import eu.qualimaster.observables.AnalysisObservables;
import eu.qualimaster.observables.CloudResourceUsage;
import eu.qualimaster.observables.FunctionalSuitability;
import eu.qualimaster.observables.IObservable;
import eu.qualimaster.observables.ResourceUsage;
import eu.qualimaster.observables.Scalability;
import eu.qualimaster.observables.TimeBehavior;

/**
 * A static mapping between implementing classes of observables and variables of the model.
 * @author El-Sharkawy
 *
 */
public class ObservableMapping {
    
    /**
     * Mapping for observables of algorithms.
     */
    private static final Map<String, String> ALGORITHM_OBERVABLES = new HashMap<String, String>();
    
    /**
     * Reverse mapping for {@link #ALGORITHM_OBERVABLES}.
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
        ALGORITHM_OBERVABLES.put(observable.name(), variableName);
        REVERSE_ALGORITHM_MAPPING.put(variableName, observable.name());
    }
    /**
     * Part of the static block, adds a mapping between class name of an obervable and the model item to the two
     * maps. Not suitable for algorithms as they have different slot names for the same observables as the other
     * elements.
     * @param observable The implementing obervable enumeration.
     * @param variableName The name of the model element.
     */
    private static void put(IObservable observable, String variableName) {
        GENERAL_OBSERVABLES.put(observable.name(), variableName);
        REVERSE_GENERAL_MAPPING.put(variableName, observable.name());
    }
    
    static {
        putAlgorithmMapping(FunctionalSuitability.ACCURACY_CONFIDENCE, "accuracyConfidence");
        putAlgorithmMapping(FunctionalSuitability.ACCURACY_ERROR_RATE, "accuracyErrorRate");
        putAlgorithmMapping(FunctionalSuitability.BELIEVABILITY, "believability");
        putAlgorithmMapping(FunctionalSuitability.COMPLETENESS, "completeness");
        putAlgorithmMapping(ResourceUsage.HOSTS, "pipeline_Hosts");
        putAlgorithmMapping(AnalysisObservables.IS_VALID, "isValid");
        putAlgorithmMapping(Scalability.ITEMS, "family_Items");
        putAlgorithmMapping(TimeBehavior.LATENCY, "latency");
        putAlgorithmMapping(FunctionalSuitability.RELEVANCY, "relevancy");
        putAlgorithmMapping(TimeBehavior.THROUGHPUT_ITEMS, "throughputItems");
        putAlgorithmMapping(TimeBehavior.THROUGHPUT_VOLUME, "throughputVolume");
        putAlgorithmMapping(ResourceUsage.USED_MEMORY, "memoryUse");
        putAlgorithmMapping(Scalability.VARIETY, "variety");
        putAlgorithmMapping(Scalability.VELOCITY, "velocity");
        putAlgorithmMapping(Scalability.VOLUME, "volume");
        
        put(FunctionalSuitability.ACCURACY_CONFIDENCE, "accuracyConfidence");
        put(FunctionalSuitability.ACCURACY_ERROR_RATE, "accuracyErrorRate");
        put(ResourceUsage.AVAILABLE, "available");
        put(ResourceUsage.AVAILABLE_DFES, "availableDFEs");
//        put(ResourceUsage.AVAILABLE_DFES, "availableMachines");
        put(ResourceUsage.AVAILABLE_MEMORY, "availableMemory");
        put(ResourceUsage.AVAILABLE_FREQUENCY, "availableFrequency");
        put(ResourceUsage.BANDWIDTH, "bandwidth");
        put(ResourceUsage.CAPACITY, "capacity");
        put(FunctionalSuitability.COMPLETENESS, "completeness");
        put(ResourceUsage.EXECUTORS, "executors");
        put(ResourceUsage.HOSTS, "hosts");
        put(AnalysisObservables.IS_VALID, "isValid");
        put(AnalysisObservables.IS_ENACTING, "isEnacting");
        put(Scalability.ITEMS, "items");
        put(TimeBehavior.LATENCY, "latency");
        put(ResourceUsage.LOAD, "load");
        put(CloudResourceUsage.PING, "ping");
        put(ResourceUsage.TASKS, "tasks");
        put(TimeBehavior.THROUGHPUT_ITEMS, "throughputItems");
        put(TimeBehavior.THROUGHPUT_VOLUME, "throughputVolume");
        put(ResourceUsage.USED_DFES, "usedDFEs");
//        put(ResourceUsage.USED_DFES, "usedMachines");
        put(CloudResourceUsage.USED_HARDDISC_MEM, "UsedHarddiscMem");
        put(ResourceUsage.USED_MEMORY, "usedMemory");
        put(CloudResourceUsage.USED_PROCESSORS, "UsedProcessors");
        put(CloudResourceUsage.USED_WORKING_STORAGE, "UsedWorkingStorage");
        put(Scalability.VELOCITY, "velocity");
        put(Scalability.VOLATILITY, "volatility");
        put(Scalability.VOLUME, "volume");
    }
    
    /**
     * Returns the variable name for an {@link IObservable} of an algorithm.
     * @param implementingObservableName The name of an {@link IObservable} implementation.
     * @return the variable name of the IVML model, or <tt>null</tt> if there is no such {@link IObservable}.
     */
    public static String mapAlgorithmObervable(String implementingObservableName) {
        return ALGORITHM_OBERVABLES.get(implementingObservableName);
    }
    
    /**
     * Returns {@link IObservable#name()} for a given runtime variable of the IVML model of an algorithm.
     * @param variableObservableName a runtime variable of an algorithm
     * @return {@link IObservable#name()} or <tt>null</tt> if there exist no such {@link IObservable} for the given
     *     variable name. This must be an variable name and not a display name! 
     */
    public static String mapReverseAlgorithmObervable(String variableObservableName) {
        return REVERSE_ALGORITHM_MAPPING.get(variableObservableName);
    }
    
    /**
     * Returns the variable name for an {@link IObservable} (not useable for algorithms).
     * @param implementingObservableName The name of an {@link IObservable} implementation.
     * @return the variable name of the IVML model, or <tt>null</tt> if there is no such {@link IObservable}.
     */
    public static String mapGeneralObervable(String implementingObservableName) {
        return GENERAL_OBSERVABLES.get(implementingObservableName);
    }
    
    /**
     * Returns {@link IObservable#name()} for a given runtime variable of the IVML model (not useable for algorithms).
     * @param variableObservableName a runtime variable
     * @return {@link IObservable#name()} or <tt>null</tt> if there exist no such {@link IObservable} for the given
     *     variable name. This must be an variable name and not a display name! 
     */
    public static String mapReverseGeneralObervable(String variableObservableName) {
        return REVERSE_GENERAL_MAPPING.get(variableObservableName);
    }
    
}
