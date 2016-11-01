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

import eu.qualimaster.observables.IObservable;
import net.ssehub.easy.instantiation.core.model.vilTypes.IVilType;
import net.ssehub.easy.instantiation.core.model.vilTypes.Instantiator;
import net.ssehub.easy.instantiation.core.model.vilTypes.Map;
import net.ssehub.easy.instantiation.core.model.vilTypes.OperationMeta;
import net.ssehub.easy.instantiation.core.model.vilTypes.ParameterMeta;
import net.ssehub.easy.instantiation.core.model.vilTypes.Set;

/**
 * Performs parameter predictions for pipeline elements.
 * 
 * @author Holger Eichelberger
 */
@Instantiator("parameterPrediction")
public class ParameterPrediction implements IVilType {
    
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
    @OperationMeta(returnGenerics = {String.class, Map.class, IObservable.class, Double.class})
    public static Map<String, Map<IObservable, Double>> parameterPrediction(String pipeline, String pipelineElement, 
        String parameter,
        @ParameterMeta(generics = {IObservable.class}) 
        Set<IObservable> observables, 
        @ParameterMeta(generics = {Object.class, Serializable.class}) 
        Map<Object, Serializable> targetValues) {
        return AlgorithmPrediction.toResult(AlgorithmPrediction.getInstance().parameterPrediction(pipeline, 
            pipelineElement, parameter, observables.toMappedSet(), AlgorithmPrediction.toMappedMap(targetValues)));
    }

}
