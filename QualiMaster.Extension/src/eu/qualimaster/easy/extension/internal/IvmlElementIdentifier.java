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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import eu.qualimaster.coordination.events.AlgorithmProfilingEvent;
import eu.qualimaster.easy.extension.ObservableMapping;
import eu.qualimaster.easy.extension.QmConstants;
import eu.qualimaster.easy.extension.internal.PipelineContentsContainer.MappedInstanceType;
import eu.qualimaster.events.EventHandler;
import eu.qualimaster.events.EventManager;
import eu.qualimaster.monitoring.events.FrozenSystemState;
import eu.qualimaster.monitoring.systemState.TypeMapper;
import eu.qualimaster.monitoring.systemState.TypeMapper.TypeCharacterizer;
import eu.qualimaster.observables.IObservable;
import net.ssehub.easy.instantiation.core.model.vilTypes.configuration.AbstractIvmlVariable;
import net.ssehub.easy.instantiation.core.model.vilTypes.configuration.IvmlElement;
import net.ssehub.easy.instantiation.rt.core.model.confModel.AbstractVariableIdentifier;
import net.ssehub.easy.varModel.confModel.AssignmentState;
import net.ssehub.easy.varModel.confModel.Configuration;
import net.ssehub.easy.varModel.confModel.ConfigurationException;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.model.ModelQueryException;
import net.ssehub.easy.varModel.model.datatypes.BooleanType;
import net.ssehub.easy.varModel.model.datatypes.IDatatype;
import net.ssehub.easy.varModel.model.datatypes.IntegerType;
import net.ssehub.easy.varModel.model.values.BooleanValue;
import net.ssehub.easy.varModel.model.values.ContainerValue;
import net.ssehub.easy.varModel.model.values.Value;
import net.ssehub.easy.varModel.model.values.ValueDoesNotMatchTypeException;
import net.ssehub.easy.varModel.model.values.ValueFactory;

/**
 * Uses {@link IvmlElement}s and {@link IObservable}s to identify temporary value mappings inside the
 * {@link net.ssehub.easy.adaptiveVarModel.confModel.AdaptiveConfiguration}.
 * 
 * @author El-Sharkawy
 */
public class IvmlElementIdentifier extends AbstractVariableIdentifier<IvmlElementIdentifier.ObservableTuple> {

    private static final String MAIN_PROJECT_ID = FrozenSystemState.INFRASTRUCTURE + FrozenSystemState.SEPARATOR;
    private static final Set<String> PROFILING_PIPELINES =  new HashSet<String>();
    
    /**
     * Handles profiling lifecycle information.
     * 
     * @author Holger Eichelberger
     */
    private static class ProfilingEventHandler extends EventHandler<AlgorithmProfilingEvent> {

        /**
         * Creates a handler instance.
         */
        protected ProfilingEventHandler() {
            super(AlgorithmProfilingEvent.class);
        }

        @Override
        protected void handle(AlgorithmProfilingEvent event) {
            switch(event.getStatus()) {
            case START:
                PROFILING_PIPELINES.add(event.getPipeline());
                break;
            case END:
                PROFILING_PIPELINES.remove(event.getPipeline());
                break;
            default:
                break;
            }
        }
        
    }
    
    static {
        EventManager.register(new ProfilingEventHandler());
    }
    
    /**
     * Part of the iterator, stores which kind of observable/variable mapper shall be used.
     * @author El-Sharkawy
     *
     */
    private static enum ObservableMappingType {
        ALGORITHM;
        
        /**
         * Returns for the iterator in {@link IvmlElementIdentifier#getIDIterator(String)} the correct
         * mapped observable.
         * @param type The type as specified in the first segment of the ID.
         * @param observable An {@link IObservable#name()}.
         * @return {@link ObservableMapping#mapGeneralObservable(String)} by default or a specific one if necessary.
         */
        private static String getMapping(ObservableMappingType type, String observable) {
            String variableName = null;
            
            if (null != type) {
                switch (type) {
                case ALGORITHM:
                    variableName = ObservableMapping.mapAlgorithmObservable(observable);
                    break;
                default:
                    variableName = ObservableMapping.mapGeneralObservable(observable);
                    break;
                }
            } else {
                variableName = ObservableMapping.mapGeneralObservable(observable);
            }
            
            return variableName;
        }
    }

