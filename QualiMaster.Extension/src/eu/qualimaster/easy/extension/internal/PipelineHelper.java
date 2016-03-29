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
package eu.qualimaster.easy.extension.internal;

import java.util.HashSet;
import java.util.Iterator;

import de.uni_hildesheim.sse.easy_producer.instantiator.model.common.VilException;
import de.uni_hildesheim.sse.easy_producer.instantiator.model.vilTypes.Collection;
import de.uni_hildesheim.sse.easy_producer.instantiator.model.vilTypes.IVilType;
import de.uni_hildesheim.sse.easy_producer.instantiator.model.vilTypes.Instantiator;
import de.uni_hildesheim.sse.easy_producer.instantiator.model.vilTypes.Invisible;
import de.uni_hildesheim.sse.easy_producer.instantiator.model.vilTypes.OperationMeta;
import de.uni_hildesheim.sse.easy_producer.instantiator.model.vilTypes.Set;
import de.uni_hildesheim.sse.easy_producer.instantiator.model.vilTypes.SetSet;
import de.uni_hildesheim.sse.easy_producer.instantiator.model.vilTypes.configuration.Configuration;
import de.uni_hildesheim.sse.easy_producer.instantiator.model.vilTypes.configuration.DecisionVariable;
import eu.qualimaster.adaptation.events.ViolatingClause;
import eu.qualimaster.common.QMInternal;
import eu.qualimaster.easy.extension.QmConstants;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.model.AbstractVariable;
import net.ssehub.easy.varModel.model.ContainableModelElement;
import net.ssehub.easy.varModel.model.DecisionVariableDeclaration;
import net.ssehub.easy.varModel.model.IModelElement;
import net.ssehub.easy.varModel.model.ModelQuery;
import net.ssehub.easy.varModel.model.ModelQueryException;
import net.ssehub.easy.varModel.model.Project;
import net.ssehub.easy.varModel.model.datatypes.IDatatype;
import net.ssehub.easy.varModel.model.datatypes.IResolutionScope;

/**
 * Some pipeline helper functions mapped into rt-VIL.
 * 
 * @author Holger Eichelberger
 */
@Instantiator("obtainPipeline")
public class PipelineHelper implements IVilType {

    /**
     * Returns the pipeline related to the variable given by its instance name.
     * 
     * @param configuration the configuration as basis for the lookup
     * @param variableName the instance name of the variable
     * @return the pipelines (may be empty)
     * @throws VilException in case that accessing a pipeline fails
     */
    public static DecisionVariable obtainPipeline(Configuration configuration, String variableName) 
        throws VilException {
        DecisionVariable result = null;
        if (null != variableName) {
            DecisionVariable var = getByName(configuration, variableName);
            if (null != var) {
                IDecisionVariable decVar = var.getVariable();
                // determine top-level
                while (decVar.getParent() instanceof IDecisionVariable) {
                    decVar = (IDecisionVariable) decVar.getParent();
                }
                try {
                    IDecisionVariable pipVar = obtainPipeline(configuration.getConfiguration(), decVar);
                    if (null != pipVar) { // top-level var
                        // better: config.findVariable                        
                        result = configuration.getByName(
                            net.ssehub.easy.varModel.confModel.Configuration.getInstanceName(pipVar));
                    }
                } catch (ModelQueryException e) {
                    throw new VilException(e, VilException.ID_EXECUTION_ERROR);
                }
            }
        }
        return result;
    }

    /**
     * Finds a variable via its qualified instance name, i.e., the qualified name including nested variable
     * names.
     * 
     * @param configuration the configuration to search on
     * @param variableName the name of the variable
     * @return the related decision variable (may be <b>null</b> if none was found)
     */
    static DecisionVariable getByName(Configuration configuration, String variableName) {
        DecisionVariable result = configuration.getByName(variableName);
        if (null == result && variableName.length() > 0) {
            int pos = variableName.lastIndexOf("::");
            if (pos > 0) {
                String prefix = variableName.substring(0, pos);
                String postfix = variableName.substring(pos + 2);
                DecisionVariable tmp = getByName(configuration, prefix);
                if (null != tmp) {
                    result = tmp.getByName(postfix);
                }
            } else {
                variableName = "";
            }
        }
        return result;
    }
    
