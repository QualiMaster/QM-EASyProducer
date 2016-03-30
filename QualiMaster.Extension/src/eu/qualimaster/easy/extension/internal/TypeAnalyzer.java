/*
 * Copyright 2009-2014 University of Hildesheim, Software Systems Engineering
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
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import eu.qualimaster.common.QMGenerics;
import eu.qualimaster.common.QMInternal;
import eu.qualimaster.common.QMName;
import eu.qualimaster.common.QMNoSimulation;
import net.ssehub.easy.instantiation.core.model.common.VilException;
import net.ssehub.easy.instantiation.core.model.vilTypes.IRegisteredStringValueProvider;
import net.ssehub.easy.instantiation.core.model.vilTypes.TypeDescriptor;
import net.ssehub.easy.instantiation.core.model.vilTypes.TypeHelper;
import net.ssehub.easy.instantiation.core.model.vilTypes.TypeRegistry;
import net.ssehub.easy.instantiation.core.model.vilTypes.IStringValueProvider.StringComparator;
import net.ssehub.easy.instantiation.rt.core.model.rtVil.types.ITypeAnalyzer;
import net.ssehub.easy.instantiation.rt.core.model.rtVil.types.RtVilTypeRegistry;

/**
 * A QualiMaster specific type analyzer for reflective descriptor building in rt-VIL.
 * 
 * @author Holger Eichelberger
 */
class TypeAnalyzer implements ITypeAnalyzer {

    public static final String NAMESPACE = "qualimaster";
    private static final java.util.Map<String, TypeDescriptor<?>> FALLBACK_TYPES 
        = new HashMap<String, TypeDescriptor<?>>();
    private static final java.util.Set<Class<?>> IMPORTING = new HashSet<Class<?>>();
    
    static {
        registerSpecialType(Serializable.class, TypeRegistry.anyType());
    }

    /**
     * Records the importing classes for pre-lookup.
     * 
     * @param importing the importing classes
     */
    void setImportingTypes(List<Class<?>> importing) {
        for (Class<?> cls : importing) {
            IMPORTING.add(cls);            
        }
    }
    
    /**
     * Registers a fallback type mapping.
     * 
     * @param cls the class to register the type for
     * @param descriptor the related descriptor
     */
    private static void registerSpecialType(Class<?> cls, TypeDescriptor<?> descriptor) {
        if (null != cls) {
            FALLBACK_TYPES.put(cls.getSimpleName(), descriptor);
            FALLBACK_TYPES.put(cls.getName(), descriptor);
        }
    }    
    
    @Override
    public String getVilName(Class<?> cls) {
        String name = cls.getName().replace("$", "");
        QMName annotation = cls.getAnnotation(QMName.class);
        if (null != annotation) {
            String tmp = annotation.name();
            if (null != tmp && tmp.length() > 0) {
                name = tmp;
            }
        }
        
        // if already qualified... 
        name = RtVilTypeRegistry.strip(name, SEPARATOR);
        // if field access... 
        name = RtVilTypeRegistry.strip(name, ".");
        if (cls.isInterface() && name.startsWith("I")) {
            if (name.length() > 1) {
                name = name.substring(1);
            }
        }
        // prefix namespace by default
        if (name.length() > 0) {
            name = NAMESPACE + SEPARATOR + name;
        }
        return name;
    }

    @Override
    public boolean isVisible(Class<?> cls) {
        return null == cls.getAnnotation(QMInternal.class);
    }

    @Override
    public boolean isVisible(Method method) {
        return !method.getDeclaringClass().isEnum() 
            && method.getDeclaringClass() != Comparable.class
            && method.getDeclaringClass() != Enum.class
            && null == method.getAnnotation(QMInternal.class);
    }
    
    @Override
    public boolean isDisabledDuringSimulation(Method method) {
        return null != method.getAnnotation(QMNoSimulation.class);
    }

    @Override
    public boolean isVisible(Field field) {
        int mod = field.getModifiers();
        return Modifier.isPublic(mod) && Modifier.isStatic(mod) && Modifier.isFinal(mod) 
            && null == field.getAnnotation(QMInternal.class);
    }

    @Override
    public boolean isVisible(Constructor<?> constructor) {
        Class<?> declaring = constructor.getDeclaringClass();
        // CoordinationCommand.class.isAssignableFrom(declaring) &&  show also events
        return !declaring.isEnum() && null == constructor.getAnnotation(QMInternal.class);
    }
    
    @Override
    public IRegisteredStringValueProvider getStringValueProvider(final Class<?> cls) {
        return new IRegisteredStringValueProvider() {

            @Override
            public String getStringValue(Object object, StringComparator comparator) {
                String result;
                if (null != object && object.getClass().isEnum()) {
                    result = ((Enum<?>) object).name();
                } else {
                    result = "<" + cls.getSimpleName() + ">";
                }
                return result;
            }
            
        };
    }

    @Override
    public String getVilName(Method method) {
        QMName annotation = method.getAnnotation(QMName.class);
        return null == annotation ? null : annotation.name();
    }

    @Override
    public String getVilName(Field field) {
        QMName annotation = field.getAnnotation(QMName.class);
        return null == annotation ? null : annotation.name();
    }

    @Override
    public Class<?>[] getFieldGenerics(Field field) {
        return scanTypes(field.getAnnotation(QMGenerics.class));
    }

    @Override
    public Class<?>[] getParameterGenerics(Method method, int index) {
        return scanTypes(TypeHelper.getParameterAnnotation(method.getParameterAnnotations(), index, QMGenerics.class));
    }

    @Override
    public Class<?>[] getParameterGenerics(Constructor<?> constructor, int index) {
        return scanTypes(TypeHelper.getParameterAnnotation(constructor.getParameterAnnotations(), index, 
            QMGenerics.class));
    }

    @Override
    public Class<?>[] getReturnGenerics(Method method) {
        return scanTypes(method.getAnnotation(QMGenerics.class));
    }

    @Override
    public TypeDescriptor<?> resolveTypeFallback(String name) {
        return FALLBACK_TYPES.get(name);
    }
    
    /**
     * Scans types in order to figure out whether they already have been registered.
     * 
     * @param annotation the annotation to scan
     * @return the types in the annotation
     */
    private Class<?>[] scanTypes(QMGenerics annotation) {
        Class<?>[] result;
        if (null == annotation) {
            result = null;
        } else {
            result = annotation.types();
            if (null != result) {
                RtVilTypeRegistry registry = RtVilTypeRegistry.INSTANCE;
                for (int t = 0; t < result.length; t++) {
                    Class<?> cls = result[t];
                    if (IMPORTING.contains(cls) && !registry.hasType(getVilName(cls))) { // don't care for the others
                        try {
                            RtVilTypeRegistry.registerRtType(cls);
                        } catch (VilException e) {
                            // ignore if this fails
                        }
                    }
                }
            }
        }
        return result;
    }

}
