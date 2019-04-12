/*
 * Copyright 2009-2018 University of Hildesheim, Software Systems Engineering
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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import eu.qualimaster.coordination.RepositoryConnector;
import eu.qualimaster.coordination.RepositoryHelper;
import eu.qualimaster.coordination.RuntimeVariableMapping;
import eu.qualimaster.coordination.RepositoryConnector.Models;
import eu.qualimaster.easy.extension.QmConstants;
import eu.qualimaster.easy.extension.debug.AbstractDebug;
import eu.qualimaster.easy.extension.internal.BindValuesInstantiator;
import eu.qualimaster.easy.extension.internal.ConfigurationInitializer;
import eu.qualimaster.easy.extension.internal.CoordinationHelper;
import eu.qualimaster.monitoring.events.FrozenSystemState;
import net.ssehub.easy.basics.modelManagement.ModelInitializer;
import net.ssehub.easy.basics.modelManagement.ModelManagementException;
import net.ssehub.easy.basics.progress.ProgressObserver;
import net.ssehub.easy.instantiation.core.model.vilTypes.Sequence;
import net.ssehub.easy.instantiation.core.model.vilTypes.configuration.Configuration;
import net.ssehub.easy.instantiation.core.model.vilTypes.configuration.DecisionVariable;
import net.ssehub.easy.instantiation.core.model.vilTypes.configuration.NoVariableFilter;
import net.ssehub.easy.instantiation.rt.core.model.rtVil.RtVilModel;
import net.ssehub.easy.instantiation.rt.core.model.rtVil.Script;
import net.ssehub.easy.varModel.management.VarModel;
import net.ssehub.easy.varModel.model.IvmlDatatypeVisitor;
import net.ssehub.easy.varModel.model.ModelQueryException;
import net.ssehub.easy.varModel.model.Project;
import net.ssehub.easy.varModel.model.datatypes.BooleanType;
import net.ssehub.easy.varModel.model.datatypes.DerivedDatatype;
import net.ssehub.easy.varModel.model.datatypes.IDatatype;
import net.ssehub.easy.varModel.model.datatypes.IntegerType;
import net.ssehub.easy.varModel.model.datatypes.RealType;

/**
 * Tests the {@link BindValuesInstantiator}.
 * 
 * @author Holger Eichelberger
 */
public class BindValuesTest extends AbstractDebug {
    
