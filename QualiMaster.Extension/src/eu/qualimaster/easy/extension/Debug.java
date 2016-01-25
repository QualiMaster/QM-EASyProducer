package eu.qualimaster.easy.extension;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
//import java.util.List;

import org.osgi.service.component.ComponentContext;

//import de.uni_hildesheim.sse.model.varModel.datatypes.Compound;
//import de.uni_hildesheim.sse.model.varModel.datatypes.Enum;
//import de.uni_hildesheim.sse.model.varModel.datatypes.OclKeyWords;
//import de.uni_hildesheim.sse.model.varModel.values.ValueDoesNotMatchTypeException;
import de.uni_hildesheim.sse.easy_producer.instantiator.model.rtVil.Executor;
import de.uni_hildesheim.sse.easy_producer.instantiator.model.rtVil.RtVilModel;
import de.uni_hildesheim.sse.easy_producer.instantiator.model.rtVil.Script;
//import de.uni_hildesheim.sse.easy_producer.instantiator.model.rtVil.VariableValueCopier;
//import de.uni_hildesheim.sse.easy_producer.instantiator.model.rtVil.VariableValueCopier.IFreezeProvider;
//import de.uni_hildesheim.sse.easy_producer.instantiator.model.rtVil.VariableValueCopier.CopySpec;
//import de.uni_hildesheim.sse.easy_producer.instantiator.model.rtVil.VariableValueCopier.EnumAttributeFreezeProvider;
import de.uni_hildesheim.sse.model.confModel.Configuration;
//import de.uni_hildesheim.sse.model.confModel.ConfigurationException;
//import de.uni_hildesheim.sse.model.cst.CSTSemanticException;
import de.uni_hildesheim.sse.model.management.VarModel;
//import de.uni_hildesheim.sse.model.varModel.Attribute;
//import de.uni_hildesheim.sse.model.varModel.ModelQuery;
//import de.uni_hildesheim.sse.model.varModel.ModelQueryException;
import de.uni_hildesheim.sse.model.varModel.Project;
import de.uni_hildesheim.sse.reasoning.core.frontend.ReasonerFrontend;
import de.uni_hildesheim.sse.reasoning.core.reasoner.ReasonerConfiguration;
import de.uni_hildesheim.sse.reasoning.core.reasoner.ReasonerConfiguration.IAdditionalInformationLogger;
//import de.uni_hildesheim.sse.utils.modelManagement.IModel;
//import de.uni_hildesheim.sse.utils.modelManagement.ModelInfo;
import de.uni_hildesheim.sse.utils.modelManagement.ModelInitializer;
//import de.uni_hildesheim.sse.utils.modelManagement.ModelManagement;
import de.uni_hildesheim.sse.utils.modelManagement.ModelManagementException;
//import de.uni_hildesheim.sse.utils.modelManagement.Version;
//import de.uni_hildesheim.sse.utils.modelManagement.VersionFormatException;
//import de.uni_hildesheim.sse.utils.modelManagement.VersionedModelInfos;
import de.uni_hildesheim.sse.utils.progress.ProgressObserver;
import eu.qualimaster.adaptation.events.AdaptationEvent;
import eu.qualimaster.coordination.RepositoryHelper;
import eu.qualimaster.monitoring.events.FrozenSystemState;

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
public class Debug {
    
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
     * Simulates Eclipse-DS initialization.
     * 
     * @param cls the class to be initialized
     */
    private static void initialize(Class<?> cls) {
        try {
            Method m = cls.getDeclaredMethod("activate", ComponentContext.class);
            m.setAccessible(true);
            Object o  = cls.newInstance();
            Object[] param = new Object[1];
            param[0] = null;
            m.invoke(o, param);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
            //File modelLocation = new File("W:\\runtime-EclipseApplication15\\QM2.devel\\EASy");
            File modelLocation = new File(args[0]);
            String prefix = null;
            if (args.length > 1) {
                if ("monitor".equals(args[1])) {
                    prefix = "monitoring_";     
                } else if ("adapt".equals(args[1])) {
                    prefix = "adaptation_";
                }
            }

            initialize(de.uni_hildesheim.sse.IvmlParser.class);
            initialize(de.uni_hildesheim.sse.VilExpressionParser.class);
            initialize(de.uni_hildesheim.sse.vil.templatelang.VtlExpressionParser.class);
            initialize(de.uni_hildesheim.sse.reasoning.reasoner.Reasoner.class);
            initialize(de.uni_hildesheim.sse.vil.rt.RtVilExpressionParser.class);
            initialize(de.uni_hildesheim.sse.easy_producer.instantiator.model.BuiltIn.class);
            initialize(de.uni_hildesheim.sse.easy_producer.instantiator.model.rtVil.BuiltIn.class);
            initialize(eu.qualimaster.easy.extension.internal.Registration.class);
            
            ModelInitializer.registerLoader(ProgressObserver.NO_OBSERVER);
            ModelInitializer.addLocation(modelLocation, ProgressObserver.NO_OBSERVER);
            Project project = RepositoryHelper.obtainModel(VarModel.INSTANCE, "QM", null);
            Script rtVilModel = RepositoryHelper.obtainModel(RtVilModel.INSTANCE, "QM", null);
            Configuration config = RepositoryHelper.createConfiguration(project, "TESTING");
            System.out.println("Model loaded...");
            
            if (null != prefix) {
                process(prefix, config, rtVilModel);
            }
        }
        
    }

}
