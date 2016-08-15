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

import eu.qualimaster.coordination.ZkUtils;
import net.ssehub.easy.instantiation.core.model.vilTypes.IVilType;
import net.ssehub.easy.instantiation.core.model.vilTypes.Instantiator;

/**
 * Provides access to properties of the QM coordination layer.
 * 
 * @author Holger Eichelberger
 */
@Instantiator("supportsTaskReallocation")
public class CoordinationHelper implements IVilType {

    private static boolean inTesting = false;

    /**
     * Changes this class to testing mode pretending that task re-allocation is always available.
     * 
     * @param testing <code>true</code> for testing mode, <code>false</code> else
     */
    public static void setInTesting(boolean testing) {
        inTesting = testing;
    }
    
    /**
     * Returns whether this installation of the QualiMaster infrastructure supports runtime task re-allocation.
     * 
     * @return <code>true</code> for runtime task re-allocation, <code>false</code> else
     */
    public static boolean supportsTaskReallocation() {
        return inTesting || ZkUtils.isQmStormVersion();
    }
    
}
