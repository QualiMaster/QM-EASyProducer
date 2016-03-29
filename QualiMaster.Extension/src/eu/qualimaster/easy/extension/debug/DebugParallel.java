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
import java.util.concurrent.atomic.AtomicInteger;

import de.uni_hildesheim.sse.reasoning.core.frontend.ReasonerFrontend;
import de.uni_hildesheim.sse.reasoning.core.reasoner.ReasonerConfiguration;
import eu.qualimaster.coordination.RepositoryHelper;
import net.ssehub.easy.basics.modelManagement.ModelInitializer;
import net.ssehub.easy.basics.modelManagement.ModelManagementException;
import net.ssehub.easy.basics.progress.ProgressObserver;
import net.ssehub.easy.varModel.confModel.Configuration;
import net.ssehub.easy.varModel.management.VarModel;
import net.ssehub.easy.varModel.model.Project;

/**
 * Debugs reasoning on the QM model in parallel.
 * 
 * @author Holger Eichelberger
 */
public class DebugParallel extends AbstractDebug {

    private static final ReasonerConfiguration RCFG = new ReasonerConfiguration();
    private static AtomicInteger count = new AtomicInteger(10);
    
    static {
        RCFG.setRuntimeMode(true);
    }

    /**
     * Implements a reasoning runnble for reasoning in parallel.
     * 
     * @author Holger Eichelberger
     */
    private static class ReasoningRunnable implements Runnable {
        
        private String name;
        private Configuration cfg;
        
        /**
         * Creates a test runnable.
         * 
         * @param name the name for output log
         * @param cfg the configuration to reason on
         */
        private ReasoningRunnable(String name, Configuration cfg) {
            this.name = name;
            this.cfg = cfg;
        }

        // checkstyle: stop exception type check
        
        @Override
        public void run() {
            while (count.getAndDecrement() > 0) {
                System.out.println("> " + name);
                try {
                    ReasonerFrontend.getInstance().propagate(cfg.getProject(), cfg, RCFG, 
                        ProgressObserver.NO_OBSERVER);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
                System.out.println("< " + name);
                sleep(5);
            }
        }

        // checkstyle: resume exception type check

    }

    /**
     * Executes the test.
     * 
     * @param args the first argument shall be the model location
     * @throws ModelManagementException in case that obtaining the models fails
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
            initialize();
            ModelInitializer.registerLoader(ProgressObserver.NO_OBSERVER);
            ModelInitializer.addLocation(modelLocation, ProgressObserver.NO_OBSERVER);
            Project project = RepositoryHelper.obtainModel(VarModel.INSTANCE, "QM", null);
            Configuration monConfig = RepositoryHelper.createConfiguration(project, "MONITORING", null);
            ModelInitializer.removeLocation(modelLocation, ProgressObserver.NO_OBSERVER);

            ModelInitializer.addLocation(modelLocation, ProgressObserver.NO_OBSERVER);
            project = RepositoryHelper.obtainModel(VarModel.INSTANCE, "QM", null);
            //Script rtVilModel = RepositoryHelper.obtainModel(RtVilModel.INSTANCE, "QM", null);
            Configuration adaptConfig = RepositoryHelper.createConfiguration(project, "ADAPTATION", null);
            ModelInitializer.removeLocation(modelLocation, ProgressObserver.NO_OBSERVER);

            ReasoningRunnable r1 = new ReasoningRunnable("Monitoring", monConfig);
            ReasoningRunnable r2 = new ReasoningRunnable("Adaptation", adaptConfig);
            Thread t1 = new Thread(r1);
            Thread t2 = new Thread(r2);
            t1.start();
            t2.start();
            while (count.get() > 0) {
                sleep(500);
            }
        }
    }

}
