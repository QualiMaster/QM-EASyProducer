/*
 * Copyright 2009-2015 University of Hildesheim, Software Systems Engineering
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
package test.eu.qualimaster.easy.extension.internal;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.uni_hildesheim.sse.easy_producer.instantiator.model.common.VilException;
import de.uni_hildesheim.sse.easy_producer.instantiator.model.vilTypes.configuration.Configuration;
import de.uni_hildesheim.sse.model.confModel.AllFreezeSelector;
import de.uni_hildesheim.sse.model.confModel.AssignmentState;
import de.uni_hildesheim.sse.model.confModel.ConfigurationException;
import de.uni_hildesheim.sse.model.confModel.IDecisionVariable;
import de.uni_hildesheim.sse.model.varModel.AbstractVariable;
import de.uni_hildesheim.sse.model.varModel.DecisionVariableDeclaration;
import de.uni_hildesheim.sse.model.varModel.ModelQueryException;
import de.uni_hildesheim.sse.model.varModel.Project;
import de.uni_hildesheim.sse.model.varModel.ProjectImport;
import de.uni_hildesheim.sse.model.varModel.datatypes.Compound;
import de.uni_hildesheim.sse.model.varModel.datatypes.IDatatype;
import de.uni_hildesheim.sse.model.varModel.datatypes.StringType;
import de.uni_hildesheim.sse.model.varModel.values.ValueDoesNotMatchTypeException;
import de.uni_hildesheim.sse.model.varModel.values.ValueFactory;
import de.uni_hildesheim.sse.utils.modelManagement.ModelManagementException;
import eu.qualimaster.easy.extension.QmConstants;
import eu.qualimaster.easy.extension.internal.PipelineHelper;

/**
 * Tests {@link PipelineHelper}. Currently no jUnit test as d
 * 
 * @author Holger Eichelberger
 */
public class PipelineHelperTest {

    private static Configuration qmCfg;
    private static final String VAR_FAM1 = "fam1";
    private static final String VAR_FAM2 = "fam2";
    private static final String VAR_PIP = "pip";
    private static final Map<String, AbstractVariable> VARIABLES = new HashMap<String, AbstractVariable>(); 
    
    /**
     * Creates a compound type with a name variable slot.
     * 
     * @param name the name of the type
     * @param project the parent project
     * @param refines an optional refined compound (may be <b>null</b> for no refinement)
     * @return the created compound type
     */
    private static Compound createCompoundWithName(String name, Project project, Compound refines) {
        Compound result = new Compound(name, project, refines);
        DecisionVariableDeclaration nameDecl = new DecisionVariableDeclaration("name", StringType.TYPE, result);
        result.add(nameDecl);
        project.add(result);
        return result;
    }
    
    /**
     * Adds a project import from <code>target</code> to <code>resolved</code> to <code>target</code>.
     * 
     * @param resolved the imported (resolved) project
     * @param target the target project where to add the import into
     * @return the import object
     * @throws ModelManagementException in case that setting <code>resolved</code> in the import fails
     */
    private static ProjectImport addProjectImport(Project resolved, Project target) throws ModelManagementException {
        ProjectImport imp = new ProjectImport(resolved.getName());
        imp.setResolved(resolved);
        target.addImport(imp);
        return imp;
    }
    
    /**
     * Creates a decision variable declaration and hooks it with <code>parent</code>.
     * 
     * @param name the name of the decision
     * @param type the type of the decision
     * @param parent the parent project
     * @return the created variable declaration
     */
    private static DecisionVariableDeclaration createDecisionVariableDeclaration(String name, IDatatype type, 
        Project parent) {
        DecisionVariableDeclaration decl = new DecisionVariableDeclaration(name, type, parent);
        parent.add(decl);
        VARIABLES.put(decl.getName(), decl);
        VARIABLES.put(decl.getQualifiedName(), decl);
        return decl;
    }
    
    /**
     * Configures a decision and freezes it.
     * 
     * @param cfg the configuration
     * @param var the variable declaration
     * @param values the values to use for the configuration
     * @return the configured decision
     * @throws ValueDoesNotMatchTypeException in case that the value does not match the type of <code>var</code>
     * @throws ConfigurationException in case that configuring <code>var</code> fails
     */
    private static IDecisionVariable configureAndFreeze(de.uni_hildesheim.sse.model.confModel.Configuration cfg, 
        DecisionVariableDeclaration var, Object... values) throws ValueDoesNotMatchTypeException, 
        ConfigurationException {
        IDecisionVariable result = cfg.getDecision(var);
        result.setValue(ValueFactory.createValue(var.getType(), values), AssignmentState.ASSIGNED);
        result.freeze(AllFreezeSelector.INSTANCE);
        return result;
    }

    /**
     * Returns a decision via {@link #VARIABLES}.
     * 
     * @param cfg the configuration to take the decision from
     * @param name the name of the variable
     * @return the decision or <b>null</b> if not found
     */
    private static IDecisionVariable getDecision(de.uni_hildesheim.sse.model.confModel.Configuration cfg, String name) {
        IDecisionVariable result = null;
        AbstractVariable var = VARIABLES.get(name);
        if (null != var) {
            result = cfg.getDecision(var);
        }
        return result;
    }
    
    /**
     * Returns a decision via {@link #VARIABLES}.
     * 
     * @param cfg the configuration to take the decision from
     * @param name the name of the variable
     * @return the decision or <b>null</b> if not found
     */
    private static IDecisionVariable getDecision(Configuration cfg, String name) {
        return getDecision(cfg.getConfiguration(), name);
    }
    
