package com.jogamp.gluegen.annotation;


import com.jogamp.gluegen.GlueEmitter;
import com.jogamp.gluegen.JavaEmitter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a package as an invocation of GlueGen. GlueGen will be invoked by JavaC when it encounters this annotation.
 */
@Target(ElementType.PACKAGE)
@Retention(RetentionPolicy.SOURCE)
public @interface GlueGen {

    /**
     * (optional) adds dir to the include path. Similarly to a C compiler or preprocessor, GlueGen scans a set of directories to
     * locate header files it encounters in #include directives. Unlike most C preprocessors, however, GlueGen has no
     * default include path, so it is typically necessary to supply at least one option in order
     * to handle any #include directives in the file being parsed.
     *
     * @return
     */
    String[] includePaths() default {};

    /**
     *  (optional) uses emitterClass as the emitter class which will be used by GlueGen
     *  to generate the glue code. The emitter class must implement the com.sun.gluegen.GlueEmitter interface. If this
     *  option is not specified, a com.sun.gluegen.JavaEmitter will be used by default.
     * @return
     */
    Class<? extends GlueEmitter> emitterClass() default JavaEmitter.class;

    /**
     * adds cfgFile to the list of configuration files used to set up the chosen emitter. This is the means by which a
     * large number of options are passed in to the GlueGen tool and to the emitter in particular.
     * @return
     */
    String[] cfgFiles() default "";

    /**
     * selects the file from which GlueGen should read the C header file for which glue code should be generated.
     * This must be the last command-line argument, and only one filename argument is supported. To cause multiple
     * header files to be parsed, write a small .c file #including the multiple headers and point GlueGen at the .c file.
     * @return
     */
    String fileName();
}
