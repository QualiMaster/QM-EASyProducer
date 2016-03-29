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

import java.util.ArrayList;
import java.util.List;

import net.ssehub.easy.varModel.cst.AttributeVariable;
import net.ssehub.easy.varModel.cst.CSTSemanticException;
import net.ssehub.easy.varModel.cst.ConstantValue;
import net.ssehub.easy.varModel.cst.ConstraintSyntaxTree;
import net.ssehub.easy.varModel.cst.OCLFeatureCall;
import net.ssehub.easy.varModel.cst.Variable;
import net.ssehub.easy.varModel.model.Attribute;
import net.ssehub.easy.varModel.model.ContainableModelElement;
import net.ssehub.easy.varModel.model.DecisionVariableDeclaration;
import net.ssehub.easy.varModel.model.FreezeBlock;
import net.ssehub.easy.varModel.model.IFreezable;
import net.ssehub.easy.varModel.model.ModelQuery;
import net.ssehub.easy.varModel.model.ModelQueryException;
import net.ssehub.easy.varModel.model.Project;
import net.ssehub.easy.varModel.model.datatypes.Compound;
import net.ssehub.easy.varModel.model.datatypes.EnumLiteral;
import net.ssehub.easy.varModel.model.datatypes.FreezeVariableType;
import net.ssehub.easy.varModel.model.values.ValueDoesNotMatchTypeException;
import net.ssehub.easy.varModel.model.values.ValueFactory;

import static eu.qualimaster.easy.extension.QmConstants.*;

/**
 * Utility methods.
 * 
 * @author Holger Eichelberger
 */
public class Utils {

    /**
     * Finds a compound type.
     * 
     * @param project the project to start searching
     * @param name the name of the compound
     * @return the compound type or <b>null</b> if none was found
     * @throws ModelQueryException in case of violated project access restrictions
     */
    public static Compound findCompound(Project project, String name) throws ModelQueryException {
        return (Compound) ModelQuery.findType(project, name, Compound.class);
    }

    /**
     * Creates a freeze block for project. [legacy style, does not add to project]
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
        IFreezable[] tmp = new IFreezable[freezables.size()];
        freezables.toArray(tmp);
        return createFreezeBlock(tmp, project, fallbackForType);
    }
    
    /**
     * Creates a freeze block for project. [legacy style, does not add to project]
     * 
     * @param freezables the freezables
     * @param project the IVML project to add to (may be <b>null</b> if failed)
     * @param fallbackForType in case that <code>project</code> is being written the first time, may be <b>null</b>
     * @return the created freeze block
     * @throws CSTSemanticException in case of CST errors
     * @throws ValueDoesNotMatchTypeException in case of unmatching values
     * @throws ModelQueryException in case of model access problems
     */
    public static FreezeBlock createFreezeBlock(IFreezable[] freezables, Project project, Project fallbackForType) 
        throws CSTSemanticException, ValueDoesNotMatchTypeException, ModelQueryException {
        FreezeBlock result = null;
        FreezeVariableType iterType = new FreezeVariableType(freezables, project);
        DecisionVariableDeclaration iter = new DecisionVariableDeclaration("f", iterType, project);
        net.ssehub.easy.varModel.model.datatypes.Enum type = ModelQuery.findEnum(project, TYPE_BINDING_TIME);
        if (null == type && null != fallbackForType) {
            type = ModelQuery.findEnum(fallbackForType, TYPE_BINDING_TIME);
        }
        String butOperation = "==";
        EnumLiteral literal = type.get(CONST_BINDING_TIME_RUNTIME);
        if (null == literal) { // newer version of the model
            literal = type.get(CONST_BINDING_TIME_RUNTIME_MON);
            butOperation = ">=";
        } 
        ConstraintSyntaxTree runtime = new ConstantValue(ValueFactory.createValue(type, literal));
        Variable iterEx = new AttributeVariable(new Variable(iter), iterType.getAttribute(ANNOTATION_BINDING_TIME));
        OCLFeatureCall op = new OCLFeatureCall(iterEx, butOperation, runtime);
        op.inferDatatype();
        result = new FreezeBlock(freezables, iter, op, project);
        return result;
    }

    /**
     * Adds a freeze block to the project containing all elements.
     * 
     * @param project the IVML project to freeze
     * @return the created freeze block
     * @throws CSTSemanticException in case of CST errors
     * @throws ValueDoesNotMatchTypeException in case of unmatching values
     * @throws ModelQueryException in case of model access problems
     */
    public static FreezeBlock createFreezeBlock(Project project) throws CSTSemanticException, 
        ValueDoesNotMatchTypeException, ModelQueryException {
        List<IFreezable> freezables = new ArrayList<IFreezable>();
        for (int e = 0; e < project.getElementCount(); e++) {
            ContainableModelElement elt = project.getElement(e);
            if (elt instanceof IFreezable) {
                freezables.add((IFreezable) elt);
            }
        }
        FreezeBlock result = createFreezeBlock(freezables, project, project);
        project.add(result);
        return result;
    }
    
    /**
     * Adds the binding time attribute to <code>project</code>.
     * 
     * @param project the IVML project to add the attribute to
     * @param fallbackForType in case that <code>project</code> is being written the first time, may be <b>null</b>
     * @throws CSTSemanticException in case of CST errors
     * @throws ValueDoesNotMatchTypeException in case of unmatching values
     * @throws ModelQueryException in case of model access problems
     */
    public static void addRuntimeAttributeToProject(Project project, Project fallbackForType) 
        throws CSTSemanticException, ValueDoesNotMatchTypeException, ModelQueryException {
        //attribute BindingTime bindingTime = BindingTime.compile to PriorityPipCfg;
        net.ssehub.easy.varModel.model.datatypes.Enum type = ModelQuery.findEnum(project, TYPE_BINDING_TIME);
        if (null == type && null != fallbackForType) {
            type = ModelQuery.findEnum(fallbackForType, TYPE_BINDING_TIME);
        }
        EnumLiteral literal = type.get(CONST_BINDING_TIME_COMPILE);
        Attribute attr = new Attribute(ANNOTATION_BINDING_TIME, type, project, project);
        attr.setValue(new ConstantValue(ValueFactory.createValue(type, literal)));
        project.add(attr);
    }

}
