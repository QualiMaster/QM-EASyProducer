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

import net.ssehub.easy.instantiation.core.model.common.VilException;
import net.ssehub.easy.instantiation.core.model.vilTypes.Map;
import net.ssehub.easy.instantiation.core.model.vilTypes.TypeDescriptor;
import net.ssehub.easy.instantiation.rt.core.model.rtVil.types.RtVilTypeRegistry;

/**
 * Some utility methods for algorithm predictions.
 * 
 * @author Holger Eichelberger
 */
public class PredictionUtils {

    /**
     * Translates a VIL map to a Java map.
     * 
     * @param <K> the key type
     * @param <V> the value type
     * @param map the map to be translated (may be <b>null</b>)
     * @return the translated map (may be <b>null</b> if <code>map</code> is <b>null</b>)
     */
    static <K, V> java.util.Map<K, V> toMappedMap(Map<K, V> map) {
        java.util.Map<K, V> result;
        if (null != map) {
            result = map.toMappedMap();
        } else {
            result = null;
        }
        return result;
    }

    /**
     * Translates the Java result instance to a VIL result instance.
     * 
     * @param <K> the main access key
     * @param <KV> the secondary access key to the internal map/values
     * @param <V> the value type
     * @param res the Java result instance
     * @param classK the actual class of <code>K</code>
     * @param classKV the actual class of <code>KV</code>
     * @param classV the actual class of <code>V</code>
     * @return the corresponding VIL result instance, always a result even if empty
     */
    static <K, KV, V> Map<K, Map<KV, V>> transferMap(java.util.Map<K, java.util.Map<KV, V>> res, 
        Class<K> classK, Class<KV> classKV, Class<V> classV) {

        TypeDescriptor<?>[] predTypesInner = TypeDescriptor.createArray(2);
        predTypesInner[0] = RtVilTypeRegistry.INSTANCE.getType(classKV);
        predTypesInner[1] = RtVilTypeRegistry.INSTANCE.getType(classV);
        TypeDescriptor<?>[] predTypes = TypeDescriptor.createArray(2);
        predTypes[0] = RtVilTypeRegistry.INSTANCE.getType(classK);
        try {
            predTypes[1] = RtVilTypeRegistry.getMapType(predTypesInner);
        } catch (VilException e) {
            predTypes[1] = RtVilTypeRegistry.anyType();
        }
        
        java.util.Map<Object, Object> resTemp = new HashMap<Object, Object>();
        if (null != res) {
            for (java.util.Map.Entry<K, java.util.Map<KV, V>> ent : res.entrySet()) {
                K key = ent.getKey();
                java.util.Map<KV, V> inner = ent.getValue();
                java.util.Map<Object, Object> tmpInner = new HashMap<Object, Object>();
                tmpInner.putAll(inner);
                resTemp.put(key, new Map<KV, V>(tmpInner, predTypesInner));
            }
        }
        return new Map<K, Map<KV, V>>(resTemp, predTypes);
    }

}