    /**
     * Returns the pipeline decision for the given pipeline <code>element</code>.
     * 
     * @param configuration the configuration for the lookup
     * @param element the pipeline element (may be <b>null</b>, leads to <b>null</b>)
     * @return the pipeline, may be <b>null</b> if the pipeline was not found
     * @throws ModelQueryException if accessing type information fails
     */
    @Invisible
    public static IDecisionVariable obtainPipeline(net.ssehub.easy.varModel.confModel.Configuration configuration, 
        IDecisionVariable element) throws ModelQueryException {
        IDecisionVariable result = null;
        if (null != element) {
            AbstractVariable elementDecl = element.getDeclaration();
            IDatatype elementType = elementDecl.getType();
            IModelElement par = elementDecl.getTopLevelParent();
            if (par instanceof Project) {
                Project prj = (Project) par;
                IDatatype pipelineElementType = ModelQuery.findType(prj, QmConstants.TYPE_PIPELINE_ELEMENT, null);
                if (null != pipelineElementType && pipelineElementType.isAssignableFrom(elementType)) {
                    IDatatype pipelineType = ModelQuery.findType(prj, QmConstants.TYPE_PIPELINE, null);
                    if (null != pipelineType) {
                        result = searchScope(configuration, prj, pipelineType);
                    }
                }
            }
        }
        return result;
    }
    
    /**
     * Searches a given <code>scope</code> for a variable declaration of <code>type</code> and returns for the first 
     * matched decision variable the related decision.
     *  
     * @param configuration the configuration to retrieve the decision from
     * @param scope the search scope for the declarations
     * @param type the type to search for
     * @return the decision or <b>null</b> if not found
     */
    private static IDecisionVariable searchScope(net.ssehub.easy.varModel.confModel.Configuration configuration, 
        IResolutionScope scope, IDatatype type) {
        IDecisionVariable result = null;
        DecisionVariableDeclaration decl = ModelQuery.findDeclaration(scope, 
            new ModelQuery.FirstDeclTypeSelector(type));
        if (null != decl) {
            result = configuration.getConfiguration().getDecision(decl); 
        }
        return result;
    }
    
    /**
     * Returns all pipelines mentioned in <code>clauses</code>.
     * 
     * @param configuration the configuration as basis for the lookup
     * @param clauses the clauses to look into
     * @return the pipelines (may be empty)
     * @throws VilException in case that accessing a pipeline fails
     */
    @OperationMeta(returnGenerics = DecisionVariable.class)
    public static Set<DecisionVariable> obtainPipeline(Configuration configuration, Collection<?> clauses) 
        throws VilException {
        Iterator<?> iter = null == clauses ? null : clauses.iterator();
        return obtainPipeline(configuration, iter);
    }

    /**
     * Returns all pipelines mentioned in <code>clauses</code>.
     * 
     * @param configuration the configuration as basis for the lookup
     * @param clauses the clauses to look into
     * @return the pipelines (may be empty)
     * @throws VilException in case that accessing a pipeline fails
     */
    @OperationMeta(returnGenerics = DecisionVariable.class)
    public static Set<DecisionVariable> obtainPipeline(Configuration configuration, Iterator<?> clauses) 
        throws VilException {
        java.util.Set<String> done = new HashSet<String>();
        java.util.Set<DecisionVariable> result = new HashSet<DecisionVariable>();
        if (null != clauses) {
            while (clauses.hasNext()) {
                Object cl = clauses.next();
                if (cl instanceof ViolatingClause) {
                    String variableName = ((ViolatingClause) cl).getVariable();
                    if (!done.contains(variableName)) {
                        done.add(variableName);
                        DecisionVariable var = obtainPipeline(configuration, variableName);
                        if (null != var) {
                            result.add(var);
                        }
                    }
                }
            }
        }
        return new SetSet<DecisionVariable>(result, DecisionVariable.class);
    }
    
