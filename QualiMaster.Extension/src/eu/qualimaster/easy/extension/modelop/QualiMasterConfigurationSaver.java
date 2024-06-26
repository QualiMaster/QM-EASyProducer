package eu.qualimaster.easy.extension.modelop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.qualimaster.easy.extension.QmConstants;
import eu.qualimaster.easy.extension.internal.Bundle;
import net.ssehub.easy.basics.modelManagement.IVersionRestriction;
import net.ssehub.easy.basics.modelManagement.ModelManagementException;
import net.ssehub.easy.basics.modelManagement.RestrictionEvaluationException;
import net.ssehub.easy.varModel.confModel.AssignmentState;
import net.ssehub.easy.varModel.confModel.Configuration;
import net.ssehub.easy.varModel.confModel.ConfigurationException;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.cst.CSTSemanticException;
import net.ssehub.easy.varModel.cst.ConstantValue;
import net.ssehub.easy.varModel.cst.ConstraintSyntaxTree;
import net.ssehub.easy.varModel.cst.CopyVisitor;
import net.ssehub.easy.varModel.cst.OCLFeatureCall;
import net.ssehub.easy.varModel.cst.Variable;
import net.ssehub.easy.varModel.model.AbstractVariable;
import net.ssehub.easy.varModel.model.ContainableModelElement;
import net.ssehub.easy.varModel.model.DecisionVariableDeclaration;
import net.ssehub.easy.varModel.model.FreezeBlock;
import net.ssehub.easy.varModel.model.IFreezable;
import net.ssehub.easy.varModel.model.Project;
import net.ssehub.easy.varModel.model.ProjectImport;
import net.ssehub.easy.varModel.model.datatypes.ConstraintType;
import net.ssehub.easy.varModel.model.datatypes.IDatatype;
import net.ssehub.easy.varModel.model.datatypes.IntegerType;
import net.ssehub.easy.varModel.model.datatypes.OclKeyWords;
import net.ssehub.easy.varModel.model.datatypes.Reference;
import net.ssehub.easy.varModel.model.datatypes.Sequence;
import net.ssehub.easy.varModel.model.filter.FilterType;
import net.ssehub.easy.varModel.model.filter.FrozenElementsFinder;
import net.ssehub.easy.varModel.model.values.ContainerValue;
import net.ssehub.easy.varModel.model.values.Value;
import net.ssehub.easy.varModel.model.values.ValueDoesNotMatchTypeException;
import net.ssehub.easy.varModel.model.values.ValueFactory;

/**
 * Specializes the default configuration saver. Create an instance and call {@link #getSavedConfiguration()}
 * for writing via {@link net.ssehub.easy.varModel.persistency.IVMLWriter}.
 * 
 * @author Holger Eichelberger
 */
public class QualiMasterConfigurationSaver extends net.ssehub.easy.varModel.confModel.ConfigurationSaver {


    // not initialized due to method calls in super constructor
    private Map<AbstractVariable, AbstractVariable> varMapping;
    
    /**
     * Creates a configuration saver instance.
     * 
     * @param srcConfiguration The configuration which should be saved.
     * @throws ConfigurationException in case of any configuration errors
     */
    public QualiMasterConfigurationSaver(Configuration srcConfiguration) throws ConfigurationException {
        this(srcConfiguration, true, true);
    }

    /**
     * Creates a configuration saver instance.
     * 
     * @param srcConfiguration The configuration which should be saved.
     * @param ownProject return an own project (<code>true</code>) or add the 
     *   configuration to {@link Configuration#getProject()} (<code>false</code>)
     * @param onlyUserInput Specifies whether only user Input should be stored:
     * <ul>
     *     <li><code>true:</code> Assignments in state {@link AssignmentState#ASSIGNED} and 
     *         {@link AssignmentState#FROZEN} will be saved.</li>
     *     <li><code>false</code>: Assignments in state {@link AssignmentState#ASSIGNED}, 
     *         {@link AssignmentState#FROZEN}, and {@link AssignmentState#DERIVED} will be saved (i.e. also computed 
     *         values).</li>
     * </ul>
     * @throws ConfigurationException in case of any configuration errors
     */
    protected QualiMasterConfigurationSaver(Configuration srcConfiguration, boolean ownProject, boolean onlyUserInput)
        throws ConfigurationException {
        
        super(srcConfiguration, ownProject, onlyUserInput);
    }
    
    /**
     * Initializes attributes lazily due to work in constructor.
     */
    private void lazyInit() {
        if (null == varMapping) {
            varMapping = new HashMap<AbstractVariable, AbstractVariable>();            
        }
    }

    /**
     * Creates the project to store the configuration into.
     * 
     * @param srcConfiguration the configuration to be stored
     * @return the project to store the configuration into
     */
    protected Project createProject(Configuration srcConfiguration) {
        // leave out the EASy-specific _conf postfix
        return new Project(srcConfiguration.getProject().getName());
    }

