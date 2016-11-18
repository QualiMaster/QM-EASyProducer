package eu.qualimaster.easy.extension.internal;

import java.util.ArrayList;
import java.util.HashMap;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import eu.qualimaster.easy.extension.QmConstants;
import eu.qualimaster.easy.extension.internal.PipelineContentsContainer.MappedInstanceType;
import eu.qualimaster.monitoring.events.FrozenSystemState;
import eu.qualimaster.monitoring.systemState.TypeMapper;
import eu.qualimaster.monitoring.systemState.TypeMapper.TypeCharacterizer;
import eu.qualimaster.observables.AnalysisObservables;
import eu.qualimaster.observables.CloudResourceUsage;
import eu.qualimaster.observables.FunctionalSuitability;
import eu.qualimaster.observables.IObservable;
import eu.qualimaster.observables.ResourceUsage;
import eu.qualimaster.observables.Scalability;
import eu.qualimaster.observables.TimeBehavior;
import net.ssehub.easy.instantiation.core.model.vilTypes.configuration.AbstractIvmlVariable;
import net.ssehub.easy.instantiation.core.model.vilTypes.configuration.IvmlElement;
import net.ssehub.easy.instantiation.rt.core.model.confModel.AbstractVariableIdentifier;
import net.ssehub.easy.varModel.confModel.Configuration;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.model.ModelQueryException;
import net.ssehub.easy.varModel.model.datatypes.BooleanType;
import net.ssehub.easy.varModel.model.datatypes.IDatatype;
import net.ssehub.easy.varModel.model.datatypes.IntegerType;
import net.ssehub.easy.varModel.model.values.BooleanValue;
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

    private static final Map<String, String> RUNTIME_VAR_NORMALIZATION = new HashMap<String, String>();
    private static final Map<String, String> RUNTIME_ALGORITHM_NORMALIZATION = new HashMap<String, String>();
    private static final Map<String, String> REVERSE_RUNTIME_VAR_NORMALIZATION = new HashMap<String, String>();
    private static final String MAIN_PROJECT_ID = "Infrastructure:";

    static {
        putAlgorithmMapping(FunctionalSuitability.ACCURACY_CONFIDENCE, "accuracyConfidence");
        putAlgorithmMapping(FunctionalSuitability.ACCURACY_ERROR_RATE, "accuracyErrorRate");
        putAlgorithmMapping(FunctionalSuitability.BELIEVABILITY, "believability");
        putAlgorithmMapping(FunctionalSuitability.COMPLETENESS, "completeness");
        putAlgorithmMapping(ResourceUsage.HOSTS, "pipeline_Hosts");
        putAlgorithmMapping(AnalysisObservables.IS_VALID, "isValid");
        putAlgorithmMapping(Scalability.ITEMS, "family_Items");
        putAlgorithmMapping(TimeBehavior.LATENCY, "latency");
        putAlgorithmMapping(FunctionalSuitability.RELEVANCY, "relevancy");
        putAlgorithmMapping(TimeBehavior.THROUGHPUT_ITEMS, "throughputItems");
        putAlgorithmMapping(TimeBehavior.THROUGHPUT_VOLUME, "throughputVolume");
        putAlgorithmMapping(ResourceUsage.USED_MEMORY, "memoryUse");
        putAlgorithmMapping(Scalability.VARIETY, "variety");
        putAlgorithmMapping(Scalability.VELOCITY, "velocity");
        putAlgorithmMapping(Scalability.VOLUME, "volume");
        
        put(FunctionalSuitability.ACCURACY_CONFIDENCE, "accuracyConfidence");
        put(FunctionalSuitability.ACCURACY_ERROR_RATE, "accuracyErrorRate");
        put(ResourceUsage.AVAILABLE, "available");
        put("AVAILABLE_DFES", "availableMachines");
        put(ResourceUsage.AVAILABLE_MEMORY, "availableMemory");
        put(ResourceUsage.AVAILABLE_FREQUENCY, "availableFrequency");
        put(ResourceUsage.BANDWIDTH, "bandwidth");
        put(ResourceUsage.CAPACITY, "capacity");
        put(FunctionalSuitability.COMPLETENESS, "completeness");
        put(ResourceUsage.EXECUTORS, "executors");
        put(ResourceUsage.HOSTS, "hosts");
        put(AnalysisObservables.IS_VALID, "isValid");
        put("IS_ENACTING", "isEnacting");
        put(Scalability.ITEMS, "items");
        put(TimeBehavior.LATENCY, "latency");
        put(ResourceUsage.LOAD, "load");
        put(CloudResourceUsage.PING, "ping");
        put(ResourceUsage.TASKS, "tasks");
        put(TimeBehavior.THROUGHPUT_ITEMS, "throughputItems");
        put(TimeBehavior.THROUGHPUT_VOLUME, "throughputVolume");
        put("USED_DFES", "usedMachines");
        put(CloudResourceUsage.USED_HARDDISC_MEM, "UsedHarddiscMem");
        put(ResourceUsage.USED_MEMORY, "usedMemory");
        put(CloudResourceUsage.USED_PROCESSORS, "UsedProcessors");
        put(CloudResourceUsage.USED_WORKING_STORAGE, "UsedWorkingStorage");
        put(Scalability.VELOCITY, "velocity");
        put(Scalability.VOLATILITY, "volatility");
        put(Scalability.VOLUME, "volume");
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
    protected Iterator<String> getIDIterator(final String id) {
        final List<String> segments = splitID(id);
        
        return new Iterator<String>() {
            
            private int index = 1;
            private boolean isAlgorithm = false;

            @Override
            public boolean hasNext() {
                return segments.size() > index;
            }

            @Override
            public String next() {
                String id;
                if (1 == index) {
                    index = Math.max(segments.size() - 2, 1);
                    String fistSegment = segments.get(0);
                    if (fistSegment.equals("PipelineElement")) {
                        id = fistSegment + FrozenSystemState.SEPARATOR + segments.get(1)
                            + FrozenSystemState.SEPARATOR + segments.get(index++);
                    } else {
                        if (fistSegment.equals("Algorithm")) {
                            isAlgorithm = true;
                        }
                        id = fistSegment + FrozenSystemState.SEPARATOR + segments.get(index++);
                    }
                } else if ((segments.size() - 1) == index) {
                    id = segments.get(index++);
                    Map<String, String> mapping = isAlgorithm ? RUNTIME_ALGORITHM_NORMALIZATION
                        : RUNTIME_VAR_NORMALIZATION;
                    String mappedValue = mapping.get(id);
                    if (null != mappedValue) {
                        id = mappedValue;
                    }
                } else {
                    id = segments.get(index++);
                }

                return id;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Removing segments are not supported. Tried this on: " + id);
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
            if (QmConstants.TYPE_ALGORITHM.equals(arraySegments[0])) {
                fillSegmentList(MappedInstanceType.ALGORITHM, arraySegments, segments);
            } else if (QmConstants.TYPE_SOURCE.equals(arraySegments[0])) {
                fillSegmentList(MappedInstanceType.SOURCE, arraySegments, segments);
            } else if (QmConstants.TYPE_SINK.equals(arraySegments[0])) {
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
                    + arraySegments[2]);
            }
        } else {
            Bundle.getLogger(IvmlElementIdentifier.class).warn("No pipeline information found for: "
                + arraySegments[1]);
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
            String normalizedName = REVERSE_RUNTIME_VAR_NORMALIZATION.get(varName);
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
    
    /**
     * Part of the static block, adds a mapping between class name of an obervable and the algorithm item
     * to the map.
     * @param observable The implementing obervable enumeration.
     * @param variableName The name of the model element.
     */
    private static void putAlgorithmMapping(IObservable observable, String variableName) {
        RUNTIME_ALGORITHM_NORMALIZATION.put(observable.name(), variableName);
    }
    /**
     * Part of the static block, adds a mapping between class name of an obervable and the model item to the two
     * maps. Not suitable for algorithms as they have different slot names for the same observables as the other
     * elements.
     * @param observable The implementing obervable enumeration.
     * @param variableName The name of the model element.
     */
    private static void put(IObservable observable, String variableName) {
        put(observable.name(), variableName);
    }
    
    /**
     * Part of the static block, adds a mapping between class name of an obervable and the model item to the two
     * maps. Not suitable for algorithms as they have different slot names for the same observables as the other
     * elements.
     * @param observableName The name of the implementing obervable enumeration.
     * @param variableName The name of the model element.
     */
    private static void put(String observableName, String variableName) {
        RUNTIME_VAR_NORMALIZATION.put(observableName, variableName);
        REVERSE_RUNTIME_VAR_NORMALIZATION.put(variableName, observableName);
    }
}
