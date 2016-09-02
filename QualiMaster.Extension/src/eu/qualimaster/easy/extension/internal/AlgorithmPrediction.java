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
     * Emits a class loading error.
     * 
     * @param exc the exception/throwable
     */
    private static void error(Throwable exc) {
        Registration.error("Error loading AlgorithmPredictorImpl - falling back to default: " + exc.getMessage());
    }

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
    public static Double algorithmPrediction(IObservable observable, double actual, 
        String element, String parameter, Object value) {
        return IMPL.algorithmPrediction(observable, actual, element, parameter, value);
    }

}
