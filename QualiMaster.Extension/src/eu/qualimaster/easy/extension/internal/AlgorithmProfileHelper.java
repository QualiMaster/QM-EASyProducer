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
package eu.qualimaster.easy.extension.internal;

import eu.qualimaster.common.QMInternal;
import eu.qualimaster.coordination.RepositoryHelper;
import net.ssehub.easy.basics.modelManagement.ModelManagementException;
import net.ssehub.easy.instantiation.core.model.common.VilException;
import net.ssehub.easy.instantiation.core.model.execution.Executor;
import net.ssehub.easy.instantiation.core.model.execution.TracerFactory;
import net.ssehub.easy.instantiation.core.model.tracing.ConsoleTracerFactory;
import net.ssehub.easy.instantiation.core.model.vilTypes.IProjectDescriptor;
import net.ssehub.easy.producer.core.persistence.standard.StandaloneProjectDescriptor;
import net.ssehub.easy.varModel.confModel.Configuration;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.cst.CSTSemanticException;
import net.ssehub.easy.varModel.cst.CompoundAccess;
import net.ssehub.easy.varModel.cst.ConstantValue;
import net.ssehub.easy.varModel.cst.ConstraintSyntaxTree;
import net.ssehub.easy.varModel.cst.OCLFeatureCall;
import net.ssehub.easy.varModel.cst.Variable;
import net.ssehub.easy.varModel.model.Constraint;
import net.ssehub.easy.varModel.model.ContainableModelElement;
import net.ssehub.easy.varModel.model.DecisionVariableDeclaration;
import net.ssehub.easy.varModel.model.FreezeBlock;
import net.ssehub.easy.varModel.model.IFreezable;
import net.ssehub.easy.varModel.model.IvmlKeyWords;
import net.ssehub.easy.varModel.model.ModelQuery;
import net.ssehub.easy.varModel.model.ModelQueryException;
import net.ssehub.easy.varModel.model.Project;
import net.ssehub.easy.varModel.model.ProjectImport;
import net.ssehub.easy.varModel.model.datatypes.Compound;
import net.ssehub.easy.varModel.model.datatypes.ConstraintType;
import net.ssehub.easy.varModel.model.datatypes.Container;
import net.ssehub.easy.varModel.model.datatypes.IDatatype;
import net.ssehub.easy.varModel.model.datatypes.IntegerType;
import net.ssehub.easy.varModel.model.values.Value;
import net.ssehub.easy.varModel.model.values.ValueDoesNotMatchTypeException;
import net.ssehub.easy.varModel.model.values.ValueFactory;

import static eu.qualimaster.easy.extension.QmConstants.*;
import static eu.qualimaster.easy.extension.internal.Utils.*;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.log4j.LogManager;
import org.eclipse.xtext.util.Arrays;

/**
 * Support for creating on-demand pipelines for algorithm profiling.
 * 
 * @author Holger Eichelberger
 */
public class AlgorithmProfileHelper {

    public static final String PARAM_HDFS_DATAFILE = "hdfsDataFile";
    public static final String PARAM_DATAFILE = "dataFile";
    public static final String PARAM_REPLAYSPEED = "replaySpeed";
    public static final String SRC_NAME = "TestSource"; // must be valid IVML identifier
    public static final String FAM_NAME = "TestFamily"; // must be valid IVML identifier
    public static final String DATASRC_NAME = SRC_NAME + "Profiling";
    
    private static final String[] PIPELINE_IMPORTS = {PROJECT_BASICS, PROJECT_PIPELINES, PROJECT_FAMILIESCFG, 
        PROJECT_DATAMGTCFG};
    private static final String[] PIPELINES_IMPORTS = {PROJECT_BASICS, PROJECT_PIPELINES};
    private static final String[] INFRASTRUCTURE_IMPORTS = {PROJECT_INFRASTRUCTURE};
    private static final String[] TOP_IMPORTS = {PROJECT_HARDWARECFG, PROJECT_RECONFHWCFG, PROJECT_DATAMGTCFG, 
        PROJECT_OBSERVABLESCFG, PROJECT_ADAPTIVITYCFG, PROJECT_ALGORITHMSCFG, PROJECT_FAMILIESCFG};
    private static final String PIP_VERSION = "0.0.1-SNAPSHOT";
    private static final String DATA_FILE = "profile.data";
    private static final String CTL_FILE = "profile.ctl";