    /**
     * A 2-tuple consisting of {@link IvmlElement} and {@link IObservable}, which are used to generate unique
     * identifiers.
     * 
     * @author El-Sharkawy
     *
     */
    public static class ObservableTuple {

        private IvmlElement element;
        private IObservable observable;

        /**
         * Sole constructor of this class.
         * 
         * @param element
         *            The (top level) variable to map.
         * @param observable
         *            An observable nested inside of <tt>element</tt>
         */
        public ObservableTuple(IvmlElement element, IObservable observable) {
            this.element = element;
            this.observable = observable;
        }
    }
    
    private List<IDecisionVariable> pipelines;
    private Map<String, PipelineContentsContainer> pipelineInfos;
    private Map<String, List<String>> cachedIDSegments;
    
    /**
     * Sole constructor for this class.
     * @param config The used configuration, needed to perform queries.
     */
    public IvmlElementIdentifier(Configuration config) {
        pipelines = new ArrayList<>();
        for (IDecisionVariable variable : config) {
            if (variable.getDeclaration().getType().getName().equals(QmConstants.TYPE_PIPELINE)) {
                pipelines.add(variable);
            }
        }

        cachedIDSegments = new HashMap<>();
        pipelineInfos = new HashMap<>();
    }
    
    /**
     * Returns the collected mappings for elements of the specified pipeline.
     * @param pipName The configured name of the pipeline.
     * @return A container containing all elements + mapped elements.
     */
    private PipelineContentsContainer getPipelineInfos(String pipName) {
        PipelineContentsContainer infos = pipelineInfos.get(pipName);
        if (null == infos) {
            IDecisionVariable pipeline = null;
            for (int i = 0, end = pipelines.size(); i < end && null == pipeline; i++) {
                IDecisionVariable tmpPip = pipelines.get(i);
                if (tmpPip.getNestedElement(QmConstants.SLOT_PIPELINE_NAME).getValue().getValue().equals(pipName)) {
                    pipeline = tmpPip;
                }
            }
            
            if (null != pipeline) {
                PipelineVisitor visitor = new PipelineVisitor(pipeline);
                infos = visitor.getPipelineContents();
            }
            
            pipelineInfos.put(pipName, infos);
        }
        
        return infos;
    }

    @Override
    protected String variableToID(ObservableTuple variable) {
        String id = null; 
        if (variable.element instanceof AbstractIvmlVariable) {
            AbstractIvmlVariable var = (AbstractIvmlVariable) variable.element;
            TypeCharacterizer characterizer = TypeMapper.findCharacterizer(var.getIvmlType());
            if (null != characterizer) {
                String prefix = characterizer.getFrozenStatePrefix();
                String key = characterizer.getFrozenStateKey(var.getDecisionVariable());

                id = prefix + FrozenSystemState.SEPARATOR + key + FrozenSystemState.SEPARATOR
                        + (null == variable.observable ? null : variable.observable.name());
            }
        }

        return id;
    }

    @Override
    protected boolean isNestedVariable(String id) {
        return null != id && !id.startsWith(MAIN_PROJECT_ID)
            && StringUtils.countMatches(id, FrozenSystemState.SEPARATOR) > 1;
    }

