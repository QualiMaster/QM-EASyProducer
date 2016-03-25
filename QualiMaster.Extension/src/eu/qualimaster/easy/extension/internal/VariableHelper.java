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

import java.util.Iterator;

import de.uni_hildesheim.sse.model.confModel.Configuration;
import de.uni_hildesheim.sse.model.confModel.IDecisionVariable;
import de.uni_hildesheim.sse.model.varModel.datatypes.IDatatype;
import de.uni_hildesheim.sse.model.varModel.values.BooleanValue;
import de.uni_hildesheim.sse.model.varModel.values.StringValue;
import de.uni_hildesheim.sse.model.varModel.values.Value;
import eu.qualimaster.easy.extension.QmConstants;

/**
 * Some utility functions for IVML variables.
 * 
 * @author Holger Eichelberger
 */
public class VariableHelper {

    /**
     * Returns the logical name of <code>var</code> from slot {@link QmConstants#SLOT_NAME}.
     * 
     * @param var the variable (may be <b>null</b>)
     * @return the name of <code>var</code>, <b>null</b> if there is none
     */
    public static String getName(IDecisionVariable var) {
        String result = null;
        if (null != var) {
            IDecisionVariable nameVar = var.getNestedElement(QmConstants.SLOT_NAME);
            if (null != nameVar) {
                Value nameValue = nameVar.getValue();
                if (nameValue instanceof StringValue) {
                    result = ((StringValue) nameValue).getValue();
                }
            }
        }
        return result;
    }

    /**
     * Returns whether <code>var</code> has a slot {@link QmConstants#SLOT_NAME} and the 
     * value is a string equals to <code>name</code>.
     * 
     * @param var the variable (may be <b>null</b>)
     * @param name the name to look for
     * @return <code>true</code> if <code>var</code> has the given name, <code>false</code> (in all cases)
     */
    public static boolean hasName(IDecisionVariable var, String name) {
        boolean result = false;
        String varName = getName(var);
        if (null != varName) {
            result = varName.equals(name);
        }
        return result;
    }

    /**
     * Returns the value of a boolean compound slot.
     * 
     * @param var the variable to look into (may be <b>null</b>)
     * @param name the name of the slot
     * @return the boolean value of the slot, <b>null</b> if there is no variable, no slot or no boolean value in 
     *     the slot 
     */
    public static final Boolean getBoolean(IDecisionVariable var, String name) {
        Boolean result = null;
        if (null != var) {
            IDecisionVariable nested = var.getNestedElement(name);
            if (null != nested) {
                Value value = nested.getValue();
                if (value instanceof BooleanValue) {
                    result = ((BooleanValue) value).getValue();
                }
            }
        }
        return result;
    }
    
    /**
     * Finds a named variable of given <code>type</code> in <code>config</code>.
     * 
     * @param config the configuration
     * @param type the type to search for (ignored if <b>null</b>)
     * @param name the (logical) name
     * @return the decision variable
     */
    public static final IDecisionVariable findNamedVariable(Configuration config, IDatatype type, String name) {
        IDecisionVariable result = null;
        Iterator<IDecisionVariable> iter = config.iterator();
        while (null == result && iter.hasNext()) {
            IDecisionVariable var = Configuration.dereference(iter.next());
            if ((null == type || type.isAssignableFrom(var.getDeclaration().getType())) && hasName(var, name)) {
                result = var;
            }
        }
        return result;
    }
    
    /**
     * Finds a named variable of given <code>type</code> in <code>variable</code>.
     * 
     * @param variable the variable to search for
     * @param type the type to search for (ignored if <b>null</b>)
     * @param name the (logical) name
     * @return the decision variable
     */
    public static final IDecisionVariable findNamedVariable(IDecisionVariable variable, IDatatype type, String name) {
        IDecisionVariable result = null;
        for (int n = 0; null == result && n < variable.getNestedElementsCount(); n++) {
            IDecisionVariable var = Configuration.dereference(variable.getNestedElement(n));
            if ((null == type || type.isAssignableFrom(var.getDeclaration().getType())) && hasName(var, name)) {
                result = var;
            }
        }
        return result;
    }
    
    

}
