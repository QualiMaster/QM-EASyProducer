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

import eu.qualimaster.easy.extension.QmConstants;
import net.ssehub.easy.varModel.confModel.AbstractConfigurationStatisticsVisitor;

/**
 * Statistical information of the QualiMaster (Meta-) Model.
 * @author El-Sharkawy
 *
 */
public class ModelStatistics extends AbstractConfigurationStatisticsVisitor.ConfigStatistics {
    
    private int nSWAlgorithms = 0;
    private int nHWAlgorithms = 0;
    private int nSPAlgorithms = 0;

    // General Structure
    private int nFamilies = 0;
    private int nGeneralMachines = 0;
    
    // Pipelines
    private int nPipelines = 0;
    private int nSubPipelines = 0;
    
    // Pipeline Elements
    private int nSources = 0;
    private int nFamilyElements = 0;
    private int nDMElements = 0;
    private int nReplaySinks = 0;
    private int nSinks = 0;
    private int nFlows = 0;
    
    // Static information
    private int nConstraints;
    private int nOperations;
    private int nTopLevelDeclarations;
    private int nNestedDeclarations;
    private int nTopLevelAnnotations;
    private int nNestedAnnotations;
    
    /**
     * Constructor shall only be called by the {@link QMConfigStatisticsVisitor}.
     */
    ModelStatistics() {}
    
    /**
     * Counts a new variable instance.
     * @param typeName The kind of model element to count, e.g., ALgorithm or Family. Must not be <tt>null</tt>.
     */
    void incInstance(String typeName) {
        switch (typeName) {
        // Algorithms
        case QmConstants.TYPE_SOFTWARE_ALGORITHM:
            nSWAlgorithms++;
            break;
        case QmConstants.TYPE_HARDWARE_ALGORITHM:
            nHWAlgorithms++;
            break;
        case QmConstants.TYPE_SUBPIPELINE_ALGORITHM:
            nSPAlgorithms++;
            break;
        // General Structure
        case QmConstants.TYPE_FAMILY:
            nFamilies++;
            break;
        case QmConstants.TYPE_MACHINE:
            nGeneralMachines++;
            break;
        // Pipelines
        case QmConstants.TYPE_PIPELINE:
            nPipelines++;
            break;
        case QmConstants.TYPE_SUBPIPELINE:
            nSubPipelines++;
            break;
        // Pipeline Elements
        case QmConstants.TYPE_SOURCE:
            nSources++;
            break;
        case QmConstants.TYPE_FAMILYELEMENT:
            nFamilyElements++;
            break;
        case QmConstants.TYPE_DATAMANAGEMENTELEMENT:
            nDMElements++;
            break;
        case QmConstants.TYPE_REPLAYSINK:
            nReplaySinks++;
            break;
        case QmConstants.TYPE_SINK:
            nSinks++;
            break;
        case QmConstants.TYPE_FLOW:
            nFlows++;
            break;
        default:
            // No handling needed
            break;
        }
    }
    
    /**
     * Sets the number of static constraints (nested directly in {@link net.ssehub.easy.varModel.model.Project}s).
     * @param nConstraints Should be &ge; 0.
     */
    void setStaticConstraints(int nConstraints) {
        this.nConstraints = nConstraints;
    }
    
    /**
     * Sets the number of user defined operations.
     * @param nOperations Should be &ge; 0.
     */
    void setOperations(int nOperations) {
        this.nOperations = nOperations;
    }
    
    /**
     * Sets the number of static declarations (nested directly in {@link net.ssehub.easy.varModel.model.Project}s).
     * @param nTopLevelDeclarations Should be &ge; 0.
     */
    void setTopLevelDeclarations(int nTopLevelDeclarations) {
        this.nTopLevelDeclarations = nTopLevelDeclarations;
    }
    
    /**
     * Sets the number of static declarations (nested in Compounds).
     * @param nNestedDeclarations Should be &ge; 0.
     */
    void setNestedDeclarations(int nNestedDeclarations) {
        this.nNestedDeclarations = nNestedDeclarations;
    }
    
