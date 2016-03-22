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

import de.uni_hildesheim.sse.easy_producer.instantiator.model.rtVil.VariableValueCopier.IFreezeProvider;
import de.uni_hildesheim.sse.easy_producer.instantiator.model.vilTypes.Invisible;
import de.uni_hildesheim.sse.model.confModel.ConfigurationException;
import de.uni_hildesheim.sse.model.confModel.IDecisionVariable;
import de.uni_hildesheim.sse.model.cst.CSTSemanticException;
import de.uni_hildesheim.sse.model.varModel.Attribute;
import de.uni_hildesheim.sse.model.varModel.IvmlKeyWords;
import de.uni_hildesheim.sse.model.varModel.ModelQuery;
import de.uni_hildesheim.sse.model.varModel.ModelQueryException;
import de.uni_hildesheim.sse.model.varModel.Project;
import de.uni_hildesheim.sse.model.varModel.datatypes.Compound;
import de.uni_hildesheim.sse.model.varModel.datatypes.Enum;
import de.uni_hildesheim.sse.model.varModel.datatypes.IDatatype;
import de.uni_hildesheim.sse.model.varModel.datatypes.OclKeyWords;
import de.uni_hildesheim.sse.model.varModel.values.ReferenceValue;
import de.uni_hildesheim.sse.model.varModel.values.Value;
import de.uni_hildesheim.sse.model.varModel.values.ValueDoesNotMatchTypeException;
import eu.qualimaster.common.QMInternal;
import eu.qualimaster.coordination.RuntimeVariableMapping;
import de.uni_hildesheim.sse.easy_producer.instantiator.model.common.VilException;
import de.uni_hildesheim.sse.easy_producer.instantiator.model.rtVil.VariableValueCopier;
import de.uni_hildesheim.sse.easy_producer.instantiator.model.rtVil.VariableValueCopier.CopySpec;
import de.uni_hildesheim.sse.easy_producer.instantiator.model.rtVil.VariableValueCopier.EnumAttributeFreezeProvider;
import de.uni_hildesheim.sse.easy_producer.instantiator.model.rtVil.VariableValueCopier.IAssignmentListener;

import static eu.qualimaster.easy.extension.QmConstants.*;

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
     * @param copyListener the copy listener
     * @throws VilException in case that initialization fails
     */
    @QMInternal
    public static void initializeConfiguration(de.uni_hildesheim.sse.model.confModel.Configuration config, 
        String newVariablePrefix, IAssignmentListener copyListener) throws VilException {
        Project project = config.getProject();
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
            copier.setAssignmentListener(copyListener);
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
        de.uni_hildesheim.sse.model.confModel.Configuration config) throws ModelQueryException {
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

}
