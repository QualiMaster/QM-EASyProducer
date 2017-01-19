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
import net.ssehub.easy.instantiation.core.model.vilTypes.ParameterMeta;
import net.ssehub.easy.instantiation.core.model.vilTypes.Set;

/**
 * Extended multi-algorithm mass prediction with varying parameters. 
 * The name is a reminiscence to Microsoft interfaces.
 * 
 * @author Holger Eichelberger
 */
@Instantiator("algorithmPredictionEx")
public class AlgorithmPredictionEx implements IVilType {

    /**
     * Creates a request to obtain the prediction for multiple algorithms applicable in this 
     * in this situation (including varying parameters).
     * 
     * @param pipeline the pipeline to predict for
     * @param pipelineElement the pipeline element
     * @param algorithms the algorithms to take into account
     * @param observables the observables
     * @return the predictions per algorithm/observables, if not possible individual predictions may be <b>null</b>
     *     or the entire result may be <b>null</b> if there is no prediction at all
     */
    public static AlgorithmPredictionResult algorithmPredictionEx(String pipeline, String pipelineElement, 
        @ParameterMeta(generics = {String.class}) 
        Set<String> algorithms, 
        @ParameterMeta(generics = {IObservable.class}) 
        Set<IObservable> observables) {
        return AlgorithmPrediction.algorithmPredictionEx(pipeline, pipelineElement, algorithms, observables);
    }

}
