/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.infra.expr.core;

import groovy.lang.Closure;
import org.apache.shardingsphere.infra.expr.spi.JVMInlineExpressionParser;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;

import java.util.List;

/**
 * Inline expression parser.
 */
public final class InlineExpressionParser {
    
    private static final boolean IS_SUBSTRATE_VM;
    
    private final JVMInlineExpressionParser jvmInlineExpressionParser;
    
    static {
        // workaround for https://github.com/helidon-io/helidon-build-tools/issues/858
        IS_SUBSTRATE_VM = "Substrate VM".equals(System.getProperty("java.vm.name"));
    }
    
    public InlineExpressionParser() {
        if (IS_SUBSTRATE_VM) {
            jvmInlineExpressionParser = TypedSPILoader.getService(JVMInlineExpressionParser.class, "ESPRESSO");
        } else {
            jvmInlineExpressionParser = TypedSPILoader.getService(JVMInlineExpressionParser.class, "HOTSPOT");
        }
    }
    
    /**
     * Replace all inline expression placeholders.
     *
     * @param inlineExpression inline expression with {@code $->}
     * @return result inline expression with {@code $}
     */
    public String handlePlaceHolder(final String inlineExpression) {
        if (IS_SUBSTRATE_VM) {
            return TypedSPILoader.getService(JVMInlineExpressionParser.class, "ESPRESSO").handlePlaceHolder(inlineExpression);
        } else {
            return TypedSPILoader.getService(JVMInlineExpressionParser.class, "HOTSPOT").handlePlaceHolder(inlineExpression);
        }
    }
    
    /**
     * Split and evaluate inline expression.
     *
     * @param inlineExpression inline expression
     * @return result list
     */
    public List<String> splitAndEvaluate(final String inlineExpression) {
        return jvmInlineExpressionParser.splitAndEvaluate(inlineExpression);
    }
    
    /**
     * Evaluate closure.
     *
     * @param inlineExpression inline expression
     * @return closure
     */
    public Closure<?> evaluateClosure(final String inlineExpression) {
        return jvmInlineExpressionParser.evaluateClosure(inlineExpression);
    }
}
