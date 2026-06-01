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

package org.apache.shardingsphere.mcp.support.workflow.service;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPInvalidRequestException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Workflow SQL utilities.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WorkflowSQLUtils {
    
    private static final String SAFE_IDENTIFIER_PATTERN = "[A-Za-z0-9_$]+";
    
    private static final String UNQUOTED_IDENTIFIER_PATTERN = "[A-Za-z_][A-Za-z0-9_$]*";
    
    private static final Set<String> DIST_SQL_RESERVED_IDENTIFIERS = Set.of(
            "address_random_replace", "aes", "algorithm", "alter", "and", "assisted_query", "assisted_query_algorithm", "assisted_query_column", "by", "cipher", "column", "columns", "count",
            "create", "delete", "drop", "encrypt", "encrypt_algorithm", "exists", "false", "from", "generic_table_random_replace", "group", "if", "index", "insert", "keep_first_n_last_m",
            "keep_from_x_to_y", "key", "like_query", "like_query_algorithm", "like_query_column", "mask", "mask_after_special_chars", "mask_before_special_chars", "mask_first_n_last_m",
            "mask_from_x_to_y", "md5", "name", "not", "order", "plugins", "properties", "rule", "rules", "select", "show", "table", "true", "type", "update", "where");
    
    private static final char BACK_QUOTE = '`';
    
    private static final char DOUBLE_QUOTE = '"';
    
    /**
     * Check whether an identifier can be used as an unquoted SQL identifier.
     *
     * @param identifier identifier to check
     * @return whether the identifier is safe
     */
    public static boolean isSafeIdentifier(final String identifier) {
        return null != identifier && identifier.matches(SAFE_IDENTIFIER_PATTERN);
    }
    
    /**
     * Normalize a SQL identifier from user input.
     *
     * @param identifier identifier to normalize
     * @return normalized identifier
     */
    public static String normalizeIdentifier(final String identifier) {
        String result = trimToEmpty(identifier);
        if (isDelimitedIdentifier(result, BACK_QUOTE, BACK_QUOTE)) {
            return result.substring(1, result.length() - 1).replace("``", "`");
        }
        if (isDelimitedIdentifier(result, DOUBLE_QUOTE, DOUBLE_QUOTE)) {
            return result.substring(1, result.length() - 1).replace("\"\"", "\"");
        }
        if (isDelimitedIdentifier(result, '[', ']')) {
            return result.substring(1, result.length() - 1).replace("]]", "]");
        }
        return result;
    }
    
    /**
     * Canonicalize a workflow identifier for metadata lookup or identifier comparison.
     *
     * @param databaseType database type
     * @param identifier identifier to canonicalize
     * @return canonicalized identifier
     */
    public static String canonicalizeIdentifier(final String databaseType, final String identifier) {
        String rawIdentifier = trimToEmpty(identifier);
        String result = normalizeIdentifier(rawIdentifier);
        return !isDelimitedIdentifier(rawIdentifier) && isLowerCaseFoldedIdentifierDatabase(databaseType) && !isSpecialSQLIdentifier(result)
                ? result.toLowerCase(Locale.ENGLISH)
                : result;
    }
    
    /**
     * Check whether an identifier is supported by workflow planning.
     *
     * @param identifier identifier to check
     * @return whether the identifier is supported
     */
    public static boolean isSupportedIdentifier(final String identifier) {
        return !containsUnsupportedIdentifierCharacter(normalizeIdentifier(identifier));
    }
    
    /**
     * Validate a workflow SQL identifier.
     *
     * @param fieldName field name for error reporting
     * @param identifier identifier to check
     * @throws MCPInvalidRequestException when the identifier contains unsupported characters
     */
    public static void checkSupportedIdentifier(final String fieldName, final String identifier) {
        String actualIdentifier = normalizeIdentifier(identifier);
        ShardingSpherePreconditions.checkState(!containsUnsupportedIdentifierCharacter(actualIdentifier),
                () -> new MCPInvalidRequestException(String.format(
                        "%s `%s` contains unsupported characters that cannot be rendered as a reviewable SQL identifier.", fieldName, actualIdentifier)));
    }
    
    /**
     * Format a DistSQL identifier.
     *
     * @param identifier identifier to format
     * @return formatted DistSQL identifier
     */
    public static String formatDistSQLIdentifier(final String identifier) {
        String rawIdentifier = trimToEmpty(identifier);
        String actualIdentifier = normalizeIdentifier(rawIdentifier);
        checkSupportedIdentifier("identifier", actualIdentifier);
        return actualIdentifier.isEmpty() || !isSpecialDistSQLIdentifier(actualIdentifier) && !isDelimitedIdentifier(rawIdentifier)
                ? actualIdentifier
                : IdentifierQuoteStyle.BACK_QUOTE.wrap(actualIdentifier);
    }
    
    /**
     * Format a database SQL identifier.
     *
     * @param databaseType database type
     * @param identifier identifier to format
     * @return formatted SQL identifier
     */
    public static String formatSQLIdentifier(final String databaseType, final String identifier) {
        String rawIdentifier = trimToEmpty(identifier);
        String actualIdentifier = normalizeIdentifier(rawIdentifier);
        checkSupportedIdentifier("identifier", actualIdentifier);
        return actualIdentifier.isEmpty() || !isSpecialSQLIdentifier(actualIdentifier) && !isDelimitedIdentifier(rawIdentifier)
                ? actualIdentifier
                : getSQLIdentifierQuoteStyle(databaseType).wrap(actualIdentifier);
    }
    
    /**
     * Judge whether a workflow identifier token references an existing identifier for the target database type.
     *
     * @param databaseType database type
     * @param identifier identifier token
     * @param existingIdentifier existing identifier
     * @return whether the identifier references the existing identifier
     */
    public static boolean isSameIdentifier(final String databaseType, final String identifier, final String existingIdentifier) {
        String actualIdentifier = normalizeIdentifier(identifier);
        String actualExistingIdentifier = normalizeIdentifier(existingIdentifier);
        if (isCaseInsensitiveIdentifierDatabase(databaseType)) {
            return actualIdentifier.equalsIgnoreCase(actualExistingIdentifier);
        }
        return isLowerCaseFoldedIdentifierDatabase(databaseType)
                ? canonicalizeIdentifier(databaseType, identifier).equals(actualExistingIdentifier)
                : actualIdentifier.equals(actualExistingIdentifier);
    }
    
    /**
     * Escape a SQL string literal value.
     *
     * @param value raw value
     * @return escaped literal value
     */
    public static String escapeLiteral(final String value) {
        return trimToEmpty(value).replace("'", "''");
    }
    
    /**
     * Create properties with trimmed string values.
     *
     * @param entries property entries
     * @return created properties
     */
    public static Properties createProperties(final Map<String, String> entries) {
        Properties result = new Properties();
        for (Entry<String, String> entry : entries.entrySet()) {
            result.setProperty(entry.getKey(), trimToEmpty(entry.getValue()));
        }
        return result;
    }
    
    /**
     * Create an algorithm fragment for DistSQL.
     *
     * @param algorithmType algorithm type
     * @param properties algorithm properties
     * @return DistSQL algorithm fragment
     */
    public static String createAlgorithmFragment(final String algorithmType, final Map<String, String> properties) {
        String actualType = trimToEmpty(algorithmType).toLowerCase(Locale.ENGLISH);
        if (actualType.isEmpty()) {
            return "";
        }
        Properties actualProperties = createProperties(properties);
        return actualProperties.isEmpty()
                ? String.format("TYPE(NAME='%s')", escapeLiteral(actualType))
                : String.format("TYPE(NAME='%s', PROPERTIES(%s))", escapeLiteral(actualType), createPropertiesFragment(actualProperties));
    }
    
    private static String createPropertiesFragment(final Properties props) {
        return new TreeMap<>(props).entrySet().stream()
                .map(entry -> String.format("'%s'='%s'", escapeLiteral(String.valueOf(entry.getKey())), escapeLiteral(String.valueOf(entry.getValue()))))
                .collect(Collectors.joining(", "));
    }
    
    /**
     * Parse property entries from a list of {@code key=value} or {@code key:value} strings.
     *
     * @param entries property entries
     * @return parsed property map
     */
    public static Map<String, String> parsePropertyEntries(final List<String> entries) {
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
    
    /**
     * Create a property map from supported property carrier types.
     *
     * @param value property carrier value
     * @return normalized property map
     */
    public static Map<String, String> createPropertyMap(final Object value) {
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
            String key = trimToEmpty(Objects.toString(entry.getKey(), ""));
            if (!key.isEmpty()) {
                result.put(key, trimToEmpty(Objects.toString(entry.getValue(), "")));
            }
        }
        return result;
    }
    
    private static boolean isDelimitedIdentifier(final String identifier) {
        return isDelimitedIdentifier(identifier, BACK_QUOTE, BACK_QUOTE) || isDelimitedIdentifier(identifier, DOUBLE_QUOTE, DOUBLE_QUOTE) || isDelimitedIdentifier(identifier, '[', ']');
    }
    
    private static boolean isDelimitedIdentifier(final String identifier, final char startDelimiter, final char endDelimiter) {
        return identifier.length() >= 2 && startDelimiter == identifier.charAt(0) && endDelimiter == identifier.charAt(identifier.length() - 1);
    }
    
    private static boolean containsUnsupportedIdentifierCharacter(final String identifier) {
        return identifier.chars().anyMatch(each -> BACK_QUOTE == each || 0 == each || '\r' == each || '\n' == each);
    }
    
    private static boolean isSpecialDistSQLIdentifier(final String identifier) {
        return !identifier.matches(UNQUOTED_IDENTIFIER_PATTERN) || DIST_SQL_RESERVED_IDENTIFIERS.contains(identifier.toLowerCase(Locale.ENGLISH));
    }
    
    private static boolean isSpecialSQLIdentifier(final String identifier) {
        return !identifier.matches(UNQUOTED_IDENTIFIER_PATTERN);
    }
    
    private static boolean isCaseInsensitiveIdentifierDatabase(final String databaseType) {
        String actualDatabaseType = trimToEmpty(databaseType).toLowerCase(Locale.ENGLISH);
        return "mysql".equals(actualDatabaseType) || "mariadb".equals(actualDatabaseType) || "doris".equals(actualDatabaseType);
    }
    
    private static boolean isLowerCaseFoldedIdentifierDatabase(final String databaseType) {
        String actualDatabaseType = trimToEmpty(databaseType).toLowerCase(Locale.ENGLISH);
        return "postgresql".equals(actualDatabaseType) || "opengauss".equals(actualDatabaseType);
    }
    
    private static IdentifierQuoteStyle getSQLIdentifierQuoteStyle(final String databaseType) {
        String actualDatabaseType = trimToEmpty(databaseType).toLowerCase(Locale.ENGLISH);
        if (actualDatabaseType.isEmpty() || "mysql".equals(actualDatabaseType) || "mariadb".equals(actualDatabaseType) || "doris".equals(actualDatabaseType) || "hive".equals(actualDatabaseType)) {
            return IdentifierQuoteStyle.BACK_QUOTE;
        }
        return "sqlserver".equals(actualDatabaseType) ? IdentifierQuoteStyle.BRACKETS : IdentifierQuoteStyle.DOUBLE_QUOTE;
    }
    
    private enum IdentifierQuoteStyle {
        
        BACK_QUOTE("`", "`"),
        
        DOUBLE_QUOTE("\"", "\""),
        
        BRACKETS("[", "]");
        
        private final String startDelimiter;
        
        private final String endDelimiter;
        
        IdentifierQuoteStyle(final String startDelimiter, final String endDelimiter) {
            this.startDelimiter = startDelimiter;
            this.endDelimiter = endDelimiter;
        }
        
        private String wrap(final String value) {
            return startDelimiter + value.replace(endDelimiter, endDelimiter + endDelimiter) + endDelimiter;
        }
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
    
    private static int findPropertySeparatorIndex(final String propertyEntry) {
        int equalsIndex = propertyEntry.indexOf('=');
        return -1 == equalsIndex ? propertyEntry.indexOf(':') : equalsIndex;
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
    
    private static String trimToEmpty(final String value) {
        return null == value ? "" : value.trim();
    }
}
