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

import eu.qualimaster.observables.IObservable;

/**
 * Encapsulates the algorithm (profile) predictor. This indirection is necessary, because when testing / running layers
 * independently of each other, the Monitoring Layer may not be available. However, the VIL interface shall still be
 * available.
 * 
 * @author Holger Eichelberger
 */
public class AlgorithmPredictor {

    /**
     * Creates a request for predicting the characteristics for a given algorithm in the current target setting.
     * 
     * @param pipeline the pipeline to predict for
     * @param pipelineElement the pipeline element
     * @param algorithm the algorithm (may be <b>null</b> for the actual one)
     * @param observable the observable to predict for
     * @return the predicted value or <b>null</b> if there is no prediction
     */
    public Double algorithmPrediction(String pipeline, String pipelineElement, String algorithm, 
        IObservable observable) {
        return null;
    }
    
    /**
     * Creates a request for predicting the characteristics for a given algorithm in a target setting.
     * 
     * @param pipeline the pipeline to predict for
     * @param pipelineElement the pipeline element
     * @param algorithm the algorithm (may be <b>null</b> for the actual one)
     * @param observable the observable to predict for
     * @param targetValues the target values for a modified situation (may be <b>null</b> if just the algorithm may 
     *     change based on the current situation)
     * @return the predicted value or <b>null</b> if there is no prediction
     */
    public Double algorithmPrediction(String pipeline, String pipelineElement, String algorithm, 
        IObservable observable, Map<Object, Serializable> targetValues) {
        return null;
    }
    
    /**
     * Creates a mass-request to obtain the predictions for multiple algorithms/observables.
     * 
     * @param pipeline the pipeline to predict for
     * @param pipelineElement the pipeline element
     * @param algorithms the algorithms to take into account
     * @param observables weighting the weighting
     * @param targetValues the target values for a modified situation (may be <b>null</b> if just the algorithm may 
     *     change based on the current situation)
     * @return the best algorithm or <b>null</b> if there is no prediction
     */
    public Map<String, Map<IObservable, Double>> algorithmPrediction(String pipeline, String pipelineElement, 
        Set<String> algorithms, Set<IObservable> observables, Map<Object, Serializable> targetValues) {
        return null;
    }
    
}