    @Override
    protected Iterator<String> getIDIterator(final String observableID) {
        final List<String> segments = splitID(observableID);
        
        return new Iterator<String>() {
            
            private int index = 1;
            private ObservableMappingType type = null;

            @Override
            public boolean hasNext() {
                return segments.size() > index;
            }

            @Override
            public String next() {
                String id;
                
                try {
                    if (1 == index) {
                        // Returns the compound
                        index = Math.max(segments.size() - 2, 1);
                        String fistSegment = segments.get(0);
                        if (fistSegment.equals("PipelineElement")) {
                            id = fistSegment + FrozenSystemState.SEPARATOR + segments.get(1)
                                + FrozenSystemState.SEPARATOR + segments.get(index++);
                        } else {
                            if (fistSegment.equals(QmConstants.TYPE_ALGORITHM)) {
                                type = ObservableMappingType.ALGORITHM;
                            }
                            id = fistSegment + FrozenSystemState.SEPARATOR + segments.get(index++);
                        }
                    } else if ((segments.size() - 1) == index) {
                        // Returns the observable
                        id = segments.get(index++);
                        String mappedValue = ObservableMappingType.getMapping(type, id);
                        if (null != mappedValue) {
                            id = mappedValue;
                        }  else {
                            id = null;
                        }
                    } else {
                        // Should not be needed (would return an intermediate compound)
                        id = segments.get(index++);
                    }
                } catch (ArrayIndexOutOfBoundsException exc) {
                    throw new RuntimeException("Unable to split \"" + observableID + "\" into sufficient segments."
                        , exc);
                }

                return id;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Removing segments are not supported. Tried this on: "
                    + observableID);
            }
        };
    }

    /**
     * Splits a given ID into segments for iteration through the element parts.
     * Will also use a cached to minimize String operations.
     * @param id The ID to split into individual segments.
     * @return An list of the split elements.
     */
    private List<String> splitID(final String id) {
        List<String> segments = cachedIDSegments.get(id);
        if (null == segments) {
            segments = new ArrayList<String>();
            cachedIDSegments.put(id, segments);
            
            String[] arraySegments = id.split(FrozenSystemState.SEPARATOR);
            
            // Special treatment for elements for which adaptation/monitoring copies are created
            if (QmConstants.TYPE_ALGORITHM.equals(arraySegments[0])) {
                fillSegmentList(MappedInstanceType.ALGORITHM, arraySegments, segments);
            } else if (QmConstants.TYPE_DATASOURCE.equals(arraySegments[0])) {
                fillSegmentList(MappedInstanceType.SOURCE, arraySegments, segments);
            } else if (QmConstants.TYPE_DATASINK.equals(arraySegments[0])) {
                fillSegmentList(MappedInstanceType.SINK, arraySegments, segments);
            }
            
            // Default operation and fall back
            if (segments.isEmpty()) {
                for (int i = 0; i < arraySegments.length; i++) {
                    segments.add(arraySegments[i]);
                }
            }
        }
        
        return segments;
    }

    /**
     * Part of {@link #splitID(String)} to fill the list for mapped runtime variable instances.
     * @param type The type of mapped variable
     * @param arraySegments The already split ID for an variable
     * @param segments The empty list to fill via side effect
     */
    private void fillSegmentList(MappedInstanceType type, String[] arraySegments, List<String> segments) {
        PipelineContentsContainer infos = getPipelineInfos(arraySegments[1]);
        if (null != infos) {
            IDecisionVariable mappedVar = infos.getMappedInstance(type, arraySegments[2]);
            if (null != mappedVar) {
                segments.add(arraySegments[0]);
                segments.add(mappedVar.getDeclaration().getName());
                segments.add(arraySegments[arraySegments.length - 1]);
            } else {
                Bundle.getLogger(IvmlElementIdentifier.class).warn("No mapped variable found for: "
                    + arraySegments[2] + " with type " + type);
            }
        } else {
            String pipName = arraySegments[1];
            if (!PROFILING_PIPELINES.contains(pipName)) {
                Bundle.getLogger(IvmlElementIdentifier.class).warn("No pipeline information found for: " + pipName);
            }
        }
    }
    
    @Override
    protected String iDecisionVariableToID(IDecisionVariable variable) {
        String id = null;
        TypeCharacterizer characterizer = TypeMapper.findCharacterizer(variable.getDeclaration().getType());
        if (null != characterizer) {
            IDecisionVariable pipelineVar = null;
            try {
                pipelineVar = PipelineHelper.obtainPipeline(variable.getConfiguration(), variable);
            } catch (ModelQueryException e) {
                // Not critical since this is only used as a test
                Bundle.getLogger(IvmlElementIdentifier.class).debug(e.getMessage());
            }
            
            String prefix = characterizer.getFrozenStatePrefix();
            String key;
            if (null != pipelineVar) {
                String pipName = VariableHelper.getName(pipelineVar);
                key = FrozenSystemState.obtainPipelineElementSubkey(pipName, VariableHelper.getName(variable));
            } else {
                key = PipelineContentsContainer.isMappingVariable(variable) ? variable.getDeclaration().getName()
                    : VariableHelper.getName(variable);                
            }
            id = prefix + FrozenSystemState.SEPARATOR + key;
        } else {
            String varName = VariableHelper.getName(variable);
            if (null == varName) {
                varName = variable.getDeclaration().getName();
            }
            String normalizedName = ObservableMapping.mapReverseGeneralObservable(varName);
            if (null != normalizedName) {
                varName = ":" + normalizedName;
            }
            id = MAIN_PROJECT_ID + varName;
        }

        return id;
    }

    @Override
    protected Value toIVMLValue(IDecisionVariable trgVariable, Object oValue) throws ValueDoesNotMatchTypeException {
        IDatatype type = trgVariable.getDeclaration().getType();
        Value result = null;
        if (IntegerType.TYPE.isAssignableFrom(type) && oValue instanceof Double) {
            oValue = ((Double) oValue).intValue();
        } else if (BooleanType.TYPE.isAssignableFrom(type) && oValue instanceof Double) {
            result = ((Double) oValue) >= 0.5 ? BooleanValue.TRUE : BooleanValue.FALSE;
        }
        if (null == result) {
            result = ValueFactory.createValue(type, oValue);
        }
        
        return result;
    }
    
    @Override
    protected void assignValue(IDecisionVariable variable, Value value) throws ConfigurationException {
        super.assignValue(variable, value);
        if (variable.getParent() instanceof IDecisionVariable) {
            IDecisionVariable parentVariable = (IDecisionVariable) variable.getParent();
            String variableName = variable.getDeclaration().getName();
            String typeName = parentVariable.getDeclaration().getType().getName();
            
            if (QmConstants.TYPE_PIPELINE.equals(typeName) && "hosts".equals(variableName)) {
                // Assign pipeline_Hosts to all algorithms of pipeline
                String pipeline = parentVariable.getDeclaration().getName();
                PipelineContentsContainer infos = getPipelineInfos(pipeline);
                if (null != infos) {
                    List<IDecisionVariable> familyElements = infos.getFamilyElements();
                    for (IDecisionVariable familyElement : familyElements) {
                        setValueForAvailableAlgorithms(value, familyElement, "pipeline_Hosts");
                    }
                }
            } else if (QmConstants.TYPE_FAMILYELEMENT.equals(typeName) && "items".equals(variableName)) {
                // Assign family_Items to all algorithms of family element
                setValueForAvailableAlgorithms(value, parentVariable, "family_Items");
            }
        }
    }
    
    @Override
    protected AssignmentState getAssignmentState() {
        return AssignmentState.USER_ASSIGNED;
    }

    /**
     * Sets the specified value to all available algorithms of the given family element.
     * @param value The value to set.
     * @param familyElement A family element of a pipeline.
     * @param slot The slot to configure with the given value.
     * @throws ConfigurationException in case that the types of 
     *   {@link #getDeclaration()} and <code>value</code> do not comply
     */
    private void setValueForAvailableAlgorithms(Value value, IDecisionVariable familyElement, String slot)
        throws ConfigurationException {
        
        IDecisionVariable availableAlgos = familyElement.getNestedElement(QmConstants.SLOT_FAMILYELEMENT_AVAILABLE);
        List<IDecisionVariable> algos = null;
        if (null != availableAlgos) {
            Value refferencedAlgos = availableAlgos.getValue();
            if (null != refferencedAlgos && refferencedAlgos instanceof ContainerValue) {
                ContainerValue container = (ContainerValue) refferencedAlgos;
                algos = Utils.extractVariables(container, familyElement.getConfiguration());
            }
        }
        // Set same value for all available algorithms
        if (null != algos) {
            for (IDecisionVariable algorithm : algos) {
                IDecisionVariable algoSlot = algorithm.getNestedElement(slot);
                if (null != algoSlot) {
                    algoSlot.setValue(value, getAssignmentState());
                }
            }
        }
    }
}
