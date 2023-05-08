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

package org.apache.shardingsphere.infra.expr.espresso;

import groovy.lang.Closure;
import org.apache.shardingsphere.infra.expr.spi.JVMInlineExpressionParser;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
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

    private static final String JAVA_HOME;

    private static final String JAVA_CLASSPATH;

    static {
        // TODO https://github.com/oracle/graal/issues/4555 not yet closed
        JAVA_HOME = System.getenv("JAVA_HOME");
        ShardingSpherePreconditions.checkNotNull(JAVA_HOME, () -> new RuntimeException("Failed to determine the system's environment variable JAVA_HOME!"));
        URL resource = Objects.requireNonNull(EspressoInlineExpressionParser.class.getClassLoader().getResource("espresso-need-libs"));
        String dir = resource.getPath();
        JAVA_CLASSPATH = String.join(":", dir + "/groovy.jar", dir + "/guava.jar", dir + "/shardingsphere-infra-expr-hotsopt.jar");
    }
    
    @Override
    public String handlePlaceHolder(final String inlineExpression) {
        try (Context context = getContext()) {
            return context.getBindings("java")
                    .getMember("org.apache.shardingsphere.infra.expr.hotsopt.HotspotInlineExpressionParser")
                    .invokeMember("handlePlaceHolder", inlineExpression).asString();
        }
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
    
    @Override
    public String getType() {
        return "ESPRESSO";
    }
    
    private Value getInlineExpressionParser() {
        try (Context context = getContext()) {
            return context.getBindings("java")
                    .getMember("org.apache.shardingsphere.infra.expr.hotsopt.HotspotInlineExpressionParser")
                    .newInstance();
        }
    }

    private Context getContext() {
        return Context.newBuilder().allowAllAccess(true)
                .option("java.Properties.org.graalvm.home", JAVA_HOME)
                .option("java.MultiThreaded", Boolean.TRUE.toString())
                .option("java.Classpath", JAVA_CLASSPATH)
                .build();
    }
}
