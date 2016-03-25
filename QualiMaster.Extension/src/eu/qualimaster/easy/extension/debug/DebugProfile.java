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
package eu.qualimaster.easy.extension.debug;

import java.io.File;

import de.uni_hildesheim.sse.model.confModel.Configuration;
import de.uni_hildesheim.sse.model.cst.CSTSemanticException;
import de.uni_hildesheim.sse.model.management.VarModel;
import de.uni_hildesheim.sse.model.varModel.ModelQueryException;
import de.uni_hildesheim.sse.model.varModel.Project;
import de.uni_hildesheim.sse.model.varModel.values.ValueDoesNotMatchTypeException;
import de.uni_hildesheim.sse.utils.modelManagement.ModelInitializer;
import de.uni_hildesheim.sse.utils.modelManagement.ModelManagementException;
import de.uni_hildesheim.sse.utils.progress.ProgressObserver;
import eu.qualimaster.coordination.RepositoryHelper;
import eu.qualimaster.easy.extension.internal.AlgorithmProfileHelper;

/**
 * Debugs creating profiling pipelines.
 * 
 * @author Holger Eichelberger
 */
public class DebugProfile extends AbstractDebug {

    /**
     * Executes the test.
     * 
     * @param args the first argument shall be the model location
     * @throws ModelManagementException in case that obtaining the models fails
     */
    public static void main(String[] args) throws ModelManagementException {
        if (0 == args.length) {
            System.out.println("qualimaster.profile: <model location>");
            System.exit(0);
        } else {
            File modelLocation = new File(args[0]);
            if (!modelLocation.exists()) {
                System.out.println("model location " + modelLocation + " does not exist");
                System.exit(0);
            }
            initialize();
            ModelInitializer.registerLoader(ProgressObserver.NO_OBSERVER);
            ModelInitializer.addLocation(modelLocation, ProgressObserver.NO_OBSERVER);
            Project project = RepositoryHelper.obtainModel(VarModel.INSTANCE, "QM", null);
            Configuration monConfig = RepositoryHelper.createConfiguration(project, "MONITORING", null);
            ModelInitializer.removeLocation(modelLocation, ProgressObserver.NO_OBSERVER);

            try {
                AlgorithmProfileHelper.profile(monConfig, "fCorrelationFinancial", "TopoSoftwareCorrelationFinancial");
                System.out.println("Creation successful.");
            } catch (CSTSemanticException e) {
                e.printStackTrace();
            } catch (ModelQueryException e) {
                e.printStackTrace();
            } catch (ValueDoesNotMatchTypeException e) {
                e.printStackTrace();
            }
        }
    }

}
