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

import eu.qualimaster.monitoring.ObservableMapper;
import eu.qualimaster.observables.IObservable;
import net.ssehub.easy.instantiation.core.model.vilTypes.IVilType;
import net.ssehub.easy.instantiation.core.model.vilTypes.Instantiator;
import net.ssehub.easy.instantiation.core.model.vilTypes.Map;
import net.ssehub.easy.instantiation.core.model.vilTypes.OperationMeta;
import net.ssehub.easy.instantiation.core.model.vilTypes.ParameterMeta;
import net.ssehub.easy.instantiation.core.model.vilTypes.Set;
import net.ssehub.easy.instantiation.core.model.vilTypes.TypeDescriptor;
import net.ssehub.easy.instantiation.rt.core.model.rtVil.types.RtVilTypeRegistry;

/**
 * Maps IVML names to observables.
 * 
 * @author Holger Eichelberger
 */
@Instantiator("mapObservable")
public class ObservableHelper implements IVilType {

    /**
     * Turns the IVML name of an observable into an observable.
     * 
     * @param ivmlName the IVML name
     * @return the observable (may be <b>null</b> if there is no mapping)
     */
    public static IObservable mapObservable(String ivmlName) {
        return ObservableMapper.getObservableByType(ivmlName);
    }
    
    /**
     * Turns a set of string names into a mapping of names and observables. Observables
     * are added only if there is a mapping through {@link #mapObservable(String)},
     * 
     * @param ivmlNames the names to be mapped
     * @return the name-observable mapping
     */
    @OperationMeta(returnGenerics = {String.class, IObservable.class})
    public static Map<String, IObservable> mapObservable(
        @ParameterMeta(generics = {String.class}) 
        Set<String> ivmlNames) {
        HashMap<Object, Object> tmpResult = new HashMap<Object, Object>();
        for (String n : ivmlNames) {
            IObservable obs = mapObservable(n);
            if (null != obs) {
                tmpResult.put(n, obs);
            }
        }
        TypeDescriptor<?>[] types = TypeDescriptor.createArray(2);
        types[0] = RtVilTypeRegistry.stringType();
        types[1] = RtVilTypeRegistry.INSTANCE.getType(IObservable.class);
        return new Map<String, IObservable>(tmpResult, types);
    }
    
}
