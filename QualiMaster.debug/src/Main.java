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
public class Main {
    
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

    /**
     * Obtains a model considering given name and version.
     * 
     * @param <M> the actual model type
     * @param mgt the model management instance to obtain the model from
     * @param name the name of the model
     * @param version the version (<b>null</b> for obtaining the maximum version)
     * @return the obtained model, <b>null</b> if none was found
     */
    /*public static <M extends IModel> M obtainModel(ModelManagement<M> mgt, String name, String version) {
        ModelInfo<M> info = null;
        if (null != version) {
            Version ver = null;
            if (version.length() > 0) {
                try {
                    ver = new Version(version);
                } catch (VersionFormatException e) {
                    System.err.println("Obtaining model (fallback to no version given):" + e.getMessage());
                }
            }
            List<ModelInfo<M>> infos = mgt.availableModels().getModelInfo(name, ver);
            if (infos.size() > 0) { // more than one shall not happen
                info = infos.get(0);
            }
        }
        if (null == info) {
            List<ModelInfo<M>> infos = mgt.availableModels().getModelInfo(name);
            info = VersionedModelInfos.maxVersion(infos);
        }
        if (null != info && !info.isResolved()) {
            try {
                System.out.println("Loading model " + info.getName() + " @ " + info.getLocation() + "...");
                mgt.load(info);
                System.out.println("Loading model " + info.getName() + " @ " + info.getLocation() + " done");
            } catch (ModelManagementException e) {
                System.err.println("Obtaining model: " + e.getMessage());
            }
        }
        M result = null;
        if (null != info) {
            result = info.getResolved();
        }
        return result;
    }*/
    
    /**
     * Creates a temporary folder for executing the adaptation specification within.
     * 
     * @return the temporary folder
     */
    /*public static File createTmpFolder() {
        File tmp = null;
        try {
            tmp = File.createTempFile("qmAdapt", "tmp");
            tmp.delete();
            tmp.mkdirs();
            tmp.deleteOnExit();
        } catch (IOException e) {
            System.err.println("While creating the temp instantiation folder: " + e.getClass().getName()
                + " " + e.getMessage());
        }
        return tmp;
    }*/

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
     * Creates an rt-VIL executor instance.
     * 
     * @param rtVilModel the rt-VIL model
     * @param folder the folder to execute the model within
     * @param config the IVML configuration
     * @param event the adaptation event
     * @param state the system state
     * @return the executor
     */
    /*public static Executor createExecutor(Script rtVilModel, File folder, Configuration config, AdaptationEvent event, 
        FrozenSystemState state) {
        Executor exec = new Executor(rtVilModel);
        exec.addBase(folder);
        exec.addConfiguration(config);
        exec.addSource(folder);
        exec.addTarget(folder);
        if (rtVilModel.getParameterCount() > 3) {
            exec.addCustomArgument(rtVilModel.getParameter(3).getName(), event);
        }
        if (rtVilModel.getParameterCount() > 4) {
            exec.addCustomArgument(rtVilModel.getParameter(4).getName(), state.getMapping());
        }
        return exec;
    }*/
  
/*    public static Configuration createConfiguration(Project project, String newVariablePrefix) {
        Configuration configuration = new Configuration(project);
        try {
            // did not want to introduce an IVML copy operation by now
            Enum bindingTime = (Enum) ModelQuery.findType(project, "BindingTime", Enum.class);
            // take any one - just used for type and name
            Attribute annotation = (Attribute) ModelQuery.findElementByName(project, "bindingTime", Attribute.class);
            IFreezeProvider freezeProvider = new EnumAttributeFreezeProvider("b", annotation, 
                OclKeyWords.GREATER_EQUALS, bindingTime.getLiteral(1));

            Compound sourceType = RepositoryHelper.findCompound(project, "Source");
            CopySpec specSource = new CopySpec(sourceType, "source", freezeProvider, "available", "actual");
            Compound familyElementType = RepositoryHelper.findCompound(project, "FamilyElement");
            CopySpec specFamily = new CopySpec(familyElementType, "family.members", freezeProvider, "available");
            Compound sinkType = RepositoryHelper.findCompound(project, "Sink");
            CopySpec specSink = new CopySpec(sinkType, "sink", freezeProvider, "available", "actual");
            VariableValueCopier copier 
                = new VariableValueCopier(newVariablePrefix, specSource, specFamily, specSink);
            copier.process(configuration);
        } catch (ConfigurationException | ValueDoesNotMatchTypeException 
            | ModelQueryException | CSTSemanticException e) {
            System.out.println("Cannot initialize runtime model: " + e.getMessage());
        }
        return configuration;
    }*/

    /**
     * Executes the test in sequence. Please adjust your model location and the files to be analyzed.
     * 
     * @param args location of the model, requested functionality (none: just load the model, monitor: process 
     *   file/monitoring_ in sequence, adapt: process file/adaptation_ in sequence)
     * @throws ModelManagementException shall not occur
     */
    public static void main(String[] args) throws ModelManagementException {
        if (0 == args.length) {
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

}
