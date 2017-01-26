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

import java.io.Serializable;

import eu.qualimaster.common.QMGenerics;
import eu.qualimaster.monitoring.events.AlgorithmProfilePredictionResponse;
import eu.qualimaster.observables.IObservable;
import net.ssehub.easy.instantiation.core.model.vilTypes.IVilType;
import net.ssehub.easy.instantiation.core.model.vilTypes.Map;

/**
 * The result of a multi-algorithm mass prediction. Please note that the access happens via algorithm
 * identifiers as the results are not unique for algorithm names. Turning an algorithm identifier back
 * into an algorithm name happens via {@link #getAlgorithmName(String)}.
 * 
 * @author Holger Eichelberger
 */
public class AlgorithmPredictionResult implements IVilType {

    private Map<String, Map<IObservable, Double>> predictions;
    private Map<String, Map<Object, Serializable>> parameters;
    
    /**
     * Creates an algorithm prediction result.
     * 
     * @param predictions the predictions
     * @param parameters the related required parameters to enact the predictions
     */
    AlgorithmPredictionResult(Map<String, Map<IObservable, Double>> predictions, 
        Map<String, Map<Object, Serializable>> parameters) {
        this.predictions = predictions;
        this.parameters = parameters;
    }
    
    /**
     * Turns an algorithm-prediction identifier used in multi-algorithm mass-predictions to the original algorithm name.
     * 
     * @param identifier the algorithm-prediction identifier
     * @return the algorithm name
     */
    public String getAlgorithmName(String identifier) {
        return AlgorithmProfilePredictionResponse.getAlgorithmName(identifier);
    }

    /**
     * Returns the predictions for algorithms in terms of their observables. May contain distribution
     * settings to enable tradeoffs/weighting over them.
     * 
     * @return the predictions as mapping of algorithm identifiers to predicted values per observables
     */
    @QMGenerics(types = {String.class, Map.class, IObservable.class, Double.class})
    public Map<String, Map<IObservable, Double>> getPredictions() {
        return predictions;
    }

    /**
     * Returns the parameters needed for enacting an algorithm to achieve the predicted values.
     * 
     * @return a mapping of algorithm identifiers to parameters, parameters may contain distribution 
     * settings given as observables such as tasks and executors
     */
    @QMGenerics(types = {String.class, Map.class, Object.class, Serializable.class})
    public Map<String, Map<Object, Serializable>> getParameters() {
        return parameters;
    }
    
    @Override
    public String toString() {
        return "predictions: " + predictions + " parameters: " + parameters;
    }
    
}
