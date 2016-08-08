/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.config.common.internal.parser;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import groovy.lang.GString;
import groovy.lang.GroovyShell;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 行内配置解析器.
 * 
 * @author gaohongtao
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class InlineParser {
    
    private static final char SPLITTER = ',';
    
    private final String inlineExpression;
    
    /**
     * 分隔行内配置.
     * 
     * @return 分隔后的配置集合
     */
    public List<String> split() {
        return Splitter.on(SPLITTER).trimResults().splitToList(inlineExpression);
    }
    
    /**
     * 分隔并求inline表达式值.
     *
     * @return 求值后的配置集合
     */
    public List<String> evaluate() {
        final GroovyShell shell = new GroovyShell();
        return flattenSegments(Lists.transform(splitWithInlineExpression(), new Function<String, Object>() {
            
            @Override
            public Object apply(final String input) {
                StringBuilder expression = new StringBuilder(input);
                if (!input.startsWith("\"")) {
                    expression.insert(0, "\"");
                }
                if (!input.endsWith("\"")) {
                    expression.append("\"");
                }
                return shell.evaluate(expression.toString());
            }
        }));
    }
    
    List<String> splitWithInlineExpression() {
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
    
    private List<String> flattenSegments(final List<Object> segments) {
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
    
    @SuppressWarnings("unchecked")
    private Set<List<String>> getCartesianValues(final GString segment) {
        List<Set<String>> result = new ArrayList<>(segment.getValues().length);
        for (Object each : segment.getValues()) {
            if (null == each) {
                continue;
            }
            if (each instanceof Collection) {
                result.add(Sets.newHashSet(Collections2.transform((Collection<Object>) each, new Function<Object, String>() {
                    
                    @Override
                    public String apply(final Object input) {
                        return input.toString();
                    }
                })));
            } else {
                result.add(Sets.newHashSet(each.toString()));
            }
        }
        return Sets.cartesianProduct(result);
    }
}
