/*
 * Copyright 2016 University of Hildesheim, Software Systems Engineering
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
package eu.qualimaster.easy.extension.modelop;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import eu.qualimaster.easy.extension.ProjectFreezeModifier;
import eu.qualimaster.easy.extension.QmConstants;
import eu.qualimaster.easy.extension.internal.Bundle;
import net.ssehub.easy.basics.modelManagement.IModel;
import net.ssehub.easy.basics.modelManagement.ModelInfo;
import net.ssehub.easy.basics.modelManagement.ModelManagement;
import net.ssehub.easy.basics.modelManagement.ModelManagementException;
import net.ssehub.easy.basics.modelManagement.Version;
import net.ssehub.easy.basics.progress.ProgressObserver;
import net.ssehub.easy.instantiation.core.model.buildlangModel.BuildModel;
import net.ssehub.easy.instantiation.core.model.buildlangModel.Script;
import net.ssehub.easy.instantiation.core.model.execution.Executor;
import net.ssehub.easy.instantiation.core.model.templateModel.TemplateModel;
import net.ssehub.easy.instantiation.core.model.vilTypes.IProjectDescriptor;
import net.ssehub.easy.instantiation.rt.core.model.rtVil.RtVilModel;
import net.ssehub.easy.producer.core.persistence.IVMLFileWriter;
import net.ssehub.easy.producer.core.persistence.standard.StandaloneProjectDescriptor;
import net.ssehub.easy.varModel.confModel.Configuration;
import net.ssehub.easy.varModel.confModel.ConfigurationException;
import net.ssehub.easy.varModel.management.VarModel;
import net.ssehub.easy.varModel.model.AbstractVariable;
import net.ssehub.easy.varModel.model.Comment;
import net.ssehub.easy.varModel.model.DecisionVariableDeclaration;
import net.ssehub.easy.varModel.model.Project;
import net.ssehub.easy.varModel.model.filter.DeclarationFinder;
import net.ssehub.easy.varModel.model.filter.DeclarationFinder.VisibilityType;
import net.ssehub.easy.varModel.model.filter.FilterType;
import net.ssehub.easy.varModel.model.rewrite.ProjectCopyVisitor;
import net.ssehub.easy.varModel.model.rewrite.ProjectRewriteVisitor;
import net.ssehub.easy.varModel.model.rewrite.UncopiedElement;
import net.ssehub.easy.varModel.model.rewrite.modifier.FrozenCompoundConstraintsOmitter;
import net.ssehub.easy.varModel.model.rewrite.modifier.FrozenConstraintVarFilter;
import net.ssehub.easy.varModel.model.rewrite.modifier.FrozenConstraintsFilter;
import net.ssehub.easy.varModel.model.rewrite.modifier.FrozenTypeDefResolver;
import net.ssehub.easy.varModel.model.rewrite.modifier.ModelElementFilter;

/**
 * This class should modify and prune the model and it's configuration before instantiation.
 * Specifically, this modifier does the following:
 * <ul>
 *   <li>Dynamically freeze values ({@value #FREEZE})</li>
 *   <li>Stores propagated values inside the configuration ({@value #SAVE_VALUES})</li>
 *   <li>Optimizes the model for runtime (prune config) ({@value #PRUNE_CONFIG})</li>
 *   <li>Saves the modified configuration to {@value #COPIED_MODELS_LOCATION} ({@value #WRITE_MODIFIED_CONFIG})</li>
 *   <li>Saves the VIL model to {@value #COPIED_MODELS_LOCATION}</li>
 * </ul>
 * @author El-Sharkawy
 *
 */
public class ModelModifier {
    
    /*
     * Settings, which treatments shall be applied to the model during pruning.
     * These may be enabled/disabled for testing purposes.
     */
    
    /**
     * Adds freeze blocks to the configuration projects.
     */
    private static final boolean FREEZE = false;
    
    /**
     * Saves the configured values (stores the into the models), before pruning. <br/>
     * This is necessary as some values are set by constraints (which shall be removed).
     * 
     */
    private static final boolean SAVE_VALUES = true;
    
    /**
     * Saves the pruned configuration (writes it to disk).
     */
    private static final boolean WRITE_MODIFIED_CONFIG = true;
    
    /**
     * Specifies whether elements shall be deleted, which are not necessary for runtime:
     * <code>true</code> delete frozen and unused elements, <code>false</code> do not delete anything.
     */
    private static final boolean PRUNE_CONFIG = true;
    
