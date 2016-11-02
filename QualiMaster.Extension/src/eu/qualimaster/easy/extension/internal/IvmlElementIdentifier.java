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
import eu.qualimaster.monitoring.events.FrozenSystemState;
import eu.qualimaster.monitoring.systemState.TypeMapper;
import eu.qualimaster.monitoring.systemState.TypeMapper.TypeCharacterizer;
import eu.qualimaster.observables.IObservable;
import net.ssehub.easy.instantiation.core.model.vilTypes.configuration.AbstractIvmlVariable;
import net.ssehub.easy.instantiation.core.model.vilTypes.configuration.IvmlElement;
import net.ssehub.easy.instantiation.rt.core.model.confModel.AbstractVariableIdentifier;
import net.ssehub.easy.varModel.confModel.Configuration;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.model.ModelQueryException;

/**
 * Uses {@link IvmlElement}s and {@link IObservable}s to identify temporary value mappings inside the
 * {@link net.ssehub.easy.adaptiveVarModel.confModel.AdaptiveConfiguration}.
 * 
 * @author El-Sharkawy
 */
public class IvmlElementIdentifier extends AbstractVariableIdentifier<IvmlElementIdentifier.ObservableTuple> {

    private static final Map<String, String> RUNTIME_VAR_NORMALIZATION = new HashMap<String, String>();

    static {
        RUNTIME_VAR_NORMALIZATION.put("CAPACITY", "capacity");
        RUNTIME_VAR_NORMALIZATION.put("EXECUTORS", "executors");
        RUNTIME_VAR_NORMALIZATION.put("HOSTS", "hosts");
        RUNTIME_VAR_NORMALIZATION.put("IS_VALID", "isValid");
        RUNTIME_VAR_NORMALIZATION.put("IS_ENACTING", "isEnacting");
        RUNTIME_VAR_NORMALIZATION.put("ITEMS", "items");
        RUNTIME_VAR_NORMALIZATION.put("LATENCY", "latency");
        RUNTIME_VAR_NORMALIZATION.put("THROUGHPUT_ITEMS", "throughputItems");
        RUNTIME_VAR_NORMALIZATION.put("THROUGHPUT_VOLUME", "throughputVolume");
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
            System.out.println("New info for " + pipName);
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
        return null != id && StringUtils.countMatches(id, FrozenSystemState.SEPARATOR) > 1;
    }

    @Override
    protected Iterator<String> getIDIterator(final String id) {
        final List<String> segments = new ArrayList<String>();
        String[] arraySegments = id.split(FrozenSystemState.SEPARATOR);
        System.out.println(id);
        if (arraySegments[0].equals("Algorithm")) {
            PipelineContentsContainer infos = getPipelineInfos(arraySegments[1]);
            if (null != infos) {
                IDecisionVariable mappedVar = infos.getMappedAlgorithm(arraySegments[2]);
                if (null != mappedVar) {
                    segments.add(arraySegments[0]);
                    segments.add(mappedVar.getDeclaration().getName());
                    segments.add(arraySegments[arraySegments.length - 1]);
                } else {
                    System.out.println("No mapped infos for: " + arraySegments[2]);
                }
            } else {
                System.out.println("No pip infos for: " + arraySegments[1]);
            }
        } else {
            for (int i = 0; i < arraySegments.length; i++) {
                segments.add(arraySegments[i]);
            }
        }
        
        return new Iterator<String>() {
            
            private int index = 1;

            @Override
            public boolean hasNext() {
                return segments.size() > index;
            }

            @Override
            public String next() {
                String id;
                if (1 == index) {
                    index = Math.max(segments.size() - 2, 1);
                    id = segments.get(0) + FrozenSystemState.SEPARATOR + segments.get(index++);
                } else if ((segments.size() - 1) == index) {
                    id = segments.get(index++);
                    String mappedValue = RUNTIME_VAR_NORMALIZATION.get(id);
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
        }

        return id;
    }
}
