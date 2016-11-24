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
package eu.qualimaster.easy.extension.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.qualimaster.common.QMInternal;
import eu.qualimaster.common.QMSupport;
import eu.qualimaster.coordination.commands.CoordinationCommand;
import eu.qualimaster.events.IEvent;
import eu.qualimaster.observables.IObservable;
import net.ssehub.easy.basics.logger.EASyLoggerFactory;
import net.ssehub.easy.basics.logger.EASyLoggerFactory.EASyLogger;
import net.ssehub.easy.instantiation.core.model.common.VilException;
import net.ssehub.easy.instantiation.core.model.vilTypes.BuiltIn;
import net.ssehub.easy.instantiation.core.model.vilTypes.FieldDescriptor;
import net.ssehub.easy.instantiation.core.model.vilTypes.ILazyDescriptor;
import net.ssehub.easy.instantiation.core.model.vilTypes.IRegistration;
import net.ssehub.easy.instantiation.core.model.vilTypes.IVilType;
import net.ssehub.easy.instantiation.core.model.vilTypes.OperationDescriptor;
import net.ssehub.easy.instantiation.core.model.vilTypes.ReflectionResolver;
import net.ssehub.easy.instantiation.core.model.vilTypes.TypeDescriptor;
import net.ssehub.easy.instantiation.core.model.vilTypes.TypeRegistry;
import net.ssehub.easy.instantiation.rt.core.model.rtVil.RtVilStorage;
import net.ssehub.easy.instantiation.rt.core.model.rtVil.types.RtVilTypeRegistry;

/**
 * Registers QM Java additions to EASy-Producer, in particular to rt-VIL. Unless configured otherwise, it reads a list 
 * of class names as a resource produced by just executing this class. This bridges QM-specific classes with 
 * EASy-Producer (original OSGI execution), as well as infrastructure execution (OSGI through standalone). In the EASy
 * case, the underlying classes are taken from the bundle classpath. For the standalone execution, the libraries
 * are left out while bundling and before Maven deployment and taken from the actual infrastructure. For executing 
 * within EASy standalone (QM), it is safe to assume that the OSGI interfaces are present.
 * 
 * @author Holger Eichelberger
 */
public class Registration implements IRegistration {

    public static final String QM_LIB_PROPERTY = "eu.qualiMaster.rtVil.libs";
    public static final String RESOURCE_CLASS_LIST = "qmExtension.list";
    
    private static final String CLASS_EXTENSION = ".class";
    private static final Logging LOGGING;
    private static final TypeAnalyzer ANALYZER = new TypeAnalyzer();

    private static boolean registered = false;
    private static boolean debug = false;
    private static ClassLoader loader = Registration.class.getClassLoader();
    
    /**
     * A simple logging abstractor.
     * 
     * @author Holger Eichelberger
     */
    private interface Logging {
        
        /**
         * Logs an error.
         * 
         * @param message the error message
         */
        public void error(String message);

        /**
         * Logs an error.
         * 
         * @param message the error message
         * @param exception the related exception
         */
        public void error(String message, Exception exception);
        
        /**
         * Logs an information message.
         * 
         * @param message the information
         */
        public void info(String message);
    }
    
    static {
        Logging tmp;
        if (null != System.getProperty(QM_LIB_PROPERTY, null)) {
            tmp = new Logging() {

                private Logger logger = LoggerFactory.getLogger(Registration.class);
                
                @Override
                public void error(String message) {
                    logger.error(message);
                }

                @Override
                public void error(String message, Exception exception) {
                    logger.error(message, exception);
                }

                @Override
                public void info(String message) {
                    logger.info(message);
                }
                
            };
        } else {
            tmp = new Logging() {

                private EASyLogger logger = EASyLoggerFactory.INSTANCE.getLogger(Registration.class, 
                    "QualiMaster.Extension");
                
                @Override
                public void error(String message) {
                    logger.error(message);
                }

                @Override
                public void error(String message, Exception exception) {
                    logger.error(message);
                    logger.exception(exception);
                }

                @Override
                public void info(String message) {
                    logger.info(message);
                }
                
            };
        }
        LOGGING = tmp;
    }
    
    /**
     * Obtains a JAR file from a resource stream.
     * 
     * @param loader the class loader to consider
     * @param name the name of the resource
     * @param result the JAR files to be modified as a side effect
     */
    private static void obtainStreamFromClassLoader(ClassLoader loader, String name, List<JarInputStream> result) {
        InputStream stream = loader.getResourceAsStream(name);
        if (null != stream) {
            try {
                result.add(new JarInputStream(stream));
            } catch (IOException e) {
                LOGGING.error("Resource " + name + ":" + e.getMessage(), e);
            }
        } /*else {
            LOGGING.error("Resource does not exist: " + name);
        }*/
    }

