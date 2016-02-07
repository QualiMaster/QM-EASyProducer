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
 * Defines global model constants.
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
    
    public static final String PROJECT_TOP_LEVEL = "QM";
    
    public static final String TYPE_PIPELINE_ELEMENT = "PipelineElement";
    public static final String TYPE_FAMILY_ELEMENT = "FamilyElement";
    public static final String TYPE_PIPELINE = "Pipeline";
    public static final String TYPE_ALGORITHM = "Algorithm";
    public static final String SLOT_ALGORITHM_MEMBERS = "members";
    public static final String TYPE_FAMILY = "Family";
    public static final String TYPE_MACHINE = "Machine";
    public static final String TYPE_DATAELEMENT = "DataElement";
    
    public static final String TYPE_FIELDTYPE = "FieldType";
    public static final String SLOT_FIELDTYPE = SLOT_NAME;
    public static final String SLOT_FIELDTYPE_CLASS = "class";
    public static final String SLOT_FIELDTYPE_ARTIFACT = "artifact";
    public static final String SLOT_FIELDTYPE_SERIALIZER = "serializer";
    public static final String SLOT_FIELDTYPE_SERIALIZERARTIFACT = "serializerArtifact"; 

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
    
    public static final String SLOT_INFRASTRUCTURE_REPOSITORY_URL = "repositoryURL";
    public static final String SLOT_INFRASTRUCTURE_DEPLOYMENT_URL = "deploymentURL";
    
    public static final String VAR_PIPELINES_PIPELINES = "pipelines";
   
}
