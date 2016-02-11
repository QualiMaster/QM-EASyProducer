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
package eu.qualimaster.easy.extension;

/**
 * Defines global model constants to avoid the use of simple strings and inconsistencies
 * when the model changes. Some constants are global due to conventions such as {@link #SLOT_NAME}, 
 * others follow a certain naming convention according to their containment in projects / compounds. 
 * 
 * @author Holger Eichelberger
 */
public class QmConstants {

    /**
     * Model name postfix to distinguish model definition and configuration model parts.
     */
    public static final String CFG_POSTFIX = "Cfg";
    
    /**
     * Configurable elements have by convention a logical name.
     */
    public static final String SLOT_NAME = "name";

    /**
     * Configurable elements may have a runtime flag whether they are valid at all.
     */
    public static final String SLOT_ISVALID = "isValid";
    
    public static final String PROJECT_TOP_LEVEL = "QM";

    // -------------------------------- infrastructure --------------------------
    
    public static final String PROJECT_INFRASTRUCTURE = "Infrastructure";
    public static final String VAR_INFRASTRUCTURE_REPOSITORY_URL = "repositoryURL";
    public static final String VAR_INFRASTRUCTURE_DEPLOYMENT_URL = "deploymentURL";

    // -------------------------- pipelines --------------------------------

    public static final String PROJECT_PIPELINES = "Pipelines";

    public static final String TYPE_PIPELINE_ELEMENT = "PipelineElement";
    public static final String SLOT_PIPELINE_ELEMENT_ISVALID = SLOT_ISVALID;
    
    public static final String TYPE_PIPELINE_NODE = "PipelineNode";
    public static final String SLOT_PIPELINE_NODE_ISVALID = SLOT_ISVALID;
    
    public static final String TYPE_SOURCE = "Source";
    public static final String SLOT_SOURCE_ISVALID = SLOT_ISVALID;
    
    public static final String TYPE_SINK = "Sink";
    public static final String SLOT_SINK_ISVALID = SLOT_ISVALID;
    
    public static final String TYPE_FLOW = "Flow";
    public static final String SLOT_FLOW_ISVALID = SLOT_ISVALID;
    
    public static final String TYPE_PROCESSINGELEMENT = "ProcessingElement";
    public static final String SLOT_PROCESSINGELEMENT_ISVALID = SLOT_ISVALID;
    
    public static final String TYPE_DATAMANAGEMENTELEMENT = "DataMangementElement";
    public static final String SLOT_DATAMANAGEMENTELEMENT_ISVALID = SLOT_ISVALID;
    
    public static final String TYPE_FAMILYELEMENT = "FamilyElement";
    public static final String TYPE_FAMILY_ELEMENT = TYPE_FAMILYELEMENT; // TODO remove
    public static final String SLOT_FAMILYELEMENT_NAME = SLOT_NAME;
    public static final String SLOT_FAMILYELEMENT_AVAILABLE = "available";
    public static final String SLOT_FAMILYELEMENT_ACTUAL = "actual";
    public static final String SLOT_FAMILYELEMENT_FAMILY = "family";
    public static final String SLOT_FAMILYELEMENT_DEFAULT = "default";
    public static final String SLOT_FAMILYELEMENT_ISVALID = SLOT_ISVALID;
    
    public static final String TYPE_PIPELINE = "Pipeline";
    public static final String SLOT_PIPELINE_ISVALID = SLOT_ISVALID;
    public static final String VAR_PIPELINES_PIPELINES = "pipelines";

    // ------------------------- adaptivity ---------------------------------

    public static final String PROJECT_ADAPTIVITY = "Adaptivity";
    public static final String TYPE_ADAPTIVITY_QPARAMWEIGHTING = "QualityParameterWeighting";
    public static final String VAR_ADAPTIVITY_PIPELINEIMPORTANCE = "pipelineImportance";
    public static final String VAR_ADAPTIVITY_CROSSPIPELINETRADEOFFS = "crossPipelineTradeoffs";
   
    // ------------------------- observables  ---------------------------------

    public static final String PROJECT_OBSERVABLES = "Observables";
    public static final String TYPE_OBSERVABLES_CONFIGUREDQPARAM = "ConfiguredQualityParameter";
    public static final String VAR_OBSERVABLES_CONFIGUREDPARAMS = "configuredParameters";
    
    // -------------------------- families  ---------------------------------

    public static final String PROJECT_FAMILIES = "Families";
    public static final String TYPE_FAMILY = "Family";
    public static final String VAR_FAMILIES_FAMILIES = "families";

    // -------------------------- algorithms  -------------------------------

    public static final String PROJECT_ALGORITHMS = "Algorithms";

    public static final String TYPE_ALGORITHM = "Algorithm";
    public static final String SLOT_ALGORITHM_MEMBERS = "members";
    public static final String SLOT_ALGORITHM_ISVALID = SLOT_ISVALID;
    
    public static final String VAR_ALGORITHMS_ALGORITHMS = "algorithms";

    // ------------------------ data management -----------------------------

    public static final String PROJECT_DATAMGT = "DataManagement";

    public static final String TYPE_DATAELEMENT = "DataElement";
    
    public static final String TYPE_DATASOURCE = "DataSource";
    public static final String SLOT_DATASOURCE_ISVALID = SLOT_ISVALID;
    
