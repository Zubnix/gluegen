/**
 * Copyright 2015 JogAmp Community. All rights reserved.
 * <p/>
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 * <p/>
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 * <p/>
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list
 * of conditions and the following disclaimer in the documentation and/or other materials
 * provided with the distribution.
 * <p/>
 * THIS SOFTWARE IS PROVIDED BY JogAmp Community ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JogAmp Community OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * <p/>
 * The views and conclusions contained in the software and documentation are those of the
 * authors and should not be interpreted as representing official policies, either expressed
 * or implied, of JogAmp Community.
 */

package com.jogamp.gluegen.annotation;


import com.jogamp.common.util.PropertyAccess;
import jogamp.common.Debug;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

//TODO unit tests

/**
 * @author Erik De Rijcke
 */
public class GlueGenAnnotationProcessor extends AbstractProcessor {

    private static final boolean DEBUG;

    public static final String INCLUDE_PATHS = "jogamp.gluegen.annotation.includePaths.";
    public static final String CFG_FILES     = "jogamp.gluegen.annotation.cfgFiles.";
    public static final String HEADER        = "jogamp.gluegen.annotation.header.";
    public static final String OUTPUT        = "jogamp.gluegen.annotation.output.";
    public static final String EMITTER       = "jogamp.gluegen.annotation.emitter.";

    static {
        Debug.initSingleton();
        DEBUG = PropertyAccess.isPropertyDefined("jogamp.gluegen.annotation.debug",
                                                 true);
        if (DEBUG) {
            com.jogamp.gluegen.GlueGen
                    .debug();
        }
    }

    private Elements              elementUtils;
    private Filer                 filer;
    private Messager              messager;
    private Types                 typeUtils;
    private ProcessingEnvironment processingEnv;

