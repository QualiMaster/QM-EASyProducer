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
package eu.qualimaster.easy.extension.modelop;

import net.ssehub.easy.instantiation.core.Bundle;
import net.ssehub.easy.varModel.confModel.Configuration;
import net.ssehub.easy.varModel.confModel.ConfigurationException;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.model.Project;

/**
 * {@link QualiMasterConfigurationSaver} used by the {@link ModelModifier} to save values generated by constraints.
 * @author El-Sharkawy
 *
 */
class QmPrunedConfigSaver extends QualiMasterConfigurationSaver {

    /**
     * Single constructor for this class.
     * @param srcConfiguration The configuration which should be saved.
     * @throws ConfigurationException in case of any configuration errors
     */
    QmPrunedConfigSaver(Configuration srcConfiguration) throws ConfigurationException {
        super(srcConfiguration, false, false);
    }

    @Override
    protected void saveFreezeStates(Project confProject) {
        // Freeze States are added by the next step
        super.saveFreezeStates(confProject);
    }
    
    @Override
    protected boolean isSavingEnabled(Project destProject, IDecisionVariable var) {
        boolean savingEnabled = super.isSavingEnabled(destProject, var);

        if (savingEnabled) {
            // QMI-Conf app does not modify predefined annotation values -> do not save them 
            savingEnabled = !var.getDeclaration().isAttribute();
            if (!savingEnabled) {
                Bundle.getLogger(QmPrunedConfigSaver.class).debug("Ommiting annotation value: ",
                    var.getDeclaration().getName(), " = " , var.getValue().getValue());
            }
        }
        
        return savingEnabled;
    }
}
