/*
 * Copyright 2016 University of Hildesheim, Software Systems Engineering
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
package qmextension.ui.handlers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import eu.qualimaster.easy.extension.ProjectFreezeModifier;
import eu.qualimaster.easy.extension.QmConstants;
import net.ssehub.easy.producer.core.mgmt.PLPInfo;
import net.ssehub.easy.producer.core.mgmt.SPLsManager;
import net.ssehub.easy.producer.eclipse.persistency.ResourcesMgmt;
import net.ssehub.easy.producer.eclipse.persistency.eclipse.EASyNature;
import net.ssehub.easy.producer.eclipse.persistency.eclipse.NatureUtils;
import net.ssehub.easy.varModel.model.AbstractVariable;
import net.ssehub.easy.varModel.model.DecisionVariableDeclaration;
import net.ssehub.easy.varModel.model.Project;
import net.ssehub.easy.varModel.model.filter.DeclarationFinder;
import net.ssehub.easy.varModel.model.filter.DeclarationFinder.VisibilityType;
import net.ssehub.easy.varModel.model.filter.FilterType;
import net.ssehub.easy.varModel.model.rewrite.ProjectRewriteVisitor;

/**
 * Freezes the QM model as it is done in the QM tool.
 * @author El-Sharkawy
 */
public class FreezeHandler implements IObjectActionDelegate {
	
    private Shell shell;
    private ISelection selection;

    @Override
    public void run(IAction action) {
        if (selection instanceof IStructuredSelection) {
            StringBuilder errors = new StringBuilder();
            for (Iterator<?> it = ((IStructuredSelection) selection).iterator(); it.hasNext();) {
                Object element = it.next();
                IProject project = null;
                if (element instanceof IProject) {
                    project = (IProject) element;
                } else if (element instanceof IAdaptable) {
                    project = (IProject) ((IAdaptable) element).getAdapter(IProject.class);
                }
                try {
                    if (project != null && NatureUtils.hasNature(project, EASyNature.NATURE_ID)) {
                        String id = ResourcesMgmt.INSTANCE.getIDfromResource(project);
                        PLPInfo plp = SPLsManager.INSTANCE.getPLP(id);
                        Project mainProject = plp.getProject();
                        
                        ProjectRewriteVisitor rewriter = new ProjectRewriteVisitor(mainProject, FilterType.ALL);
                        DeclarationFinder finder = new DeclarationFinder(mainProject, FilterType.ALL, null);
                        List<DecisionVariableDeclaration> allDeclarations = new ArrayList<DecisionVariableDeclaration>();
                        List<AbstractVariable> tmpList = finder.getVariableDeclarations(VisibilityType.ALL);
                        for (int i = 0, end = tmpList.size(); i < end; i++) {
                            AbstractVariable declaration = tmpList.get(i);
                            if (declaration instanceof DecisionVariableDeclaration
                                && !(declaration.getNameSpace().equals(QmConstants.PROJECT_OBSERVABLESCFG)
                                && declaration.getName().equals("qualityParameters"))) {
                                
                                allDeclarations.add((DecisionVariableDeclaration) declaration);
                            }
                        };
                        ProjectFreezeModifier freezer = new ProjectFreezeModifier(mainProject, allDeclarations);
                        rewriter.addProjectModifier(freezer);
                        // Freezes all projects as a side effect, but won't save them
                        mainProject.accept(rewriter);
                    }
                } catch (CoreException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if (errors.length() > 0) {
                MessageDialog.openError(shell, "Error while modifying natures", errors.toString());
            }
        }
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        this.selection = selection;
    }

    @Override
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        shell = targetPart.getSite().getShell();
    }
}
