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

import static eu.qualimaster.easy.extension.internal.PredictionUtils.*;

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
    public Map<String, Map<IObservable, Double>> algorithmPrediction(String pipeline, String pipelineElement, 
        Set<String> algorithms, Set<IObservable> observables, Map<Object, Serializable> targetValues) {
        AlgorithmProfilePredictionResponse resp = waitFor(
            new AlgorithmProfilePredictionRequest(pipeline, pipelineElement, algorithms, observables, targetValues));
        return resp.getMassPrediction();
    }

    /**
     * Creates a request to obtain the best algorithm in this situation.
     * 
     * @param pipeline the pipeline to predict for
     * @param pipelineElement the pipeline element
     * @param parameter the parameter to predict for
     * @param observables the observables
     * @param targetValues the target values for a modified situation (may be <b>null</b> if just the algorithm may 
     *     change based on the current situation)
     * @return the predictions per algorithm/observables, if not possible individual predictions may be <b>null</b>
     *     or the entire result may be <b>null</b> if there is no prediction at all
     */
    @Override
    public Map<String, Map<IObservable, Double>> parameterPrediction(String pipeline, String pipelineElement, 
        String parameter, Set<IObservable> observables, Map<Object, Serializable> targetValues) {
        AlgorithmProfilePredictionResponse resp = waitFor(
            new AlgorithmProfilePredictionRequest(pipeline, pipelineElement, parameter, observables, targetValues));
        return resp.getMassPrediction();
    }

    /**
     * Creates a request to obtain the prediction for multiple algorithms applicable in this 
     * in this situation (including varying parameters).
     * 
     * @param pipeline the pipeline to predict for
     * @param pipelineElement the pipeline element
     * @param algorithms the algorithms to take into account
     * @param observables the observables
     * @return the predictionsper algorithm/observables, if not possible individual predictions may be <b>null</b>
     *     or the entire result may be <b>null</b> if there is no prediction at all
     */
    public AlgorithmPredictionResult algorithmPredictionEx(String pipeline, String pipelineElement, 
        Set<String> algorithms, Set<IObservable> observables) {
        // although type transfer shall be done in AlgorithmPredictor, we accept the type transfer here 
        AlgorithmPredictionResult res = null;
        AlgorithmProfilePredictionRequest req = new AlgorithmProfilePredictionRequest(pipeline, 
            pipelineElement, algorithms, observables, null);
        req.doMultiAlgorithmPrediction();
        AlgorithmProfilePredictionResponse resp = waitFor(req);
        if (null != resp) {
            res = new AlgorithmPredictionResult(
                transferMap(resp.getMassPrediction(), String.class, IObservable.class, Double.class),
                transferMap(resp.getParameters(), String.class, Object.class, Serializable.class));
        }
        return res;
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
