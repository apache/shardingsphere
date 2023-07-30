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
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.expr.hotsopt.HotspotInlineExpressionParser;
import org.apache.shardingsphere.infra.expr.spi.InlineExpressionParser;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.TypeLiteral;
import org.graalvm.polyglot.Value;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Espresso inline expression parser.
 */
public final class EspressoInlineExpressionParser implements InlineExpressionParser {
    
    private static final String JAVA_CLASSPATH;
    
    private static final String JAVA_HOME;
    
    static {
        JAVA_HOME = System.getenv("JAVA_HOME");
        URL resource = Thread.currentThread().getContextClassLoader().getResource("espresso-need-libs");
        String dir = null != resource ? resource.getPath() : null;
        JAVA_CLASSPATH = Stream.of("groovy.jar", "guava.jar", "shardingsphere-infra-expr-hotsopt.jar", "shardingsphere-infra-expr-spi.jar", "shardingsphere-infra-util.jar")
                .map(each -> dir + File.separator + each).collect(Collectors.joining(":"));
    }
    
    @Override
    public String handlePlaceHolder(final String inlineExpression) {
        try (Context context = createContext()) {
            return createInlineExpressionParser(context).invokeMember("handlePlaceHolder", inlineExpression).asString();
        }
    }
    
    @Override
    public List<String> splitAndEvaluate(final String inlineExpression) {
        try (Context context = createContext()) {
            List<String> listProjection = createInlineExpressionParser(context).invokeMember("splitAndEvaluate", inlineExpression)
                    .as(new TypeLiteral<List<String>>() {
                    });
            // org.graalvm.polyglot.Value#as only creates projections for classes in Truffle Context
            return new ArrayList<>(listProjection);
        }
    }
    
    @Override
    public Closure<?> evaluateClosure(final String inlineExpression) {
        try (Context context = createContext()) {
            return createInlineExpressionParser(context).invokeMember("evaluateClosure", inlineExpression).as(Closure.class);
        }
    }
    
    private Value createInlineExpressionParser(final Context context) {
        return context.getBindings("java").getMember(HotspotInlineExpressionParser.class.getName()).newInstance();
    }
    
    private Context createContext() {
        // TODO https://github.com/oracle/graal/issues/4555 not yet closed
        ShardingSpherePreconditions.checkNotNull(JAVA_HOME, () -> new RuntimeException("Failed to determine the system's environment variable JAVA_HOME!"));
        return Context.newBuilder()
                .allowAllAccess(true)
                .option("java.Properties.org.graalvm.home", JAVA_HOME)
                .option("java.MultiThreaded", Boolean.TRUE.toString())
                .option("java.Classpath", JAVA_CLASSPATH)
                .build();
    }
    
    @Override
    public String getType() {
        return "ESPRESSO";
    }
}
