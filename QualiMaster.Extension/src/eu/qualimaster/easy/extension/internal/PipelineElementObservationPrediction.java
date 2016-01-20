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

import de.uni_hildesheim.sse.easy_producer.instantiator.model.vilTypes.IVilType;
import de.uni_hildesheim.sse.easy_producer.instantiator.model.vilTypes.Instantiator;
import eu.qualimaster.observables.IObservable;

/**
 * Performs observation predictions for pipeline elements.
 * 
 * @author Holger Eichelberger
 */
@Instantiator("obtainPipelineElementObservationPrediction")
public class PipelineElementObservationPrediction implements IVilType {
    
    /**
     * Performs a prediction on a pipeline element observable when changing a parameter.
     * 
     * @param observable the observable to predict
     * @param actual the actual value of <code>observable</code>
     * @param element the name of the pipeline element to predict on
     * @param parameter the parameter name
     * @param value the actual value 
     * @return the predicted value (may be <b>null</b> if there is no prediction)
     */
    public static Double obtainPipelineElementObservationPrediction(IObservable observable, double actual, 
        String element, String parameter, Object value) {
        return null;
    }

}
