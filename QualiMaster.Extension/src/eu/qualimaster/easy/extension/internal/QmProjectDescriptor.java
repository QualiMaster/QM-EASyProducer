package eu.qualimaster.easy.extension.internal;

import java.io.File;
import java.util.List;

import eu.qualimaster.easy.extension.QmConstants;
import net.ssehub.easy.basics.modelManagement.ModelInfo;
import net.ssehub.easy.basics.modelManagement.ModelManagementException;
import net.ssehub.easy.instantiation.core.model.buildlangModel.BuildModel;
import net.ssehub.easy.instantiation.core.model.buildlangModel.Script;
import net.ssehub.easy.producer.core.persistence.standard.StandaloneProjectDescriptor;

/**
 * Implements a descriptor for the source and target VIL project locations.
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
     * @param parent the parent descriptor (also representing the source)
     * @param base the folder to instantiate into
     */
    public QmProjectDescriptor(QmProjectDescriptor parent, File base) {
        super(parent, base);
    }

    /**
     * Selects the main VIL script.
     * 
     * @return the main VIL script
     * @throws ModelManagementException in case that resolving the model, obtaining 
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
