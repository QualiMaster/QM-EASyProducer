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

import net.ssehub.easy.basics.logger.EASyLoggerFactory;
import net.ssehub.easy.basics.logger.EASyLoggerFactory.EASyLogger;

/**
 * Bundle / Activator for this plug-in.
 * @author El-Sharkawy
 *
 */
public class Bundle {
    
    public static final String PLUGIN_ID = "QualiMaster.Extension";
    
    /**
     * Returns a logger for a class specified in this extension.
     * @param clazz A class of this extension for which the logger shall be returned.
     * @return The logger instance for the given class.
     */
    public static EASyLogger getLogger(Class<?> clazz) {
        return EASyLoggerFactory.INSTANCE.getLogger(clazz, PLUGIN_ID);
    }

}