    /**
     * Destination of the pruned configuration / projects.
     * @see #WRITE_MODIFIED_CONFIG
     */
    private static final String COPIED_MODELS_LOCATION = "QM-Model";
    
    /**
     * Connection to functionalities of the QM-IConf platform.
     * @author El-Sharkawy
     *
     */
    public static interface QMPlatformProvider {
        /**
         * Should start the reasoning to validate a newly generated configuration and to propagate values
         * before its usage.
         * Should call <code>Reasoning.reasonOn(false, config);</code>.
         * @param config The configuration to check (and propagate).
         */
        public void reason(Configuration config);
        
        /**
         * Should show exceptions in an error dialog.
         * @param title The title of the dialog.
         * @param exception The caught exception to show.
         */
        public void showExceptionDialog(final String title, final Exception exception);
    }
    
    private File targetFolder;
    private IProjectDescriptor source;
    
    // Variables for restoring the old state inside the clear method.
    private File orgModelsFolder;
    private File tempModelsFolder;
    
    private final Project toplevelProject;
    private final File baseLocation;
    private final QMPlatformProvider qmApp;
    
    /**
     * Single constructor for this class.
     * @param targetFolder The destination folder where to instantiate all artifacts
     * @param toplevelProject base project, which imports all other projects, e.g.,
     *     <code>VariabilityModel.Definition.TOP_LEVEL.getConfiguration().getProject()</code>
     * @param baseLocation The folder where all EASy files (VTL, VIL, IVML) are placed in, e.g.,
     *     <code>Location.getModelLocationFile()</code>
     * @param qmApp Optional instance of the application to show error dialogs and to allow reasoning (validation
     * of the generated configuration).
     */
    public ModelModifier(File targetFolder, Project toplevelProject, File baseLocation, QMPlatformProvider qmApp) {
        this.targetFolder = targetFolder;
        this.toplevelProject = toplevelProject;
        this.baseLocation = baseLocation;
        this.qmApp = qmApp;
        orgModelsFolder = null;
        tempModelsFolder = null;
        source = null;
    }
    
    /**
     * Prepares the underlying IVML {@link Project} and VIL, VTL {@link Script} models
     * for instantiation and generates a pruned and frozen {@link Configuration},
     * which should be used for the instantiation of the QM model.
     * @return {@link Configuration}, which should be used for the instantiation of the QM model
     */
    public Executor createExecutor() {
        // Create frozen and pruned config
        Executor executor = null;
        prepareConfig(targetFolder);
        
        // Register copied model
        tempModelsFolder = new File(targetFolder, COPIED_MODELS_LOCATION);
        ModelInfo<Project> oldProjectInfo = VarModel.INSTANCE.availableModels().getModelInfo(toplevelProject);
        orgModelsFolder = new File(oldProjectInfo.getLocation().getPath()).getParentFile();
        
        // Copy build model and load this temporarily
        copyBuildModel();
        
        BuildModel.INSTANCE.locations().getLocation(0);
        addOrRemoveLocation(orgModelsFolder, false);
      
        addOrRemoveLocation(tempModelsFolder, true);
        Project project = load(VarModel.INSTANCE, ".ivml");
        if (null != project) {
            Configuration config = new Configuration(project, true);
            Script startScript = load(BuildModel.INSTANCE, "_0.vil");
            if (null != config && null != startScript) {
                executor = new Executor(startScript);
                executor.addConfiguration(config);
                try {
                    source = new StandaloneProjectDescriptor(startScript, tempModelsFolder);
                    executor.addSource(source);
                } catch (ModelManagementException e) {
                    executor = null;
                    Bundle.getLogger(ModelModifier.class).exception(e);
                }
            }
        }
        
        if (null == executor) {
            // Allow fallback -> reset changes done to the models
            clear();
        }
        
        return executor;
    }
    
    /**
     * Returns the internally used {@link IProjectDescriptor} used by the executor returned by the
     * {@link #createExecutor()} method.
     * @return The internally used source descriptor or <code>null</code> if also no executor is returned.
     */
    public IProjectDescriptor getSourceDescriptor() {
        return source;
    }
    
