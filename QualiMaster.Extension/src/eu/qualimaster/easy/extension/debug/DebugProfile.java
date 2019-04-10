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
import java.util.Properties;

import org.apache.commons.io.FileUtils;

import eu.qualimaster.coordination.CoordinationConfiguration;
import eu.qualimaster.coordination.RepositoryHelper;
import eu.qualimaster.easy.extension.internal.AlgorithmProfileHelper;
import eu.qualimaster.easy.extension.internal.AlgorithmProfileHelper.ProfileData;
import eu.qualimaster.easy.extension.internal.QmProjectDescriptor;
import net.ssehub.easy.basics.modelManagement.ModelInitializer;
import net.ssehub.easy.basics.modelManagement.ModelManagementException;
import net.ssehub.easy.basics.progress.ProgressObserver;
import net.ssehub.easy.instantiation.core.model.common.VilException;
import net.ssehub.easy.varModel.confModel.Configuration;
import net.ssehub.easy.varModel.management.VarModel;
import net.ssehub.easy.varModel.model.Project;

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
            Properties prop = new Properties();
            prop.put(CoordinationConfiguration.PIPELINE_ELEMENTS_REPOSITORY, 
                "https://projects.sse.uni-hildesheim.de/qm/maven/");
            CoordinationConfiguration.configure(prop, false);
            File tmp = new File(FileUtils.getTempDirectory(), "qmDebugProfile");
            FileUtils.deleteDirectory(tmp);
            tmp.mkdirs();

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
            QmProjectDescriptor source = new QmProjectDescriptor(tmp);
            try {
                ProfileData data = AlgorithmProfileHelper.createProfilePipeline(monConfig, "ProfileTestPip", 
                    "fCorrelationFinancial", "TopoSoftwareCorrelationFinancial", source);
//                  "fPreprocessor", "Preprocessor", source);
                System.out.println("Creation successful. " + data.getPipeline());
            } catch (VilException e) {
                e.printStackTrace();
            }
            ModelInitializer.removeLocation(modelLocation, ProgressObserver.NO_OBSERVER);
        }
    }

}