    /**
     * Information describing a new profile pipeline.
     * 
     * @author Holger Eichelberger
     */
    public static class ProfileData {
        private String pipelineName;
        private File pipeline;
        private File dataFile;
        private File controlFile;

        /**
         * Creates a profile data instance (public for testing).
         * 
         * @param pipelineName the name of the pipeline
         * @param pipeline the pipeline file
         * @param dataFile the data file
         * @param controlFile the control file
         */
        public ProfileData(String pipelineName, File pipeline, File dataFile, File controlFile) {
            this.pipelineName = pipelineName;
            this.pipeline = pipeline;
            this.dataFile = dataFile;
            this.controlFile = controlFile;
        }

        /**
         * Returns the pipeline name.
         * 
         * @return the pipeline name
         */
        public String getPipelineName() {
            return pipelineName;
        }
        
        /**
         * Returns the pipeline Jar.
         * 
         * @return the pipeline Jar
         */
        public File getPipeline() {
            return pipeline;
        }

        /**
         * Returns the pipeline Jar.
         * 
         * @return the pipeline Jar
         */
        public File getDataFile() {
            return dataFile;
        }

        /**
         * Returns the control file.
         * 
         * @return the control file
         */
        public File getControlFile() {
            return controlFile;
        }
        
    }
    
    /**
     * Profiles the given algorithm. Create a specific pipeline with data source, specific family holding
     * only the test algorithm. Stores data and control file into <code>source</code>.
     * 
     * @param config the configuration to be used as basis for creating a profiling pipeline
     * @param pipelineName the name of the pipeline to be created (must be a valid Java identifier)
     * @param familyName the name of the family to test
     * @param algorithmName the name of the algorithm within <code>family</code> to test
     * @param source the source project descriptor, also to be used as target folder for instantiation (the folder may 
     *     be empty)
     * @return pipeline information
     * @throws VilException in case of model query problems, model management problems, CST errors, unmatching IVML 
     *     values or VIL execution errors
     */
    @QMInternal
    public static ProfileData createProfilePipeline(net.ssehub.easy.varModel.confModel.Configuration config, 
        String pipelineName, String familyName, String algorithmName, IProjectDescriptor source) throws VilException {
        ProfileData result = null;
        if (null == config.getProject()) {
            throw new VilException("no project available - syntax/parsing error?", VilException.ID_INVALID);
        }
        try {
            Project qm = createNewRoot(config, pipelineName, familyName, algorithmName);
            Configuration cfg = new Configuration(qm);
            
            TracerFactory.setInstance(ConsoleTracerFactory.INSTANCE); // clear thread specific instance
            StandaloneProjectDescriptor target = new StandaloneProjectDescriptor(source, source.getBase());
            Executor executor = new Executor(source.getMainVilScript())
                .addSource(source).addTarget(target)
                .addConfiguration(cfg)
                .addCustomArgument("pipelineName", pipelineName)
                .addStartRuleName("pipeline");
            executor.execute();
            TracerFactory.setInstance(null); // clear thread specific instance

            File base = source.getBase();
            Compound familyType = findCompound(qm, TYPE_FAMILY);
            IDecisionVariable testFamily = findNamedVariable(config, familyType, familyName);
            IDecisionVariable testAlgorithm = Configuration.dereference(findAlgorithm(testFamily, algorithmName, true));
            String algArtifact = VariableHelper.getString(testAlgorithm, SLOT_ALGORITHM_ARTIFACT);
            extractProfilingArtifact(algArtifact, algorithmName, base);
            File pipFile = new File(base, "pipelines/eu/qualimaster/" + pipelineName + "/target/" + pipelineName 
                + "-" + PIP_VERSION + "-jar-with-dependencies.jar");
            File dataFile = getDataFile(base);
            File controlFile = getControlFile(base);
            result = new ProfileData(pipelineName, pipFile, dataFile, controlFile);
        } catch (ModelQueryException | ModelManagementException | ValueDoesNotMatchTypeException 
            | CSTSemanticException e) {
            throw new VilException(e.getMessage(), VilException.ID_RUNTIME);
        }
        return result;
    }
    
