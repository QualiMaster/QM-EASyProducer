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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import eu.qualimaster.coordination.CoordinationConfiguration;
import net.ssehub.easy.instantiation.core.model.common.VilException;
import net.ssehub.easy.instantiation.core.model.vilTypes.IVilType;
import net.ssehub.easy.instantiation.core.model.vilTypes.Instantiator;

/**
 * Access to the pipeline elements repository as far as needed from VIL.
 * 
 * @author Holger Eichelberger
 */
@Instantiator("obtainArtifactUrl")
public class RepositoryHelper implements IVilType {

    /**
     * Obtains the URL for a specific artifact. In case of snapshot artifacts, the most recently deployed artifact
     * URL is determined. Requires a correct infrastructure configuration.
     * 
     * @param artifactSpec the artifact specification (may be <b>null</b>)
     * @param classifier the optional classifier (may be <b>null</b>, empty becomes <b>null</b>) 
     * @param suffix file name extension
     * @return the URL to the artifact, may be undefined (<b>null</b>) if the URL cannot be constructed
     * @throws VilException in case of further failures
     * @see #setRepositoryBaseUrl(String)
     */
    public static String obtainArtifactUrl(String artifactSpec, String classifier, String suffix) throws VilException {
        String result;
        try {
            if (null != classifier && 0 == classifier.length()) {
                classifier = null;
            }
            URL url = eu.qualimaster.coordination.RepositoryHelper.obtainArtifactUrl(artifactSpec, classifier, suffix);
            result = url.toString();
        } catch (MalformedURLException e) {
            result = null;
        }
        return result;
    }
    
    /**
     * Changes the repository base URL for resolving artifacts.
     * 
     * @param url the base URL of the repository
     */
    public static void setRepositoryBaseUrl(String url) {
        Properties prop = new Properties();
        prop.setProperty(CoordinationConfiguration.PIPELINE_ELEMENTS_REPOSITORY, url);
        CoordinationConfiguration.configure(prop, false);
    }
    
}
