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

package org.apache.shardingsphere.test.e2e.mcp.support.assertion;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Baseline contract assertions for normalized MCP model-facing payloads.
 */
public final class MCPBaselineContractAssertions {
    
    private static final String PLAN_ID_PLACEHOLDER = "<plan_id>";
    
    private MCPBaselineContractAssertions() {
    }
    
    /**
     * Assert one model-facing payload projection against a normalized baseline YAML resource.
     *
     * @param resourcePath baseline resource path
     * @param actual actual payload projection
     */
    public static void assertMatchesNormalizedBaselineContract(final String resourcePath, final Object actual) {
        Object actualNormalized = normalize(actual);
        Object expected = loadBaselineContract(resourcePath);
        assertThat("Baseline contract resource: " + resourcePath + System.lineSeparator() + new Yaml().dump(actualNormalized), actualNormalized, is(expected));
    }
    
    private static Object loadBaselineContract(final String resourcePath) {
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath)) {
            if (null == inputStream) {
                fail("Missing baseline contract resource: " + resourcePath);
            }
            Object result = new Yaml().load(inputStream);
            return null == result ? Map.of() : result;
        } catch (final IOException ex) {
            throw new IllegalStateException("Failed to load baseline contract resource: " + resourcePath, ex);
        }
    }
    
    private static Object normalize(final Object value) {
        if (value instanceof Map) {
            return normalizeMap((Map<?, ?>) value);
        }
        if (value instanceof Collection) {
            return ((Collection<?>) value).stream().map(MCPBaselineContractAssertions::normalize).toList();
        }
        return value;
    }
    
    private static Map<String, Object> normalizeMap(final Map<?, ?> value) {
        Map<String, Object> result = new LinkedHashMap<>(value.size(), 1F);
        for (Entry<?, ?> entry : value.entrySet()) {
            String key = String.valueOf(entry.getKey());
            result.put(key, normalizeValue(key, entry.getValue()));
        }
        return result;
    }
    
    private static Object normalizeValue(final String key, final Object value) {
        if ("plan_id".equals(key) && value instanceof String && !((String) value).isEmpty() && !"server_generated".equals(value)) {
            return PLAN_ID_PLACEHOLDER;
        }
        return normalize(value);
    }
}
