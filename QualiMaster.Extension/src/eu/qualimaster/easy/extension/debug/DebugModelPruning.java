package eu.qualimaster.easy.extension.debug;

import java.io.File;

import org.apache.commons.io.FileUtils;

import eu.qualimaster.coordination.RepositoryHelper;
import eu.qualimaster.easy.extension.internal.Bundle;
import eu.qualimaster.easy.extension.modelop.ModelModifier;
import eu.qualimaster.easy.extension.modelop.ModelModifier.QMPlatformProvider;
import net.ssehub.easy.basics.modelManagement.ModelInitializer;
import net.ssehub.easy.basics.progress.ProgressObserver;
import net.ssehub.easy.instantiation.core.model.buildlangModel.BuildModel;
import net.ssehub.easy.instantiation.core.model.buildlangModel.Script;
import net.ssehub.easy.instantiation.core.model.execution.Executor;
import net.ssehub.easy.instantiation.core.model.execution.TracerFactory;
import net.ssehub.easy.instantiation.core.model.tracing.ConsoleTracerFactory;
import net.ssehub.easy.instantiation.core.model.vilTypes.IProjectDescriptor;
import net.ssehub.easy.producer.core.persistence.standard.StandaloneProjectDescriptor;
import net.ssehub.easy.reasoning.core.frontend.ReasonerFrontend;
import net.ssehub.easy.reasoning.core.reasoner.ReasonerConfiguration;
import net.ssehub.easy.reasoning.core.reasoner.ReasoningResult;
import net.ssehub.easy.varModel.confModel.Configuration;
import net.ssehub.easy.varModel.management.VarModel;
import net.ssehub.easy.varModel.model.Project;

/**
 * Tests that the {@link ModelModifier} can be executed before instantiating the platform.
 * @author El-Sharkawy
 *
 */
public class DebugModelPruning extends AbstractDebug {
    
    public static final ReasonerConfiguration RCONFIG;
    static {
        RCONFIG = new ReasonerConfiguration();
        RCONFIG.enableCustomMessages();
    }

    // checkstyle: stop exception type check
    /**
     * Executes the test.
     * 
     * @param args the first argument shall be the model location
     * @throws Exception in case of any failures
     */
    public static void main(String[] args) throws Exception {
        // Create and load folders
        File modelLocation = loadModelLocation(args);
        File tmpFolder = new File(System.getProperty("java.io.tmpdir"), "DebugModelPruningTest");
        FileUtils.deleteQuietly(tmpFolder);
        tmpFolder.mkdirs();
        tmpFolder.deleteOnExit();
        File pruneFolder = new File(tmpFolder, "prune");
        File trgFolder = new File(tmpFolder, "trg");

        TracerFactory.setInstance(ConsoleTracerFactory.INSTANCE);
        // Init EASy (and models)
        initialize();     
        ModelInitializer.registerLoader(ProgressObserver.NO_OBSERVER);
        ModelInitializer.addLocation(modelLocation, ProgressObserver.NO_OBSERVER);
        
        // Initialize models as it is done in QM-IConf
        Project project = RepositoryHelper.obtainModel(VarModel.INSTANCE, "QM", null);
        Configuration config = new Configuration(project, true);
        Script script = RepositoryHelper.obtainModel(BuildModel.INSTANCE, "QM", null);
        
        // Validate model before instantiation as it is done in QM-IConf
        ReasoningResult rr = ReasonerFrontend.getInstance().propagate(config, RCONFIG, 
            ProgressObserver.NO_OBSERVER);
        rr.logInformation(config.getProject(), RCONFIG);
        
        // Model Modifier
        ModelModifier modifier = new ModelModifier(pruneFolder, project, modelLocation, new QMPlatformProvider() {
            
            @Override
            public void showExceptionDialog(String title, Exception exception) {
                Bundle.getLogger(this.getClass()).exception(exception);
            }
            
            @Override
            public void reason(Configuration config) {
                ReasoningResult rr = ReasonerFrontend.getInstance().propagate(config, RCONFIG,
                    ProgressObserver.NO_OBSERVER);
                rr.logInformation(config.getProject(), RCONFIG);
            }
        });
        Executor executor = modifier.createExecutor();
        
        if (null == executor) {
            System.err.println("ModelModifier did not created an executor, an unknown exception must be occured.");
            System.exit(0);
        }
        
        // Run instantiation
        IProjectDescriptor source = new StandaloneProjectDescriptor(script, modelLocation);
        IProjectDescriptor target = new StandaloneProjectDescriptor(script, trgFolder);
        executor.addSource(source).addTarget(target);
        executor.execute();
    }
    
    // checkstyle: resume exception type check
}