    /**
     * Just returns a file instance pointing to the data file.
     * 
     * @param base the base folder
     * @return the file instance (regardless whether it exists)
     */
    public static File getDataFile(File base) {
        return new File(base, DATA_FILE);
    }

    /**
     * Just returns a file instance pointing to the control file.
     * 
     * @param base the base folder
     * @return the file instance (regardless whether it exists)
     */
    public static File getControlFile(File base) {
        return new File(base, CTL_FILE);
    }

    /**
     * Extracts the profiling artifact.
     * 
     * @param artifactSpec the artifact specification
     * @param name the logical name of the artifact
     * @param base the base folder where to extract to
     * @throws VilException in case that obtaining the artifact fails
     */
    public static void extractProfilingArtifact(String artifactSpec, String name, File base) throws VilException {
        if (null != artifactSpec) {
            File dataArtifact = RepositoryHelper.obtainArtifact(artifactSpec, name, "profiling", ".zip", base);
            if (null != dataArtifact) {
                extractDataArtifact(dataArtifact, base);
            } else {
                throw new VilException("artifact for spec " + artifactSpec + " not found", VilException.ID_RUNTIME);
            }
        } else {
            throw new VilException("no artifact spec given ", VilException.ID_RUNTIME);
        }
    }
    
    /**
     * Extracts the data artifact to <code>base</code>.
     * 
     * @param file the file to extract
     * @param base the base folder to extract to
     */
    private static void extractDataArtifact(File file, File base) {
        ZipInputStream zis = null;
        byte[] buf = new byte[2048];
        try {
            zis = new ZipInputStream(new FileInputStream(file));
            ZipEntry entry;
            do {
                entry = zis.getNextEntry();
                if (null != entry) {
                    String name = entry.getName();
                    if (name.equals(DATA_FILE) || name.equals(CTL_FILE)) {
                        // write the file to the disk
                        int count;
                        File outFile = new File(base, name);
                        FileOutputStream fos = new FileOutputStream(outFile);
                        BufferedOutputStream dest = new BufferedOutputStream(fos, buf.length);
                        while ((count = zis.read(buf, 0, buf.length)) != -1) {
                            dest.write(buf, 0, count);
                        }
                        dest.flush();
                        dest.close();                        
                    }
                }
            } while (null != entry);
            zis.close();
        } catch (IOException e) {
            LogManager.getLogger(AlgorithmProfileHelper.class).error(
                "Extracting algorithm data artifact: " + e.getMessage());
            if (null != zis) {
                try {
                    zis.close();
                } catch (IOException e1) {
                }
            }
        }
    }
    
