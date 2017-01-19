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

import eu.qualimaster.common.QMInternal;
import eu.qualimaster.observables.IObservable;
import net.ssehub.easy.instantiation.core.model.vilTypes.IVilType;
import net.ssehub.easy.instantiation.core.model.vilTypes.Instantiator;
import net.ssehub.easy.instantiation.core.model.vilTypes.Map;
import net.ssehub.easy.instantiation.core.model.vilTypes.OperationMeta;
import net.ssehub.easy.instantiation.core.model.vilTypes.ParameterMeta;
import net.ssehub.easy.instantiation.core.model.vilTypes.Set;

import static eu.qualimaster.easy.extension.internal.PredictionUtils.*;

/**
 * Performs observation predictions for pipeline elements.
 * 
 * @author Holger Eichelberger
 */
@Instantiator("algorithmPrediction")
public class AlgorithmPrediction implements IVilType {
    
    // explanation see AlgorithmPredictor
    private static final AlgorithmPredictor IMPL;
    
    static {
        AlgorithmPredictor impl = null;
        try {
            Class<?> cls = Class.forName("eu.qualimaster.easy.extension.internal.AlgorithmPredictorImpl");
            impl = (AlgorithmPredictor) cls.newInstance();
        } catch (ClassNotFoundException e) {
            error(e);
        } catch (InstantiationException e) {
            error(e);
        } catch (IllegalAccessException e) {
            error(e);
        } catch (ClassCastException e) {
            error(e);
        }
        if (null == impl) {
            impl = new AlgorithmPredictor();
        }
        IMPL = impl;
    }

    /**
     * Returns the internal predictor instance.
     * 
     * @return the instance
     */
    static AlgorithmPredictor getInstance() {
        return IMPL;
    }
    
    /**
     * Emits a class loading error.
     * 
     * @param exc the exception/throwable
     */
    private static void error(Throwable exc) {
        Registration.error("Error loading AlgorithmPredictorImpl - falling back to default: " + exc.getMessage());
    }

    /**
     * Creates a request for predicting the characteristics for a given algorithm in the current target setting.
     * 
     * @param pipeline the pipeline to predict for
     * @param pipelineElement the pipeline element
     * @param algorithm the algorithm (may be <b>null</b> for the actual one)
     * @param observable the observable to predict for
     * @return the predicted value or <b>null</b> if there is no prediction
     */
    public static Double algorithmPrediction(String pipeline, String pipelineElement, String algorithm, 
        IObservable observable) {
        return IMPL.algorithmPrediction(pipeline, pipelineElement, algorithm, observable);
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
    public static Double algorithmPrediction(String pipeline, String pipelineElement, String algorithm, 
        IObservable observable, 
        @ParameterMeta(generics = {Object.class, Serializable.class}) // serializable is not known to VIL -> any/object
        Map<Object, Serializable> targetValues) {
        return IMPL.algorithmPrediction(pipeline, pipelineElement, algorithm, observable, toMappedMap(targetValues));
    }

    /**
     * Creates a request to obtain the best algorithm in this situation.
     * 
     * @param pipeline the pipeline to predict for
     * @param pipelineElement the pipeline element
     * @param algorithms the algorithms to take into account
     * @param observables the observables
     * @return the predictions per algorithm/observables, if not possible individual predictions may be <b>null</b>
     *     or the entire result may be <b>null</b> if there is no prediction at all
     */
    @OperationMeta(returnGenerics = {String.class, Map.class, IObservable.class, Double.class})
    public static Map<String, Map<IObservable, Double>> algorithmPrediction(String pipeline, String pipelineElement, 
        @ParameterMeta(generics = {String.class}) 
        Set<String> algorithms, 
        @ParameterMeta(generics = {IObservable.class}) 
        Set<IObservable> observables) {
        return algorithmPrediction(pipeline, pipelineElement, algorithms, observables, null);
    }
    
    /**
     * Creates a request to obtain the best algorithm in this situation.
     * 
     * @param pipeline the pipeline to predict for
     * @param pipelineElement the pipeline element
     * @param algorithms the algorithms to take into account
     * @param observables the observables
     * @param targetValues the target values for a modified situation (may be <b>null</b> if just the algorithm may 
     *     change based on the current situation)
     * @return the predictions per algorithm/observables, if not possible individual predictions may be <b>null</b>
     *     or the entire result may be <b>null</b> if there is no prediction at all
     */
    @OperationMeta(returnGenerics = {String.class, Map.class, IObservable.class, Double.class})
    public static Map<String, Map<IObservable, Double>> algorithmPrediction(String pipeline, String pipelineElement, 
        @ParameterMeta(generics = {String.class}) 
        Set<String> algorithms, 
        @ParameterMeta(generics = {IObservable.class}) 
        Set<IObservable> observables, 
        @ParameterMeta(generics = {Object.class, Serializable.class}) // serializable is not known to VIL -> any/object
        Map<Object, Serializable> targetValues) {
        return transferMap(IMPL.algorithmPrediction(pipeline, pipelineElement, algorithms.toMappedSet(), 
            observables.toMappedSet(), toMappedMap(targetValues)), String.class, IObservable.class, Double.class);
    }

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
    @QMInternal
    static AlgorithmPredictionResult algorithmPredictionEx(String pipeline, String pipelineElement, 
            @ParameterMeta(generics = {String.class}) 
            Set<String> algorithms, 
            @ParameterMeta(generics = {IObservable.class}) 
            Set<IObservable> observables) {
        return IMPL.algorithmPredictionEx(pipeline, pipelineElement, algorithms.toMappedSet(), 
            observables.toMappedSet());
    }

}
