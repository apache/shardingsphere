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

package org.apache.shardingsphere.infra.expr.groovy;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import groovy.lang.Closure;
import groovy.lang.GString;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import groovy.util.Expando;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.expr.core.GroovyUtils;
import org.apache.shardingsphere.infra.expr.spi.InlineExpressionParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Groovy inline expression parser.
 */
public final class GroovyInlineExpressionParser implements InlineExpressionParser {
    
    private static final String INLINE_EXPRESSION_KEY = "inlineExpression";
    
    private static final GroovyShell SHELL = new GroovyShell();
    
    private static volatile long currentCacheSize = Long.parseLong(ConfigurationPropertyKey.GROOVY_INLINE_EXPRESSION_PARSING_CACHE_MAX_SIZE.getDefaultValue());
    
    private static volatile Cache<String, Script> scriptCache = Caffeine.newBuilder().maximumSize(currentCacheSize).softValues().build();
    
    private String inlineExpression;
    
    @Override
    public void init(final Properties props) {
        inlineExpression = props.getProperty(INLINE_EXPRESSION_KEY);
        long maxCacheSize = new ConfigurationProperties(props).getValue(ConfigurationPropertyKey.GROOVY_INLINE_EXPRESSION_PARSING_CACHE_MAX_SIZE);
        updateMaxCacheSize(maxCacheSize);
    }
    
    private static void updateMaxCacheSize(final long newMaxCacheSize) {
        if (newMaxCacheSize == currentCacheSize) {
            return;
        }
        synchronized (GroovyInlineExpressionParser.class) {
            if (newMaxCacheSize != currentCacheSize) {
                scriptCache = Caffeine.newBuilder().maximumSize(newMaxCacheSize).softValues().build();
                currentCacheSize = newMaxCacheSize;
            }
        }
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
        return inlineExpression.contains("$->{") ? inlineExpression.replaceAll("\\$->\\{", "\\${") : inlineExpression;
    }
    
    /**
     * Split and Evaluate inline expression. This function will replace all inline expression placeholders.
     *
     * @return result inline expression with {@code $}
     */
    @Override
    public List<String> splitAndEvaluate() {
        if (Strings.isNullOrEmpty(inlineExpression)) {
            return Collections.emptyList();
        }
        if (isConstantExpression(inlineExpression)) {
            return Arrays.stream(inlineExpression.split("\\s*,\\s*")).map(String::trim).filter(each -> !each.isEmpty()).collect(Collectors.toList());
        }
        return flatten(evaluate(GroovyUtils.split(handlePlaceHolder(inlineExpression))));
    }
    
    /**
     * Turn inline expression into Groovy Closure. This function will replace all inline expression placeholders.
     * For compatibility reasons, it does not check whether the unit of the input parameter map is null.
     * @return The result of the Groovy Closure pattern.
     */
    @Override
    public String evaluateWithArgs(final Map<String, Comparable<?>> map) {
        if (isConstantExpression(inlineExpression)) {
            return inlineExpression;
        }
        Object scriptResult = evaluate("{it -> \"" + handlePlaceHolder(inlineExpression) + "\"}");
        if (scriptResult instanceof Closure) {
            Closure<?> result = ((Closure<?>) scriptResult).rehydrate(new Expando(), null, null);
            result.setResolveStrategy(Closure.DELEGATE_ONLY);
            map.forEach(result::setProperty);
            return result.call().toString();
        }
        return scriptResult.toString();
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
        if (isConstantExpression(expression)) {
            return expression.replaceAll("^\"|\"$", "");
        }
        Script script = scriptCache.get(expression, SHELL::parse);
        return null == script ? expression : script.run();
    }
    
    private boolean isConstantExpression(final String expression) {
        return Strings.isNullOrEmpty(expression) || !isDynamicExpression(expression);
    }
    
    private boolean isDynamicExpression(final String expression) {
        return expression.contains("${") || expression.contains("$->{");
    }
    
    private List<String> flatten(final List<Object> segments) {
        List<String> result = new ArrayList<>();
        for (Object each : segments) {
            if (each instanceof GString) {
                result.addAll(assemblyCartesianSegments((GString) each));
            } else if (each instanceof Script) {
                result.addAll(flattenScript((Script) each));
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
    
    private Collection<String> flattenScript(final Script script) {
        Collection<String> result = new LinkedList<>();
        Object scriptResult = script.run();
        if (scriptResult instanceof Iterable) {
            for (Object item : (Iterable<?>) scriptResult) {
                result.add(item.toString());
            }
        } else {
            result.add(scriptResult.toString());
        }
        return result;
    }
    
    @Override
    public String getType() {
        return "GROOVY";
    }
}