    /**
     * Returns the relevant JAR files.
     * 
     * @param loader the class loader to use
     * @param jarLocations optional (authoritive) Jar locations to search separated by pathSeparator, 
     *   use <b>null</b> to ignore
     * 
     * @return the relevant JAR files
     */
    private static List<JarInputStream> getJars(ClassLoader loader, String jarLocations) {
        List<JarInputStream> result = new ArrayList<JarInputStream>();
        String libs = null != jarLocations ? jarLocations : System.getProperty("eu.qualiMaster.rtVil.libs", null);
        if (null == libs) {
            obtainStreamFromClassLoader(loader, "lib/CoordinationLayer.jar", result);
            obtainStreamFromClassLoader(loader, "lib/QualiMaster.Events.jar", result);
            obtainStreamFromClassLoader(loader, "lib/AdaptationLayer.jar", result);
        } else {
            String[] tmp = libs.split(File.pathSeparator);
            for (int i = 0; i < tmp.length; i++) {
                try {
                    result.add(new JarInputStream(new FileInputStream(new File(tmp[i]))));
                } catch (IOException e) {
                    LOGGING.error(e.getMessage(), e);
                }
            }
        }
        return result;
    }

    /**
     * Reads the class list to determine the classes to import from {@link #RESOURCE_CLASS_LIST}.
     * 
     * @param classes the class list (modified as a side effect)
     * @param loader the class loader
     * @param prefix to be used where the resource is located in
     */
    private static void readClassList(List<Class<?>> classes, ClassLoader loader, String prefix) {
        InputStream in = loader.getResourceAsStream(prefix + RESOURCE_CLASS_LIST);
        if (null != in) {
            try {
                LineNumberReader lnr = new LineNumberReader(new InputStreamReader(in));
                String line;
                do {
                    line = lnr.readLine();
                    if (null != line) {
                        try {
                            classes.add(loader.loadClass(line));
                        } catch (ClassNotFoundException e) {
                            LOGGING.error("Class not found " + e.getMessage());
                        }
                    }
                } while (null != line);
                in.close();
            } catch (IOException e) {
                LOGGING.error("Reading: " + e.getMessage(), e);
            }
        } else {
            LOGGING.info("Reading: resource " + RESOURCE_CLASS_LIST + " not found");
        }
    }

