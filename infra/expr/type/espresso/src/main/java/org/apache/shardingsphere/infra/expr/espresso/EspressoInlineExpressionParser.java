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

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import groovy.lang.GroovyShell;
import org.apache.shardingsphere.infra.expr.spi.InlineExpressionParser;
import org.apache.shardingsphere.infra.util.groovy.GroovyUtils;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Espresso inline expression parser.
 */
public final class EspressoInlineExpressionParser implements InlineExpressionParser {
    
    private static final String JAVA_CLASSPATH;
    
    private String inlineExpression;
    
    /**
     * TODO <a href="https://github.com/oracle/graal/issues/4555">espressoHome not defined</a> not yet closed.
     * Maybe sometimes we need `.option("java.Properties.org.graalvm.home", System.getenv("JAVA_HOME"))`
     *
     * @see org.graalvm.polyglot.Context
     */
    private final Context context = Context.newBuilder()
            .allowAllAccess(true)
            .option("java.Classpath", JAVA_CLASSPATH)
            .build();
    
    static {
        URL resource = EspressoInlineExpressionParser.class.getClassLoader().getResource("espresso-need-libs");
        String dir = null == resource ? null : resource.getPath();
        JAVA_CLASSPATH = dir + File.separator + "groovy.jar";
    }
    
    @Override
    public void init(final Properties props) {
        inlineExpression = props.getProperty(INLINE_EXPRESSION_KEY);
    }
    
    @Override
    public String handlePlaceHolder() {
        return handlePlaceHolder(inlineExpression);
    }
    
    /**
     * Replace all inline expression placeholders.
     *
     * @param inlineExpression inline expression with {@code $->}
     * @return result inline expression with {@code $}
     */
    private String handlePlaceHolder(final String inlineExpression) {
        return inlineExpression.contains("$->{") ? inlineExpression.replaceAll("\\$->\\{", "\\$\\{") : inlineExpression;
    }
    
    @Override
    public List<String> splitAndEvaluate() {
        return Strings.isNullOrEmpty(inlineExpression) ? Collections.emptyList() : flatten(evaluate(GroovyUtils.split(handlePlaceHolder(inlineExpression)), context));
    }
    
    private List<Value> evaluate(final List<String> inlineExpressions, final Context context) {
        List<Value> result = new ArrayList<>(inlineExpressions.size());
        for (String each : inlineExpressions) {
            StringBuilder expression = new StringBuilder(handlePlaceHolder(each));
            if (!each.startsWith("\"")) {
                expression.insert(0, '"');
            }
            if (!each.endsWith("\"")) {
                expression.append('"');
            }
            result.add(evaluate(expression.toString(), context));
        }
        return result;
    }
    
    private Value evaluate(final String expression, final Context context) {
        return context.getBindings("java")
                .getMember(GroovyShell.class.getName())
                .newInstance()
                .invokeMember("parse", expression)
                .invokeMember("run");
    }
    
    private List<String> flatten(final List<Value> segments) {
        List<String> result = new ArrayList<>();
        for (Value each : segments) {
            if (!each.isString()) {
                result.addAll(assemblyCartesianSegments(each));
            } else {
                result.add(each.toString());
            }
        }
        return result;
    }
    
    private List<String> assemblyCartesianSegments(final Value segment) {
        Set<List<String>> cartesianValues = getCartesianValues(segment);
        List<String> result = new ArrayList<>(cartesianValues.size());
        for (List<String> each : cartesianValues) {
            result.add(assemblySegment(each, segment));
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private Set<List<String>> getCartesianValues(final Value segment) {
        Object[] temp = segment.invokeMember("getValues").as(Object[].class);
        List<Set<String>> result = new ArrayList<>(temp.length);
        for (Object each : temp) {
            if (null == each) {
                continue;
            }
            if (each instanceof Collection) {
                result.add(((Collection<Object>) each).stream().map(Object::toString).collect(Collectors.toCollection(LinkedHashSet::new)));
            } else {
                result.add(Sets.newHashSet(each.toString()));
            }
        }
        return Sets.cartesianProduct(result);
    }
    
    private String assemblySegment(final List<String> cartesianValue, final Value segment) {
        String[] temp = segment.invokeMember("getStrings").as(String[].class);
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < temp.length; i++) {
            result.append(temp[i]);
            if (i < cartesianValue.size()) {
                result.append(cartesianValue.get(i));
            }
        }
        return result.toString();
    }
    
    @Override
    public String getType() {
        return "ESPRESSO";
    }
}
