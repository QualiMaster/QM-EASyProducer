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

import static eu.qualimaster.easy.extension.QmConstants.*;

import eu.qualimaster.easy.extension.internal.PipelineElementHelper;
import eu.qualimaster.easy.extension.internal.PipelineHelper;
import net.ssehub.easy.basics.modelManagement.ModelManagementException;
import net.ssehub.easy.instantiation.core.model.common.VilException;
import net.ssehub.easy.instantiation.core.model.vilTypes.configuration.Configuration;
import net.ssehub.easy.instantiation.core.model.vilTypes.configuration.DecisionVariable;
import net.ssehub.easy.varModel.confModel.AllFreezeSelector;
import net.ssehub.easy.varModel.confModel.AssignmentState;
import net.ssehub.easy.varModel.confModel.ConfigurationException;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.model.AbstractVariable;
import net.ssehub.easy.varModel.model.DecisionVariableDeclaration;
import net.ssehub.easy.varModel.model.IModelElement;
import net.ssehub.easy.varModel.model.IvmlKeyWords;
import net.ssehub.easy.varModel.model.ModelQueryException;
import net.ssehub.easy.varModel.model.Project;
import net.ssehub.easy.varModel.model.ProjectImport;
import net.ssehub.easy.varModel.model.datatypes.Compound;
import net.ssehub.easy.varModel.model.datatypes.IDatatype;
import net.ssehub.easy.varModel.model.datatypes.RealType;
import net.ssehub.easy.varModel.model.datatypes.Reference;
import net.ssehub.easy.varModel.model.datatypes.Set;
import net.ssehub.easy.varModel.model.datatypes.StringType;
import net.ssehub.easy.varModel.model.values.NullValue;
import net.ssehub.easy.varModel.model.values.ReferenceValue;
import net.ssehub.easy.varModel.model.values.Value;
import net.ssehub.easy.varModel.model.values.ValueDoesNotMatchTypeException;
import net.ssehub.easy.varModel.model.values.ValueFactory;

/**
 * Tests {@link PipelineHelper}. Currently no jUnit test as d
 * 
 * @author Holger Eichelberger
 */
public class PipelineHelperTest {

    private static Configuration qmCfg;
    private static final String PRJ_MYPIP_CFG = "MyPipelineCfg";
    private static final String VAR_ALG1 = "alg1";
    private static final String VAR_ALG2 = "alg2";
    private static final String VAR_FAM1 = "fam1";
    private static final String VAR_FAM2 = "fam2";
    private static final String VAR_PIP = "pip";
    private static final String VAR_CAPACITY = "capacity";
    
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
        Compound result = new Compound(name, project, refines); // legacy, consider only one refines
        DecisionVariableDeclaration nameDecl = new DecisionVariableDeclaration(SLOT_NAME, StringType.TYPE, result);
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
     * @param parent the parent model element
     * @return the created variable declaration
     */
    private static DecisionVariableDeclaration createDecisionVariableDeclaration(String name, IDatatype type, 
        IModelElement parent) {
        DecisionVariableDeclaration decl = new DecisionVariableDeclaration(name, type, parent);
        if (parent instanceof Project) {
            ((Project) parent).add(decl);
        } else if (parent instanceof Compound) {
            ((Compound) parent).add(decl);
        } else {
            System.err.println("invalid parent " + parent);
        }
        VARIABLES.put(decl.getName(), decl);
        VARIABLES.put(decl.getQualifiedName(), decl);
        return decl;
    }
    
