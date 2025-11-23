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
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.expr.core.GroovyUtils;
import org.apache.shardingsphere.infra.expr.spi.InlineExpressionParser;

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
    
    static {
        URL groovyJarUrl = EspressoInlineExpressionParser.class.getClassLoader().getResource("build/libs/groovy.jar");
        ShardingSpherePreconditions.checkNotNull(groovyJarUrl, NullPointerException::new);
        JAVA_CLASSPATH = groovyJarUrl.getPath();
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
        try (ReflectContext context = new ReflectContext(JAVA_CLASSPATH)) {
            if (Strings.isNullOrEmpty(inlineExpression)) {
                return Collections.emptyList();
            }
            return flatten(evaluate(context, GroovyUtils.split(handlePlaceHolder(inlineExpression))));
        }
    }
    
    private List<ReflectValue> evaluate(final ReflectContext context, final List<String> inlineExpressions) {
        List<ReflectValue> result = new ArrayList<>(inlineExpressions.size());
        for (String each : inlineExpressions) {
            StringBuilder expression = new StringBuilder(handlePlaceHolder(each));
            if (!each.startsWith("\"")) {
                expression.insert(0, '"');
            }
            if (!each.endsWith("\"")) {
                expression.append('"');
            }
            result.add(evaluate(context, expression.toString()));
        }
        return result;
    }
    
    private ReflectValue evaluate(final ReflectContext context, final String expression) {
        return context.getBindings("java")
                .getMember("groovy.lang.GroovyShell")
                .newInstance()
                .invokeMember("parse/(Ljava/lang/String;)Lgroovy/lang/Script;", expression)
                .invokeMember("run/()Ljava/lang/Object;");
    }
    
    /**
     * Flatten.
     *
     * @param segments Actually corresponds to some class instance of {@link java.lang.Object}.
     *                 This Object may or may not correspond to a class instance of `groovy.lang.GString`.
     * @return List of String
     */
    private List<String> flatten(final List<ReflectValue> segments) {
        List<String> result = new ArrayList<>();
        for (ReflectValue each : segments) {
            if (each.isString()) {
                result.add(each.as(String.class));
            } else {
                result.addAll(assemblyCartesianSegments(each));
            }
        }
        return result;
    }
    
    /**
     * Assembly cartesian segments.
     *
     * @param segment Actually corresponds to a class instance of `groovy.lang.GString`.
     * @return List of String
     */
    private List<String> assemblyCartesianSegments(final ReflectValue segment) {
        Set<List<String>> cartesianValues = getCartesianValues(segment);
        List<String> result = new ArrayList<>(cartesianValues.size());
        for (List<String> each : cartesianValues) {
            result.add(assemblySegment(each, segment));
        }
        return result;
    }
    
    /**
     * Get cartesian values.
     *
     * @param segment Actually corresponds to a class instance of `groovy.lang.GString`.
     * @return A Set consisting of a List of Strings
     */
    @SuppressWarnings("unchecked")
    private Set<List<String>> getCartesianValues(final ReflectValue segment) {
        Object[] segmentAsObjectArray = segment.invokeMember("getValues/()[Ljava/lang/Object;").as(Object[].class);
        List<Set<String>> result = new ArrayList<>(segmentAsObjectArray.length);
        for (Object each : segmentAsObjectArray) {
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
    
    /**
     * Assembly segment.
     *
     * @param cartesianValue List of String
     * @param segment        Actually corresponds to a class instance of `groovy.lang.GString`.
     * @return {@link java.lang.String}
     */
    private String assemblySegment(final List<String> cartesianValue, final ReflectValue segment) {
        String[] segmentAsStringArray = segment.invokeMember("getStrings/()[Ljava/lang/String;").as(String[].class);
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < segmentAsStringArray.length; i++) {
            result.append(segmentAsStringArray[i]);
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