    /**
     * Tests binding values.
     * 
     * @throws IOException shall not occur
     * @throws ModelManagementException shall not occur
     * @throws ModelQueryException shall not occur
     */
    @Test
    public void testBindValuesInstantiator() throws IOException, ModelManagementException, ModelQueryException {
        File testDir = new File(new File(System.getProperty("qm.base.dir", "."), "testdata"), "bindValues");

        File stateFile = new File(testDir, "frozenState");
        FrozenSystemState systemState = new FrozenSystemState(stateFile);

        RepositoryHelper.IConnectorInitializer init =  RepositoryHelper.getInitializer();
        RepositoryHelper.setInitializer(new RepositoryHelper.NullConnectorInitializer());
        CoordinationHelper.setInTesting(true);
        initialize();
        
        ModelInitializer.registerLoader(ProgressObserver.NO_OBSERVER);
        ModelInitializer.addLocation(testDir, ProgressObserver.NO_OBSERVER);
        Project project = RepositoryHelper.obtainModel(VarModel.INSTANCE, "QM", null);
        Script rtVilModel = RepositoryHelper.obtainModel(RtVilModel.INSTANCE, "QM", null);
        RuntimeVariableMapping rMapping = new RuntimeVariableMapping();
        net.ssehub.easy.varModel.confModel.Configuration config 
            = RepositoryHelper.createConfiguration(project, "TESTING", rMapping);
        rMapping = ConfigurationInitializer.createVariableMapping(config, rMapping);
        new Models(RepositoryConnector.Phase.MONITORING, config, rtVilModel, null, rMapping); // registers itself

        Configuration cfg = new Configuration(config, NoVariableFilter.INSTANCE); // assert access as in rtVIL
        Map<String, Object> binding = new HashMap<String, Object>();
        binding.putAll(systemState.getMapping());
        BindValuesInstantiator.storeValueBinding(cfg, binding);
        
        //Infrastructure\:AVAILABLE_MACHINES=50
        assertInteger(cfg, "availableMachines", 50);
        //Infrastructure\:USED_MACHINES=20
        assertInteger(cfg, "usedMachines", 20);
        //Machine\:host-10.ssecluster.local\:AVAILABLE=1.0
        assertBoolean(cfg, "mach10", "available", true);
        //HwNode\:olynthos1\:AVAILABLE=1.0
        assertBoolean(cfg, "olynthos", "available", true);
        //Cloud\:AWS\:PING=150
        assertDouble(cfg, "AWS", "ping", 150.0);
        //Pipeline:\SwitchPip\:LATENCY=140
        assertInteger(cfg, "pip", "latency", 140);
        //PipelineElement\:SwitchPip\:processor\:USED_MEMORY=0.0
        assertInteger(cfg, "famElt1", "usedMemory", 25000);
        //PipelineElement\:SwitchPip\:src\:ITEMS=880.6387225548903
        assertDouble(cfg, "src", "items", 880.638);
        //DataSource\:SwitchPip\:Random\ Source\:VOLUME=1000
        assertAvailableDouble(cfg, "src", "Random Source", "volume", 1000.0);
        //Actual\:SwitchPip\:processor\:SwitchProcessor1\:AVAILABLE=1.0
        assertActual(cfg, "famElt1", "SwitchProcessor1");

        ModelInitializer.removeLocation(testDir, ProgressObserver.NO_OBSERVER);
        RepositoryHelper.setInitializer(init);
    }

    /**
     * Asserts the existence of a compound slot and returns the slot using VIL accessors.
     * 
     * @param cfg the configuration to obtain the slot from
     * @param var the top-level variable name
     * @param slot the slot name
     * @return the slot variable
     */
    private static DecisionVariable assertSlot(Configuration cfg, String var, String slot) {
        DecisionVariable dVar = cfg.getByName(var);
        Assert.assertNotNull("Variable " + var + " not expected to be null", dVar);
        DecisionVariable dSlot = dVar.getByName(slot);
        Assert.assertNotNull("Slot " + slot + " not expected to be null", dSlot);
        Assert.assertNotNull("Slot value not expected to be null", dSlot.getValue());
        return dSlot;
    }
    
    /**
     * Asserts the IVML type of a slot/variable.
     * 
     * @param var the variable
     * @param type the expected type of {@code var}
     */
    private static void assertType(DecisionVariable var, IDatatype type) {
        Assert.assertEquals("Variable/slot type not " + IvmlDatatypeVisitor.getQualifiedType(type), type, 
            DerivedDatatype.resolveToBasis(var.getDecisionVariable().getDeclaration().getType()));
    }
    
    /**
     * Asserts a Boolean slot value.
     * 
     * @param cfg the VIL configuration (no filter!)
     * @param var the variable name
     * @param slot the slot name
     * @param value the expected value of {@code slot}
     */
    private static void assertBoolean(Configuration cfg, String var, String slot, boolean value) {
        DecisionVariable dSlot = assertSlot(cfg, var, slot);
        assertType(dSlot, BooleanType.TYPE);
        Assert.assertEquals(value, dSlot.getBooleanValue());        
    }

    /**
     * Asserts a Real slot value.
     * 
     * @param cfg the VIL configuration (no filter!)
     * @param var the variable name
     * @param slot the slot name
     * @param value the expected value of {@code slot}
     */
    private static void assertDouble(Configuration cfg, String var, String slot, double value) {
        DecisionVariable dSlot = assertSlot(cfg, var, slot);
        assertType(dSlot, RealType.TYPE);
        Assert.assertEquals(value, dSlot.getRealValue(), 1.0);        
    }
    
