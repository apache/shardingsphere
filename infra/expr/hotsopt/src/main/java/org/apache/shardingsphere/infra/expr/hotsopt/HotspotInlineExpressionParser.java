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

package org.apache.shardingsphere.infra.expr.hotsopt;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import groovy.lang.Closure;
import groovy.lang.GString;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.apache.shardingsphere.infra.expr.spi.InlineExpressionParser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Hotspot inline expression parser.
 */
public final class HotspotInlineExpressionParser implements InlineExpressionParser {
    
    private static final char SPLITTER = ',';
    
    private static final Map<String, Script> SCRIPTS = new ConcurrentHashMap<>();
    
    private static final GroovyShell SHELL = new GroovyShell();
    
    @Override
    public String handlePlaceHolder(final String inlineExpression) {
        return inlineExpression.contains("$->{") ? inlineExpression.replaceAll("\\$->\\{", "\\$\\{") : inlineExpression;
    }
    
    @Override
    public List<String> splitAndEvaluate(final String inlineExpression) {
        return Strings.isNullOrEmpty(inlineExpression) ? Collections.emptyList() : flatten(evaluate(split(inlineExpression)));
    }
    
    @Override
    public Closure<?> evaluateClosure(final String inlineExpression) {
        return (Closure<?>) evaluate("{it -> \"" + inlineExpression + "\"}");
    }
    
    private List<Object> evaluate(final List<String> inlineExpressions) {
        List<Object> result = new ArrayList<>(inlineExpressions.size());
        for (String each : inlineExpressions) {
            StringBuilder expression = new StringBuilder(handlePlaceHolder(each));
            if (!each.startsWith("\"")) {
                expression.insert(0, '"');
            }
            if (!each.endsWith("\"")) {
                expression.append('"');
            }
            result.add(evaluate(expression.toString()));
        }
        return result;
    }
    
    private Object evaluate(final String expression) {
        Script script;
        if (SCRIPTS.containsKey(expression)) {
            script = SCRIPTS.get(expression);
        } else {
            script = SHELL.parse(expression);
            SCRIPTS.put(expression, script);
        }
        return script.run();
    }
    
    private List<String> split(final String inlineExpression) {
        List<String> result = new ArrayList<>();
        StringBuilder segment = new StringBuilder();
        int bracketsDepth = 0;
        for (int i = 0; i < inlineExpression.length(); i++) {
            char each = inlineExpression.charAt(i);
            switch (each) {
                case SPLITTER:
                    if (bracketsDepth > 0) {
                        segment.append(each);
                    } else {
                        result.add(segment.toString().trim());
                        segment.setLength(0);
                    }
                    break;
                case '$':
                    if ('{' == inlineExpression.charAt(i + 1)) {
                        bracketsDepth++;
                    }
                    if ("->{".equals(inlineExpression.substring(i + 1, i + 4))) {
                        bracketsDepth++;
                    }
                    segment.append(each);
                    break;
                case '}':
                    if (bracketsDepth > 0) {
                        bracketsDepth--;
                    }
                    segment.append(each);
                    break;
                default:
                    segment.append(each);
                    break;
            }
        }
        if (segment.length() > 0) {
            result.add(segment.toString().trim());
        }
        return result;
    }
    
    private List<String> flatten(final List<Object> segments) {
        List<String> result = new ArrayList<>();
        for (Object each : segments) {
            if (each instanceof GString) {
                result.addAll(assemblyCartesianSegments((GString) each));
            } else {
                result.add(each.toString());
            }
        }
        return result;
    }
    
    private List<String> assemblyCartesianSegments(final GString segment) {
        Set<List<String>> cartesianValues = getCartesianValues(segment);
        List<String> result = new ArrayList<>(cartesianValues.size());
        for (List<String> each : cartesianValues) {
            result.add(assemblySegment(each, segment));
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private Set<List<String>> getCartesianValues(final GString segment) {
        List<Set<String>> result = new ArrayList<>(segment.getValues().length);
        for (Object each : segment.getValues()) {
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
    
    private String assemblySegment(final List<String> cartesianValue, final GString segment) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < segment.getStrings().length; i++) {
            result.append(segment.getStrings()[i]);
            if (i < cartesianValue.size()) {
                result.append(cartesianValue.get(i));
            }
        }
        return result.toString();
    }
    
    @Override
    public String getType() {
        return "HOTSPOT";
    }
    
    @Override
    public boolean isDefault() {
        return true;
    }
}