    /**
     * Turns arbitrary objects into an array.
     * 
     * @param values the values
     * @return the array
     */
    private static Object[] toArray(Object... values) {
        return values;
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
    private static IDecisionVariable configureAndFreeze(net.ssehub.easy.varModel.confModel.Configuration cfg, 
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
    private static IDecisionVariable getDecision(net.ssehub.easy.varModel.confModel.Configuration cfg, String name) {
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
            result = net.ssehub.easy.varModel.confModel.Configuration.getInstanceName(var, true);
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
        Project algorithms = new Project("Algorithms");
        Compound algorithmType = createCompoundWithName(TYPE_ALGORITHM, algorithms, null);
        Reference refAlgorithmType = new Reference("refTo(Algorithm)", algorithmType, algorithms);
        Set refAlgorithmsType = new Set("setOf(refTo(Algorithm))", refAlgorithmType, algorithms);
        DecisionVariableDeclaration algs = createDecisionVariableDeclaration(
            VAR_ALGORITHMS_ALGORITHMS, refAlgorithmsType, algorithms);

        Project algorithmsCfg = new Project("AlgorithmsCfg");
        addProjectImport(algorithms, algorithmsCfg);
        DecisionVariableDeclaration alg1 = createDecisionVariableDeclaration(VAR_ALG1, 
            algorithmType, algorithmsCfg);
        DecisionVariableDeclaration alg2 = createDecisionVariableDeclaration(VAR_ALG2, 
            algorithmType, algorithmsCfg);

        Project pipelines = new Project("Pipelines");
        addProjectImport(pipelines, algorithms);
        Compound pipelineElementType = createCompoundWithName(TYPE_PIPELINE_ELEMENT, pipelines, null);
        Compound familyElementType = createCompoundWithName(TYPE_FAMILYELEMENT, pipelines, 
            pipelineElementType);
        createDecisionVariableDeclaration(SLOT_FAMILYELEMENT_ACTUAL, refAlgorithmType, familyElementType);
        createDecisionVariableDeclaration(SLOT_FAMILYELEMENT_AVAILABLE, refAlgorithmsType, familyElementType);
        Compound pipeline = createCompoundWithName(TYPE_PIPELINE, pipelines, null);
        createDecisionVariableDeclaration(VAR_CAPACITY, RealType.TYPE, pipeline);
        Set pipelinesType = new Set("setOf(refTo(Pipeline))", 
            new Reference("refTo(pipeline)", pipeline, pipelines), pipelines);
        DecisionVariableDeclaration pips = createDecisionVariableDeclaration(VAR_PIPELINES_PIPELINES, 
            pipelinesType, pipelines);
        
        Project myPipelineCfg = new Project(PRJ_MYPIP_CFG);
        addProjectImport(pipelines, myPipelineCfg);
        addProjectImport(algorithmsCfg, myPipelineCfg);
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
        
        net.ssehub.easy.varModel.confModel.Configuration cfg 
            = new net.ssehub.easy.varModel.confModel.Configuration(qm);
        configureAndFreeze(cfg, alg1, SLOT_NAME, VAR_ALG1);
        configureAndFreeze(cfg, alg2, SLOT_NAME, VAR_ALG2);
        configureAndFreeze(cfg, algs, alg1, alg2);
        configureAndFreeze(cfg, fam1, SLOT_NAME, VAR_FAM1, SLOT_FAMILYELEMENT_AVAILABLE, toArray(alg1));
        configureAndFreeze(cfg, fam2, SLOT_NAME, VAR_FAM2, SLOT_FAMILYELEMENT_AVAILABLE, toArray(alg2));
        configureAndFreeze(cfg, pip, SLOT_NAME, VAR_PIP);
        configureAndFreeze(cfg, pips, new Object[] {pip});
        // de.uni_hildesheim.sse.model.confModel.Configuration.printConfig(System.out, cfg);
        
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
        net.ssehub.easy.varModel.confModel.Configuration cfg = qmCfg.getConfiguration();
        IDecisionVariable pipeline = getDecision(cfg, VAR_PIP);
        Assert.assertNotNull(pipeline);
        Assert.assertTrue(pipeline == PipelineHelper.obtainPipeline(cfg, getDecision(cfg, VAR_FAM1)));
        Assert.assertTrue(pipeline == PipelineHelper.obtainPipeline(cfg, getDecision(cfg, VAR_FAM2)));
        Assert.assertNull(PipelineHelper.obtainPipeline(cfg, getDecision(cfg, VAR_PIP))); // limitation on famElts
        Assert.assertNull(PipelineHelper.obtainPipeline(cfg, (IDecisionVariable) null)); // limitation on famElts
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
        Assert.assertNull(PipelineHelper.obtainPipeline(qmCfg, getVariableInstanceName(qmCfg, VAR_FAM1), true));
        
        Assert.assertTrue(getDecision(qmCfg, VAR_PIP) == PipelineHelper.obtainPipeline(
            qmCfg, getVariableInstanceName(qmCfg, VAR_FAM2)).getVariable());
        Assert.assertNull(PipelineHelper.obtainPipeline(qmCfg, getVariableInstanceName(qmCfg, VAR_FAM2), true));
        
        Assert.assertNull(PipelineHelper.obtainPipeline(qmCfg, getVariableInstanceName(qmCfg, VAR_PIP)));
        Assert.assertTrue(getDecision(qmCfg, VAR_PIP) == PipelineHelper.obtainPipeline(
            qmCfg, getVariableInstanceName(qmCfg, VAR_PIP), true).getVariable());
        Assert.assertTrue(getDecision(qmCfg, VAR_PIP) == PipelineHelper.obtainPipeline(
            qmCfg, getVariableInstanceName(qmCfg, VAR_PIP) + "::" + VAR_CAPACITY, true).getVariable());
        Assert.assertNull(PipelineHelper.obtainPipeline(qmCfg, (String) null));
        
        // try nested
        IDecisionVariable var = getDecision(qmCfg, VAR_FAM1);
        Assert.assertNotNull(var);
        Assert.assertTrue(var.getNestedElementsCount() > 0);
        var = var.getNestedElement(0);
        
        String instanceName = net.ssehub.easy.varModel.confModel.Configuration.getInstanceName(var, true);
        Assert.assertTrue(getDecision(qmCfg, VAR_PIP) 
            == PipelineHelper.obtainPipeline(qmCfg, instanceName).getVariable());
    }

    /**
     * Tests {@link PipelineElementHelper#obtainPipelineElement(
     *    net.ssehub.easy.varModel.confModel.Configuration, String)}.
     * 
     * @throws VilException in case that accessing the pipeline or a type fails
     */
    @Test
    public void testObtainPipelineElementVIL() throws VilException {
        testObtainPipelineElement(true);
    }
    
    /**
     * Tests {@link PipelineElementHelper#obtainPipelineElement(Configuration, String)}.
     * 
     * @throws VilException in case that accessing the pipeline or a type fails
     */
    @Test
    public void testObtainPipelineElementIVML() throws VilException {
        testObtainPipelineElement(false);
    }

    /**
     * Tests to obtain a pipeline element.
     * 
     * @param vil use VIL or IVML access
     * @throws VilException in case that accessing the pipeline or a type fails
     */
    private void testObtainPipelineElement(boolean vil) throws VilException {
        assertVarEquals(VAR_FAM1, VAR_FAM1, vil);
        String varName = getDecision(qmCfg, VAR_FAM1).getDeclaration().getName();
        assertVarEquals(VAR_FAM1, varName, vil);

        // qualified with cfg name
        assertVarEquals(VAR_FAM1, PRJ_MYPIP_CFG + IvmlKeyWords.NAMESPACE_SEPARATOR + VAR_FAM1, vil);
        assertVarEquals(VAR_FAM1, PRJ_MYPIP_CFG + IvmlKeyWords.NAMESPACE_SEPARATOR + varName, vil);

        assertVarEquals(VAR_FAM1, PRJ_MYPIP_CFG + IvmlKeyWords.NAMESPACE_SEPARATOR + VAR_FAM1 
            + IvmlKeyWords.NAMESPACE_SEPARATOR + "name", vil);
        
        assertVarEquals(VAR_FAM1, PRJ_MYPIP_CFG + IvmlKeyWords.NAMESPACE_SEPARATOR + varName
            + IvmlKeyWords.NAMESPACE_SEPARATOR + "name", vil);
        
        assertVarEquals(VAR_FAM1, PRJ_MYPIP_CFG + IvmlKeyWords.NAMESPACE_SEPARATOR + VAR_FAM1 
            + IvmlKeyWords.COMPOUND_ACCESS + "name", vil);

        assertVarEquals(VAR_FAM1, PRJ_MYPIP_CFG + IvmlKeyWords.NAMESPACE_SEPARATOR + varName 
            + IvmlKeyWords.COMPOUND_ACCESS + "name", vil);

        assertVarEquals(null, null, vil);
        assertVarEquals(null, PRJ_MYPIP_CFG + IvmlKeyWords.NAMESPACE_SEPARATOR + "?bla?" 
            + IvmlKeyWords.COMPOUND_ACCESS + "name", vil);
    }    

    /**
     * Asserts variable equality.
     * 
     * @param expectedVarName the name of the expected variable (may be <b>null</b>)
     * @param givenVarName the name of the actual variable (may be <b>null</b>)
     * @param vil use VIL or IVML way
     * @throws VilException in case that accessing the pipeline or a type fails
     */
    private void assertVarEquals(String expectedVarName, String givenVarName, boolean vil) throws VilException {
        if (null != givenVarName && givenVarName.indexOf(IvmlKeyWords.NAMESPACE_SEPARATOR) < 0) {
            givenVarName = getVariableInstanceName(qmCfg, givenVarName);
        } 
        IDecisionVariable actualVar = null;
        if (vil) {
            DecisionVariable actual = PipelineElementHelper.obtainPipelineElement(qmCfg, givenVarName);
            if (null != actual) {
                actualVar = actual.getVariable();
            }
        } else {
            actualVar = PipelineElementHelper.findPipelineElement(qmCfg.getConfiguration(), givenVarName);
        }
        
        if (null == expectedVarName) {
            Assert.assertNull(actualVar);
        } else {
            IDecisionVariable expected = getDecision(qmCfg, expectedVarName);
            Assert.assertNotNull(actualVar);
            Assert.assertTrue(expected == actualVar);
        }
    }

    // cannot access ViolatingClause from here...

    /**
     * Tests obtaining a pipeline from a configuration.
     */
    @Test
    public void testObtainPipelineFromIVML() {
        Assert.assertNull(PipelineHelper.obtainPipelineByName(qmCfg.getConfiguration(), "bla"));
        Assert.assertNotNull(PipelineHelper.obtainPipelineByName(qmCfg.getConfiguration(), VAR_PIP));
    }
    
    /**
     * Obtains a family from IVML.
     */
    @Test
    public void obtainFamilyFromIVML() {
        IDecisionVariable pip = PipelineHelper.obtainPipelineByName(qmCfg.getConfiguration(), VAR_PIP);
        Assert.assertNotNull(pip);
        Assert.assertNotNull(PipelineHelper.obtainFamilyByName(pip, VAR_FAM1));
        Assert.assertNull(PipelineHelper.obtainFamilyByName(pip, "unknown"));
    }
    
    /**
     * Obtains an algorithm from IVML.
     */
    @Test
    public void obtainAlgorithmFromIVML() {
        IDecisionVariable pip = PipelineHelper.obtainPipelineByName(qmCfg.getConfiguration(), VAR_PIP);
        Assert.assertNotNull(pip);
        IDecisionVariable family1 = PipelineHelper.obtainFamilyByName(pip, VAR_FAM1);
        Assert.assertNotNull(family1);
        IDecisionVariable family2 = PipelineHelper.obtainFamilyByName(pip, VAR_FAM2);
        Assert.assertNotNull(family2);
        
        Assert.assertNotNull(PipelineHelper.obtainAlgorithmFromFamilyByName(family1, VAR_ALG1));
        Assert.assertNull(PipelineHelper.obtainAlgorithmFromFamilyByName(family1, VAR_ALG2));
        Assert.assertNull(PipelineHelper.obtainAlgorithmFromFamilyByName(family2, VAR_ALG1));
        Assert.assertNotNull(PipelineHelper.obtainAlgorithmFromFamilyByName(family2, VAR_ALG2));
    }

    /**
     * Tests setting the actual value.
     * 
     * @throws ConfigurationException shall not occur
     */
    @Test
    public void testSetActual() throws ConfigurationException {
        IDecisionVariable pip = PipelineHelper.obtainPipelineByName(qmCfg.getConfiguration(), VAR_PIP);
        Assert.assertNotNull(pip);
        IDecisionVariable family1 = PipelineHelper.obtainFamilyByName(pip, VAR_FAM1);
        Assert.assertNotNull(family1);
        IDecisionVariable actual = family1.getNestedElement(SLOT_ACTUAL);
        Assert.assertNotNull(actual);
        actual.setValue(NullValue.INSTANCE, AssignmentState.USER_ASSIGNED);
        Assert.assertEquals(NullValue.INSTANCE, actual.getValue());
        IDecisionVariable alg1 = PipelineHelper.obtainAlgorithmFromFamilyByName(family1, VAR_ALG1);
        Assert.assertNotNull(alg1);
        
        try {
            PipelineHelper.setActual(qmCfg.getConfiguration(), VAR_PIP, VAR_FAM1, VAR_ALG1);
        } catch (VilException e) {
            Assert.fail(e.getMessage());
        }
        Value val = actual.getValue();
        Assert.assertNotNull(val);
        Assert.assertNotEquals(NullValue.INSTANCE, val);
        Assert.assertTrue(val instanceof ReferenceValue);
        ReferenceValue refVal = (ReferenceValue) val;
        Assert.assertEquals(alg1.getDeclaration(), refVal.getValue());
    }

}
