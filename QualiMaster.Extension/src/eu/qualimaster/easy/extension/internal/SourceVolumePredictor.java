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

/**
 * Encapsulates the source volume predictor. This indirection is necessary, because when testing / running layers
 * independently of each other, the Monitoring Layer may not be available.
 * 
 * @author Holger Eichelberger
 */
class SourceVolumePredictor {

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
        return null;
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
        return null;
    }
    
}