    /**
     * Returns the variable instance name via {@link #VARIABLES}.
     * 
     * @param cfg the configuration to take the decision from
     * @param name the name of the variable
     * @return the instance name or <b>null</b> if not found
     */
    private static String getVariableInstanceName(Configuration cfg, String name) {
        String result;
        IDecisionVariable var = getDecision(cfg, name);
        if (null != var) {
            result = de.uni_hildesheim.sse.model.confModel.Configuration.getInstanceName(var, true);
        } else {
            result = null;
        }
        return result;
    }
    
    /**
     * Starts up the test.
     * 
     * @throws ModelManagementException shall not occur
     * @throws ValueDoesNotMatchTypeException shall not occur
     * @throws ConfigurationException shall not occur
     */
    @BeforeClass
    public static void startUp() throws ModelManagementException, ValueDoesNotMatchTypeException, 
        ConfigurationException {
        Project pipelines = new Project("Pipelines");
        Compound pipelineElementType = createCompoundWithName(QmConstants.TYPE_PIPELINE_ELEMENT, pipelines, null);
        Compound familyElementType = createCompoundWithName(QmConstants.TYPE_FAMILY_ELEMENT, pipelines, 
            pipelineElementType);
        Compound pipeline = createCompoundWithName(QmConstants.TYPE_PIPELINE, pipelines, null);
        
        Project myPipelineCfg = new Project("MyPipelineCfg");
        addProjectImport(pipelines, myPipelineCfg);
        DecisionVariableDeclaration fam1 = createDecisionVariableDeclaration(VAR_FAM1, 
            familyElementType, myPipelineCfg);
        DecisionVariableDeclaration fam2 = createDecisionVariableDeclaration(VAR_FAM2, 
            familyElementType, myPipelineCfg);
        DecisionVariableDeclaration pip = createDecisionVariableDeclaration(VAR_PIP, pipeline, myPipelineCfg);
        
        Project pipelinesCfg = new Project("PipelinesCfg");
        addProjectImport(pipelines, pipelinesCfg);
        addProjectImport(myPipelineCfg, pipelinesCfg);

        Project qm = new Project("QM");
        addProjectImport(pipelinesCfg, qm);
        
        de.uni_hildesheim.sse.model.confModel.Configuration cfg 
            = new de.uni_hildesheim.sse.model.confModel.Configuration(qm);
        configureAndFreeze(cfg, fam1, new Object[] {"name", VAR_FAM1});
        configureAndFreeze(cfg, fam2, new Object[] {"name", VAR_FAM2});
        configureAndFreeze(cfg, pip, new Object[] {"name", VAR_PIP});
        
        qmCfg = new Configuration(cfg);
    }

    /**
     * Tears down the test.
     */
    @AfterClass
    public static void shutDown() {
    }

    /**
     * Tests accessing a pipeline via the plain IVML functionality.
     * 
     * @throws ModelQueryException shall not occur
     */
    @Test
    public void testGetPipelineIVML() throws ModelQueryException {
        de.uni_hildesheim.sse.model.confModel.Configuration cfg = qmCfg.getConfiguration();
        IDecisionVariable pipeline = getDecision(cfg, VAR_PIP);
        Assert.assertNotNull(pipeline);
        Assert.assertTrue(pipeline == PipelineHelper.obtainPipeline(cfg, getDecision(cfg, VAR_FAM1)));
        Assert.assertTrue(pipeline == PipelineHelper.obtainPipeline(cfg, getDecision(cfg, VAR_FAM2)));
        Assert.assertNull(PipelineHelper.obtainPipeline(cfg, getDecision(cfg, VAR_PIP))); // limitation on famElts
        Assert.assertNull(PipelineHelper.obtainPipeline(cfg, null)); // limitation on famElts
    }
    
    /**
     * Tests accessing a pipeline via the wrapping VIL functionality.
     * 
     * @throws VilException in case that accessing the pipeline or a type fails
     */
    @Test
    public void testGetPipelineVIL() throws VilException {
        Assert.assertTrue(getDecision(qmCfg, VAR_PIP) == PipelineHelper.obtainPipeline(
            qmCfg, getVariableInstanceName(qmCfg, VAR_FAM1)).getVariable());
        Assert.assertTrue(getDecision(qmCfg, VAR_PIP) == PipelineHelper.obtainPipeline(
            qmCfg, getVariableInstanceName(qmCfg, VAR_FAM2)).getVariable());
        Assert.assertNull(PipelineHelper.obtainPipeline(qmCfg, getVariableInstanceName(qmCfg, VAR_PIP)));
        Assert.assertNull(PipelineHelper.obtainPipeline(qmCfg, (String) null));
        
        // try nested
        IDecisionVariable var = getDecision(qmCfg, VAR_FAM1);
        Assert.assertNotNull(var);
        Assert.assertTrue(var.getNestedElementsCount() > 0);
        var = var.getNestedElement(0);
        
        String instanceName = de.uni_hildesheim.sse.model.confModel.Configuration.getInstanceName(var, true);
        Assert.assertTrue(getDecision(qmCfg, VAR_PIP) 
            == PipelineHelper.obtainPipeline(qmCfg, instanceName).getVariable());
    }

    // cannot access ViolatingClause from here...

}
