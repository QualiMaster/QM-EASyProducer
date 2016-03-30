package eu.qualimaster.easy.extension.debug;

import java.io.File;
import java.io.IOException;

import eu.qualimaster.adaptation.events.AdaptationEvent;
import eu.qualimaster.coordination.RepositoryHelper;
import eu.qualimaster.monitoring.events.FrozenSystemState;
import net.ssehub.easy.basics.modelManagement.ModelInitializer;
import net.ssehub.easy.basics.modelManagement.ModelManagementException;
import net.ssehub.easy.basics.progress.ProgressObserver;
import net.ssehub.easy.instantiation.rt.core.model.rtVil.Executor;
import net.ssehub.easy.instantiation.rt.core.model.rtVil.RtVilModel;
import net.ssehub.easy.instantiation.rt.core.model.rtVil.Script;
import net.ssehub.easy.reasoning.core.frontend.ReasonerFrontend;
import net.ssehub.easy.reasoning.core.reasoner.ReasonerConfiguration;
import net.ssehub.easy.reasoning.core.reasoner.ReasonerConfiguration.IAdditionalInformationLogger;
import net.ssehub.easy.varModel.confModel.Configuration;
import net.ssehub.easy.varModel.management.VarModel;
import net.ssehub.easy.varModel.model.Project;

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

/**
 * Performs a sequential test of frozen state log files produced by the infrastructure.
 * 
 * @author Holger Eichelberger
 */
public class Debug extends AbstractDebug {
    
    private static AdaptationEvent event = new AdaptationEvent() {

        private static final long serialVersionUID = 2164881726000323540L;
    };
    private static final ReasonerConfiguration CONFIGURATION = new ReasonerConfiguration();

    static {
        CONFIGURATION.setRuntimeMode(true);
        CONFIGURATION.setAdditionalInformationLogger(new IAdditionalInformationLogger() {
            
            @Override
            public void info(String arg0) {
            }
        });
    }
    
    // checkstyle: stop exception type check

    /**
     * Processes the logged files.
     * 
     * @param prefix the file name prefix
     * @param config the configuration
     * @param rtVilModel the rt-VIL model
     */
    private static void process(String prefix, Configuration config, Script rtVilModel) {
        int file = 0;
        File tmp = RepositoryHelper.createTmpFolder();
        while (true) {
            File stateFile = new File("files", prefix + file);
            System.out.println("Checking " + stateFile.getAbsolutePath());            
            if (stateFile.exists()) {
                try {
                    FrozenSystemState state = new FrozenSystemState(stateFile);
                    Executor exec = RepositoryHelper.createExecutor(rtVilModel, tmp, config, event, state);
                    exec.stopAfterBindValues();
                    try {
                        exec.execute();
                        ReasonerFrontend.getInstance().check(config.getProject(), config, CONFIGURATION,
                                ProgressObserver.NO_OBSERVER);
                    } catch (Exception e) { // be extremely careful
                        System.err.println("During value binding: " + e.getMessage());
                        e.printStackTrace();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Throwable t) {
                    t.printStackTrace();
                    break;
                }
                file++;
            } else {
                break;
            }
        }
    }

    // checkstyle: resume exception type check

    /**
     * Executes the test in sequence. Please adjust your model location and the files to be analyzed.
     * 
     * @param args location of the model, requested functionality (none: just load the model, monitor: process 
     *   file/monitoring_ in sequence, adapt: process file/adaptation_ in sequence)
     * @throws ModelManagementException shall not occur
     */
    public static void main(String[] args) throws ModelManagementException {
        if (0 == args.length) {
            System.out.println("qualimaster.debug: <model location> [monitor|adapt]");
            System.exit(0);
        } else {
            File modelLocation = new File(args[0]);
            if (!modelLocation.exists()) {
                System.out.println("model location " + modelLocation + " does not exist");
                System.exit(0);
            }
            String prefix = null;
            if (args.length > 1) {
                if ("monitor".equals(args[1])) {
                    prefix = "monitoring_";     
                } else if ("adapt".equals(args[1])) {
                    prefix = "adaptation_";
                }
            }

            initialize();
            
            ModelInitializer.registerLoader(ProgressObserver.NO_OBSERVER);
            ModelInitializer.addLocation(modelLocation, ProgressObserver.NO_OBSERVER);
            Project project = RepositoryHelper.obtainModel(VarModel.INSTANCE, "QM", null);
            Script rtVilModel = RepositoryHelper.obtainModel(RtVilModel.INSTANCE, "QM", null);
            Configuration config = RepositoryHelper.createConfiguration(project, "TESTING", null);
            System.out.println("Model loaded...");
            
            if (null != prefix) {
                process(prefix, config, rtVilModel);
            }
        }
        
    }

}
