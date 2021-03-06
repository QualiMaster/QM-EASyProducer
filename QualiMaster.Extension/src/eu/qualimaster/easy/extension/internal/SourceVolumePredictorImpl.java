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

import java.util.List;
import java.util.Map;

import eu.qualimaster.events.SynchronousEventStore;
import eu.qualimaster.monitoring.events.SourceVolumePredictionRequest;
import eu.qualimaster.monitoring.events.SourceVolumePredictionResponse;

/**
 * Implements the source volume predictor based on the Monitoring Layer (via synchronous event communication).
 * 
 * @author Holger Eichelberger
 */
class SourceVolumePredictorImpl extends SourceVolumePredictor {

    private SynchronousEventStore<SourceVolumePredictionRequest, SourceVolumePredictionResponse> store 
        = new SynchronousEventStore<SourceVolumePredictionRequest, SourceVolumePredictionResponse>(
            SourceVolumePredictionResponse.class);

    // class must have a non-argument constructor, be accessible within this package and not be moved/renamed!
    
    /**
     * Performs a prediction of source volumes for certain keywords.
     * 
     * @param pipeline the name of the pipeline to predict for
     * @param source the name of the source to predict for
     * @param keywords the keywords to predict for
     * @return the predicted new value (may be <b>null</b> if there is no prediction)
     */
    public Map<String, Double> sourceVolumePrediction(String pipeline, String source, 
        List<String> keywords) {
        SourceVolumePredictionResponse resp = waitFor(new SourceVolumePredictionRequest(pipeline, source, keywords));
        return resp.getPredictions();
    }
    
    /**
     * Performs a prediction of source volumes for a certain keyword.
     * 
     * @param pipeline the name of the pipeline to predict for
     * @param source the name of the source to predict for
     * @param keyword the keyword to predict for
     * @return the predicted new value (may be <b>null</b> if there is no prediction)
     */
    public Double sourceVolumePrediction(String pipeline, String source, String keyword) {
        Double result = null; 
        SourceVolumePredictionResponse resp = waitFor(new SourceVolumePredictionRequest(pipeline, source, keyword));
        Map<String, Double> predictions = resp.getPredictions();
        if (null != predictions) {
            result = predictions.get(keyword);
        }
        return result;
    }
    
    /**
     * Waits for a response to the given <code>request</code>.
     * 
     * @param request the request
     * @return the response (may be <b>null</b> if there was none within the predefined timeout)
     */
    private SourceVolumePredictionResponse waitFor(SourceVolumePredictionRequest request) {
        return store.waitFor(2000, 100, request);
    }
    
}