    @Override
    protected void addLocalVariables(Project destProject, Configuration srcConfiguration) {
        lazyInit();
        Project srcProject = srcConfiguration.getProject();

        // copy the local variables - we need them
        for (int e = 0; e < srcProject.getElementCount(); e++) {
            ContainableModelElement elt = srcProject.getElement(e);
            if (elt instanceof DecisionVariableDeclaration) {
                DecisionVariableDeclaration decl = (DecisionVariableDeclaration) elt;
                DecisionVariableDeclaration destDecl = new DecisionVariableDeclaration(
                    decl.getName(), decl.getType(), destProject);
                varMapping.put(decl, destDecl);
                destProject.add(destDecl);
            }
        }
        
        // this is a bit overkill as *currently* the defaults in Decision variables are constants only
        if (!varMapping.isEmpty()) {
            CopyVisitor vis = new CopyVisitor(varMapping);
            for (Map.Entry<AbstractVariable, AbstractVariable> entry : varMapping.entrySet()) {
                ConstraintSyntaxTree dflt = entry.getKey().getDefaultValue();
                if (null != dflt) {
                    dflt.accept(vis);
                    try {
                        entry.getKey().setValue(vis.getResult());
                    } catch (ValueDoesNotMatchTypeException e) {
                        Bundle.getLogger(QualiMasterConfigurationSaver.class).exception(e);
                    } catch (CSTSemanticException e) {
                        Bundle.getLogger(QualiMasterConfigurationSaver.class).exception(e);
                    }
                    vis.clear();
                }
            }
        }
    }
    
    /**
     * Tries to create an index access for <code>value</code> within <code>containerValue</code>.
     * 
     * @param decl the container variable declaration (must match <code>containerValue</code>)
     * @param containerValue the container value to access
     * @param value the value to search for within <code>containerValue</code>
     * @return the container access expression if the <code>value</code> can be found in <code>containerValue</code>
     *   and the access expression can be created, <b>null</b> else
     */
    private ConstraintSyntaxTree createIndexAccess(DecisionVariableDeclaration decl, Value containerValue, 
        Value value) {
        ConstraintSyntaxTree result = null;
        if (containerValue instanceof ContainerValue) {
            int pos = ((ContainerValue) containerValue).indexOf(value);
            if (pos >= 0) {
                try {
                    Value indexValue = ValueFactory.createValue(IntegerType.TYPE, OclKeyWords.toIvmlIndex(pos));
                    result = new OCLFeatureCall(new Variable(decl), OclKeyWords.INDEX_ACCESS, 
                        new ConstantValue(indexValue));
                } catch (ValueDoesNotMatchTypeException ex) {
                    Bundle.getLogger(QualiMasterConfigurationSaver.class).exception(ex);
                }
            }
        }
        return result;
    }

    /**
     * Searches the given <code>project</code> for a sequence that contains <code>value</code> and returns the sequence
     * index access expression if successful. 
     * 
     * @param project the project to start searching
     * @param value the value to be used as reference
     * @param config the actual configuration
     * @return the access expression or <b>null</b> if none can be created
     */
    private ConstraintSyntaxTree searchSequenceValue(Project project, Value value, Configuration config) {
        return searchSequenceValue(project, value, config, new HashSet<Object>());
    }
    
    /**
     * Searches the given <code>project</code> for a sequence that contains <code>value</code> and returns the sequence
     * index access expression if successful. This method shall not be called directly. Use 
     * {@link #searchSequenceValue(Project, Value, Configuration)} instead.
     * 
     * @param project the project to start searching
     * @param value the value to be used as reference
     * @param config the actual configuration
     * @param done all the projects that have been searched so far
     * @return the access expression or <b>null</b> if none can be created
     */
    private ConstraintSyntaxTree searchSequenceValue(Project project, Value value, Configuration config, 
        Set<Object> done) {
        ConstraintSyntaxTree result = null;
        if (!done.contains(project)) {
            done.add(project);
            IDatatype valueType = value.getType();
            // search for a sequence that has the value type as generic type, try to access the value
            // and if this works, create a index access expression
            for (int e = 0; e < project.getElementCount(); e++) {
                ContainableModelElement elt = project.getElement(e);
                if (elt instanceof DecisionVariableDeclaration) {
                    DecisionVariableDeclaration decl = (DecisionVariableDeclaration) elt;
                    IDatatype declType = decl.getType();
                    if (Sequence.isSequence(declType, valueType)) {
                        IDecisionVariable candidateVar = config.getDecision(decl);
                        result = createIndexAccess(decl, candidateVar.getValue(), value);
                    }
                }
            }
            // if not found, search in related projects
            if (null == result) {
                for (int i = 0; null == result && i < project.getImportsCount(); i++) {
                    Project imp = project.getImport(i).getResolved();
                    if (null != imp) {
                        result = searchSequenceValue(imp, value, config, done);
                    }
                }
            }
        }
        return result;
    }

