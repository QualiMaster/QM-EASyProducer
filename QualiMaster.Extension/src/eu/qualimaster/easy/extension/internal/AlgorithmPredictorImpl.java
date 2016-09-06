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
import java.util.Map;
import java.util.Set;

import eu.qualimaster.events.SynchronousEventStore;
import eu.qualimaster.monitoring.events.AlgorithmProfilePredictionRequest;
import eu.qualimaster.monitoring.events.AlgorithmProfilePredictionResponse;
import eu.qualimaster.observables.IObservable;

/**
 * Implements the algorithm (profile) predictor based on the Monitoring Layer (via synchronous event communication).
 * 
 * @author Holger Eichelberger
 */
class AlgorithmPredictorImpl extends AlgorithmPredictor {

    private SynchronousEventStore<AlgorithmProfilePredictionRequest, AlgorithmProfilePredictionResponse> store 
        = new SynchronousEventStore<AlgorithmProfilePredictionRequest, AlgorithmProfilePredictionResponse>(
             AlgorithmProfilePredictionResponse.class);

    // class must have a non-argument constructor, be accessible within this package and not be moved/renamed!

    @Override
    public Double algorithmPrediction(String pipeline, String pipelineElement, String algorithm, 
        IObservable observable) {
        return algorithmPrediction(pipeline, pipelineElement, algorithm, observable, null);
    }
    
    @Override
    public Double algorithmPrediction(String pipeline, String pipelineElement, String algorithm, 
        IObservable observable, Map<Object, Serializable> targetValues) {
        Double result;
        AlgorithmProfilePredictionResponse resp = waitFor(
            new AlgorithmProfilePredictionRequest(pipeline, pipelineElement, algorithm, observable, targetValues));
        double tmp = resp.getPrediction();
        if (Double.MIN_VALUE == tmp) {
            result = null;
        } else {
            result = tmp;
        }
        return result;
    }

    @Override
    public String algorithmPrediction(String pipeline, String pipelineElement, Set<String> algorithms, 
        Map<IObservable, Double> weighting, Map<Object, Serializable> targetValues) {
        AlgorithmProfilePredictionResponse resp = waitFor(
            new AlgorithmProfilePredictionRequest(pipeline, pipelineElement, algorithms, weighting, targetValues));
        return resp.getAlgorithm();
    }
    
    /**
     * Waits for a response to the given <code>request</code>.
     * 
     * @param request the request
     * @return the response (may be <b>null</b> if there was none within the predefined timeout)
     */
    private AlgorithmProfilePredictionResponse waitFor(AlgorithmProfilePredictionRequest request) {
        return store.waitFor(2000, 100, request);
    }
    
}
