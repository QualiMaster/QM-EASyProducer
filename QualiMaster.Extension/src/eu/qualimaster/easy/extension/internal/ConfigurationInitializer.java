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
import eu.qualimaster.coordination.RuntimeVariableMapping;
import net.ssehub.easy.instantiation.core.model.common.VilException;
import net.ssehub.easy.instantiation.core.model.vilTypes.Invisible;
import net.ssehub.easy.instantiation.rt.core.model.rtVil.VariableValueCopier;
import net.ssehub.easy.instantiation.rt.core.model.rtVil.VariableValueCopier.CopySpec;
import net.ssehub.easy.instantiation.rt.core.model.rtVil.VariableValueCopier.EnumAttributeFreezeProvider;
import net.ssehub.easy.instantiation.rt.core.model.rtVil.VariableValueCopier.IFreezeProvider;
import net.ssehub.easy.varModel.confModel.ConfigurationException;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.cst.CSTSemanticException;
import net.ssehub.easy.varModel.model.Attribute;
import net.ssehub.easy.varModel.model.IvmlKeyWords;
import net.ssehub.easy.varModel.model.ModelQuery;
import net.ssehub.easy.varModel.model.ModelQueryException;
import net.ssehub.easy.varModel.model.Project;
import net.ssehub.easy.varModel.model.datatypes.Compound;
import net.ssehub.easy.varModel.model.datatypes.Enum;
import net.ssehub.easy.varModel.model.datatypes.IDatatype;
import net.ssehub.easy.varModel.model.datatypes.OclKeyWords;
import net.ssehub.easy.varModel.model.values.ReferenceValue;
import net.ssehub.easy.varModel.model.values.Value;
import net.ssehub.easy.varModel.model.values.ValueDoesNotMatchTypeException;

import static eu.qualimaster.easy.extension.QmConstants.*;
import static eu.qualimaster.easy.extension.internal.Utils.*;

import java.util.Iterator;

/**
 * Configuration initialization utility functions. [map into VIL]
 * 
 * @author Holger Eichelberger
 */
public class ConfigurationInitializer {

    /**
     * Initializes a QM model with runtime instances. [preliminary, separate concerns]
     * 
     * @param config the configuration
     * @param newVariablePrefix the prefix for the new variables
     * @throws VilException in case that initialization fails
     */
    @QMInternal
    public static void initializeConfiguration(net.ssehub.easy.varModel.confModel.Configuration config, 
        String newVariablePrefix) throws VilException {
        Project project = config.getProject();
        if (null == project) {
            throw new VilException("no project available - syntax/parsing error?", VilException.ID_INVALID);
        }
        try {
            // did not want to introduce an IVML copy operation by now
            Enum bindingTime = (Enum) ModelQuery.findType(project, TYPE_BINDING_TIME, Enum.class);
            // take any one - just used for type and name
            Attribute annotation = (Attribute) ModelQuery.findElementByName(project, ANNOTATION_BINDING_TIME, 
                Attribute.class);
            IFreezeProvider freezeProvider = new EnumAttributeFreezeProvider("b", annotation, 
                OclKeyWords.GREATER_EQUALS, bindingTime.getLiteral(1));

            Compound sourceType = findCompound(project, TYPE_SOURCE);
            CopySpec specSource = new CopySpec(sourceType, SLOT_SOURCE_SOURCE, freezeProvider, SLOT_SOURCE_AVAILABLE, 
                SLOT_SOURCE_ACTUAL);
            Compound familyElementType = findCompound(project, TYPE_FAMILYELEMENT);
            CopySpec specFamily = new CopySpec(familyElementType, SLOT_FAMILYELEMENT_FAMILY 
                + IvmlKeyWords.COMPOUND_ACCESS + SLOT_FAMILY_MEMBERS, freezeProvider, 
                SLOT_FAMILYELEMENT_AVAILABLE, SLOT_FAMILYELEMENT_ACTUAL);
            Compound sinkType = findCompound(project, TYPE_SINK);
            CopySpec specSink = new CopySpec(sinkType, SLOT_SINK_SINK, freezeProvider, SLOT_SINK_AVAILABLE, 
                SLOT_SINK_ACTUAL);
            VariableValueCopier copier = new VariableValueCopier(newVariablePrefix, specSource, specFamily, specSink);
            copier.process(config);
        } catch (ConfigurationException e1) {
            throw new VilException(e1, VilException.ID_RUNTIME);
        } catch (ValueDoesNotMatchTypeException e2) {
            throw new VilException(e2, VilException.ID_RUNTIME);
        } catch (ModelQueryException e3) {
            throw new VilException(e3, VilException.ID_RUNTIME);
        } catch (CSTSemanticException e4) {
            throw new VilException(e4, VilException.ID_RUNTIME);
        }
    }
    
    /**
     * Creates a runtime variable mapping for <code>configuration</code>.
     * 
     * @param config the configuration
     * @return the runtime variable mapping
     * @throws ModelQueryException in case of problems accessing model elements
     */
    @Invisible
    public static RuntimeVariableMapping createVariableMapping(
        net.ssehub.easy.varModel.confModel.Configuration config) throws ModelQueryException {
        Project project = config.getProject();
        Compound sourceType = findCompound(project, TYPE_SOURCE);
        Compound familyElementType = findCompound(project, TYPE_FAMILYELEMENT);
        Compound sinkType = findCompound(project, TYPE_SINK);
        RuntimeVariableMapping result = new RuntimeVariableMapping();
        Iterator<IDecisionVariable> iter = config.iterator();
        while (iter.hasNext()) {
            IDecisionVariable var = iter.next();
            IDatatype type = var.getDeclaration().getType();
            if (sourceType.isAssignableFrom(type) || sinkType.isAssignableFrom(type)
                || familyElementType.isAssignableFrom(type)) {
                addVariableMapping(var, SLOT_AVAILABLE, result);
            }
        }
        return result;
    }
    
    /**
     * Adds the variable mapping for <code>var</code> on field <code>fieldName</code> to <code>result</code>.
     * 
     * @param var the variable to analyze
     * @param fieldName the field name to analyze
     * @param result the mapping to be modified as a side effect
     */
    private static void addVariableMapping(IDecisionVariable var,  
        String fieldName, RuntimeVariableMapping result) {
        IDecisionVariable nested = VariableValueCopier.findVariable(var, fieldName);
        if (null != nested) {
            for (int n = 0; n < nested.getNestedElementsCount(); n++) {
                IDecisionVariable tmp = nested.getNestedElement(n);
                Value tmpValue = tmp.getValue();
                if (tmpValue instanceof ReferenceValue) {
                    ReferenceValue rValue = (ReferenceValue) tmpValue;
                    IDecisionVariable referenced = var.getConfiguration().getDecision(rValue.getValue());
                    if (null != referenced) {
                        result.addReferencedBy(referenced, var);
                    }
                }
            }
        }
    }

}
