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
package eu.qualimaster.easy.extension.debug;

import java.lang.reflect.Method;

import org.osgi.service.component.ComponentContext;

/**
 * Abstract debug utils.
 * 
 * @author Holger Eichelberger
 */
public abstract class AbstractDebug {

    // checkstyle: stop exception type check

    /**
     * Simulates Eclipse-DS initialization.
     * 
     * @param cls the class to be initialized
     */
    private static void initialize(Class<?> cls) {
        try {
            Method m = cls.getDeclaredMethod("activate", ComponentContext.class);
            m.setAccessible(true);
            Object o  = cls.newInstance();
            Object[] param = new Object[1];
            param[0] = null;
            m.invoke(o, param);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // checkstyle: resume exception type check

    /**
     * Pragmatic initialization of EASy.
     */
    protected static final void initialize() {
        initialize(de.uni_hildesheim.sse.IvmlParser.class);
        initialize(de.uni_hildesheim.sse.VilExpressionParser.class);
        initialize(de.uni_hildesheim.sse.vil.templatelang.VtlExpressionParser.class);
        initialize(net.ssehub.easy.reasoning.sseReasoner.Reasoner.class);
        initialize(de.uni_hildesheim.sse.vil.rt.RtVilExpressionParser.class);
        initialize(de.uni_hildesheim.sse.easy_producer.instantiator.model.BuiltIn.class);
        initialize(de.uni_hildesheim.sse.easy_producer.instantiator.model.rtVil.BuiltIn.class);
        initialize(de.uni_hildesheim.sse.easy.maven.Registration.class);
        initialize(eu.qualimaster.easy.extension.internal.Registration.class);
    }
    
    /**
     * Sleeps a certain time.
     * 
     * @param ms the ms to sleep
     */
    protected static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
        }
    }

}
