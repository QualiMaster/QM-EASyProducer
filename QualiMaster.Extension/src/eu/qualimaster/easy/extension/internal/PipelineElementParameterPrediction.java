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

import eu.qualimaster.observables.IObservable;
import net.ssehub.easy.instantiation.core.model.vilTypes.IVilType;
import net.ssehub.easy.instantiation.core.model.vilTypes.Instantiator;

/**
 * Performs parameter value predictions.
 * 
 * @author Holger Eichelberger
 */
@Instantiator("obtainPipelineElementParameterPrediction")
public class PipelineElementParameterPrediction implements IVilType {

    /**
     * Performs a prediction of an adequate parameter under a certain deviation.
     * 
     * @param observable the observable to predict
     * @param deviation the actual deviation of <code>observable</code>
     * @param element the name of the pipeline element to predict on
     * @param parameter the parameter name
     * @param value the actual value 
     * @return the predicted new value (may be <b>null</b> if there is no prediction)
     */
    public static Object obtainPipelineElementParameterPrediction(IObservable observable, double deviation, 
        String element, String parameter, Object value) {
        return null;
    }

}
