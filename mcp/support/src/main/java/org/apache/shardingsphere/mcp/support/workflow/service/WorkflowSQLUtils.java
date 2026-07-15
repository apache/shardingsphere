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
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicy;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.support.database.capability.MCPDatabaseDialect;

import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Workflow SQL utilities.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WorkflowSQLUtils {
    
    private static final String UNQUOTED_IDENTIFIER_PATTERN = "[A-Za-z_][A-Za-z0-9_$]*";
    
    private static final Set<String> DIST_SQL_RESERVED_IDENTIFIERS = Set.of(
            "address_random_replace", "aes", "algorithm", "alter", "and", "assisted_query", "assisted_query_algorithm", "assisted_query_column", "by", "cipher", "column", "columns", "count",
            "create", "delete", "drop", "encrypt", "encrypt_algorithm", "exists", "false", "from", "generic_table_random_replace", "group", "if", "index", "insert", "keep_first_n_last_m",
            "keep_from_x_to_y", "key", "like_query", "like_query_algorithm", "like_query_column", "mask", "mask_after_special_chars", "mask_before_special_chars", "mask_first_n_last_m",
            "mask_from_x_to_y", "md5", "name", "not", "order", "plugins", "properties", "rule", "rules", "select", "show", "table", "true", "type", "update", "where");
    
    private static final char BACK_QUOTE = '`';
    
    private static final char DOUBLE_QUOTE = '"';
    
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
                : wrapIdentifier(QuoteCharacter.BACK_QUOTE, actualIdentifier);
    }
    
    /**
     * Format an identifier rendered by generated rule DistSQL artifacts.
     *
     * @param identifier identifier to format
     * @return formatted DistSQL identifier
     */
    public static String formatGeneratedRuleDistSQLIdentifier(final String identifier) {
        String actualIdentifier = normalizeIdentifier(trimToEmpty(identifier));
        checkSupportedIdentifier("identifier", actualIdentifier);
        return actualIdentifier.isEmpty() ? actualIdentifier : wrapIdentifier(QuoteCharacter.BACK_QUOTE, actualIdentifier);
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
                : wrapIdentifier(MCPDatabaseDialect.of(databaseType).getIdentifierQuoteCharacter(), actualIdentifier);
    }
    
    /**
     * Judge whether a workflow identifier token references an existing identifier under the target database policy.
     *
     * @param identifierCasePolicy identifier case policy
     * @param identifier identifier token
     * @param existingIdentifier existing identifier
     * @return whether the identifier references the existing identifier
     */
    public static boolean isSameIdentifier(final IdentifierCasePolicy identifierCasePolicy, final String identifier, final String existingIdentifier) {
        String actualIdentifier = normalizeIdentifier(identifier);
        String actualExistingIdentifier = normalizeIdentifier(existingIdentifier);
        return identifierCasePolicy.matches(actualExistingIdentifier, actualIdentifier, getQuoteCharacter(identifier));
    }
    
    static boolean requiresExactIdentifierMatch(final String identifier) {
        String rawIdentifier = trimToEmpty(identifier);
        return isDelimitedIdentifier(rawIdentifier) || isSpecialSQLIdentifier(normalizeIdentifier(rawIdentifier));
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
        Properties actualProperties = WorkflowAlgorithmUtils.createProperties(properties);
        return actualProperties.isEmpty()
                ? String.format("TYPE(NAME='%s')", escapeLiteral(actualType))
                : String.format("TYPE(NAME='%s', PROPERTIES(%s))", escapeLiteral(actualType), createPropertiesFragment(actualProperties));
    }
    
    private static String createPropertiesFragment(final Properties props) {
        return new TreeMap<>(props).entrySet().stream()
                .map(entry -> String.format("'%s'='%s'", escapeLiteral(String.valueOf(entry.getKey())), escapeLiteral(String.valueOf(entry.getValue()))))
                .collect(Collectors.joining(", "));
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
    
    private static QuoteCharacter getQuoteCharacter(final String identifier) {
        String actualIdentifier = trimToEmpty(identifier);
        return isDelimitedIdentifier(actualIdentifier) ? QuoteCharacter.getQuoteCharacter(actualIdentifier) : QuoteCharacter.NONE;
    }
    
    private static String wrapIdentifier(final QuoteCharacter quoteCharacter, final String value) {
        return QuoteCharacter.NONE == quoteCharacter
                ? value
                : quoteCharacter.getStartDelimiter() + value.replace(quoteCharacter.getEndDelimiter(), quoteCharacter.getEndDelimiter() + quoteCharacter.getEndDelimiter())
                        + quoteCharacter.getEndDelimiter();
    }
    
    private static String trimToEmpty(final String value) {
        return null == value ? "" : value.trim();
    }
}