    public static final String TYPE_DATASINK = "DataSink";
    public static final String SLOT_DATASINK_ISVALID = SLOT_ISVALID;
    
    public static final String TYPE_PERSISTENTDATAELT = "PersistentDataElement";
    
    public static final String VAR_DATAMGT_DATASOURCES = "dataSources";
    public static final String VAR_DATAMGT_DATASINKS = "dataSinks";
    public static final String VAR_DATAMGT_PERSISTENTDATAELTS = "persistentDataElements";
    
    // -------------------- general-purpose hardware  -----------------------

    public static final String PROJECT_HARDWARE = "Hardware";
    public static final String TYPE_MACHINE = "Machine";
    public static final String VAR_HARDWARE_MACHINES = "machines";

    // -------------------- reconfigurable hardware  ------------------------

    public static final String PROJECT_RECONFHW = "ReconfigurableHardware";
    public static final String TYPE_HWNODE = "HwNode";
    public static final String VAR_RECONFHW_CLUSTERS = "clusters";
    
    // ---------------------------- basics ----------------------------------
    
    public static final String PROJECT_BASICS = "Basics";

    public static final String TYPE_FIELDTYPE = "FieldType";
    public static final String SLOT_FIELDTYPE = SLOT_NAME;
    public static final String SLOT_FIELDTYPE_CLASS = "class";
    public static final String SLOT_FIELDTYPE_ARTIFACT = "artifact";
    public static final String SLOT_FIELDTYPE_SERIALIZER = "serializer";
    public static final String SLOT_FIELDTYPE_SERIALIZERARTIFACT = "serializerArtifact"; 
    public static final String VAR_BASICS_TYPES = "types";

    public static final String TYPE_TUPLE = "Tuple";
    public static final String SLOT_TUPLE_NAME = SLOT_NAME;
    public static final String SLOT_TUPLE_FIELDS = "fields";
    public static final String TYPE_TUPLES = "Tuples";

    public static final String TYPE_FIELD = "Field";
    public static final String SLOT_FIELD_NAME = SLOT_NAME;
    public static final String SLOT_FIELD_TYPE = "type";
    public static final String SLOT_FIELD_KEYPART = "keyPart";
    public static final String TYPE_FIELDS = "Fields";
    
    // slots are specific - defaultValue/value not defined in parameter but in all refined types
    public static final String TYPE_PARAMETER = "Parameter";
    public static final String SLOT_PARAMETER_NAME = SLOT_NAME;
    public static final String SLOT_PARAMETER_DEFAULTVALUE = "defaultValue";
    public static final String SLOT_PARAMETER_VALUE = "value";
    
    public static final String TYPE_INTEGERPARAMETER = "IntegerParameter";
    public static final String SLOT_INTEGERPARAMETER_NAME = SLOT_PARAMETER_NAME;
    public static final String SLOT_INTEGERPARAMETER_DEFAULTVALUE = SLOT_PARAMETER_DEFAULTVALUE;
    public static final String SLOT_INTEGERPARAMETER_VALUE = SLOT_PARAMETER_VALUE;

    public static final String TYPE_STRINGPARAMETER = "StringParameter";
    public static final String SLOT_STRINGPARAMETER_NAME = SLOT_PARAMETER_NAME;
    public static final String SLOT_STRINGPARAMETER_DEFAULTVALUE = SLOT_PARAMETER_DEFAULTVALUE;
    public static final String SLOT_STRINGPARAMETER_VALUE = SLOT_PARAMETER_VALUE;

    public static final String TYPE_REALPARAMETER = "RealParameter";
    public static final String SLOT_REALPARAMETER_NAME = SLOT_PARAMETER_NAME;
    public static final String SLOT_REALPARAMETER_DEFAULTVALUE = SLOT_PARAMETER_DEFAULTVALUE;
    public static final String SLOT_REALPARAMETER_VALUE = SLOT_PARAMETER_VALUE;

    public static final String TYPE_BOOLEANPARAMETER = "BooleanParameter";
    public static final String SLOT_BOOLEANPARAMETER_NAME = SLOT_PARAMETER_NAME;
    public static final String SLOT_BOOLEANPARAMETER_DEFAULTVALUE = SLOT_PARAMETER_DEFAULTVALUE;
    public static final String SLOT_BOOLEANPARAMETER_VALUE = SLOT_PARAMETER_VALUE;

    public static final String TYPE_LONGPARAMETER = "LongParameter";
    public static final String SLOT_LONGPARAMETER_NAME = SLOT_PARAMETER_NAME;
    public static final String SLOT_LONGPARAMETER_DEFAULTVALUE = SLOT_PARAMETER_DEFAULTVALUE;
    public static final String SLOT_LONGPARAMETER_VALUE = SLOT_PARAMETER_VALUE;

    public static final String TYPE_BINDING_TIME = "BindingTime";
    
    public static final String CONST_BINDING_TIME_COMPILE = "compile";
    public static final String CONST_BINDING_TIME_RUNTIME = "runtime"; // kept for legacy
    public static final String CONST_BINDING_TIME_RUNTIME_MON = "runtimeMon";
    public static final String CONST_BINDING_TIME_RUNTIME_ENACT = "runtimeEnact";
    public static final String ANNOTATION_BINDING_TIME = "bindingTime";
    public static final String ANNOTATION_USER_VISIBLE = "userVisible";
    
}