    /**
     * Sets the number of static annotations (nested directly in {@link net.ssehub.easy.varModel.model.Project}s).
     * @param nTopLevelAnnotations Should be &ge; 0.
     */
    void setTopLevelAnnotations(int nTopLevelAnnotations) {
        this.nTopLevelAnnotations = nTopLevelAnnotations;
    }
    
    /**
     * Sets the number of static annotations (nested in Compounds).
     * @param nNestedAnnotations Should be &ge; 0.
     */
    void setNestedAnnotations(int nNestedAnnotations) {
        this.nNestedAnnotations = nNestedAnnotations;
    }

    /**
     * Returns the number of software algorithm instances.
     * @return Will be &ge; 0.
     */
    public int noOfSWAlgorithms() {
        return nSWAlgorithms;
    }
    
    /**
     * Returns the number of hardware algorithm instances.
     * @return Will be &ge; 0.
     */
    public int noOfHWAlgorithms() {
        return nHWAlgorithms;
    }
    /**
     * Returns the number of sub pipeline algorithm instances.
     * @return Will be &ge; 0.
     */
    public int noOfSPAlgorithms() {
        return nSPAlgorithms;
    }

    /**
     * Returns the number of family instances.
     * @return Will be &ge; 0.
     */
    public int noOfFamilies() {
        return nFamilies;
    }

    /**
     * Returns the number of family instances.
     * @return Will be &ge; 0.
     */
    /**
     * Returns the number of general purpose machine instances.
     * @return Will be &ge; 0.
     */
    public int noOfGeneralMachines() {
        return nGeneralMachines;
    }

    /**
     * Returns the number of pipeline (without sub pipelines) instances.
     * @return Will be &ge; 0.
     */
    public int noOfPipelines() {
        return nPipelines;
    }

    /**
     * Returns the number of subpipeline instances.
     * @return Will be &ge; 0.
     */
    public int noOfSubPipelines() {
        return nSubPipelines;
    }

    /**
     * Returns the number of source instances.
     * @return Will be &ge; 0.
     */
    public int noOfSources() {
        return nSources;
    }

    /**
     * Returns the number of family element instances.
     * @return Will be &ge; 0.
     */
    public int noOfFamilyElements() {
        return nFamilyElements;
    }

    /**
     * Returns the number of data management instances.
     * @return Will be &ge; 0.
     */
    public int noOfDataManagementElements() {
        return nDMElements;
    }

    /**
     * Returns the number of replay sink instances.
     * @return Will be &ge; 0.
     */
    public int noOfReplaySinks() {
        return nReplaySinks;
    }

    /**
     * Returns the number of sink (without replay sinks) instances.
     * @return Will be &ge; 0.
     */
    public int noOfSinks() {
        return nSinks;
    }

    /**
     * Returns the number of flow instances.
     * @return Will be &ge; 0.
     */
    public int noOfFlows() {
        return nFlows;
    }

    /**
     * Returns the number of constraints defined on project level.
     * @return Will be &ge; 0.
     */
    public int noOfConstraints() {
        return nConstraints;
    }

    /**
     * Returns the number of user defined operations.
     * @return Will be &ge; 0.
     */
    public int noOfOperations() {
        return nOperations;
    }
    
    /** Returns the number of declarations nested inside the project (not part of compounds).
     * @return Will be &ge; 0.
     */
    public int noOfToplevelDeclarations() {
        return nTopLevelDeclarations;
    }
   
    /**
     * Returns the number of declarations nested in compounds.
     * @return Will be &ge; 0.
     */
    public int noOfNestedDeclarations() {
        return nNestedDeclarations;
    }
   
    /**
     * Returns the number of annotations nested in compounds.
     * @return Will be &ge; 0.
     */
    public int noOfNestedAnnotations() {
        return nNestedAnnotations;
    }
   
    /**
     * Returns the number of annotations nested inside the project (not part of compounds).
     * @return Will be &ge; 0.
     */
    public int noOfToplevelAnnotations() {
        return nTopLevelAnnotations;
    }
}
