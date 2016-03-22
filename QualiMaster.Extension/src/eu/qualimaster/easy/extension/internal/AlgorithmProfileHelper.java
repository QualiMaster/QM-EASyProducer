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

import de.uni_hildesheim.sse.model.confModel.Configuration;
import de.uni_hildesheim.sse.model.confModel.IDecisionVariable;
import de.uni_hildesheim.sse.model.cst.CSTSemanticException;
import de.uni_hildesheim.sse.model.cst.ConstantValue;
import de.uni_hildesheim.sse.model.cst.ConstraintSyntaxTree;
import de.uni_hildesheim.sse.model.cst.OCLFeatureCall;
import de.uni_hildesheim.sse.model.cst.Variable;
import de.uni_hildesheim.sse.model.varModel.Constraint;
import de.uni_hildesheim.sse.model.varModel.ContainableModelElement;
import de.uni_hildesheim.sse.model.varModel.DecisionVariableDeclaration;
import de.uni_hildesheim.sse.model.varModel.IFreezable;
import de.uni_hildesheim.sse.model.varModel.IvmlKeyWords;
import de.uni_hildesheim.sse.model.varModel.ModelQuery;
import de.uni_hildesheim.sse.model.varModel.ModelQueryException;
import de.uni_hildesheim.sse.model.varModel.Project;
import de.uni_hildesheim.sse.model.varModel.ProjectImport;
import de.uni_hildesheim.sse.model.varModel.datatypes.Compound;
import de.uni_hildesheim.sse.model.varModel.datatypes.Container;
import de.uni_hildesheim.sse.model.varModel.datatypes.IDatatype;
import de.uni_hildesheim.sse.model.varModel.values.ValueDoesNotMatchTypeException;
import de.uni_hildesheim.sse.model.varModel.values.ValueFactory;
import de.uni_hildesheim.sse.utils.modelManagement.ModelManagementException;
import eu.qualimaster.common.QMInternal;

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
    
    /**
     * Profiles the given algorithm.
     * 
     * @param config the configuration to be used as basis for creating a profiling pipeline
     * @param familyName the name of the family to test
     * @throws ModelQueryException in case of model query problems
     * @throws ModelManagementException in case of model management problems
     * @throws CSTSemanticException in case of CST errors
     * @throws ValueDoesNotMatchTypeException in case of unmatching values
     */
    @QMInternal
    public static void profile(de.uni_hildesheim.sse.model.confModel.Configuration config, String familyName) 
        throws ModelQueryException, ModelManagementException, ValueDoesNotMatchTypeException, CSTSemanticException {
        Project cfgProject = config.getProject();
        Project cfgInfra = ModelQuery.findProject(cfgProject, PROJECT_INFRASTRUCTURE);
        
        Compound familyType = findCompound(cfgProject, TYPE_FAMILY);
        IDecisionVariable testFamily = VariableHelper.findNamedVariable(config, familyType, familyName);
        if (null == testFamily) {
            throw new ModelQueryException("family '" + familyName + "' not found", ModelQueryException.ACCESS_ERROR);
        }
        
        Project pip = new Project("ProfilingTestPipeline" + CFG_POSTFIX);
        addImports(cfgProject, PIPELINE_IMPORTS, pip);

        Compound dataSourceType = findCompound(pip, TYPE_DATASOURCE);
        Compound flowType = findCompound(pip, TYPE_FLOW);
        Compound pipelineType = findCompound(pip, TYPE_PIPELINE);
        Compound sourceType = findCompound(pip, TYPE_SOURCE);
        Compound familyElementType = findCompound(pip, TYPE_FAMILYELEMENT);
        
        DecisionVariableDeclaration dataSourceVar = createDecisionVariable("prDataSource0", dataSourceType, pip, 
            SLOT_DATASOURCE_NAME, "testSource",
            SLOT_DATASOURCE_TUPLES, testFamily.getNestedElement(SLOT_FAMILY_INPUT).getValue().clone(),
            SLOT_DATASOURCE_ARTIFACT, "eu.qualimaster:genericSource:0.5.0-SNAPSHOT",
            SLOT_DATASOURCE_STORAGELOCATION, "null",
            SLOT_DATASOURCE_DATAMANAGEMENTSTRATEGY, CONST_DATAMANAGEMENTSTRATEGY_NONE,
            //SLOT_DATASOURCE_PARAMETERS,
            SLOT_DATASOURCE_SOURCECLS, "eu.qualimaster.genericSource.Source");
        DecisionVariableDeclaration familyVar = createDecisionVariable("prFamily0", familyElementType, pip, 
            SLOT_FAMILYELEMENT_NAME, "family",
            SLOT_FAMILYELEMENT_FAMILY, testFamily.getDeclaration());
        DecisionVariableDeclaration flowVar = createDecisionVariable("prFlow0", flowType, pip,
            SLOT_FLOW_NAME, "f1",
            SLOT_FLOW_DESTINATION, familyVar,
            SLOT_FLOW_GROUPING, CONST_GROUPING_SHUFFLEGROUPING);
        DecisionVariableDeclaration sourceVar = createDecisionVariable("prSource0", sourceType, pip,
            SLOT_SOURCE_NAME, "source",
            SLOT_SOURCE_OUTPUT, new Object[]{flowVar},
            SLOT_SOURCE_SOURCE, dataSourceVar);
        DecisionVariableDeclaration pipVar = createDecisionVariable("prPipeline0", pipelineType, pip, 
            SLOT_PIPELINE_NAME, "ProfilingTestPip", 
            SLOT_PIPELINE_SOURCES, new Object[]{sourceVar},
            SLOT_PIPELINE_NUMWORKERS, 1);
        createFreezeBlock(pip);
        
        Project pipelines = new Project(PROJECT_PIPELINESCFG);
        addImports(cfgProject, PIPELINES_IMPORTS, pipelines, pip);
        DecisionVariableDeclaration pipelinesVar = setPipelines(pipelines, VAR_PIPELINES_PIPELINES, pipVar);
        createFreezeBlock(new IFreezable[]{pipelinesVar}, pipelines, pipelines);
        
        Project infra = new Project(PROJECT_INFRASTRUCTURECFG);
        addImports(cfgProject, INFRASTRUCTURE_IMPORTS, infra, pipelines);
        List<IFreezable> freezes = addTopLevelValues(config, cfgInfra, infra, VAR_INFRASTRUCTURE_ACTIVEPIPELINES);
        freezes.add(setPipelines(pipelines, VAR_INFRASTRUCTURE_ACTIVEPIPELINES, pipVar));
        createFreezeBlock(freezes, infra, infra);
        
        Project qm = new Project(PROJECT_TOP_LEVEL);
        addImports(cfgProject, TOP_IMPORTS, pipelines, infra);
        
        @SuppressWarnings("unused")
        Configuration cfg = new Configuration(qm);
        
        // instantiate and package pipeline
        // set pipeline options -> family member, configure generic source
        // start pipeline
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
                    ConstraintSyntaxTree cst = new OCLFeatureCall(new Variable(decl), IvmlKeyWords.ASSIGN, 
                        new ConstantValue(decVar.getValue().clone()));
                    cst.inferDatatype();
                    Constraint constraint = new Constraint(cst, target);
                    target.addConstraint(constraint);
                    result.add(decl);
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
            ConstraintSyntaxTree cst = new OCLFeatureCall(new Variable(pipelinesVar), IvmlKeyWords.ASSIGN, 
                new ConstantValue(ValueFactory.createValue(cType.getContainedType(), pipeline)));
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
