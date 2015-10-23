package com.jogamp.gluegen.annotation;

import com.jogamp.gluegen.GlueEmitter;

import javax.annotation.processing.Filer;
import javax.lang.model.element.PackageElement;


public interface AnnotationGlueEmitter extends GlueEmitter{
    void setFiler(final Filer filer);

    void setPackageElement(final PackageElement packageElement);

    void setGlueGen(GlueGen glueGen);
}