    /**
     * Creates a new QM model root leaving the real one as it is.
     * 
     * @param config the configuration to be used as basis for creation
     * @param pipelineName the pipeline name (here a valid Java identifier)
     * @param familyName the name of the family to test
     * @param algorithmName the name of the algorithm within <code>family</code> to test
     * @return the new model root project
     * @throws ModelQueryException in case of model query problems
     * @throws ModelManagementException in case of model management problems
     * @throws CSTSemanticException in case of CST errors
     * @throws ValueDoesNotMatchTypeException in case of unmatching values
     */
    private static Project createNewRoot(net.ssehub.easy.varModel.confModel.Configuration config, String pipelineName, 
        String familyName, String algorithmName) throws ModelQueryException, ModelManagementException, 
        ValueDoesNotMatchTypeException, CSTSemanticException {
        Project cfgProject = config.getProject();
        Project cfgInfra = ModelQuery.findProject(cfgProject, PROJECT_INFRASTRUCTURE);
        Compound familyType = findCompound(cfgProject, TYPE_FAMILY);
        IDecisionVariable testFamily = findNamedVariable(config, familyType, familyName);
        IDecisionVariable testAlgorithm = findAlgorithm(testFamily, algorithmName, true);
        Project qm;
        Project pip = createQmProject("ProfilingTestPipeline" + CFG_POSTFIX, cfgProject);
        addImports(cfgProject, PIPELINE_IMPORTS, pip);
        Compound dataSourceType = findCompound(pip, TYPE_DATASOURCE);
        Compound flowType = findCompound(pip, TYPE_FLOW);
        Compound pipelineType = findCompound(pip, TYPE_PIPELINE);
        Compound sourceType = findCompound(pip, TYPE_SOURCE);
        Compound familyElementType = findCompound(pip, TYPE_FAMILYELEMENT);

        if (null != testFamily && null != testAlgorithm && null != dataSourceType && null != flowType) {
            IDecisionVariable famTuples = testFamily.getNestedElement(SLOT_FAMILY_INPUT);
            DecisionVariableDeclaration dataSourceVar = createDecisionVariable(DATASRC_NAME, dataSourceType, pip, 
                SLOT_DATASOURCE_NAME, DATASRC_NAME,
                SLOT_DATASOURCE_TUPLES, famTuples.getValue().clone(),
                SLOT_DATASOURCE_ARTIFACT, "eu.qualimaster:genericSource:0.5.0-SNAPSHOT",
                SLOT_DATASOURCE_STORAGELOCATION, "null",
                SLOT_DATASOURCE_PROFILINGSOURCE, true,
                SLOT_DATASOURCE_DATAMANAGEMENTSTRATEGY, CONST_DATAMANAGEMENTSTRATEGY_NONE,
                SLOT_DATASOURCE_PARAMETERS, createDataSourceParameters(cfgProject),
                SLOT_DATASOURCE_SOURCECLS, "eu.qualimaster." + pipelineName + ".topology.imp." + DATASRC_NAME 
                    + "Profiling");
            DecisionVariableDeclaration familyVar = createDecisionVariable("prFamily0", familyType, pip, 
                SLOT_FAMILY_NAME, getValue(testFamily, SLOT_FAMILY_NAME),
                SLOT_FAMILY_INPUT, getValue(testFamily, SLOT_FAMILY_INPUT),
                SLOT_FAMILY_OUTPUT, getValue(testFamily, SLOT_FAMILY_OUTPUT),
                SLOT_FAMILY_PARAMETERS, getValue(testFamily, SLOT_FAMILY_PARAMETERS),
                SLOT_FAMILY_MEMBERS, new Object[] {testAlgorithm.getValue()});
            DecisionVariableDeclaration familyEltVar = createDecisionVariable(FAM_NAME, familyElementType, pip, 
                SLOT_FAMILYELEMENT_NAME, FAM_NAME,
                SLOT_PIPELINE_NODE_PARALLELISM, 1, 
                SLOT_FAMILYELEMENT_FAMILY, familyVar);
            Object[] flowVars = new Object[famTuples.getNestedElementsCount()];
            for (int n = 0; n < flowVars.length; n++) {
                flowVars[n] = createDecisionVariable("prFlow" + n, flowType, pip,
                        SLOT_FLOW_NAME, "f" + n,
                        SLOT_FLOW_DESTINATION, familyEltVar,
                        SLOT_FLOW_TUPLE_TYPE, createRefToTuple(dataSourceVar, SLOT_DATASOURCE_TUPLES, n),
                        SLOT_FLOW_GROUPING, CONST_GROUPING_SHUFFLEGROUPING);
            }
            DecisionVariableDeclaration sourceVar = createDecisionVariable(SRC_NAME, sourceType, pip,
                SLOT_SOURCE_NAME, SRC_NAME,
                SLOT_PIPELINE_NODE_PARALLELISM, 1, 
                SLOT_SOURCE_OUTPUT, flowVars,
                SLOT_SOURCE_SOURCE, dataSourceVar);
            DecisionVariableDeclaration pipVar = createDecisionVariable("prPipeline0", pipelineType, pip, 
                SLOT_PIPELINE_NAME, pipelineName, 
                SLOT_PIPELINE_ARTIFACT, "eu.qualimaster:" + pipelineName + ":" + PIP_VERSION,
                SLOT_PIPELINE_SOURCES, new Object[]{sourceVar},
                SLOT_PIPELINE_NUMWORKERS, 1);
            Utils.createFreezeBlock(pip);
    
            Project pipelines = createQmProject(PROJECT_PIPELINESCFG, cfgProject);
            addImports(cfgProject, PIPELINES_IMPORTS, pipelines, pip);
            DecisionVariableDeclaration pipelinesVar = setPipelines(pipelines, VAR_PIPELINES_PIPELINES, pipVar);
            createFreezeBlock(new IFreezable[]{pipelinesVar}, pipelines, pipelines);
            
            Project infra = createQmProject(PROJECT_INFRASTRUCTURECFG, cfgProject);
            addImports(cfgProject, INFRASTRUCTURE_IMPORTS, infra, pipelines);
            List<IFreezable> freezes = addTopLevelValues(config, cfgInfra, infra, VAR_INFRASTRUCTURE_ACTIVEPIPELINES);
            freezes.add(setPipelines(infra, VAR_INFRASTRUCTURE_ACTIVEPIPELINES, pipVar));
            createFreezeBlock(freezes, infra, infra);
            qm = createQmProject(PROJECT_TOP_LEVEL, cfgProject);
            addImports(cfgProject, TOP_IMPORTS, qm, infra);
        } else {
            qm = cfgProject;
        }
        return qm;
    }
    
