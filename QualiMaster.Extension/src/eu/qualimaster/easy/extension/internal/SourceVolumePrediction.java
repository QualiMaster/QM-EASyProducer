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

import java.util.HashMap;

import net.ssehub.easy.instantiation.core.model.vilTypes.IVilType;
import net.ssehub.easy.instantiation.core.model.vilTypes.Instantiator;
import net.ssehub.easy.instantiation.core.model.vilTypes.Map;
import net.ssehub.easy.instantiation.core.model.vilTypes.OperationMeta;
import net.ssehub.easy.instantiation.core.model.vilTypes.ParameterMeta;
import net.ssehub.easy.instantiation.core.model.vilTypes.Sequence;
import net.ssehub.easy.instantiation.core.model.vilTypes.TypeDescriptor;
import net.ssehub.easy.instantiation.core.model.vilTypes.TypeRegistry;

/**
 * Performs parameter value predictions.
 * 
 * @author Holger Eichelberger
 */
@Instantiator("sourceVolumePrediction")
public class SourceVolumePrediction implements IVilType {

    // explanation see SourceVolumePredictor
    private static final SourceVolumePredictor IMPL;
    
    static {
        SourceVolumePredictor impl = null;
        try {
            Class<?> cls = Class.forName("eu.qualimaster.easy.extension.internal.SourceVolumePredictorImpl");
            impl = (SourceVolumePredictor) cls.newInstance();
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
            impl = new SourceVolumePredictor();
        }
        IMPL = impl;
    }

    /**
     * Emits a class loading error.
     * 
     * @param exc the exception/throwable
     */
    private static void error(Throwable exc) {
        Registration.error("Error loading SourceVolumePredictorImp - falling back to default: " + exc.getMessage());
    }
    
    /**
     * Performs a prediction of source volumes for certain keywords.
     * 
     * @param pipeline the name of the pipeline to predict for
     * @param source the name of the source to predict for
     * @param keywords the keywords to predict for
     * @return the predicted new value (may be <b>null</b> if there is no prediction)
     */
    @OperationMeta(returnGenerics = {String.class, Double.class} )
    public static Map<String, Double> sourceVolumePrediction(String pipeline, String source, 
        @ParameterMeta(generics = {String.class})
        Sequence<String> keywords) {
        TypeDescriptor<?>[] types = TypeDescriptor.createArray(2);
        types[0] = TypeRegistry.stringType();
        types[1] = TypeRegistry.realType();
        
        Map<String, Double> result;
        java.util.Map<String, Double> res = IMPL.sourceVolumePrediction(pipeline, source, keywords.toMappedList());
        if (null != res) {
            java.util.Map<Object, Object> tmp = new HashMap<Object, Object>();
            tmp.putAll(res);
            result = new Map<String, Double>(tmp, types);
        } else {
            result = null;
        }
        return result;
    }
    
    /**
     * Performs a prediction of source volumes for a certain keyword.
     * 
     * @param pipeline the name of the pipeline to predict for
     * @param source the name of the source to predict for
     * @param keyword the keyword to predict for
     * @return the predicted new value (may be <b>null</b> if there is no prediction)
     */
    public static Double sourceVolumePrediction(String pipeline, String source, String keyword) {
        return IMPL.sourceVolumePrediction(pipeline, source, keyword);
    }

}