    @Override
    public synchronized void init(final ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
        super.init(processingEnv);
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        final Set<String> annotations = new LinkedHashSet<String>();
        annotations.add(GlueGen.class.getCanonicalName());
        return annotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_6;
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations,
                           final RoundEnvironment roundEnv) {
        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(GlueGen.class)) {
            final GlueGen glueGen = annotatedElement.getAnnotation(GlueGen.class);
            final PackageElement packageElement = elementUtils.getPackageOf(annotatedElement);
            try {
                process(packageElement,
                        glueGen);
            }
            catch (Exception e) {
                e.printStackTrace();
                error(annotatedElement,
                      e.getMessage());
            }
        }
        return true;
    }

    private void error(Element e,
                       String msg,
                       Object... args) {
        messager.printMessage(
                Diagnostic.Kind.ERROR,
                String.format(msg,
                              args),
                e);
    }

    private void process(final PackageElement packageElement,
                         final GlueGen glueGen) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        //get package context
        final String packageName = packageElement.getQualifiedName()
                                                 .toString();

        //include paths
        final String includePathsFlag = PropertyAccess.getProperty(INCLUDE_PATHS + packageName,
                                                                   false);
        final boolean overruleIncludePaths = includePathsFlag != null;
        final List<String> includePaths = getIncludePaths(glueGen,
                                                          includePathsFlag,
                                                          overruleIncludePaths);

        //cfg files
        final String cfgFilesFlag = PropertyAccess.getProperty(CFG_FILES + packageName,
                                                               false);
        final boolean overruleCfgFiles = cfgFilesFlag != null;
        final List<String> cfgFiles = getCfgFiles(glueGen,
                                                  packageName,
                                                  cfgFilesFlag,
                                                  overruleCfgFiles);

        //header
        final String headerFlag = PropertyAccess.getProperty(HEADER + packageName,
                                                             false);
        final boolean overruleHeader = headerFlag != null;
        final File header = getHeader(glueGen,
                                      packageName,
                                      headerFlag,
                                      overruleHeader);

        //output
        final String outputFlag = PropertyAccess.getProperty(OUTPUT + packageName,
                                                             false);
        final boolean overruleOutput = outputFlag != null;
        final String outputRootDir = getOutputRootDir(outputFlag,
                                                      overruleOutput,
                                                      header);

        //emitter
        final String emitterFlag = PropertyAccess.getProperty(EMITTER + packageName,
                                                              false);
        final boolean overruleEmitter = emitterFlag != null;
        final Class<? extends AnnotationGlueEmitter> emitterClass = getEmitterClass(glueGen,
                                                                                    emitterFlag,
                                                                                    overruleEmitter);
        final AnnotationGlueEmitter glueEmitter = emitterClass.newInstance();
        glueEmitter.setProcessingEnvironment(processingEnv);
        glueEmitter.setPackageElement(packageElement);
        glueEmitter.setGlueGen(glueGen);

        //invoke gluegen
        final boolean copyCPPOutput2Stderr = false;
        new com.jogamp.gluegen.GlueGen().run(new BufferedReader(new FileReader(header.getPath())),
                                             header.getPath(),
                                             glueEmitter,
                                             includePaths,
                                             cfgFiles,
                                             outputRootDir,
                                             copyCPPOutput2Stderr);
    }

    private Class<? extends AnnotationGlueEmitter> getEmitterClass(final GlueGen glueGen,
                                                                   final String emitterFlag,
                                                                   final boolean overruleEmitter) throws ClassNotFoundException {
        String className;
        if (overruleEmitter) {
            className = emitterFlag;
        }
        else {
            try {
                //Remember we're still in the compilation phase so we ask for the name to make sure the class it
                //actually loaded, (see comment in catch block). A class needs to be loaded in order to instantiate objects
                //from it.
                className = glueGen.emitter()
                                   .getName();

            }
            catch (MirroredTypeException e) {
                //Thrown when the emitter() class is not yet loaded, so we can not ask for it's name (nor instantiate objects from it).
                //Luckily the exception carries the class' meta-reflection object and we can use that to find it's name and load the class.
                DeclaredType classTypeMirror = (DeclaredType) e.getTypeMirror();
                TypeElement classTypeElement = (TypeElement) classTypeMirror.asElement();
                className = classTypeElement.getQualifiedName()
                                            .toString();
            }
        }
        return (Class<? extends AnnotationGlueEmitter>) Class.forName(className);
    }

    private String getOutputRootDir(final String outputFlag,
                                    final boolean overruleOutput,
                                    final File header) {
        final String outputRootDir;
        if (overruleOutput) {
            outputRootDir = outputFlag;
        }
        else {
            outputRootDir = header.getParent();
        }
        return outputRootDir;
    }

    private List<String> getIncludePaths(final GlueGen glueGen,
                                         final String includePathsFlag,
                                         final boolean overruleIncludePaths) {
        final List<String> includePaths = new ArrayList<String>();
        if (overruleIncludePaths) {
            final StringTokenizer stringTokenizer = new StringTokenizer(includePathsFlag,
                                                                        ",");
            while (stringTokenizer.hasMoreTokens()) {
                includePaths.add(stringTokenizer.nextToken());
            }
        }
        else {
            final String[] includePathsProperty = glueGen.includePaths();
            includePaths.addAll(Arrays.asList(includePathsProperty));
        }
        return includePaths;
    }

    private File getHeader(final GlueGen glueGen,
                           final String packageName,
                           final String headerFlag,
                           final boolean overruleHeader) throws IOException {
        final File header;
        if (overruleHeader) {
            header = new File(headerFlag);
        }
        else {
            final String filename = glueGen.header();
            header = locateSource(packageName,
                                  filename);
        }
        return header;
    }

    private List<String> getCfgFiles(final GlueGen glueGen,
                                     final String packageName,
                                     final String cfgFilesFlag,
                                     final boolean overruleCfgFiles) throws IOException {
        final List<String> cfgFiles = new ArrayList<String>();
        if (overruleCfgFiles) {
            final StringTokenizer stringTokenizer = new StringTokenizer(cfgFilesFlag,
                                                                        ",");
            while (stringTokenizer.hasMoreTokens()) {
                cfgFiles.add(stringTokenizer.nextToken());
            }
        }
        else {
            final String[] cfgFilesProperty = glueGen.cfgFiles();
            for (String cfgFileProperty : cfgFilesProperty) {
                final File cfgFile = locateSource(packageName,
                                                  cfgFileProperty);
                cfgFiles.add(cfgFile.getAbsolutePath());
            }
        }
        return cfgFiles;
    }


    private File locateSource(final String packageName,
                              final String relativeName) throws IOException {
        if (DEBUG) {
            System.err.println("GlueGen.locateSource.0: p " + packageName + ", r " + relativeName);
        }

        FileObject h;
        try {
            //some build systems compiler invocation starts from the root of the package...
            h = filer.getResource(StandardLocation.SOURCE_PATH,
                                  packageName,
                                  relativeName);
        }
        catch (IOException e) {
            //...and some don't. So we have to look again with an empty package argument.
            h = filer.getResource(StandardLocation.SOURCE_PATH,
                                  "",
                                  relativeName);
        }
        if (DEBUG) {
            System.err.println("GlueGen.locateSource.1: h " + h.toUri());
        }
        final File f = new File(h.toUri()
                                 .getPath()); // URI is incomplete (no scheme), hence use path only!
        if (f.exists()) {
            return f;
        }
        else {
            throw new FileNotFoundException(f + " not found.");
        }
    }
}
