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

import net.ssehub.easy.instantiation.core.model.common.VilException;
import net.ssehub.easy.instantiation.core.model.vilTypes.IVilType;
import net.ssehub.easy.instantiation.core.model.vilTypes.Instantiator;

/**
 * Supports obtaining hardware artifact URLs.
 * 
 * @author Holger Eichelberger
 */
@Instantiator("obtainHardwareArtifactUrl")
public class HardwareRepositoryHelper implements IVilType {

    /**
     * Returns a hardware artifact URL (extension "zip", classifier "hardware"). [convenience]
     * 
     * @param artifactSpec the artifact specification
     * @return the URL
     * @throws VilException in case of further failures
     * @see RepositoryHelper#obtainArtifactUrl(String, String, String)
     */
    public static String obtainHardwareArtifactUrl(String artifactSpec) throws VilException {
        return RepositoryHelper.obtainArtifactUrl(artifactSpec, "hardware", "zip");
    }
    
}
