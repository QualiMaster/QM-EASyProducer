package eu.qualimaster.easy.extension.internal;

import java.io.File;
import java.util.List;

import de.uni_hildesheim.sse.easy_producer.core.persistence.standard.StandaloneProjectDescriptor;
import de.uni_hildesheim.sse.easy_producer.instantiator.model.buildlangModel.BuildModel;
import de.uni_hildesheim.sse.easy_producer.instantiator.model.buildlangModel.Script;
import eu.qualimaster.easy.extension.QmConstants;
import net.ssehub.easy.basics.modelManagement.ModelInfo;
import net.ssehub.easy.basics.modelManagement.ModelManagementException;

/**
 * Implements a descriptor for the source and target VIL project locations.
 * The {@link #QMProjectDescriptor(QmProjectDescriptor, File) 
 * second constructor} allows to specify a different target location.
 * 
 * @author Holger Eichelberger
 */
public class QmProjectDescriptor extends StandaloneProjectDescriptor {

    /**
     * Creates the default project descriptor for the QM model to be instantiated.
     * 
     * @param base the folder to instantiate into
     * @throws ModelManagementException in case that resolving the model, obtaining 
     *   information etc failed.
     */
    public QmProjectDescriptor(File base) throws ModelManagementException {
        super(selectMainScript(), base);
    }

    /**
     * Allows to instantiate the QM model into a given location.
     * 
     * @param parent the parent descriptor (also representing the source, to
     *   be obtained via {@link #ProjectDescriptor()})
     * @param base the folder to instantiate into
     */
    public QmProjectDescriptor(QmProjectDescriptor parent, File base) {
        super(parent, base);
    }


    /**
     * Selects the main VIL script.
     * 
     * @return the main VIL script
     * @throws ModelManagementException sin case that resolving the model, obtaining 
     *   information etc failed.
     */
    private static Script selectMainScript() throws ModelManagementException {
        Script result;
        BuildModel repository = BuildModel.INSTANCE;
        // by convention the same name, ignore versions for now
        List<ModelInfo<Script>> vilScripts = repository.availableModels().getModelInfo(QmConstants.PROJECT_TOP_LEVEL);
        if (null == vilScripts || vilScripts.isEmpty()) {
            throw new ModelManagementException("Cannot resolve main instantiation script", 
                ModelManagementException.ID_CANNOT_RESOLVE);
        } else {
            ModelInfo<Script> info = vilScripts.get(0); // primitive, ok for now
            result = repository.load(info);
        }
        return result;
    }

}
