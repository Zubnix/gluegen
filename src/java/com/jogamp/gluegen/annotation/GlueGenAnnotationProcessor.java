/**
 * Copyright 2015 JogAmp Community. All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 * <p>
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 * <p>
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list
 * of conditions and the following disclaimer in the documentation and/or other materials
 * provided with the distribution.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY JogAmp Community ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JogAmp Community OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * <p>
 * The views and conclusions contained in the software and documentation are those of the
 * authors and should not be interpreted as representing official policies, either expressed
 * or implied, of JogAmp Community.
 */

package com.jogamp.gluegen.annotation;


import com.jogamp.common.util.PropertyAccess;
import com.jogamp.gluegen.JavaEmitter;
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
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static java.lang.System.getProperty;

//TODO unit tests

/**
 * @author Erik De Rijcke
 */
public class GlueGenAnnotationProcessor extends AbstractProcessor {

    static final boolean DEBUG;

    static {
        Debug.initSingleton();
        DEBUG = PropertyAccess.isPropertyDefined("jogamp.gluegen.annotation.debug",
                                                 true);
        if (DEBUG) {
            com.jogamp.gluegen.GlueGen
                    .debug();
        }
    }

    //TODO see what we we can use from these
    private Types    typeUtils;
    private Elements elementUtils;
    private Filer    filer;
    private Messager messager;

    @Override
    public synchronized void init(final ProcessingEnvironment processingEnv) {
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
            catch (IOException e) {
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
                         final GlueGen glueGen) throws IOException {
        final String packageName = packageElement.getQualifiedName()
                                                 .toString();

        final String[]     cfgFilesProperty = glueGen.cfgFiles();
        final List<String> cfgFiles         = new ArrayList<String>();

        for (String cfgFileProperty : cfgFilesProperty) {
            final File cfgFile = locateSource(packageName,
                                              cfgFileProperty);
            cfgFiles.add(cfgFile.getAbsolutePath());
        }

        final String filename = glueGen.header();
        final File file = locateSource(packageName,
                                       filename);

        final String[]     includePathsProperty = glueGen.includePaths();
        final List<String> includePaths         = new ArrayList<String>();
        for (String include : includePathsProperty) {
            final String[] paths = include.substring(2)
                                          .split(getProperty("path.separator"));
            includePaths.addAll(Arrays.asList(paths));
        }

        final boolean copyCPPOutput2Stderr = false;

        new com.jogamp.gluegen.GlueGen().run(new BufferedReader(new FileReader(file.getPath())),
                                             file.getPath(),
                                             new AnnotationProcessorJavaStructEmitter(filer,
                                                                                      packageElement),
                                             includePaths,
                                             cfgFiles,
                                             file.getParent(),
                                             copyCPPOutput2Stderr);
    }

    private File locateSource(final String packageName,
                              final String relativeName) throws IOException {
        if (DEBUG) {
            System.err.println("GlueGen.locateSource.0: p " + packageName + ", r " + relativeName);
        }
        final FileObject h = filer.getResource(StandardLocation.SOURCE_PATH,
                                               "",
                                               relativeName);
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

    private static class AnnotationProcessorJavaStructEmitter extends JavaEmitter {

        private final Filer          filer;
        private final PackageElement packageElement;

        public AnnotationProcessorJavaStructEmitter(Filer filer,
                                                    PackageElement packageElement) {
            this.filer = filer;
            this.packageElement = packageElement;
        }

        @Override
        protected PrintWriter openFile(final String filename,
                                       final String simpleClassName) throws IOException {
            if (filename.endsWith(".java")) {
                final JavaFileObject sourceFile = filer.createSourceFile(simpleClassName,
                                                                         packageElement);
                final Writer writer = sourceFile.openWriter();
                return new PrintWriter(new BufferedWriter(writer));
            }
            else {
                return super.openFile(filename,
                                      simpleClassName);
            }
        }
    }
}
