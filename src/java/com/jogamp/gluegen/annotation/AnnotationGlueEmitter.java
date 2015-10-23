package com.jogamp.gluegen.annotation;

import com.jogamp.gluegen.GlueEmitter;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.PackageElement;

/**
 * A glue emitter for use with the {@link GlueGen} annotation. An {@code AnnotationGlueEmitter} is special in the sense
 * that it operates in a compile time annotation context. In order to fully make use of this context, a number of
 * setters are defined which the emitter is free to make use of.
 */
public interface AnnotationGlueEmitter extends GlueEmitter {

    /**
     * The processing environment of the annotation processor.
     *
     * @param processingEnv
     */
    void setProcessingEnvironment(final ProcessingEnvironment processingEnv);

    /**
     * The package on which the annotation processor currently operates.
     *
     * @param packageElement
     */
    void setPackageElement(final PackageElement packageElement);

    /**
     * The annotation encountered by the processor.
     *
     * @param glueGen
     */
    void setGlueGen(GlueGen glueGen);
}
