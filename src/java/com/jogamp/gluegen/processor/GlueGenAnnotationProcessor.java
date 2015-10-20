package com.jogamp.gluegen.processor;


import com.jogamp.gluegen.GlueEmitter;
import com.jogamp.gluegen.annotation.GlueGen;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static java.lang.System.getProperty;

//TODO unit tests
public class GlueGenAnnotationProcessor extends AbstractProcessor {

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
            try {
                process(glueGen);
            }
            catch (FileNotFoundException e) {
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

    private void process(final GlueGen glueGen) throws FileNotFoundException {

        //TODO we probably want more (all) options supported by gluegen.
        final String[]     cfgFilesProperty = glueGen.cfgFiles();
        final List<String> cfgFiles         = new ArrayList<String>();
        Collections.addAll(cfgFiles,
                           cfgFilesProperty);

        final Class<? extends GlueEmitter> emitterClass = glueGen.emitterClass();

        final String filename = glueGen.fileName();

        final String[]     includePathsProperty = glueGen.includePaths();
        final List<String> includePaths         = new ArrayList<String>();
        for (String include : includePathsProperty) {
            final String[] paths = include.substring(2)
                                          .split(getProperty("path.separator"));
            includePaths.addAll(Arrays.asList(paths));
        }

        //TODO
        final String outputRootDir = null;

        final boolean copyCPPOutput2Stderr = false;

        new com.jogamp.gluegen.GlueGen().run(new BufferedReader(new FileReader(filename)),
                                             filename,
                                             emitterClass,
                                             includePaths,
                                             cfgFiles,
                                             outputRootDir,
                                             copyCPPOutput2Stderr);
    }
}