    /**
     * Creates an expression accessing the <code>index</code> element of <code>slotName</code> in <code>var</code>.
     * 
     * @param var the variable to access
     * @param slotName the slot to access
     * @param index the index within the slot to access
     * @return the accessing expression
     * @throws CSTSemanticException in case that the constraint expression cannot be created
     * @throws ValueDoesNotMatchTypeException in case that the index value does not match
     */
    private static ConstraintSyntaxTree createRefToTuple(DecisionVariableDeclaration var, String slotName, int index) 
        throws CSTSemanticException, ValueDoesNotMatchTypeException {
        ConstraintSyntaxTree slotAccess = new CompoundAccess(new Variable(var), slotName);
        ConstraintSyntaxTree indexExpr = new ConstantValue(ValueFactory.createValue(IntegerType.TYPE, index));
        ConstraintSyntaxTree result = new OCLFeatureCall(slotAccess, "[]", indexExpr);
        result.inferDatatype();
        return result;
    }
    
    /**
     * Creates the default adaptable parameters for a data source.
     * 
     * @param cfgProject the project used to query for types
     * @return the parameters
     * @throws ModelQueryException in case that types cannot be resolved
     * @throws ValueDoesNotMatchTypeException in case that the value cannot be created due to type mismatches
     */
    private static Object[] createDataSourceParameters(Project cfgProject) throws ModelQueryException, 
        ValueDoesNotMatchTypeException {
        Compound stringParameterType = findCompound(cfgProject, TYPE_STRINGPARAMETER);
        Compound intParameterType = findCompound(cfgProject, TYPE_INTEGERPARAMETER);

        Object[] result = new Object[3];
        result[0] = ValueFactory.createValue(stringParameterType, 
            new Object[]{SLOT_STRINGPARAMETER_NAME, PARAM_DATAFILE, SLOT_STRINGPARAMETER_DEFAULTVALUE, ""});
        result[1] = ValueFactory.createValue(stringParameterType, 
            new Object[]{SLOT_STRINGPARAMETER_NAME, PARAM_HDFS_DATAFILE, SLOT_STRINGPARAMETER_DEFAULTVALUE, ""});
        result[2] = ValueFactory.createValue(intParameterType, 
            new Object[]{SLOT_STRINGPARAMETER_NAME, "replaySpeed"});
        return result;
    }

