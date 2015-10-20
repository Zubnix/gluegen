/**
 * Package scope generation of {@link CStruct}s
 * avoiding Java8 issues w/ annotation processing
 * where the generated class is not yet available.
 * <p>
 * See Bug 923.
 * </p>
 * @see BuildStruct01
 */
@CStructs({@CStruct(name="RenderingConfig", header="TestStruct01.h"), @CStruct(name="Pixel", header="TestStruct02.h")})
/**
 * Package scope generation of gluegen code. Based on:
 * <pre>
< gluegen src="${test.junit.generation.dir}/test1-gluegen.c"
                outputRootDir= "${build_t.gen}"
                config="${test.junit.generation.dir}/test1p2-gluegen.cfg"
                literalInclude="${test.junit.generation.dir}"
                includeRefid="stub.includes.fileset.test"
                emitter="com.jogamp.gluegen.procaddress.ProcAddressEmitter"
                dumpCPP="false"
                debug="false"
                logLevel="INFO">
                <classpath refid="gluegen.classpath" />
 </gluegen>
 </pre>
  */
//@GlueGen(
//            outputRootDir= "../build/test/build/gensrc",
//            cfgFiles = {"../src/junit/com/jogamp/junit/util/gluegen/test/junit/generation/test1p2-gluegen.cfg"},
//            emitterClass = ProcAddressEmitter.class,
//            fileName = "$../src/junit/com/jogamp/gluegen/test/junit/generation/test1-gluegen.c"
//        )
package com.jogamp.gluegen.test.junit.generation.annotation;

import com.jogamp.gluegen.annotation.CStructs;
import com.jogamp.gluegen.annotation.CStruct;
import com.jogamp.gluegen.annotation.GlueGen;
import com.jogamp.gluegen.procaddress.ProcAddressEmitter;

