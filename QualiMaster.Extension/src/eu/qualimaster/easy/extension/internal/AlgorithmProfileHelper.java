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

import de.uni_hildesheim.sse.easy_producer.core.persistence.standard.StandaloneProjectDescriptor;
import de.uni_hildesheim.sse.easy_producer.instantiator.model.common.VilException;
import de.uni_hildesheim.sse.easy_producer.instantiator.model.execution.Executor;
import de.uni_hildesheim.sse.easy_producer.instantiator.model.execution.TracerFactory;
import de.uni_hildesheim.sse.easy_producer.instantiator.model.tracing.ConsoleTracerFactory;
import de.uni_hildesheim.sse.easy_producer.instantiator.model.vilTypes.IProjectDescriptor;
import eu.qualimaster.common.QMInternal;
import net.ssehub.easy.basics.modelManagement.ModelManagementException;
import net.ssehub.easy.varModel.confModel.Configuration;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.cst.CSTSemanticException;
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
import net.ssehub.easy.varModel.model.values.Value;
import net.ssehub.easy.varModel.model.values.ValueDoesNotMatchTypeException;
import net.ssehub.easy.varModel.model.values.ValueFactory;

import static eu.qualimaster.easy.extension.QmConstants.*;
import static eu.qualimaster.easy.extension.internal.Utils.*;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.xtext.util.Arrays;

/**
 * Support for creating on-demand pipelines for algorithm profiling.
 * 
 * @author Holger Eichelberger
 */
public class AlgorithmProfileHelper {

    private static final String[] PIPELINE_IMPORTS = {PROJECT_BASICS, PROJECT_PIPELINES, PROJECT_FAMILIESCFG, 
        PROJECT_DATAMGTCFG};
    private static final String[] PIPELINES_IMPORTS = {PROJECT_BASICS, PROJECT_PIPELINES};
    private static final String[] INFRASTRUCTURE_IMPORTS = {PROJECT_INFRASTRUCTURE};
    private static final String[] TOP_IMPORTS = {PROJECT_HARDWARECFG, PROJECT_RECONFHWCFG, PROJECT_DATAMGTCFG, 
        PROJECT_OBSERVABLESCFG, PROJECT_ADAPTIVITYCFG, PROJECT_ALGORITHMSCFG, PROJECT_FAMILIESCFG};
    private static final String PIP_NAME = "ProfilingTestPip";
    private static final String SRC_NAME = "TestSource";
    
    /**
     * Profiles the given algorithm. Create a specific pipeline with data source, specific family holding
     * only the test algorithm.
     * 
     * @param config the configuration to be used as basis for creating a profiling pipeline
     * @param familyName the name of the family to test
     * @param algorithmName the name of the algorithm within <code>family</code> to test
     * @param source the source project descriptor, also to be used as target folder for instantiation (the folder may 
     *     be empty)
     * @throws VilException in case of model query problems, model management problems, CST errors, unmatching IVML 
     *     values or VIL execution errors
     */
    @QMInternal
    public static void profile(net.ssehub.easy.varModel.confModel.Configuration config, String familyName, 
        String algorithmName, IProjectDescriptor source) throws VilException {
        
        try {
            Project qm = createNewRoot(config, familyName, algorithmName);
            Configuration cfg = new Configuration(qm);
            
            TracerFactory.setInstance(ConsoleTracerFactory.INSTANCE);
            StandaloneProjectDescriptor target = new StandaloneProjectDescriptor(source, source.getBase());
            Executor executor = new Executor(source.getMainVilScript())
                .addSource(source).addTarget(target)
                .addConfiguration(cfg)
                .addCustomArgument("pipelineName", PIP_NAME)
                .addStartRuleName("pipeline");
            executor.execute();
        } catch (ModelQueryException | ModelManagementException | ValueDoesNotMatchTypeException 
            | CSTSemanticException e) {
            throw new VilException(e.getMessage(), VilException.ID_RUNTIME);
        }
        
        // TODO set pipeline options -> family member, configure generic source, no adaptation
        // TODO start pipeline
    }
    