    /**
     * Loads the main model.
     * @param management Either {@link VarModel#INSTANCE} or {@link BuildModel#INSTANCE}
     * @param fileEnding Either <code>.ivml</code> or <code>_0.vil</code>.
     * @param <M> Either {@link Project} or {@link Script}.
     * @return The loaded model or <code>null</code> if it was not found.
     */
    private <M extends IModel> M load(ModelManagement<M> management, String fileEnding) {
        M model = null;
        File file = new File(tempModelsFolder, QmConstants.PROJECT_TOP_LEVEL + fileEnding);
        URI vilURI = file.toURI();
        ModelInfo<M> info = management.availableModels().getModelInfo(QmConstants.PROJECT_TOP_LEVEL, new Version(0),
            vilURI);
            
        if (null != info) {
            try {
                model = management.load(info);
            } catch (ModelManagementException e) {
                Bundle.getLogger(ModelModifier.class).exception(e);
            }
        }
        
        return model;
    }
    
    /**
     * Restores the old state inside the tooling after instantiation (should be called after {@link #createExecutor()}
     * was used for instantiation). 
     */
    public void clear() {
        addOrRemoveLocation(tempModelsFolder, false);
        addOrRemoveLocation(orgModelsFolder, true);
        
        // Restore variability model
    }
    
    /**
     * Shortcut for {@link #addOrRemoveLocation(File, boolean)} to (un-)load all models an once.
     * @param folder The folder to (un-)register
     * @param add <code>true</code> the folder will be added as possible location for models, <code>false</code> the 
     *     folder will be removed.
     */
    private void addOrRemoveLocation(File folder, boolean add) {
        addOrRemoveLocation(VarModel.INSTANCE, folder, add);
        addOrRemoveLocation(BuildModel.INSTANCE, folder, add);
        addOrRemoveLocation(TemplateModel.INSTANCE, folder, add);
        addOrRemoveLocation(RtVilModel.INSTANCE, folder, add);
    }
    
    /**
     * Removed or adds a (temporary) folder for loading models from this locations.
     * @param modelManagement {@link VarModel#INSTANCE}, {@link BuildModel#INSTANCE}, or {@link TemplateModel#INSTANCE}
     * @param folder The folder to (un-)register
     * @param add <code>true</code> the folder will be added as possible location for models, <code>false</code> the 
     *     folder will be removed.
     */
    private void addOrRemoveLocation(ModelManagement<? extends IModel> modelManagement, File folder, boolean add) {
        try {
            if (add) {
                modelManagement.locations().addLocation(folder, ProgressObserver.NO_OBSERVER);
            } else {
                modelManagement.locations().removeLocation(folder, ProgressObserver.NO_OBSERVER);
            }
        } catch (ModelManagementException e) {
            Bundle.getLogger(ModelModifier.class).exception(e);
        }
    }

    /**
     * Creates a copy of the build model and place the files parallel to the copied variability model files.
     * @return The root folder of the copied model files.
     */
    private File copyBuildModel() {
        File srcFolder = new File(baseLocation, "EASy");
        File vilFolder = new File(targetFolder, COPIED_MODELS_LOCATION);
        vilFolder.mkdirs();
        try {
            FileUtils.copyDirectory(srcFolder, vilFolder, new FileFilter() {
                
                @Override
                public boolean accept(File pathname) {
                    String fileName = pathname.getName();
                    return pathname.isDirectory() || fileName.endsWith("vil") || fileName.endsWith("vtl")
                        || fileName.endsWith("rtvtl");
                }
            });
        } catch (IOException e) {
            Bundle.getLogger(ModelModifier.class).exception(e);
        }
        return vilFolder;
    }
    
