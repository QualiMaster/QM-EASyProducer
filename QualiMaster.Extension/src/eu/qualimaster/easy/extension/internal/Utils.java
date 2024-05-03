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

import static eu.qualimaster.easy.extension.QmConstants.ANNOTATION_BINDING_TIME;
import static eu.qualimaster.easy.extension.QmConstants.CONST_BINDING_TIME_COMPILE;
import static eu.qualimaster.easy.extension.QmConstants.CONST_BINDING_TIME_RUNTIME;
import static eu.qualimaster.easy.extension.QmConstants.CONST_BINDING_TIME_RUNTIME_MON;
import static eu.qualimaster.easy.extension.QmConstants.SLOT_FAMILY_MEMBERS;
import static eu.qualimaster.easy.extension.QmConstants.TYPE_BINDING_TIME;

import java.util.ArrayList;
import java.util.List;

import net.ssehub.easy.varModel.confModel.Configuration;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
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
import net.ssehub.easy.varModel.model.datatypes.IDatatype;
import net.ssehub.easy.varModel.model.values.ContainerValue;
import net.ssehub.easy.varModel.model.values.ReferenceValue;
import net.ssehub.easy.varModel.model.values.Value;
import net.ssehub.easy.varModel.model.values.ValueDoesNotMatchTypeException;
import net.ssehub.easy.varModel.model.values.ValueFactory;

/**
 * Utility methods.
 * 
 * @author Holger Eichelberger
 */
public class Utils {

    /**
     * Finds a compound type.
     * 
     * @param project
     *            the project to start searching
     * @param name
     *            the name of the compound
     * @return the compound type or <b>null</b> if none was found
     * @throws ModelQueryException
     *             in case of violated project access restrictions
     */
    public static Compound findCompound(Project project, String name) throws ModelQueryException {
        return (Compound) ModelQuery.findType(project, name, Compound.class);
    }

    /**
     * Finds a named variable and throws an exception if not found.
     * 
     * @param config
     *            the configuration to search within
     * @param type
     *            the type of the variable
     * @param name
     *            the name of the variable
     * @return the variable
     * @throws ModelQueryException
     *             if not found
     */
    public static IDecisionVariable findNamedVariable(Configuration config, IDatatype type, String name)
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
     * @param family
     *            the family
     * @param name
     *            the name of the algorithm
     * @param asReference
     *            return the reference to the algorithm or the algorithm itself
     * @return the algorithm or its reference (depending on <code>asReference</code>)
     * @throws ModelQueryException
     *             if the algorithm cannot be found
     */
    public static IDecisionVariable findAlgorithm(IDecisionVariable family, String name, boolean asReference)
            throws ModelQueryException {
        IDecisionVariable result = null;
        IDecisionVariable members = family.getNestedElement(SLOT_FAMILY_MEMBERS);
        if (null == members) {
            throw new ModelQueryException(
                    "'" + SLOT_FAMILY_MEMBERS + "' not found in variable '" + family.getDeclaration().getName() + "'",
                    ModelQueryException.ACCESS_ERROR);
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
            throw new ModelQueryException(
                    "algorithm '" + name + "' not found in variable '" + family.getDeclaration().getName() + "'",
                    ModelQueryException.ACCESS_ERROR);
        }
        return result;
    }

    /**
     * Creates a freeze block for project. [legacy style, does not add to project]
     * 
     * @param freezables
     *            the freezables
     * @param project
     *            the IVML project to add to (may be <b>null</b> if failed)
     * @param fallbackForType
     *            in case that <code>project</code> is being written the first time, may be <b>null</b>
     * @return the created freeze block
     * @throws CSTSemanticException
     *             in case of CST errors
     * @throws ValueDoesNotMatchTypeException
     *             in case of unmatching values
     * @throws ModelQueryException
     *             in case of model access problems
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
     * @param freezables
     *            the freezables
     * @param project
     *            the IVML project to add to (may be <b>null</b> if failed)
     * @param fallbackForType
     *            in case that <code>project</code> is being written the first time, may be <b>null</b>
     * @return the created freeze block
     * @throws CSTSemanticException
     *             in case of CST errors
     * @throws ValueDoesNotMatchTypeException
     *             in case of unmatching values
     * @throws ModelQueryException
     *             in case of model access problems
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
     * @param project
     *            the IVML project to freeze
     * @return the created freeze block
     * @throws CSTSemanticException
     *             in case of CST errors
     * @throws ValueDoesNotMatchTypeException
     *             in case of unmatching values
     * @throws ModelQueryException
     *             in case of model access problems
     */
    public static FreezeBlock createFreezeBlock(Project project)
            throws CSTSemanticException, ValueDoesNotMatchTypeException, ModelQueryException {
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
     * @param project
     *            the IVML project to add the attribute to
     * @param fallbackForType
     *            in case that <code>project</code> is being written the first time, may be <b>null</b>
     * @throws CSTSemanticException
     *             in case of CST errors
     * @throws ValueDoesNotMatchTypeException
     *             in case of unmatching values
     * @throws ModelQueryException
     *             in case of model access problems
     */
    public static void addRuntimeAttributeToProject(Project project, Project fallbackForType)
            throws CSTSemanticException, ValueDoesNotMatchTypeException, ModelQueryException {
        // attribute BindingTime bindingTime = BindingTime.compile to PriorityPipCfg;
        net.ssehub.easy.varModel.model.datatypes.Enum type = ModelQuery.findEnum(project, TYPE_BINDING_TIME);
        if (null == type && null != fallbackForType) {
            type = ModelQuery.findEnum(fallbackForType, TYPE_BINDING_TIME);
        }
        EnumLiteral literal = type.get(CONST_BINDING_TIME_COMPILE);
        Attribute attr = new Attribute(ANNOTATION_BINDING_TIME, type, project, project);
        attr.setValue(new ConstantValue(ValueFactory.createValue(type, literal)));
        project.add(attr);
    }

    /**
     * Extracts an {@link IDecisionVariable} from the given {@link ReferenceValue}.
     * @param refValue A value pointing to an element of a pipeline.
     * @param config The complete configuration form where to take the {@link IDecisionVariable}.
     * @return The referenced {@link IDecisionVariable} or in case of any errors <code>null</code>.
     */
    public static IDecisionVariable extractVariable(ReferenceValue refValue, Configuration config) {
        IDecisionVariable result = null;
        if (null != refValue.getValue()) {
            result = config.getDecision(refValue.getValue());
        } else {
            Bundle.getLogger(PipelineVisitor.class).error("Expressions are currently not supported for extracting "
                + "IDecisionVariables from a ReferenceValue");
        }

        return result;
    }
    
    /**
     * Extracts all referenced {@link IDecisionVariable}s from a container of {@link ReferenceValue}s.
     * @param refValues A value pointing other declarations, must not use an expression.
     * @param config The complete configuration form where to take the {@link IDecisionVariable}.
     * @return The referenced {@link IDecisionVariable}s or an empty list in case of any errors.
     */
    public static List<IDecisionVariable> extractVariables(ContainerValue refValues, Configuration config) {
        List<IDecisionVariable> result = new ArrayList<IDecisionVariable>();
        for (int i = 0, end = refValues.getElementSize(); i < end; i++) {
            Value nestedValue = refValues.getElement(i);
            if (nestedValue instanceof ReferenceValue) {
                IDecisionVariable referrencedVar = extractVariable((ReferenceValue) nestedValue, config);
                if (null != referrencedVar) {
                    result.add(referrencedVar);
                }
            }
        }
        
        return result;
    }

}
