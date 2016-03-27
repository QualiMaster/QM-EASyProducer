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
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import de.uni_hildesheim.sse.easy_producer.instantiator.model.common.VilException;
import de.uni_hildesheim.sse.model.confModel.Configuration;
import de.uni_hildesheim.sse.model.management.VarModel;
import de.uni_hildesheim.sse.model.varModel.Project;
import de.uni_hildesheim.sse.utils.modelManagement.ModelInitializer;
import de.uni_hildesheim.sse.utils.modelManagement.ModelManagementException;
import de.uni_hildesheim.sse.utils.progress.ProgressObserver;
import eu.qualimaster.coordination.RepositoryHelper;
import eu.qualimaster.easy.extension.internal.AlgorithmProfileHelper;
import eu.qualimaster.easy.extension.internal.QmProjectDescriptor;

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
     * @throws IOException if file operations fail
     */
    public static void main(String[] args) throws ModelManagementException, IOException {
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
            
            // create descriptor before clearing the location - in infrastructure pass vil directly/resolve VIL
            Configuration monConfig = RepositoryHelper.createConfiguration(project, "MONITORING", null);
            File tmp = new File(FileUtils.getTempDirectory(), "qmDebugProfile");
            FileUtils.deleteDirectory(tmp);
            tmp.mkdirs();
            QmProjectDescriptor source = new QmProjectDescriptor(tmp);
            try {
                AlgorithmProfileHelper.profile(monConfig, "fCorrelationFinancial", "TopoSoftwareCorrelationFinancial", 
                    source);
                System.out.println("Creation successful.");
            } catch (VilException e) {
                e.printStackTrace();
            }
            ModelInitializer.removeLocation(modelLocation, ProgressObserver.NO_OBSERVER);
        }
    }

}
