package com.jogamp.gluegen.annotation;

import com.jogamp.common.util.PropertyAccess;
import com.jogamp.gluegen.procaddress.ProcAddressEmitter;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.PackageElement;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

public class ProcAddressAnnotationGlueEmitter extends ProcAddressEmitter implements AnnotationGlueEmitter {
    private Filer          filer;
    private PackageElement packageElement;

    private final Set<PrintWriter> writers = new HashSet<PrintWriter>();

    @Override
    public void setPackageElement(final PackageElement packageElement) {
        this.packageElement = packageElement;
    }

    @Override
    public void setGlueGen(final GlueGen glueGen) {
        //not using
    }

    @Override
    public void setProcessingEnvironment(final ProcessingEnvironment processingEnv) {
        this.filer = processingEnv.getFiler();
    }

    @Override
    protected PrintWriter openFile(final String filename,
                                   final String simpleClassName) throws IOException {
        //if the output was set through a compiler flag, we don't use the filer to output our source files
        final String outputFlag = PropertyAccess.getProperty(GlueGenAnnotationProcessor.OUTPUT + packageElement.getQualifiedName(),
                                                             false);
        final boolean overruleOutput = outputFlag != null;
        if (overruleOutput) {
            return super.openFile(filename,
                                  simpleClassName);
        }

        final Writer writer;
        if (filename.endsWith(".java")) {
            final JavaFileObject sourceFile = filer.createSourceFile(packageElement.getQualifiedName() + "." + simpleClassName,
                                                                     packageElement);
            writer = sourceFile.openWriter();
        }
        else {
            final FileObject resourceFile = filer.createResource(StandardLocation.SOURCE_OUTPUT,
                                                                 packageElement.getQualifiedName(),
                                                                 new File(filename).getName(),
                                                                 packageElement);
            writer = resourceFile.openWriter();
        }

        final PrintWriter printWriter = new PrintWriter(new BufferedWriter(writer));
        writers.add(printWriter);

        return printWriter;
    }

    @Override
    public void endEmission() {
        super.endEmission();
        for (PrintWriter writer : writers) {
            writer.flush();
            writer.close();
        }
    }
}
