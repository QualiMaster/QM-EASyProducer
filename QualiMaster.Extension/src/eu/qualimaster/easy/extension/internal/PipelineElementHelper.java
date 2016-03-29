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

import de.uni_hildesheim.sse.easy_producer.instantiator.model.common.VilException;
import de.uni_hildesheim.sse.easy_producer.instantiator.model.vilTypes.IVilType;
import de.uni_hildesheim.sse.easy_producer.instantiator.model.vilTypes.Instantiator;
import de.uni_hildesheim.sse.easy_producer.instantiator.model.vilTypes.configuration.Configuration;
import de.uni_hildesheim.sse.easy_producer.instantiator.model.vilTypes.configuration.DecisionVariable;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.model.IvmlKeyWords;

/**
 * Helper functions on pipeline element level.
 * 
 * @author Holger Eichelberger
 */
@Instantiator("obtainPipelineElement")
public class PipelineElementHelper implements IVilType {

    /**
     * Obtains a pipeline element.
     * 
     * @param config the configuration
     * @param variableName the name of the (element) variable
     * @return the element or <b>null</b> if it does not exist for some reason
     * @throws VilException in case of resolution problems
     */
    public static DecisionVariable obtainPipelineElement(Configuration config, String variableName) 
        throws VilException {
        DecisionVariable result = null;
        DecisionVariable pip = null;
        if (null != variableName && null != config) {
            variableName = variableName.replace(String.valueOf(IvmlKeyWords.COMPOUND_ACCESS), 
                IvmlKeyWords.NAMESPACE_SEPARATOR); // compensate instanceName
            pip = PipelineHelper.obtainPipeline(config, variableName);
        }
        if (null != pip) {
            String scopeName = pip.getDecisionVariable().getDeclaration().getParent().getName();
            String tmpName = cutPrefix(variableName, scopeName);
            tmpName = cutPrefix(tmpName, IvmlKeyWords.NAMESPACE_SEPARATOR);
            IDecisionVariable tmp = PipelineHelper.obtainPipelineElementByName(pip.getDecisionVariable(), null, 
                tmpName);
            int pos = 0;
            while (null == tmp && pos >= 0) {
                pos = tmpName.lastIndexOf(IvmlKeyWords.NAMESPACE_SEPARATOR);
                if (pos > 0) {
                    tmpName = tmpName.substring(0, pos);
                    tmp = PipelineHelper.obtainPipelineElementByName(pip.getDecisionVariable(), null, tmpName);
                }
                pos--;
            }
            if (null != tmp) { // top-level var
                // better: config.findVariable
                result = config.getByName(net.ssehub.easy.varModel.confModel.Configuration.getInstanceName(tmp));
            }
        }
        return result;
    }

    /**
     * Cuts <code>prefix</code> from <code>name</code>.
     * 
     * @param name the name
     * @param prefix the prefix to remove from
     * @return <code>name</code> or <code>name</code> without prefix
     */
    private static String cutPrefix(String name, String prefix) {
        String result = name;
        if (name.startsWith(prefix)) {
            result = name.substring(prefix.length());
        }
        return result;
    }

}