    @Override
    protected ConstraintSyntaxTree createAssignmentConstraint(Project dstProject, AbstractVariable decl, 
        IDecisionVariable var, Value value) {
        Configuration config = var.getConfiguration();
        AbstractVariable dstDecl = null != varMapping ? varMapping.get(decl) : decl;
        if (null == dstDecl) {
            dstDecl = decl;
        }
        // do some referential integrity ;)
        ConstraintSyntaxTree rightSide = null;
        if (Reference.TYPE.isAssignableFrom(value.getType())) {
            // search for sequence in imported models
            rightSide = searchSequenceValue(dstProject, value, config);
        }
        if (null == rightSide) {
            // fallback
            rightSide = new ConstantValue(toSaveableValue(var, value));
        }
        ConstraintSyntaxTree constraint = new OCLFeatureCall(deriveOperand(dstDecl, var), 
            OclKeyWords.ASSIGNMENT, rightSide);
//        CopyVisitor vis = new CopyVisitor(varMapping);
//        constraint.accept(vis);
//        return vis.getResult();
        return constraint;
    }
    
    @Override
    protected void addImports(Project destProject, Configuration srcConfiguration) {
        Project srcProject = srcConfiguration.getProject();
        for (int i = 0; i < srcProject.getImportsCount(); i++) {
            ProjectImport srcImp = srcProject.getImport(i);
            try {
                IVersionRestriction restrictions = srcImp.copyVersionRestriction(destProject);
                ProjectImport dstImp = new ProjectImport(srcImp.getName(), srcImp.getInterfaceName(), 
                    srcImp.isConflict(), srcImp.isCopied(), restrictions);
                dstImp.setResolved(srcImp.getResolved()); // resolve dstImp
                destProject.addImport(dstImp);
            } catch (ModelManagementException e) {
                Bundle.getLogger(QualiMasterConfigurationSaver.class).exception(e);
            } catch (RestrictionEvaluationException e) {
                Bundle.getLogger(QualiMasterConfigurationSaver.class).exception(e);
            }
        }
    }
    
    @Override
    protected boolean isSavingEnabled(Project destProject, IDecisionVariable var) {
        // avoid that all imported config is saved over and over again
        // role separation
        boolean enabled;
        AbstractVariable decl = var.getDeclaration();
        IDatatype type = decl.getType();
        // QualiMaster convention
        if (ConstraintType.isConstraint(type)) {
            enabled = false;
        } else if (var.getParent() instanceof Configuration) {
            String decisionNamespace = var.getDeclaration().getNameSpace();
            String dstProjectNamespace = destProject.getName();
            enabled = dstProjectNamespace.equals(decisionNamespace);
    
            if (!enabled && dstProjectNamespace.endsWith(QmConstants.CFG_POSTFIX)) {
                String defProjectNamespace = dstProjectNamespace.substring(0, 
                    dstProjectNamespace.length() - QmConstants.CFG_POSTFIX.length());
                enabled = defProjectNamespace.equals(decisionNamespace);
            }
        } else {
            enabled = true;
        }
        return enabled;
    }
    
    @Override
    protected FreezeBlock createFreezeBlock(IFreezable[] freezables, Project parent) {
        FreezeBlock block = BasicIVMLModelOperations.createFreezeBlock(freezables, parent, null);
        if (null == block) {
            block = super.createFreezeBlock(freezables, parent);
        }
        return block;
    }
    
    @Override
    protected void saveFreezeStates(Project confProject) {
        // Find (all) frozen elements
        List<IFreezable> frozenElements = new ArrayList<IFreezable>();
        for (IDecisionVariable decisionVariable : getConfiguration()) {
            if (decisionVariable.getState() == AssignmentState.FROZEN
                && decisionVariable.getDeclaration() instanceof IFreezable) {
                
                frozenElements.add((IFreezable) decisionVariable.getDeclaration());
            }
        }
        
        // Filter elements: Only elements which are frozen in this project
        /* 
         * QualiMaster has cycling imports and saves into a new srcProject, but will also overwrite this.
         * For this reason, the original (srcConfig.getProject()) not the destProject must be used for the
         * FrozenElementsFinder. Thats the only change in this method.
         */
        FrozenElementsFinder finder = new FrozenElementsFinder(getConfiguration().getProject(),
            FilterType.ONLY_IMPORTS);
        List<IFreezable> alreadyFrozenElements = finder.getFrozenElements();
        frozenElements.removeAll(alreadyFrozenElements);
        
        // Freeze elements, which are frozen in this Configuration/Project.
        if (frozenElements.size() > 0) {
            IFreezable[] freezables = new IFreezable[frozenElements.size()];
            frozenElements.toArray(freezables);
            FreezeBlock freeze = createFreezeBlock(freezables, confProject);
            confProject.add(freeze);
        }
    }
}
