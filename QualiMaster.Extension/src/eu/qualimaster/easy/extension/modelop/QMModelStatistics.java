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
package eu.qualimaster.easy.extension.modelop;

import net.ssehub.easy.varModel.model.AbstractProjectVisitor;
import net.ssehub.easy.varModel.model.Attribute;
import net.ssehub.easy.varModel.model.AttributeAssignment;
import net.ssehub.easy.varModel.model.Comment;
import net.ssehub.easy.varModel.model.CompoundAccessStatement;
import net.ssehub.easy.varModel.model.Constraint;
import net.ssehub.easy.varModel.model.DecisionVariableDeclaration;
import net.ssehub.easy.varModel.model.FreezeBlock;
import net.ssehub.easy.varModel.model.OperationDefinition;
import net.ssehub.easy.varModel.model.PartialEvaluationBlock;
import net.ssehub.easy.varModel.model.Project;
import net.ssehub.easy.varModel.model.ProjectInterface;
import net.ssehub.easy.varModel.model.datatypes.Compound;
import net.ssehub.easy.varModel.model.datatypes.DerivedDatatype;
import net.ssehub.easy.varModel.model.datatypes.Enum;
import net.ssehub.easy.varModel.model.datatypes.EnumLiteral;
import net.ssehub.easy.varModel.model.datatypes.OrderedEnum;
import net.ssehub.easy.varModel.model.datatypes.Reference;
import net.ssehub.easy.varModel.model.datatypes.Sequence;
import net.ssehub.easy.varModel.model.datatypes.Set;
import net.ssehub.easy.varModel.model.filter.FilterType;

/**
 * Part of the {@link QMConfigStatisticsVisitor} to collect static information of the QualiMaster (Meta-) Model.
 * @author El-Sharkawy
 *
 */
class QMModelStatistics extends AbstractProjectVisitor {
    
    private int nConstraints;
    private int nOperations;

    /**
     * Sole constructor.
     * @param originProject The project where the visiting shall start
     */
    protected QMModelStatistics(Project originProject) {
        super(originProject, FilterType.ALL);
    }

    /**
     * Returns the number of constraints nested inside projects (not part of compounds).
     * @return Will be &ge; 0.
     */
    int noOfConstraints() {
        return nConstraints;
    }

    /**
     * Returns the number of user defined operations.
     * @return Will be &ge; 0.
     */
    int noOfOperations() {
        return nOperations;
    }
    
    @Override
    public void visitDecisionVariableDeclaration(DecisionVariableDeclaration decl) {
        // Not needed
    }

    @Override
    public void visitAttribute(Attribute attribute) {
        // Not needed
    }

    @Override
    public void visitConstraint(Constraint constraint) {
        nConstraints++;
    }

    @Override
    public void visitFreezeBlock(FreezeBlock freeze) {
        // Not needed
    }

    @Override
    public void visitOperationDefinition(OperationDefinition opdef) {
        nOperations++;
    }

    @Override
    public void visitPartialEvaluationBlock(PartialEvaluationBlock block) {
        // Not needed
    }

    @Override
    public void visitProjectInterface(ProjectInterface iface) {
        // Not needed
    }

    @Override
    public void visitComment(Comment comment) {
        // Not needed
    }

    @Override
    public void visitAttributeAssignment(AttributeAssignment assignment) {
        // Not needed
    }

    @Override
    public void visitCompoundAccessStatement(CompoundAccessStatement access) {
        // Not needed
    }

    @Override
    public void visitEnum(Enum eenum) {
        // Not needed
    }

    @Override
    public void visitOrderedEnum(OrderedEnum eenum) {
        // Not needed
    }

    @Override
    public void visitCompound(Compound compound) {
        // Not needed
    }

    @Override
    public void visitDerivedDatatype(DerivedDatatype datatype) {
        // Not needed
    }

    @Override
    public void visitEnumLiteral(EnumLiteral literal) {
        // Not needed
    }

    @Override
    public void visitReference(Reference reference) {
        // Not needed
    }

    @Override
    public void visitSequence(Sequence sequence) {
        // Not needed 
    }

    @Override
    public void visitSet(Set set) {
        // Not needed
    }
}
