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
    public static final String SLOT_INPUT = "input";
    public static final String SLOT_OUTPUT = "output";
    public static final String SLOT_PARAMETERS = "parameters";
    public static final String SLOT_AVAILABLE = "available";
    public static final String SLOT_ACTUAL = "actual";
    
    public static final String PROJECT_TOP_LEVEL = "QM";

    // -------------------------------- infrastructure --------------------------
    
    public static final String PROJECT_INFRASTRUCTURE = "Infrastructure";
    public static final String PROJECT_INFRASTRUCTURECFG = PROJECT_INFRASTRUCTURE + CFG_POSTFIX;
    public static final String VAR_INFRASTRUCTURE_ACTIVEPIPELINES = "activePipelines";
    public static final String VAR_INFRASTRUCTURE_REPOSITORY_URL = "repositoryURL";
    public static final String VAR_INFRASTRUCTURE_DEPLOYMENT_URL = "deploymentURL";

    // -------------------------- pipelines --------------------------------

    public static final String PROJECT_PIPELINES = "Pipelines";
    public static final String PROJECT_PIPELINESCFG = PROJECT_PIPELINES + CFG_POSTFIX;

    public static final String TYPE_PIPELINE_ELEMENT = "PipelineElement";
    public static final String SLOT_PIPELINE_ELEMENT_ISVALID = SLOT_ISVALID;
    public static final String TYPE_PIPELINE_ELEMENT_NAME = "name";
    public static final String SLOT_PIPELINE_ELEMENT_PERMISSIBLE_PARAMETERS = "permissibleParameters";
    
    public static final String TYPE_PIPELINE_NODE = "PipelineNode";
    public static final String SLOT_PIPELINE_NODE_ISVALID = SLOT_ISVALID;
    public static final String SLOT_PIPELINE_NODE_NUMBER_OF_TAKS = "numtasks";
    public static final String SLOT_PIPELINE_NODE_PARALLELISM = "parallelism";
    
    public static final String TYPE_SOURCE = "Source";
    public static final String SLOT_SOURCE_ISVALID = SLOT_ISVALID;
    public static final String SLOT_SOURCE_SOURCE = "source";
    public static final String SLOT_SOURCE_AVAILABLE = SLOT_AVAILABLE;
    public static final String SLOT_SOURCE_ACTUAL = SLOT_ACTUAL;
    public static final String SLOT_SOURCE_NAME = SLOT_NAME;
    public static final String SLOT_SOURCE_OUTPUT = "output";
    public static final String SLOT_SOURCE_ITEMS = "items";
    public static final String SLOT_SOURCE_CAPACITY = "capacity";

    /**
     * Inherited parallelism slot from pipeline node.
     * @deprecated Use {@link #SLOT_PIPELINE_NODE_PARALLELISM} instead, as since slot is inherited from there
     */
    @Deprecated
    public static final String SLOT_SOURCE_PARALLELISM = SLOT_PIPELINE_NODE_PARALLELISM;

    public static final String TYPE_SINK = "Sink";
    public static final String SLOT_SINK_ISVALID = SLOT_ISVALID;
    public static final String SLOT_SINK_SINK = "sink";
    public static final String SLOT_SINK_AVAILABLE = SLOT_AVAILABLE;
    public static final String SLOT_SINK_ACTUAL = SLOT_ACTUAL;
    public static final String SLOT_SINK_NAME = SLOT_NAME;
    
    public static final String TYPE_REPLAYSINK = "ReplaySink";
    public static final String SLOT_REPLAYSINK_ISVALID = SLOT_SINK_ISVALID;
    public static final String SLOT_REPLAYSINK_SINK = SLOT_SINK_SINK;
    public static final String SLOT_REPLAYSINK_AVAILABLE = SLOT_SINK_AVAILABLE;
    public static final String SLOT_REPLAYSINK_ACTUAL = SLOT_SINK_ACTUAL;
    public static final String SLOT_REPLAYSINK_NAME = SLOT_SINK_NAME;
    public static final String SLOT_REPLAYSINK_REPLAY = "replay";
    
    /**
     * Inherited parallelism slot from pipeline node.
     * @deprecated Use {@link #SLOT_PIPELINE_NODE_PARALLELISM} instead, as since slot is inherited from there
     */
    @Deprecated
    public static final String SLOT_SINK_PARALLELISM = SLOT_PIPELINE_NODE_PARALLELISM;
    
    public static final String TYPE_FLOW = "Flow";
    public static final String SLOT_FLOW_NAME = SLOT_NAME;
    public static final String SLOT_FLOW_DESTINATION = "destination";
    public static final String SLOT_FLOW_GROUPING = "grouping";
    public static final String SLOT_FLOW_ISVALID = SLOT_ISVALID;
    public static final String CONST_GROUPING_SHUFFLEGROUPING = "shuffleGrouping";
    public static final String SLOT_FLOW_TUPLE_TYPE = "tupleType";
    
    public static final String TYPE_PROCESSINGELEMENT = "ProcessingElement";
    public static final String SLOT_PROCESSINGELEMENT_ISVALID = SLOT_ISVALID;
    
    public static final String TYPE_DATAMANAGEMENTELEMENT = "DataManagementElement";
    public static final String SLOT_DATAMANAGEMENTELEMENT_ISVALID = SLOT_ISVALID;
    
    public static final String TYPE_FAMILYELEMENT = "FamilyElement";
    public static final String SLOT_FAMILYELEMENT_NAME = SLOT_NAME;
    public static final String SLOT_FAMILYELEMENT_AVAILABLE = SLOT_AVAILABLE;
    public static final String SLOT_FAMILYELEMENT_ACTUAL = SLOT_ACTUAL;
    public static final String SLOT_FAMILYELEMENT_FAMILY = "family";
    public static final String SLOT_FAMILYELEMENT_DEFAULT = "default";
    public static final String SLOT_FAMILYELEMENT_ISVALID = SLOT_ISVALID;
    /**
     * Inherited parallelism slot from pipeline node.
     * @deprecated Use {@link #SLOT_PIPELINE_NODE_PARALLELISM} instead, as since slot is inherited from there
     */
    @Deprecated
    public static final String SLOT_FAMILYELEMENT_PARALLELISM = SLOT_PIPELINE_NODE_PARALLELISM;
    
    public static final String TYPE_PIPELINE = "Pipeline";
    public static final String TYPE_SUBPIPELINE = "SubPipeline";
    public static final String SLOT_PIPELINE_NAME = SLOT_NAME;
    public static final String SLOT_PIPELINE_SOURCES = "sources";
    public static final String SLOT_PIPELINE_NUMWORKERS = "numworkers";
    public static final String SLOT_PIPELINE_ARTIFACT = "artifact";
    public static final String SLOT_PIPELINE_ISVALID = SLOT_ISVALID;
    public static final String VAR_PIPELINES_PIPELINES = "pipelines";
    public static final String TYPE_SUBPIPELINE_ALGORITHM = "SubPipelineAlgorithm";

    // ------------------------- adaptivity ---------------------------------

    public static final String PROJECT_ADAPTIVITY = "Adaptivity";
    public static final String PROJECT_ADAPTIVITYCFG = PROJECT_ADAPTIVITY + CFG_POSTFIX;
    public static final String TYPE_ADAPTIVITY_QPARAMWEIGHTING = "QualityParameterWeighting";
    public static final String SLOT_QPARAMWEIGHTING_PARAMETER = "parameter";
    public static final String SLOT_QPARAMWEIGHTING_WEIGHT = "weight";
    public static final String VAR_ADAPTIVITY_PIPELINEIMPORTANCE = "pipelineImportance";
    public static final String VAR_ADAPTIVITY_CROSSPIPELINETRADEOFFS = "crossPipelineTradeoffs";
   
    public static final String PROJECT_STRATEGIES_TACTICS = "StrategiesTactics";
    
    // ------------------------- observables  ---------------------------------

    public static final String PROJECT_OBSERVABLES = "Observables";
    public static final String PROJECT_OBSERVABLESCFG = PROJECT_OBSERVABLES + CFG_POSTFIX; 
    public static final String TYPE_OBSERVABLES_OBSERVABLE = "Observable";
    public static final String SLOT_OBSERVABLE_TYPE = "type";
    public static final String TYPE_OBSERVABLES_QPARAM = "QualityParameter";
    public static final String TYPE_OBSERVABLES_CONFIGUREDQPARAM = "ConfiguredQualityParameter";
    public static final String SLOT_CONFIGUREDQPARAM_MONITORCLS = "monitorCls";
    public static final String SLOT_CONFIGUREDQPARAM_ARTIFACT = "artifact";
    public static final String VAR_OBSERVABLES_CONFIGUREDPARAMS = "configuredParameters";
    public static final String VAR_OBSERVABLES_QUALITYPARAMS = "qualityParameters";
    
    // -------------------------- families  ---------------------------------

    public static final String PROJECT_FAMILIES = "Families";
    public static final String PROJECT_FAMILIESCFG = PROJECT_FAMILIES + CFG_POSTFIX;
    public static final String TYPE_FAMILY = "Family";
    public static final String SLOT_FAMILY_NAME = SLOT_NAME;
    public static final String SLOT_FAMILY_MEMBERS = "members";
    public static final String SLOT_FAMILY_INPUT = SLOT_INPUT;
    public static final String SLOT_FAMILY_OUTPUT = SLOT_OUTPUT;
    public static final String SLOT_FAMILY_PARAMETERS = SLOT_PARAMETERS;
    public static final String VAR_FAMILIES_FAMILIES = "families";

    // -------------------------- algorithms  -------------------------------

    public static final String PROJECT_ALGORITHMS = "Algorithms";
    public static final String PROJECT_ALGORITHMSCFG = PROJECT_ALGORITHMS + CFG_POSTFIX;
    
    public static final String TYPE_ALGORITHM = "Algorithm";
    public static final String TYPE_SOFTWARE_ALGORITHM = "SoftwareAlgorithm";
    public static final String TYPE_HARDWARE_ALGORITHM = "HardwareAlgorithm";
    public static final String SLOT_ALGORITHM_INPUT = SLOT_INPUT;
    public static final String SLOT_ALGORITHM_OUTPUT = SLOT_OUTPUT;
    public static final String SLOT_ALGORITHM_PARAMETERS = SLOT_PARAMETERS;
    /**
     * Moved to software algorithm.
     * @deprecated Use {@link #SLOT_SOFTWAREALGORITHM_TOPOLOGYCLASS} instead.
     */
    @Deprecated
    public static final String SLOT_ALGORITHM_TOPOLOGYCLASS = "algTopologyClass";
    public static final String SLOT_ALGORITHM_DESCRIPTION = "description";
    public static final String SLOT_ALGORITHM_ARTIFACT = "artifact";
    public static final String SLOT_ALGORITHM_ISVALID = SLOT_ISVALID;
    
    public static final String TYPE_SOFTWAREALGORITHM = "SoftwareAlgorithm";
    public static final String SLOT_SOFTWAREALGORITHM_CLASS = "class";
    public static final String SLOT_SOFTWAREALGORITHM_TOPOLOGYCLASS = SLOT_ALGORITHM_TOPOLOGYCLASS;
    
    public static final String TYPE_HARDWAREALGORITHM = "HardwareAlgorithm";
    public static final String SLOT_HARDWAREALGORITHM_HWNODE = "hwNode";
    public static final String SLOT_HARDWAREALGORITHM_SENDERPARALLELISM = "senderParallelism";
    public static final String SLOT_HARDWAREALGORITHM_RECEIVERPARALLELISM = "receiverParallelism";
    public static final String SLOT_HARDWAREALGORITHM_SENDERNUMTASKS = "senderNumTasks";
    public static final String SLOT_HARDWAREALGORITHM_RECEIVERNUMTASKS = "receiverNumTasks";
    public static final String SLOT_HARDWAREALGORITHM_ACTUALHWNODE = "actualHwNode";
    
    public static final String VAR_ALGORITHMS_ALGORITHMS = "algorithms";

    // ------------------------ data management -----------------------------

    public static final String PROJECT_DATAMGT = "DataManagement";
    public static final String PROJECT_DATAMGTCFG = PROJECT_DATAMGT + CFG_POSTFIX;
    
    public static final String TYPE_DATAELEMENT = "DataElement";
    
    public static final String TYPE_DATASOURCE = "DataSource";
    public static final String SLOT_DATASOURCE_NAME = SLOT_NAME;
    public static final String SLOT_DATASOURCE_TUPLES = SLOT_INPUT;
    public static final String SLOT_DATASOURCE_PARAMETERS = SLOT_PARAMETERS;
    public static final String SLOT_DATASOURCE_ISVALID = SLOT_ISVALID;
    public static final String SLOT_DATASOURCE_ARTIFACT = "artifact";
    public static final String SLOT_DATASOURCE_STORAGELOCATION = "storageLocation";
    public static final String SLOT_DATASOURCE_DATAMANAGEMENTSTRATEGY = "strategy";
    public static final String SLOT_DATASOURCE_SOURCECLS = "sourceCls";
    public static final String SLOT_DATASOURCE_PROFILINGSOURCE = "profilingSource";
    
    public static final String CONST_DATAMANAGEMENTSTRATEGY_NONE = "None";
    
    public static final String TYPE_DATASINK = "DataSink";
    public static final String SLOT_DATASINK_TUPLES = SLOT_OUTPUT;
    public static final String SLOT_DATASINK_PARAMETERS = SLOT_PARAMETERS;
    public static final String SLOT_DATASINK_ISVALID = SLOT_ISVALID;
    
    public static final String TYPE_PERSISTENTDATAELT = "PersistentDataElement";
    
    public static final String VAR_DATAMGT_DATASOURCES = "dataSources";
    public static final String VAR_DATAMGT_DATASINKS = "dataSinks";
    public static final String VAR_DATAMGT_PERSISTENTDATAELTS = "persistentDataElements";
    
    // ----------------------------- cloud ---------------------------------

    public static final String TYPE_CLOUDRESOURCE = "CloudResource";
    
    // -------------------- general-purpose hardware -----------------------

    public static final String PROJECT_HARDWARE = "Hardware";
    public static final String PROJECT_HARDWARECFG = PROJECT_HARDWARE + CFG_POSTFIX;
    public static final String TYPE_MACHINE = "Machine";
    public static final String VAR_HARDWARE_MACHINES = "machines";

    // -------------------- reconfigurable hardware  ------------------------

    public static final String PROJECT_RECONFHW = "ReconfigurableHardware";
    public static final String PROJECT_RECONFHWCFG = PROJECT_RECONFHW + CFG_POSTFIX;
    public static final String TYPE_HWNODE = "HwNode";
    public static final String VAR_RECONFHW_CLUSTERS = "clusters";
    
    public static final String TYPE_MPCCNODE = "MPCCNode";
    
    public static final String SLOT_MPCCNODE_HOST = "host";
    public static final String SLOT_MPCCNODE_MONITORINGPORT = "monitoringPort";
    public static final String SLOT_MPCCNODE_COMMANDSENDINGPORT = "commandSendingPort";
    public static final String SLOT_MPCCNODE_COMMANDRECEIVINGPORT = "commandReceivingPort";
    public static final String SLOT_MPCCNODE_NUMCPUS = "numCPUs";
    public static final String SLOT_MPCCNODE_NUMDFES = "numDFEs";
    
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