    /**
     * Creates a freeze block for <code>project</code> and adds it to <code>project</code>.
     * 
     * @param freezables the freezables
     * @param project the IVML project to add to (may be <b>null</b> if failed)
     * @param fallbackForType in case that <code>project</code> is being written the first time, may be <b>null</b>
     * @return the created freeze block
     * @throws CSTSemanticException in case of CST errors
     * @throws ValueDoesNotMatchTypeException in case of unmatching values
     * @throws ModelQueryException in case of model access problems
     */
    private static FreezeBlock createFreezeBlock(IFreezable[] freezables, Project project, Project fallbackForType) 
        throws CSTSemanticException, ValueDoesNotMatchTypeException, ModelQueryException {
        FreezeBlock result = Utils.createFreezeBlock(freezables, project, fallbackForType);
        project.add(result);
        return result;
    }
    
    /**
     * Creates a freeze block for <code>project</code> and adds it to <code>project</code>.
     * 
     * @param freezables the freezables
     * @param project the IVML project to add to (may be <b>null</b> if failed)
     * @param fallbackForType in case that <code>project</code> is being written the first time, may be <b>null</b>
     * @return the created freeze block
     * @throws CSTSemanticException in case of CST errors
     * @throws ValueDoesNotMatchTypeException in case of unmatching values
     * @throws ModelQueryException in case of model access problems
     */
    public static FreezeBlock createFreezeBlock(List<IFreezable> freezables, Project project, Project fallbackForType) 
        throws CSTSemanticException, ValueDoesNotMatchTypeException, ModelQueryException {
        FreezeBlock result = Utils.createFreezeBlock(freezables, project, fallbackForType);
        project.add(result);
        return result;
    }

    /**
     * Creates a project with basic settings for QM.
     * 
     * @param name the name of the project
     * @param typeFallback a project acting as type fallback (may be <b>null</b>)
     * @return the created project
     * @throws ModelQueryException in case of model query problems
     * @throws CSTSemanticException in case of CST errors
     * @throws ValueDoesNotMatchTypeException in case of unmatching values
     */
    private static Project createQmProject(String name, Project typeFallback) throws CSTSemanticException, 
        ValueDoesNotMatchTypeException, ModelQueryException {
        Project result = new Project(name);
        addRuntimeAttributeToProject(result, typeFallback);
        return result;
    }
    
    /**
     * Returns (a copy) of the value of the <code>slot</code> in <code>var</code>.
     * 
     * @param var the variable to look into
     * @param slot the slot name
     * @return the (copied) value
     * @throws ModelQueryException if the given slot does not exist
     */
    private static Value getValue(IDecisionVariable var, String slot) throws ModelQueryException {
        IDecisionVariable nested = var.getNestedElement(slot);
        if (null == nested) {
            throw new ModelQueryException("cannot find slot '" + slot + "' in '" + var.getDeclaration().getName() 
                + "'", ModelQueryException.ACCESS_ERROR);
        }
        Value val = nested.getValue();
        if (null != val) {
            val = val.clone();
        }
        return val;
    }
    