    /**
     * Creates a new QM model root leaving the real one as it is.
     * 
     * @param config the configuration to be used as basis for creation
     * @param familyName the name of the family to test
     * @param algorithmName the name of the algorithm within <code>family</code> to test
     * @return the new model root project
     * @throws ModelQueryException in case of model query problems
     * @throws ModelManagementException in case of model management problems
     * @throws CSTSemanticException in case of CST errors
     * @throws ValueDoesNotMatchTypeException in case of unmatching values
     */
    private static Project createNewRoot(net.ssehub.easy.varModel.confModel.Configuration config, String familyName, 
        String algorithmName) throws ModelQueryException, ModelManagementException, ValueDoesNotMatchTypeException, 
        CSTSemanticException {
        Project cfgProject = config.getProject();
        Project cfgInfra = ModelQuery.findProject(cfgProject, PROJECT_INFRASTRUCTURE);
        
        Compound familyType = findCompound(cfgProject, TYPE_FAMILY);
        IDecisionVariable testFamily = findNamedVariable(config, familyType, familyName);
        IDecisionVariable testAlgorithm = findAlgorithm(testFamily, algorithmName, true);
        
        Project pip = createQmProject("ProfilingTestPipeline" + CFG_POSTFIX, cfgProject);
        addImports(cfgProject, PIPELINE_IMPORTS, pip);

        Compound dataSourceType = findCompound(pip, TYPE_DATASOURCE);
        Compound flowType = findCompound(pip, TYPE_FLOW);
        Compound pipelineType = findCompound(pip, TYPE_PIPELINE);
        Compound sourceType = findCompound(pip, TYPE_SOURCE);
        Compound familyElementType = findCompound(pip, TYPE_FAMILYELEMENT);
        
        DecisionVariableDeclaration dataSourceVar = createDecisionVariable("prDataSource0", dataSourceType, pip, 
            SLOT_DATASOURCE_NAME, SRC_NAME,
            SLOT_DATASOURCE_TUPLES, testFamily.getNestedElement(SLOT_FAMILY_INPUT).getValue().clone(),
            SLOT_DATASOURCE_ARTIFACT, "eu.qualimaster:genericSource:0.5.0-SNAPSHOT",
            SLOT_DATASOURCE_STORAGELOCATION, "null",
            SLOT_DATASOURCE_PROFILINGSOURCE, true,
            SLOT_DATASOURCE_DATAMANAGEMENTSTRATEGY, CONST_DATAMANAGEMENTSTRATEGY_NONE,
            //SLOT_DATASOURCE_PARAMETERS,
            SLOT_DATASOURCE_SOURCECLS, "eu.qualimaster." + PIP_NAME + ".topology.imp." + SRC_NAME);
        DecisionVariableDeclaration familyVar = createDecisionVariable("prFamily0", familyType, pip, 
            SLOT_FAMILY_NAME, getValue(testFamily, SLOT_FAMILY_NAME),
            SLOT_FAMILY_INPUT, getValue(testFamily, SLOT_FAMILY_INPUT),
            SLOT_FAMILY_OUTPUT, getValue(testFamily, SLOT_FAMILY_OUTPUT),
            SLOT_FAMILY_PARAMETERS, getValue(testFamily, SLOT_FAMILY_PARAMETERS),
            SLOT_FAMILY_MEMBERS, new Object[] {testAlgorithm.getValue()});
        DecisionVariableDeclaration familyEltVar = createDecisionVariable("prFamilyElt0", familyElementType, pip, 
            SLOT_FAMILYELEMENT_NAME, "family",
            SLOT_FAMILYELEMENT_PARALLELISM, 1, 
            SLOT_FAMILYELEMENT_FAMILY, familyVar);
        DecisionVariableDeclaration flowVar = createDecisionVariable("prFlow0", flowType, pip,
            SLOT_FLOW_NAME, "f1",
            SLOT_FLOW_DESTINATION, familyEltVar,
            SLOT_FLOW_GROUPING, CONST_GROUPING_SHUFFLEGROUPING);
        DecisionVariableDeclaration sourceVar = createDecisionVariable("prSource0", sourceType, pip,
            SLOT_SOURCE_NAME, "source",
            SLOT_SOURCE_PARALLELISM, 1, 
            // #TASKS
            SLOT_SOURCE_OUTPUT, new Object[]{flowVar},
            SLOT_SOURCE_SOURCE, dataSourceVar);
        DecisionVariableDeclaration pipVar = createDecisionVariable("prPipeline0", pipelineType, pip, 
            SLOT_PIPELINE_NAME, PIP_NAME, 
            SLOT_PIPELINE_ARTIFACT, "eu.qualimaster:" + PIP_NAME + ":0.0.1-SNAPSHOT",
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
        
        Project qm = createQmProject(PROJECT_TOP_LEVEL, cfgProject);
        addImports(cfgProject, TOP_IMPORTS, qm, infra);

        return qm;
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
     * Finds a named variable and throws an exception if not found.
     * 
     * @param config the configuration to search within
     * @param type the type of the variable
     * @param name the name of the variable
     * @return the variable
     * @throws ModelQueryException if not found
     */
    private static IDecisionVariable findNamedVariable(Configuration config, IDatatype type, String name) 
        throws ModelQueryException {
        IDecisionVariable result = VariableHelper.findNamedVariable(config, type, name);
        if (null == result) {
            throw new ModelQueryException(type.getName() + " '" + name + "' not found", 
                ModelQueryException.ACCESS_ERROR);
        }
        return result;
    }

    /**
     * Finds an algorithm in <code>family</code>.
     * 
     * @param family the family
     * @param name the name of the algorithm
     * @param asReference return the reference to the algorithm or the algorithm itself
     * @return the algorithm or its reference (depending on <code>asReference</code>)
     * @throws ModelQueryException if the algorithm cannot be found
     */
    private static IDecisionVariable findAlgorithm(IDecisionVariable family, String name, boolean asReference) 
        throws ModelQueryException {
        IDecisionVariable result = null;
        IDecisionVariable members = family.getNestedElement(SLOT_FAMILY_MEMBERS);
        if (null == members) {
            throw new ModelQueryException("'" + SLOT_FAMILY_MEMBERS + "' not found in variable '" 
                + family.getDeclaration().getName() + "'", ModelQueryException.ACCESS_ERROR);
        }
        for (int n = 0; null == result && n < members.getNestedElementsCount(); n++) {
            IDecisionVariable algoRef = members.getNestedElement(n);
            IDecisionVariable algorithm = Configuration.dereference(algoRef);
            if (VariableHelper.hasName(algorithm, name)) {
                if (asReference) {
                    result = algoRef;
                } else {
                    result = algorithm;
                }
            }
        }
        if (null == result) {
            throw new ModelQueryException("algorithm '" + name + "' not found in variable '" 
                + family.getDeclaration().getName() + "'", ModelQueryException.ACCESS_ERROR);
        }
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
        return nested.getValue().clone();
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
