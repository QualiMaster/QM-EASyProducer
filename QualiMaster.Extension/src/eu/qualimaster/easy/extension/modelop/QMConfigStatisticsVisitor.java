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

import net.ssehub.easy.varModel.confModel.AbstractConfigurationStatisticsVisitor;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.model.Project;
import net.ssehub.easy.varModel.model.datatypes.IDatatype;

/**
 * Gathers statistical information about the QualiMaster (Meta-) Model.
 * @author El-Sharkawy
 *
 */
public class QMConfigStatisticsVisitor extends AbstractConfigurationStatisticsVisitor {

    /**
     * Creates a fresh model statistics visitor for the QualiMaster (Meta-) Model.
     */
    public QMConfigStatisticsVisitor() {
        super(new ModelStatistics());
    }
    
    @Override
    protected void specialTreatment(IDecisionVariable variable) {
        if (!variable.isNested()) {
            IDatatype type = variable.getDeclaration().getType();
            String typeName = type.getName();
            
            if (null != typeName) {
                getStatistics().incInstance(typeName);
            }
        }
    }

    @Override
    protected void specialTreatment(Project mainProject) {
        QMModelStatistics modelVisitor = new QMModelStatistics(mainProject);
        mainProject.accept(modelVisitor);
        getStatistics().setStaticConstraints(modelVisitor.noOfConstraints());
        getStatistics().setOperations(modelVisitor.noOfOperations());
    }

    /**
     * Returns the statistics of the QM (Meta-) Model after the configuration was visited.
     * @return The statistics of the QM (Meta-) Model, will be empty if the visit method was not called before.
     */
    public ModelStatistics getStatistics() {
        return (ModelStatistics) super.getStatistics();
    }

}