    /**
     * Scans given JAR files for classes to import.
     * 
     * @param classes the class list (modified as a side effect)
     * @param jarLocations optional (authoritive) Jar locations to search separated by pathSeparator, 
     *   use <b>null</b> to ignore
     * @param loader the class loader
     */
    private static void scanJars(List<Class<?>> classes, String jarLocations, ClassLoader loader) {
        List<JarInputStream> jars = getJars(loader, jarLocations);
        for (int i = 0; i < jars.size(); i++) {
            JarInputStream jis = jars.get(i);
            try {
                JarEntry entry;
                do {
                    entry = jis.getNextJarEntry();
                    if (null != entry && !entry.isDirectory()) {
                        String name = entry.getName();
                        if (name.endsWith(CLASS_EXTENSION) && -1 == name.indexOf('$')) {
                            name = name.substring(0, name.length() - CLASS_EXTENSION.length());
                            name = name.replace("/", ".");
                            considerImporting(loader, name, classes);
                        }
                    }
                } while (null != entry);
                jis.close();
            } catch (IOException e) {
                LOGGING.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Writes the classes list to {@link #RESOURCE_CLASS_LIST}.
     * 
     * @param classes the classes to write
     */
    private static void writeClassList(List<Class<?>> classes) {
        try {
            PrintStream out = new PrintStream(new FileOutputStream(new File("resources", RESOURCE_CLASS_LIST)));
            for (Class<?> cls : classes) {
                out.println(cls.getName());
            }
            out.close();
        } catch (IOException e) {
            LOGGING.error("Writing: " + e.getMessage(), e);
        }
    }
    
    /**
     * Explicitly sets the class loader.
     * 
     * @param userLoader the class loader to use (ignored if <b>null</b>)
     */
    public static final void setClassLoader(ClassLoader userLoader) {
        if (null != userLoader) {
            loader = userLoader;
        }
    }
    
    /**
     * Registers the Java artifacts, instantiators and types.
     * 
     * @param jarLocations optional (authoritive) Jar locations to search separated by pathSeparator, 
     *   use <b>null</b> to ignore
     */
    public static final void register(String jarLocations) {
        if (!registered) {
            registered = true;
            RtVilStorage.setStorageHint(true); // will provide storage at runtime -> AdaptationLayer internal
            RtVilTypeRegistry.setTypeAnalyzer(ANALYZER);
            TypeRegistry regSave = ReflectionResolver.setTypeRegistry(RtVilTypeRegistry.INSTANCE);

            List<Class<?>> toImport = new LinkedList<Class<?>>();
            if (null == jarLocations) {
                readClassList(toImport, loader, "");
                if (toImport.isEmpty()) { // maven without, Eclipse with resources...
                    readClassList(toImport, loader, "resources/");    
                }
            }
            if (toImport.isEmpty()) {
                scanJars(toImport, jarLocations, loader);
                writeClassList(toImport);
            }
            
            ANALYZER.setImportingTypes(toImport);
            try {
                RtVilTypeRegistry.registerRtTypes(toImport);
            } catch (VilException e) {
                LOGGING.error("While registering " + e.getMessage(), e);
            }
            if (debug) {
                Collections.sort(toImport, new ClassNameComparator());
                for (Class<?> cls : toImport) {
                    String name = ANALYZER.getVilName(cls);
                    TypeDescriptor<?> desc = RtVilTypeRegistry.INSTANCE.getType(name);
                    if (null != desc) {
                        System.out.println("    * " + desc.getName() + " / " + desc.getQualifiedName());
                        for (int f = 0; f < desc.getFieldCount(); f++) {
                            FieldDescriptor fDesc = desc.getField(f);
                            System.out.println("        * " + fDesc.getSignature());
                        }
                        printOperations(desc);
                    } else {
                        System.out.println("NOT FOUND " + cls.getName());
                    }
                }
            }
            ReflectionResolver.setTypeRegistry(regSave);
        }
    }

    /**
     * Prints all operations in <code>desc</code>.
     * 
     * @param desc the descriptor to print the operations for
     */
    private static void printOperations(TypeDescriptor<?> desc) {
        for (int o = 0; o < desc.getOperationsCount(); o++) {
            OperationDescriptor oDesc = desc.getOperation(o);
            String ret = oDesc.getReturnType().getVilName() + " ";
            if (oDesc.isConstructor()) {
                ret = "";
            }
            if (ret.startsWith("PseudoVoid")) {
                ret = "";
            }
            System.out.println("        * " + ret + oDesc.getSignature());
        }
    }
    
    /**
     * Implements a class name comparator (for debugging).
     * 
     * @author Holger Eichelberger
     */
    private static class ClassNameComparator implements Comparator<Class<?>> {

        /**
         * Returns the VIL name (or the class name).
         * 
         * @param cls the class
         * @return the VIL name
         */
        private static String getName(Class<?> cls) {
            String name = ANALYZER.getVilName(cls);
            if (null == name) {
                name = cls.getName();
            }
            return name;
        }
        
        @Override
        public int compare(Class<?> cls1, Class<?> cls2) {
            return getName(cls1).compareTo(getName(cls2));
        }
        
    }
    
    /**
     * Returns whether the class is marked as internal to the QualiMaster infrastructure.
     * 
     * @param cls the class to consider
     * @return <code>true</code> if the class is considered to be invisible
     */
    private static boolean isQmInternal(Class<?> cls) {
        return null != cls.getAnnotation(QMInternal.class);
    }
    
    /**
     * Returns whether the class is marked as support to the QualiMaster infrastructure.
     * 
     * @param cls the class to consider
     * @return <code>true</code> if the class is considered to be supporting
     */
    private static boolean isQmSupport(Class<?> cls) {
        return null != cls.getAnnotation(QMSupport.class);
    }
    
    /**
     * Considers a class for importing into rt-VIL.
     * 
     * @param loader the class loader to use
     * @param className the name of the class
     * @param toImport the classes to import (modified as a side effect)
     */
    private static void considerImporting(ClassLoader loader, String className, List<Class<?>> toImport) {
        try {
            Class<?> cls = loader.loadClass(className); // do not link
            if (Modifier.isPublic(cls.getModifiers()) && !isQmInternal(cls)) {
                if (CoordinationCommand.class.isAssignableFrom(cls)) {
                    toImport.add(cls);
                } else if (IEvent.class.isAssignableFrom(cls)) {
                    toImport.add(cls);
                } else if (IObservable.class.isAssignableFrom(cls)) {
                    toImport.add(cls);
                } else if (isQmSupport(cls)) {
                    toImport.add(cls);
                }
            }
        } catch (ClassNotFoundException e) {
            LOGGING.info("Loading " + className + ": " + e.getMessage());
        } catch (NoClassDefFoundError e) {
            LOGGING.info("Loading " + className + ": " + e.getMessage());
        } 

    }
    
    /**
     * Private method to activate plugin.
     * 
     * @param context Context.
     */
    protected void activate(ComponentContext context) {
        // this is not the official way of using DS but the official way is unstable
        register(null);

        RtVilTypeRegistry.setTypeAnalyzer(ANALYZER);
        TypeRegistry regSave = ReflectionResolver.setTypeRegistry(RtVilTypeRegistry.INSTANCE);

        List<TypeDescriptor<?>> instantiators = new ArrayList<TypeDescriptor<?>>();
        registerInstantiator(PipelineHelper.class, instantiators);
        registerInstantiator(PipelineElementHelper.class, instantiators);
        
        registerInstantiator(RepositoryHelper.class, instantiators);
        registerInstantiator(HardwareRepositoryHelper.class, instantiators);
        registerInstantiator(CoordinationHelper.class, instantiators);
        registerInstantiator(ObservableHelper.class, instantiators);
        registerInstantiator(SubTopologyComponentsHelper.class, instantiators);
        registerInstantiator(SubTopologyScalingHelper.class, instantiators);
        registerInstantiator(InitializationModeHelper.class, instantiators);
        registerInstantiator(NameMappingHelper.class, instantiators);
        registerInstantiator(SubPipelineHelper.class, instantiators);

        registerInstantiator(AlgorithmPrediction.class, instantiators);
        registerInstantiator(ParameterPrediction.class, instantiators);
        registerInstantiator(SourceVolumePrediction.class, instantiators);
        registerInstantiator(ConstraintViolationConverter.class, instantiators);
        
        registerInstantiator(Weighting.class, instantiators);
        registerInstantiator(WeightingSelector.class, instantiators);
        
        registerInstantiator(BindValuesInstantiator.class, instantiators);
        
        ReflectionResolver.setTypeRegistry(regSave);

        if (debug) {
            System.out.println("    Instantiators:");
            for (TypeDescriptor<?> desc: instantiators) {
                printOperations(desc);
            }
            System.out.println();
            System.err.println("Please refresh the project and commit the changes.");
        }
    }

    // checkstyle: stop exception type check

    /**
     * Handles the registration of an instantiator.
     * 
     * @param cls the class to be registered
     * @param instantiators the instantiators (modified as a side effect)
     */
    private static void registerInstantiator(Class<? extends IVilType> cls, List<TypeDescriptor<?>> instantiators) {
        try {
            TypeDescriptor<?> desc = RtVilTypeRegistry.INSTANCE.register(cls);
            if (null != desc) {
                for (int o = 0; o < desc.getOperationsCount(); o++) {
                    OperationDescriptor od = desc.getOperation(o);
                    if (od instanceof ILazyDescriptor) {
                        ((ILazyDescriptor) od).forceInitialization();
                    }
                }
                instantiators.add(desc);
            }
        } catch (Throwable e) { // may happen in testing if not all layers are present
            LOGGING.info("Loading " + cls.getName() + ": " + e.getMessage());
        }
    }
    
    // checkstyle: resume exception type check

    /**
     * Private method to to de-activate plugin.
     * 
     * @param context Context.
     */
    protected void deactivate(ComponentContext context) {
        // this is not the official way of using DS but the official way is instable
    }
    
    /**
     * Just for testing / debugging.
     * 
     * @param args ignored for convenience
     */
    public static void main(String[] args) {
        BuiltIn.initialize();
        net.ssehub.easy.instantiation.rt.core.model.rtVil.BuiltIn.initialize();
        debug = true;
        
        // fixed according to the structure of this projects, shall be on classpath but not be deployed
        String[] jarNames = new String[] {"CoordinationLayer", "QualiMaster.Events", "AdaptationLayer", 
            "DataManagementLayer", "MonitoringLayer", "StormCommons"};
        String jarLocations = "";
        for (int i = 0; i < jarNames.length; i++) {
            if (i > 0) {
                jarLocations += File.pathSeparator;
            }
            jarLocations += "lib/" + jarNames[i] + ".jar";
        }
        register(jarLocations); 
        
        // just as a test
        Registration reg = new Registration();
        reg.activate(null);
        reg.deactivate(null);
    }
    
    /**
     * Logs an error.
     * 
     * @param message the error message
     */
    public static void error(String message) {
        LOGGING.error(message);
    }
    
}
