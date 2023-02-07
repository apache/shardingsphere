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
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

import java.net.URL;
import java.util.Collections;
import java.util.List;

/**
 * Espresso Inline expression parser.
 */
public class EspressoInlineExpressionParser {
    
    private static final Context POLYGLOT;
    
    private final Value espressoInlineExpressionParser;
    
    static {
        // https://github.com/oracle/graal/issues/4555 not yet closed
        String javaHome = System.getenv("GRAALVM_HOME");
        if (javaHome == null) {
            javaHome = System.getenv("JAVA_HOME");
        }
        if (javaHome == null) {
            throw new RuntimeException("Failed to determine the system's environment variable GRAALVM_HOME or JAVA_HOME!");
        }
        System.setProperty("org.graalvm.home", javaHome);
        URL resource = Thread.currentThread().getContextClassLoader().getResource("espresso-need-libs");
        assert null != resource;
        String dir = resource.getPath();
        String javaClasspath = String.join(":", dir + "/groovy.jar", dir + "/guava.jar", dir + "/shardingsphere-infra-util.jar");
        POLYGLOT = Context.newBuilder().allowAllAccess(true)
                .option("java.MultiThreaded", "true")
                .option("java.Classpath", javaClasspath)
                .build();
    }
    
    public EspressoInlineExpressionParser(final String inlineExpression) {
        espressoInlineExpressionParser = POLYGLOT.getBindings("java")
                .getMember("org.apache.shardingsphere.infra.util.expr.InlineExpressionParser")
                .newInstance(inlineExpression);
    }
    
    /**
     * Replace all inline expression placeholders.
     *
     * @param inlineExpression inline expression with {@code $->}
     * @return result inline expression with {@code $}
     */
    public static String handlePlaceHolder(final String inlineExpression) {
        return POLYGLOT.getBindings("java")
                .getMember("org.apache.shardingsphere.infra.util.expr.InlineExpressionParser")
                .invokeMember("handlePlaceHolder", inlineExpression)
                .as(String.class);
    }
    
    /**
     * Split and evaluate inline expression.
     *
     * @return result list
     */
    @SuppressWarnings("unchecked")
    public List<String> splitAndEvaluate() {
        List<String> splitAndEvaluate = espressoInlineExpressionParser.invokeMember("splitAndEvaluate").as(List.class);
        // GraalVM Truffle Espresso CE 22.3.1 has a different behavior for generic List than Hotspot.
        return splitAndEvaluate.size() == 0 ? Collections.emptyList() : splitAndEvaluate;
    }
    
    /**
     * Evaluate closure.
     *
     * @return closure
     */
    public Closure<?> evaluateClosure() {
        return espressoInlineExpressionParser.invokeMember("evaluateClosure").as(Closure.class);
    }
}
