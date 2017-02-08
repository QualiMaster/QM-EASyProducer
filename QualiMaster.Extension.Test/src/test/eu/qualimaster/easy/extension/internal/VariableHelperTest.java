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
package test.eu.qualimaster.easy.extension.internal;

import org.junit.Assert;
import org.junit.Test;

import eu.qualimaster.easy.extension.QmConstants;
import eu.qualimaster.easy.extension.internal.VariableHelper;
import net.ssehub.easy.varModel.confModel.Configuration;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.cst.CSTSemanticException;
import net.ssehub.easy.varModel.cst.ConstantValue;
import net.ssehub.easy.varModel.model.DecisionVariableDeclaration;
import net.ssehub.easy.varModel.model.Project;
import net.ssehub.easy.varModel.model.datatypes.BooleanType;
import net.ssehub.easy.varModel.model.datatypes.Compound;
import net.ssehub.easy.varModel.model.datatypes.IntegerType;
import net.ssehub.easy.varModel.model.datatypes.RealType;
import net.ssehub.easy.varModel.model.datatypes.StringType;
import net.ssehub.easy.varModel.model.values.ValueDoesNotMatchTypeException;
import net.ssehub.easy.varModel.model.values.ValueFactory;

/**
 * Tests {@link VariableHelper}.
 * 
 * @author Holger Eichelberger
 */
public class VariableHelperTest {

    /**
     * Tests {@link VariableHelper#getBoolean(IDecisionVariable, String)} 
     * and {@link VariableHelper#hasName(IDecisionVariable, String)}.
     *     
     * @throws ValueDoesNotMatchTypeException shall not occur
     * @throws CSTSemanticException shall not occur
     */
    @Test
    public void testGetBooleanHasValue() throws ValueDoesNotMatchTypeException, CSTSemanticException {
        final String boolVarName = "enabled";
        final Boolean boolVarValue = Boolean.TRUE;
        final String doubleVarName = "dVal";
        final Double doubleVarValue = 25.7;
        final String integerVarName = "iVal";
        final Integer integerVarValue = 21;
        
        Project project = new Project("test");
        Compound cmp1 = new Compound("test", project);
        project.add(cmp1);
        Compound cmp2 = new Compound(QmConstants.TYPE_ALGORITHM, project);
        DecisionVariableDeclaration nameSlot = new DecisionVariableDeclaration(QmConstants.SLOT_NAME, 
            StringType.TYPE, cmp2);
        cmp2.add(nameSlot);
        DecisionVariableDeclaration boolSlot = new DecisionVariableDeclaration(boolVarName, 
            BooleanType.TYPE, cmp2);
        cmp2.add(boolSlot);
        project.add(cmp2);
        DecisionVariableDeclaration doubleSlot = new DecisionVariableDeclaration(doubleVarName, 
            RealType.TYPE, cmp2);
        cmp2.add(doubleSlot);
        project.add(cmp2);
        DecisionVariableDeclaration intSlot = new DecisionVariableDeclaration(integerVarName, 
            IntegerType.TYPE, cmp2);
        cmp2.add(intSlot);
        project.add(cmp2);

        final String name = "var2Name";
        DecisionVariableDeclaration var1 = new DecisionVariableDeclaration("var1", cmp1, project);
        var1.setValue(new ConstantValue(ValueFactory.createValue(cmp1, (Object[]) null)));
        project.add(var1);
        DecisionVariableDeclaration var2 = new DecisionVariableDeclaration("var2", cmp2, project);
        var2.setValue(new ConstantValue(ValueFactory.createValue(cmp2, 
            new Object[]{QmConstants.SLOT_NAME, name, boolVarName, boolVarValue, doubleVarName, doubleVarValue, 
                integerVarName, integerVarValue})));
        project.add(var2);
        
        Configuration cfg = new Configuration(project);
        
        IDecisionVariable varVar1 = cfg.getDecision(var1);
        Assert.assertNotNull(varVar1);
        IDecisionVariable varVar2 = cfg.getDecision(var2);
        Assert.assertNotNull(varVar2);
        
        Assert.assertNull(VariableHelper.getBoolean(null, boolVarName));
        Assert.assertNull(VariableHelper.getBoolean(varVar1, boolVarName));
        Assert.assertNull(VariableHelper.getBoolean(varVar2, QmConstants.SLOT_NAME));
        Assert.assertEquals(boolVarValue, VariableHelper.getBoolean(varVar2, boolVarName));

        Assert.assertNull(VariableHelper.getDouble(null, doubleVarName));
        Assert.assertNull(VariableHelper.getDouble(varVar1, doubleVarName));
        Assert.assertEquals(doubleVarValue, VariableHelper.getDouble(varVar2, doubleVarName));
        Assert.assertNull(VariableHelper.getInteger(null, integerVarName));
        Assert.assertNull(VariableHelper.getInteger(varVar1, integerVarName));
        Assert.assertEquals(integerVarValue, VariableHelper.getInteger(varVar2, integerVarName));
        Assert.assertEquals(Double.valueOf(integerVarValue), VariableHelper.getDouble(varVar2, integerVarName));

        Assert.assertFalse(VariableHelper.hasName(null, name));
        Assert.assertFalse(VariableHelper.hasName(varVar1, name)); 
        Assert.assertTrue(VariableHelper.hasName(varVar2, name));
        Assert.assertEquals(name, VariableHelper.getName(varVar2));
        Assert.assertFalse(VariableHelper.hasName(varVar2, "whatever"));
    }
    
}