    /**
     * Adds top level values configured for <code>source</code> to <code>target</code>.
     * 
     * @param cfg the actual configuration holding the values
     * @param source the source project
     * @param target the target project
     * @param exclude the variable names to exclude
     * @return the changed top-level variables ready for freezing
     * @throws CSTSemanticException in case of CST errors
     */
    private static List<IFreezable> addTopLevelValues(Configuration cfg, Project source, Project target, 
        String... exclude) throws CSTSemanticException {
        List<IFreezable> result = new ArrayList<IFreezable>();
        for (int e = 0; e < source.getElementCount(); e++) {
            ContainableModelElement elt = source.getElement(e);
            if (elt instanceof DecisionVariableDeclaration) {
                DecisionVariableDeclaration decl = (DecisionVariableDeclaration) elt;
                if (!Arrays.contains(exclude, decl.getName())) {
                    IDecisionVariable decVar = cfg.getDecision(decl);
                    Value value = decVar.getValue();
                    if (null != value && !ConstraintType.isConstraint(decVar.getDeclaration().getType())) {
                        ConstraintSyntaxTree cst = new OCLFeatureCall(new Variable(decl), IvmlKeyWords.ASSIGNMENT, 
                            new ConstantValue(decVar.getValue().clone()));
                        cst.inferDatatype();
                        Constraint constraint = new Constraint(cst, target);
                        target.addConstraint(constraint);
                        result.add(decl);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Adds the given imports to <code>target</code>.
     * 
     * @param source the project to look for the imports
     * @param imports the import names to add
     * @param target the target project to modify as a side effect
     * @param furtherImports further imports created in this class
     * @throws ModelManagementException in case of model management problems
     */
    private static void addImports(Project source, String[] imports, Project target, Project... furtherImports) 
        throws ModelManagementException {
        for (String s : imports) {
            Project prj = ModelQuery.findProject(source, s);
            if (null != prj) {
                ProjectImport imp = new ProjectImport(s);
                imp.setResolved(prj);
                target.addImport(imp);
            }
        }
        for (int f = 0; f < furtherImports.length; f++) {
            Project prj = furtherImports[f];
            ProjectImport imp = new ProjectImport(prj.getName());
            imp.setResolved(prj);
            target.addImport(imp);
        }
    }

    /**
     * Creates a decision variable declaration.
     * 
     * @param name the name of the variable
     * @param type the type
     * @param target the project to add the variable to
     * @param values the values as default value
     * @return the created variable
     * @throws CSTSemanticException in case of CST errors
     * @throws ValueDoesNotMatchTypeException if the given values do not match
     */
    private static DecisionVariableDeclaration createDecisionVariable(String name, IDatatype type, Project target, 
        Object... values) throws CSTSemanticException, ValueDoesNotMatchTypeException {
        DecisionVariableDeclaration result = new DecisionVariableDeclaration(name, type, target);
        result.setValue(new ConstantValue(ValueFactory.createValue(type, values)));
        target.add(result);
        return result;
    }
    
    /**
     * Sets the given <code>pipeline</code> as value in the <code>varName</code> of <code>prj</code>. 
     *
     * @param prj the project to modify
     * @param varName the variable to modify
     * @param pipeline the pipeline to set as (reference) value
     * @return the affected variable
     * @throws ModelQueryException if access to the variable failed
     * @throws CSTSemanticException in case of CST errors
     * @throws ValueDoesNotMatchTypeException if <code>pipeline</code> does not match as a value
     */
    private static DecisionVariableDeclaration setPipelines(Project prj, String varName, 
        DecisionVariableDeclaration pipeline) throws ModelQueryException, CSTSemanticException, 
        ValueDoesNotMatchTypeException {
        DecisionVariableDeclaration pipelinesVar = (DecisionVariableDeclaration) ModelQuery.findVariable(prj, 
            varName, DecisionVariableDeclaration.class);
        if (null != pipelinesVar && pipelinesVar.getType() instanceof Container) {
            Container cType = (Container) pipelinesVar.getType();
            ConstraintSyntaxTree cst = new OCLFeatureCall(new Variable(pipelinesVar), IvmlKeyWords.ASSIGNMENT, 
                new ConstantValue(ValueFactory.createValue(cType, pipeline)));
            cst.inferDatatype();
            Constraint constraint = new Constraint(cst, prj);
            prj.addConstraint(constraint);
        } else {
            throw new ModelQueryException("pipelines variable '" + varName + "' not found", 
                ModelQueryException.ACCESS_ERROR);            
        }
        return pipelinesVar;
    }
    
}