    /**
     * Prepares the underlying IVML {@link Project} for instantiation and generates a pruned 
     * {@link Configuration}, which should be used for the instantiation of the QM model.
     * @param targetLocation The destination folder where to instantiate all artifacts
     * @return {@link Configuration}, which should be used for the instantiation of the QM model
     */
    private Configuration prepareConfig(File targetLocation) {        
        // Copy base project
        Project baseProject = toplevelProject;
        ProjectCopyVisitor copier = new ProjectCopyVisitor(baseProject, FilterType.ALL);
        baseProject.accept(copier);
        baseProject = copier.getCopiedProject();
        List<UncopiedElement> uncopiedElements = copier.getUncopiedElements();
        if (!uncopiedElements.isEmpty()) {
            StringBuffer errMsg = new StringBuffer(ProjectCopyVisitor.class.getSimpleName());
            errMsg.append(" could not copy ");
            errMsg.append(uncopiedElements.size());
            errMsg.append("elements of project \"");
            errMsg.append(toplevelProject.getName());
            errMsg.append("\". These are:");
            for (int i = 0, end = uncopiedElements.size(); i < end; i++) {
                errMsg.append("\n * ");
                errMsg.append(uncopiedElements.get(i).getDescription());
            }
            Bundle.getLogger(ModelModifier.class).warn(errMsg.toString());
        }

        if (SAVE_VALUES) {
            saveValues(baseProject, new HashSet<Project>());
        }
        
        // Freeze the copy, except for the runtime elements.
        if (FREEZE) {
            freezeProject(baseProject);
        }
        
        // Prune Config to optimize runtime behaviour
        if (PRUNE_CONFIG) {
            ProjectRewriteVisitor rewriter = new ProjectRewriteVisitor(baseProject, FilterType.ALL);
            Configuration config = new Configuration(baseProject, true);
            rewriter.addModelCopyModifier(new ModelElementFilter(Comment.class));
            rewriter.addModelCopyModifier(new FrozenConstraintsFilter(config));
            rewriter.addModelCopyModifier(new FrozenTypeDefResolver(config));
            rewriter.addModelCopyModifier(new FrozenConstraintVarFilter(config));
            rewriter.addModelCopyModifier(new FrozenCompoundConstraintsOmitter(config));
            baseProject.accept(rewriter);
        }
        
        // Saved copied projects
        if (WRITE_MODIFIED_CONFIG) {
            try {
                File modelFolder = new File(targetLocation, COPIED_MODELS_LOCATION);
                if (!modelFolder.exists()) {
                    modelFolder.mkdirs();
                }
                IVMLFileWriter writer = new IVMLFileWriter(modelFolder);
                writer.forceComponundTypes(true);
                writer.setFormatInitializer(true);
                writer.save(baseProject);
            } catch (IOException e) {
                if (null != qmApp) {
                    qmApp.showExceptionDialog("Model could not be saved", e);
                }
            }
        }
        
        // Return a configuration based on the copied, frozen and pruned project
        Configuration config = new Configuration(baseProject, true);
        if (null != qmApp) {
            qmApp.reason(config);
        }
        return config;
    }
    
    /**
     * Saves the values of configuration projects(recursive function).
     * @param project The copied QM model (the starting point, which imports all the other models).
     * @param done The list of already saved projects, should be empty at the beginning.
     */
    private void saveValues(Project project, Set<Project> done) {
        if (!done.contains(project)) {
            done.add(project);
            
            if (project.getName().endsWith(QmConstants.CFG_POSTFIX)) {
                Bundle.getLogger(ModelModifier.class).debug("Saving ", project.getName());
                try {
                    Configuration tmpConfig = new Configuration(project, true);
                    // Will change the underlying Project as a side effect
                    new QmPrunedConfigSaver(tmpConfig);
                } catch (ConfigurationException e1) {
                    Bundle.getLogger(ModelModifier.class).exception(e1);
                }
            }
            
            for (int i = 0, end = project.getImportsCount(); i < end; i++) {
                saveValues(project.getImport(i).getResolved(), done);
            }
        }
    }

    /**
     * Adds freezes blocks to the configuration projects.
     * @param baseProject The copied QM model (the starting point, which imports all the other models).
     */
    private void freezeProject(Project baseProject) {
        DeclarationFinder finder = new DeclarationFinder(baseProject, FilterType.ALL, null);
        List<DecisionVariableDeclaration> allDeclarations = new ArrayList<DecisionVariableDeclaration>();
        List<AbstractVariable> tmpList = finder.getVariableDeclarations(VisibilityType.ALL);
        for (int i = 0, end = tmpList.size(); i < end; i++) {
            AbstractVariable declaration = tmpList.get(i);
            if (declaration instanceof DecisionVariableDeclaration
                && !(declaration.getNameSpace().equals(QmConstants.PROJECT_OBSERVABLESCFG)
                && declaration.getName().equals("qualityParameters"))) {
                
                allDeclarations.add((DecisionVariableDeclaration) declaration);
            }
        }
        ProjectRewriteVisitor rewriter = new ProjectRewriteVisitor(baseProject, FilterType.ALL);
        ProjectFreezeModifier freezer = new ProjectFreezeModifier(baseProject, allDeclarations);
        rewriter.addProjectModifier(freezer);
        baseProject.accept(rewriter);
    }
}