    /**
     * Returns a pipeline from the configuration.
     * 
     * @param config the configuration
     * @param name the name of the pipeline
     * @return the pipeline or <b>null</b> if it does not exist
     */
    @QMInternal
    public static IDecisionVariable obtainPipelineByName(net.ssehub.easy.varModel.confModel.Configuration config, 
        String name) {
        IDecisionVariable result = null;
        try {
            AbstractVariable pips = ModelQuery.findVariable(config.getProject(), 
                QmConstants.VAR_PIPELINES_PIPELINES, null);
            IDecisionVariable pipsVar = config.getDecision(pips);
            if (null != pipsVar) {
                for (int n = 0; null == result && n < pipsVar.getNestedElementsCount(); n++) {
                    IDecisionVariable pip = net.ssehub.easy.varModel.confModel.Configuration.dereference(
                        pipsVar.getNestedElement(n));
                    if (VariableHelper.hasName(pip, name)) {
                        result = pip;
                    }
                }
            }
        } catch (ModelQueryException e) {
                // -> result = null
        }
        return result;
    }

    /**
     * Obtains a family of given <code>name</code> from <code>pipeline</code>.
     * 
     * @param pipeline the pipeline to obtain the family from (may be <b>null</b>)
     * @param name the name of the family
     * @return the family if it exists, <b>null</b> if there is no family
     */
    @QMInternal
    public static IDecisionVariable obtainFamilyByName(IDecisionVariable pipeline, String name) {
        IDecisionVariable result = null;
        if (null != pipeline) {
            try {
                IDatatype type = ModelQuery.findType(pipeline.getConfiguration().getProject(), 
                    QmConstants.TYPE_FAMILYELEMENT, null);
                result = obtainPipelineElementByName(pipeline, type, name);
            } catch (ModelQueryException e) {
                // result -> null
            }
        }
        return result;
    }
    
    /**
     * Obtains a pipeline element.
     * 
     * @param pipeline the pipeline
     * @param type the type of the element (may be <b>null</b>)
     * @param name the name of the element
     * @return the element or <b>null</b> if it does not exist for some reason
     */
    @QMInternal
    public static IDecisionVariable obtainPipelineElementByName(IDecisionVariable pipeline, IDatatype type, 
        String name) {
        IDecisionVariable result = null;
        net.ssehub.easy.varModel.confModel.Configuration config = null;
        Project project = null;
        if (null != pipeline) {
            config = pipeline.getConfiguration();
            AbstractVariable var = pipeline.getDeclaration();
            project = var.getProject();
        }
        if (null != project) { // implies config != null
            for (int e = 0, n = project.getElementCount(); null == result && e < n; e++) {
                ContainableModelElement elt = project.getElement(e);
                if (elt instanceof DecisionVariableDeclaration) {
                    DecisionVariableDeclaration decl = (DecisionVariableDeclaration) elt;
                    if (null == type || type.isAssignableFrom(decl.getType())) {
                        IDecisionVariable decVar = config.getDecision(decl);
                        if (VariableHelper.hasName(decVar, name) || decVar.getDeclaration().getName().equals(name)) {
                            result = decVar;
                        }
                    }
                }
            }
        }
        return result;
    }
    
    /**
     * Obtains an algorithm form a family.
     * 
     * @param family the family (may be <b>null</b>)
     * @param name the name of the algorithm
     * @return the algorithm or <b>null</b> if none exists
     */
    @QMInternal
    public static IDecisionVariable obtainAlgorithmFromFamilyByName(IDecisionVariable family, String name) {
        IDecisionVariable result = null;
        if (null != family) {
            IDecisionVariable avail = family.getNestedElement(QmConstants.SLOT_FAMILYELEMENT_AVAILABLE);
            if (null != avail) {
                for (int n = 0, c = avail.getNestedElementsCount(); null == result && n < c; n++) {
                    IDecisionVariable alg = net.ssehub.easy.varModel.confModel.Configuration.dereference(
                        avail.getNestedElement(n));
                    if (VariableHelper.hasName(alg, name)) {
                        result = alg;
                    }
                }
            }
        }
        return result;
    }

}
