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

package org.apache.shardingsphere.mcp.tool.service.workflow;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.util.props.PropertiesUtils;
import org.apache.shardingsphere.mcp.protocol.exception.MCPInvalidRequestException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Workflow SQL utilities.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class WorkflowSqlUtils {
    
    private static final String SAFE_IDENTIFIER_PATTERN = "[A-Za-z0-9_$]+";
    
    static boolean isSafeIdentifier(final String identifier) {
        return null != identifier && identifier.matches(SAFE_IDENTIFIER_PATTERN);
    }
    
    static void checkSafeIdentifier(final String fieldName, final String identifier) {
        String actualIdentifier = trimToEmpty(identifier);
        if (actualIdentifier.isEmpty() || isSafeIdentifier(actualIdentifier)) {
            return;
        }
        throw new MCPInvalidRequestException(String.format("%s `%s` contains unsupported characters. Only unquoted SQL identifiers are supported in V1.",
                fieldName, actualIdentifier));
    }
    
    static String trimToEmpty(final String value) {
        return null == value ? "" : value.trim();
    }
    
    static String escapeLiteral(final String value) {
        return trimToEmpty(value).replace("'", "''");
    }
    
    static Properties createProperties(final Map<String, String> entries) {
        Properties result = new Properties();
        for (Entry<String, String> entry : entries.entrySet()) {
            result.setProperty(entry.getKey(), trimToEmpty(entry.getValue()));
        }
        return result;
    }
    
    static String createAlgorithmFragment(final String algorithmType, final Map<String, String> properties) {
        String actualType = trimToEmpty(algorithmType).toLowerCase(Locale.ENGLISH);
        if (actualType.isEmpty()) {
            return "";
        }
        Properties actualProperties = createProperties(properties);
        return actualProperties.isEmpty()
                ? String.format("TYPE(NAME='%s')", actualType)
                : String.format("TYPE(NAME='%s', PROPERTIES(%s))", actualType, PropertiesUtils.toString(actualProperties));
    }
    
    static Map<String, String> parsePropertyEntries(final List<String> entries) {
        Map<String, String> result = new LinkedHashMap<>(entries.size(), 1F);
        for (String each : entries) {
            int separatorIndex = findPropertySeparatorIndex(each);
            if (-1 == separatorIndex) {
                continue;
            }
            String key = each.substring(0, separatorIndex).trim();
            String value = each.substring(separatorIndex + 1).trim();
            if (!key.isEmpty()) {
                result.put(key, value);
            }
        }
        return result;
    }
    
    static Map<String, String> createPropertyMap(final Object value) {
        if (null == value) {
            return Map.of();
        }
        if (value instanceof Properties) {
            return createPropertyMap((Properties) value);
        }
        if (value instanceof Map) {
            return createPropertyMap((Map<?, ?>) value);
        }
        return parsePropertyString(String.valueOf(value));
    }
    
    private static Map<String, String> createPropertyMap(final Properties props) {
        Map<String, String> result = new LinkedHashMap<>(props.size(), 1F);
        for (String each : props.stringPropertyNames()) {
            result.put(each, trimToEmpty(props.getProperty(each)));
        }
        return result;
    }
    
    private static Map<String, String> createPropertyMap(final Map<?, ?> props) {
        Map<String, String> result = new LinkedHashMap<>(props.size(), 1F);
        for (Entry<?, ?> entry : props.entrySet()) {
            String key = trimToEmpty(String.valueOf(entry.getKey()));
            if (!key.isEmpty()) {
                result.put(key, trimToEmpty(null == entry.getValue() ? "" : String.valueOf(entry.getValue())));
            }
        }
        return result;
    }
    
    private static Map<String, String> parsePropertyString(final String value) {
        String actualValue = trimToEmpty(value);
        if (actualValue.isEmpty() || "{}".equals(actualValue)) {
            return Map.of();
        }
        String normalizedValue = actualValue;
        if (normalizedValue.startsWith("{") && normalizedValue.endsWith("}")) {
            normalizedValue = normalizedValue.substring(1, normalizedValue.length() - 1);
        }
        List<String> entries = List.of(normalizedValue.split(","));
        Map<String, String> result = new LinkedHashMap<>(entries.size(), 1F);
        for (Entry<String, String> entry : parsePropertyEntries(entries).entrySet()) {
            result.put(stripQuotes(entry.getKey()), stripQuotes(entry.getValue()));
        }
        return result;
    }
    
    private static int findPropertySeparatorIndex(final String entry) {
        int equalsIndex = entry.indexOf('=');
        return -1 != equalsIndex ? equalsIndex : entry.indexOf(':');
    }
    
    private static String stripQuotes(final String value) {
        String actualValue = trimToEmpty(value);
        if (2 > actualValue.length()) {
            return actualValue;
        }
        char first = actualValue.charAt(0);
        char last = actualValue.charAt(actualValue.length() - 1);
        if ('\'' == first && '\'' == last || '"' == first && '"' == last) {
            return actualValue.substring(1, actualValue.length() - 1);
        }
        return actualValue;
    }
}
