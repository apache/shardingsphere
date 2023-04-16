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

package org.apache.shardingsphere.infra.util.expr;

import groovy.lang.Closure;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.util.groovy.expr.JVMInlineExpressionParser;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.TypeLiteral;
import org.graalvm.polyglot.Value;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Espresso inline expression parser.
 */
public final class EspressoInlineExpressionParser implements JVMInlineExpressionParser {
    
    private static final Context POLYGLOT;
    
    static {
        // TODO https://github.com/oracle/graal/issues/4555 not yet closed
        String javaHome = System.getenv("JAVA_HOME");
        ShardingSpherePreconditions.checkNotNull(javaHome, () -> new RuntimeException("Failed to determine the system's environment variable JAVA_HOME!"));
        URL resource = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("espresso-need-libs"));
        String dir = resource.getPath();
        String javaClasspath = String.join(":", dir + "/groovy.jar", dir + "/guava.jar", dir + "/shardingsphere-infra-util-groovy.jar");
        POLYGLOT = Context.newBuilder().allowAllAccess(true)
                .option("java.Properties.org.graalvm.home", javaHome)
                .option("java.MultiThreaded", Boolean.TRUE.toString())
                .option("java.Classpath", javaClasspath)
                .build();
    }
    
    @Override
    public String handlePlaceHolder(final String inlineExpression) {
        return POLYGLOT.getBindings("java")
                .getMember("org.apache.shardingsphere.infra.util.expr.InlineExpressionParser").invokeMember("handlePlaceHolder", inlineExpression).asString();
    }
    
    @Override
    public List<String> splitAndEvaluate(final String inlineExpression) {
        List<String> splitAndEvaluate = getInlineExpressionParser().invokeMember("splitAndEvaluate", inlineExpression).as(new TypeLiteral<List<String>>() {
        });
        // GraalVM Truffle Espresso 22.3.1 has a different behavior for generic List than Hotspot.
        return splitAndEvaluate.isEmpty() ? Collections.emptyList() : splitAndEvaluate;
    }
    
    @Override
    public Closure<?> evaluateClosure(final String inlineExpression) {
        return getInlineExpressionParser().invokeMember("evaluateClosure", inlineExpression).as(Closure.class);
    }
    
    private Value getInlineExpressionParser() {
        return POLYGLOT.getBindings("java").getMember("org.apache.shardingsphere.infra.util.expr.InlineExpressionParser").newInstance();
    }
}
