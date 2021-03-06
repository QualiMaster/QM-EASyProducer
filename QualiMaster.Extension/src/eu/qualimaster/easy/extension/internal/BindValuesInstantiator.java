/*
 * Copyright 2016 University of Hildesheim, Software Systems Engineering
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

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import eu.qualimaster.coordination.events.ModelUpdatedEvent;
import eu.qualimaster.events.EventHandler;
import eu.qualimaster.events.EventManager;
import net.ssehub.easy.instantiation.core.model.vilTypes.IVilType;
import net.ssehub.easy.instantiation.core.model.vilTypes.Instantiator;
import net.ssehub.easy.instantiation.core.model.vilTypes.configuration.Configuration;
import net.ssehub.easy.instantiation.rt.core.model.confModel.AdaptiveConfiguration;

/**
 * RtVil Instantiator to bind a runtime mapping to {@link Configuration}.
 * @author El-Sharkawy
 *
 */
@Instantiator("storeValueBinding")
public class BindValuesInstantiator implements IVilType {
    
    private static Map<net.ssehub.easy.varModel.confModel.Configuration,
        AdaptiveConfiguration<IvmlElementIdentifier.ObservableTuple>> configMapping = new HashMap<>();

    /**
     * Handles updates of the coordination model.
     * 
     * @author Holger Eichelberger
     */
    private static class ModelUpdateEventHandler extends EventHandler<ModelUpdatedEvent> {

        /**
         * Creates a handler instance.
         */
        protected ModelUpdateEventHandler() {
            super(ModelUpdatedEvent.class);
        }

        @Override
        protected void handle(ModelUpdatedEvent event) {
            synchronized (configMapping) {
                configMapping.clear(); // clean up and force re-build
            }
        }
        
    }

    static {
        EventManager.register(new ModelUpdateEventHandler());
    }
    
    /**
     * Binds the values of the given mapping to the configuration.
     * @param configuration The configuration, which shall receive the new values from the mapping
     * @param bindings The new values to set in form of <tt>&lt;id for a (nested) variable, value&gt;</tt>
     */
    public static void storeValueBinding(Configuration configuration, Map<String, Object> bindings) {
        synchronized (configMapping) {
            AdaptiveConfiguration<IvmlElementIdentifier.ObservableTuple> aConfig =
                    configMapping.get(configuration.getConfiguration());
            if (null == aConfig) {
                net.ssehub.easy.varModel.confModel.Configuration config = configuration.getConfiguration();
                aConfig = new AdaptiveConfiguration<>(config, new IvmlElementIdentifier(config));
                configMapping.put(configuration.getConfiguration(), aConfig);
            }

            aConfig.addValues(bindings);

            // Will change the configuration as a side effect
            aConfig.takeOverValues();
        }
    }

    /**
     * Converts and Vil-Map into a Java-Map and calls {@link #storeValueBinding(Configuration, Map)}.
     * @param configuration The configuration, which shall receive the new values from the mapping
     * @param bindings The new values to set in form of <tt>&lt;id for a (nested) variable, value&gt;</tt>
     */
    public static void storeValueBinding(Configuration configuration,
        net.ssehub.easy.instantiation.core.model.vilTypes.Map<String, Object> bindings) {
        Class<?> clazz = net.ssehub.easy.instantiation.core.model.vilTypes.Map.class;
        try {
            Field nestedMap = clazz.getDeclaredField("map");
            nestedMap.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) nestedMap.get(bindings);
            storeValueBinding(configuration, map);
        } catch (ReflectiveOperationException e) {
            Bundle.getLogger(BindValuesInstantiator.class).exception(e);
        } catch (SecurityException e) {
            Bundle.getLogger(BindValuesInstantiator.class).exception(e);
        }
    }
}