    /**
     * Asserts an Integer slot value.
     * 
     * @param cfg the VIL configuration (no filter!)
     * @param var the variable name
     * @param slot the slot name
     * @param value the expected value of {@code slot}
     */
    private static void assertInteger(Configuration cfg, String var, String slot, int value) {
        DecisionVariable dSlot = assertSlot(cfg, var, slot);
        assertType(dSlot, IntegerType.TYPE);
        Assert.assertNotNull("Integer value not expected to be null", dSlot.getIntegerValue());
        Assert.assertEquals(value, dSlot.getIntegerValue().intValue());        
    }
    
    /**
     * Asserts an Integer value.
     * 
     * @param cfg the VIL configuration (no filter!)
     * @param var the variable name
     * @param value the expected value of {@code var}
     */
    private static void assertInteger(Configuration cfg, String var, int value) {
        DecisionVariable dVar = cfg.getByName(var);
        Assert.assertNotNull("Variable " + var + " not expected to be null", dVar);
        assertType(dVar, IntegerType.TYPE);
        Assert.assertNotNull("Integer value not expected to be null", dVar.getIntegerValue());
        Assert.assertEquals(value, dVar.getIntegerValue().intValue());
    }
    
    /**
     * Finds a variable by name in {@code values}.
     * 
     * @param values the values to search for
     * @param name the name of the variable to return
     * @return the found variable or <b>null</b>
     */
    private static DecisionVariable findByName(Sequence<DecisionVariable> values, String name) {
        DecisionVariable result = null;
        for (int i = 0; i < values.size(); i++) {
            DecisionVariable tmp = values.at(i);
            if (null != tmp) {
                DecisionVariable nVar = tmp.getByName(QmConstants.SLOT_NAME);
                if (null != nVar && name.equals(nVar.getStringValue())) {
                    result = tmp;
                }
            }
        }
        return result;
    }

    /**
     * Asserts a double value on an available element (found by name).
     * 
     * @param cfg the VIL configuration (no filter!)
     * @param var the variable name
     * @param elt the element name to search for in the availables of {@code var}
     * @param slot the slot name
     * @param value the expected value of {@code slot}
     */
    private static void assertAvailableDouble(Configuration cfg, String var, String elt, String slot, double value) {
        DecisionVariable dVar = cfg.getByName(var);
        Assert.assertNotNull("Variable " + var + " not expected to be null", dVar);
        DecisionVariable aVar = dVar.getByName(QmConstants.SLOT_AVAILABLE);
        Assert.assertNotNull("Slot " + QmConstants.SLOT_AVAILABLE + " not expected to be null", aVar);
        DecisionVariable eVar = findByName(aVar.variables(), elt);
        Assert.assertNotNull("Element " + elt + " not expected to be null", eVar);
        DecisionVariable sVar = eVar.getByName(slot);
        Assert.assertNotNull("Slot " + slot + " not expected to be null", sVar);
        assertType(sVar, RealType.TYPE);
        Assert.assertEquals(value, sVar.getRealValue(), 1.0);        
    }

    /**
     * Asserts an actual algorithm.
     * 
     * @param cfg the VIL configuration (no filter!)
     * @param var the variable name
     * @param alg the expected actual algorithm (data source, data sink)
     */
    private static void assertActual(Configuration cfg, String var, String alg) {
        DecisionVariable dVar = cfg.getByName(var);
        Assert.assertNotNull("Variable " + var + " not expected to be null", dVar);
        DecisionVariable aVar = dVar.getByName(QmConstants.SLOT_ACTUAL);
        Assert.assertNotNull("Slot " + QmConstants.SLOT_ACTUAL + " not expected to be null", aVar);
        DecisionVariable nVar = aVar.getByName(QmConstants.SLOT_NAME);
        Assert.assertNotNull("Slot " + QmConstants.SLOT_NAME + " not expected to be null", nVar);
        Assert.assertEquals(alg, nVar.getStringValue());
    }

}
