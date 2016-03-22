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

import de.uni_hildesheim.sse.model.cst.AttributeVariable;
import de.uni_hildesheim.sse.model.cst.CSTSemanticException;
import de.uni_hildesheim.sse.model.cst.ConstantValue;
import de.uni_hildesheim.sse.model.cst.ConstraintSyntaxTree;
import de.uni_hildesheim.sse.model.cst.OCLFeatureCall;
import de.uni_hildesheim.sse.model.cst.Variable;
import de.uni_hildesheim.sse.model.varModel.ContainableModelElement;
import de.uni_hildesheim.sse.model.varModel.DecisionVariableDeclaration;
import de.uni_hildesheim.sse.model.varModel.FreezeBlock;
import de.uni_hildesheim.sse.model.varModel.IFreezable;
import de.uni_hildesheim.sse.model.varModel.ModelQuery;
import de.uni_hildesheim.sse.model.varModel.ModelQueryException;
import de.uni_hildesheim.sse.model.varModel.Project;
import de.uni_hildesheim.sse.model.varModel.datatypes.Compound;
import de.uni_hildesheim.sse.model.varModel.datatypes.EnumLiteral;
import de.uni_hildesheim.sse.model.varModel.datatypes.FreezeVariableType;
import de.uni_hildesheim.sse.model.varModel.values.ValueDoesNotMatchTypeException;
import de.uni_hildesheim.sse.model.varModel.values.ValueFactory;

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
     * Creates a freeze block for project.
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
        de.uni_hildesheim.sse.model.varModel.datatypes.Enum type = ModelQuery.findEnum(project, TYPE_BINDING_TIME);
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
        IFreezable[] tmp = new IFreezable[freezables.size()];
        freezables.toArray(tmp);
        FreezeBlock result = createFreezeBlock(tmp, project, project);
        project.add(result);
        return result;
    }

}
