/**
 * Package scope generation of {@link CStruct}s
 * avoiding Java8 issues w/ annotation processing
 * where the generated class is not yet available.
 * <p>
 * See Bug 923.
 * </p>
 *
 * @see BuildStruct01
 */
@CStructs({@CStruct(name = "RenderingConfig",
                    header = "TestStruct01.h"),
           @CStruct(name = "Pixel",
                    header = "TestStruct02.h")})
@GlueGen(header = "test1.h"
         //cfg attribute is set in the ant test build xml.
)
package com.jogamp.gluegen.test.junit.generation.annotation;

import com.jogamp.gluegen.annotation.CStruct;
import com.jogamp.gluegen.annotation.CStructs;
import com.jogamp.gluegen.annotation.GlueGen;

