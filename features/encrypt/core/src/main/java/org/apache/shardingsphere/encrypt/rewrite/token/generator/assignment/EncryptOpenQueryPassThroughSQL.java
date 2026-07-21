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

package org.apache.shardingsphere.encrypt.rewrite.token.generator.assignment;

import lombok.Getter;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.encrypt.exception.syntax.UnsupportedEncryptSQLException;
import org.apache.shardingsphere.encrypt.rule.column.EncryptColumn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Encrypt OPENQUERY pass-through SQL.
 */
@Getter
final class EncryptOpenQueryPassThroughSQL {
    
    private static final String UNSUPPORTED_SHAPE = "OPENQUERY with query that is not SELECT ... FROM";
    
    private static final String UNSUPPORTED_SELECT_LITERAL = "OPENQUERY SELECT list with string literal";
    
    private static final String UNSUPPORTED_SELECT_EXPRESSION = "OPENQUERY SELECT list with expression";
    
    private static final String UNSUPPORTED_SPACE_DELIMITED_IDENTIFIER = "OPENQUERY with space-delimited identifier";
    
    private static final String UNSUPPORTED_MULTIPART_TABLE = "OPENQUERY with three-part table name";
    
    private static final String UNSUPPORTED_JOIN = "OPENQUERY with JOIN statement";
    
    private static final String UNSUPPORTED_COMMA_TABLE_SOURCE = "OPENQUERY with comma-separated table sources";
    
    private static final String UNSUPPORTED_APPLY = "OPENQUERY with APPLY statement";
    
    private static final String UNSUPPORTED_SET_OPERATION = "OPENQUERY with set operation";
    
    private static final String UNSUPPORTED_ENCRYPTED_PREDICATE = "OPENQUERY with predicate on encrypted column";
    
    private static final String UNSUPPORTED_PHYSICAL_COLUMN_NAME = "OPENQUERY with physical column name containing ]";
    
    private final String selectList;
    
    private final String tableExpression;
    
    private final String tableName;
    
    private final Optional<String> schemaName;
    
    private final String remainder;
    
    private EncryptOpenQueryPassThroughSQL(final String selectList, final String tableExpression, final String tableName,
                                           final Optional<String> schemaName, final String remainder) {
        this.selectList = selectList;
        this.tableExpression = tableExpression;
        this.tableName = tableName;
        this.schemaName = schemaName;
        this.remainder = remainder;
    }
    
    /**
     * Find table name from pass-through SQL without validating supported shape.
     *
     * @param passThroughSQL pass-through SQL
     * @return table name
     */
    static Optional<String> findTableName(final String passThroughSQL) {
        String trimmedSQL = passThroughSQL.trim();
        if (!startsWithKeyword(trimmedSQL, "SELECT")) {
            return Optional.empty();
        }
        Optional<Integer> fromIndex = findFromKeywordIndexIfPresent(trimmedSQL);
        if (!fromIndex.isPresent()) {
            return Optional.empty();
        }
        return extractTableNameAfterFrom(trimmedSQL, fromIndex.get() + "FROM".length());
    }
    
    /**
     * Parse and validate pass-through SQL.
     *
     * @param passThroughSQL pass-through SQL
     * @return parsed pass-through SQL
     * @throws UnsupportedEncryptSQLException if pass-through SQL shape is unsupported
     */
    static EncryptOpenQueryPassThroughSQL parse(final String passThroughSQL) {
        String trimmedSQL = passThroughSQL.trim();
        if (!startsWithKeyword(trimmedSQL, "SELECT")) {
            throw new UnsupportedEncryptSQLException(UNSUPPORTED_SHAPE);
        }
        int fromIndex = findFromKeywordIndex(trimmedSQL);
        String selectList = trimmedSQL.substring("SELECT".length(), fromIndex).trim();
        if (selectList.isEmpty()) {
            throw new UnsupportedEncryptSQLException(UNSUPPORTED_SHAPE);
        }
        validateSelectList(selectList);
        int tableStartIndex = fromIndex + "FROM".length();
        TableReference tableReference = parseTableReference(trimmedSQL, tableStartIndex);
        String remainder = trimmedSQL.substring(tableReference.getStopIndex());
        validateRemainder(remainder);
        return new EncryptOpenQueryPassThroughSQL(selectList, tableReference.getExpression(), tableReference.getTableName(), tableReference.getSchemaName(), remainder);
    }
    
    /**
     * Rewrite pass-through SQL with encrypted physical columns in the SELECT list.
     *
     * @param encryptColumns encrypt columns
     * @return rewritten pass-through SQL
     * @throws UnsupportedEncryptSQLException if remainder references an encrypted logic column
     */
    String rewrite(final Collection<EncryptColumn> encryptColumns) {
        validateRemainderHasNoEncryptColumnReference(remainder, encryptColumns);
        List<String> rewrittenItems = new ArrayList<>();
        for (String each : splitSelectList(selectList)) {
            String trimmedItem = each.trim();
            Optional<EncryptColumn> matchedEncryptColumn = findEncryptColumn(encryptColumns, unwrapIdentifier(trimmedItem));
            rewrittenItems.add(matchedEncryptColumn.map(EncryptOpenQueryPassThroughSQL::getPhysicalColumnNames).orElse(trimmedItem));
        }
        return "SELECT " + String.join(", ", rewrittenItems) + " FROM " + tableExpression + remainder;
    }
    
    private static void validateRemainderHasNoEncryptColumnReference(final String remainder, final Collection<EncryptColumn> encryptColumns) {
        if (remainder.isEmpty()) {
            return;
        }
        for (EncryptColumn each : encryptColumns) {
            if (containsLogicColumnReference(remainder, each.getName())) {
                throw new UnsupportedEncryptSQLException(UNSUPPORTED_ENCRYPTED_PREDICATE);
            }
        }
    }
    
    private static boolean containsLogicColumnReference(final String sqlFragment, final String logicColumnName) {
        int index = 0;
        boolean inString = false;
        while (index < sqlFragment.length()) {
            char current = sqlFragment.charAt(index);
            if ('\'' == current) {
                if (!inString) {
                    inString = true;
                    index++;
                    continue;
                }
                if (index + 1 < sqlFragment.length() && '\'' == sqlFragment.charAt(index + 1)) {
                    index += 2;
                    continue;
                }
                inString = false;
                index++;
                continue;
            }
            if (inString) {
                index++;
                continue;
            }
            if ('[' == current || '"' == current) {
                Optional<IdentifierPart> delimitedPart = readDelimitedIdentifierPartIfPresent(sqlFragment, index);
                if (!delimitedPart.isPresent()) {
                    return false;
                }
                if (delimitedPart.get().getValue().equalsIgnoreCase(logicColumnName)) {
                    return true;
                }
                index = delimitedPart.get().getStopIndex();
                continue;
            }
            if (Character.isLetter(current) || '_' == current) {
                int stopIndex = index + 1;
                while (stopIndex < sqlFragment.length()) {
                    char stopChar = sqlFragment.charAt(stopIndex);
                    if (Character.isLetterOrDigit(stopChar) || '_' == stopChar) {
                        stopIndex++;
                        continue;
                    }
                    break;
                }
                if (sqlFragment.substring(index, stopIndex).equalsIgnoreCase(logicColumnName)) {
                    return true;
                }
                index = stopIndex;
                continue;
            }
            index++;
        }
        return false;
    }
    
    private static void validateSelectList(final String selectList) {
        for (String each : splitSelectList(selectList)) {
            String trimmedItem = each.trim();
            if (trimmedItem.contains("'")) {
                throw new UnsupportedEncryptSQLException(UNSUPPORTED_SELECT_LITERAL);
            }
            if (trimmedItem.contains("(") || trimmedItem.contains(")")) {
                throw new UnsupportedEncryptSQLException(UNSUPPORTED_SELECT_EXPRESSION);
            }
            validateColumnIdentifier(trimmedItem);
        }
    }
    
    private static void validateColumnIdentifier(final String identifier) {
        if (identifier.isEmpty()) {
            throw new UnsupportedEncryptSQLException(UNSUPPORTED_SHAPE);
        }
        if (identifier.startsWith("[") && identifier.endsWith("]")) {
            String inner = identifier.substring(1, identifier.length() - 1);
            if (inner.contains(" ") || inner.contains("]") || inner.contains("[")) {
                throw new UnsupportedEncryptSQLException(UNSUPPORTED_SPACE_DELIMITED_IDENTIFIER);
            }
            return;
        }
        if (identifier.startsWith("\"") && identifier.endsWith("\"")) {
            if (!isClosedDelimitedIdentifier(identifier, '"')) {
                throw new UnsupportedEncryptSQLException(UNSUPPORTED_SELECT_EXPRESSION);
            }
            return;
        }
        for (int index = 0; index < identifier.length(); index++) {
            char current = identifier.charAt(index);
            if (!Character.isLetterOrDigit(current) && '_' != current) {
                throw new UnsupportedEncryptSQLException(UNSUPPORTED_SELECT_EXPRESSION);
            }
        }
    }
    
    private static void validateRemainder(final String remainder) {
        if (remainder.isEmpty()) {
            return;
        }
        validateNoCommaSeparatedTableSource(remainder);
        if (containsKeywordOutsideString(remainder, "JOIN")) {
            throw new UnsupportedEncryptSQLException(UNSUPPORTED_JOIN);
        }
        if (containsKeywordOutsideString(remainder, "APPLY")) {
            throw new UnsupportedEncryptSQLException(UNSUPPORTED_APPLY);
        }
        if (containsKeywordOutsideString(remainder, "UNION")
                || containsKeywordOutsideString(remainder, "EXCEPT")
                || containsKeywordOutsideString(remainder, "INTERSECT")) {
            throw new UnsupportedEncryptSQLException(UNSUPPORTED_SET_OPERATION);
        }
    }
    
    private static void validateNoCommaSeparatedTableSource(final String remainder) {
        int index = 0;
        int parenDepth = 0;
        boolean inString = false;
        while (index < remainder.length()) {
            char current = remainder.charAt(index);
            if (!inString && '/' == current && index + 1 < remainder.length() && '*' == remainder.charAt(index + 1)) {
                int closeIndex = remainder.indexOf("*/", index + 2);
                index = closeIndex < 0 ? remainder.length() : closeIndex + 2;
                continue;
            }
            if (!inString && '-' == current && index + 1 < remainder.length() && '-' == remainder.charAt(index + 1)) {
                int newlineIndex = remainder.indexOf('\n', index + 2);
                index = newlineIndex < 0 ? remainder.length() : newlineIndex + 1;
                continue;
            }
            if ('\'' == current) {
                if (!inString) {
                    inString = true;
                } else if (index + 1 < remainder.length() && '\'' == remainder.charAt(index + 1)) {
                    index += 2;
                    continue;
                } else {
                    inString = false;
                }
                index++;
                continue;
            }
            if (inString) {
                index++;
                continue;
            }
            if ('(' == current) {
                parenDepth++;
                index++;
                continue;
            }
            if (')' == current) {
                if (parenDepth > 0) {
                    parenDepth--;
                }
                index++;
                continue;
            }
            if (',' == current && 0 == parenDepth) {
                throw new UnsupportedEncryptSQLException(UNSUPPORTED_COMMA_TABLE_SOURCE);
            }
            if (0 == parenDepth && isClauseKeywordAt(remainder, index)) {
                return;
            }
            index++;
        }
    }
    
    private static boolean isClauseKeywordAt(final String sqlFragment, final int index) {
        return isKeywordAt(sqlFragment, index, "WHERE")
                || isKeywordAt(sqlFragment, index, "ORDER")
                || isKeywordAt(sqlFragment, index, "GROUP")
                || isKeywordAt(sqlFragment, index, "HAVING")
                || isKeywordAt(sqlFragment, index, "JOIN")
                || isKeywordAt(sqlFragment, index, "UNION")
                || isKeywordAt(sqlFragment, index, "EXCEPT")
                || isKeywordAt(sqlFragment, index, "INTERSECT")
                || isKeywordAt(sqlFragment, index, "CROSS")
                || isKeywordAt(sqlFragment, index, "OUTER")
                || isKeywordAt(sqlFragment, index, "APPLY");
    }
    
    private static boolean isKeywordAt(final String sqlFragment, final int index, final String keyword) {
        return matchesKeyword(sqlFragment, index, keyword)
                && isWordBoundary(sqlFragment, index - 1)
                && isWordBoundary(sqlFragment, index + keyword.length());
    }
    
    private static boolean containsKeywordOutsideString(final String sqlFragment, final String keyword) {
        int index = 0;
        boolean inString = false;
        while (index <= sqlFragment.length() - keyword.length()) {
            char current = sqlFragment.charAt(index);
            if ('\'' == current) {
                if (!inString) {
                    inString = true;
                    index++;
                    continue;
                }
                if (index + 1 < sqlFragment.length() && '\'' == sqlFragment.charAt(index + 1)) {
                    index += 2;
                    continue;
                }
                inString = false;
                index++;
                continue;
            }
            if (inString) {
                index++;
                continue;
            }
            if (isKeywordAt(sqlFragment, index, keyword)) {
                return true;
            }
            index++;
        }
        return false;
    }
    
    private static TableReference parseTableReference(final String passThroughSQL, final int startIndex) {
        int index = skipWhitespace(passThroughSQL, startIndex);
        IdentifierPart schemaPart = readIdentifierPart(passThroughSQL, index);
        index = schemaPart.getStopIndex();
        if (index < passThroughSQL.length() && '.' == passThroughSQL.charAt(index)) {
            IdentifierPart tablePart = readIdentifierPart(passThroughSQL, index + 1);
            if (tablePart.getStopIndex() < passThroughSQL.length() && '.' == passThroughSQL.charAt(tablePart.getStopIndex())) {
                throw new UnsupportedEncryptSQLException(UNSUPPORTED_MULTIPART_TABLE);
            }
            String expression = passThroughSQL.substring(schemaPart.getStartIndex(), tablePart.getStopIndex());
            return new TableReference(expression, tablePart.getValue(), Optional.of(schemaPart.getValue()), tablePart.getStopIndex());
        }
        String expression = passThroughSQL.substring(schemaPart.getStartIndex(), schemaPart.getStopIndex());
        return new TableReference(expression, schemaPart.getValue(), Optional.empty(), schemaPart.getStopIndex());
    }
    
    private static IdentifierPart readIdentifierPart(final String passThroughSQL, final int startIndex) {
        int index = skipWhitespace(passThroughSQL, startIndex);
        if (index >= passThroughSQL.length()) {
            throw new UnsupportedEncryptSQLException(UNSUPPORTED_SHAPE);
        }
        Optional<IdentifierPart> delimitedPart = readDelimitedIdentifierPartIfPresent(passThroughSQL, index);
        if (delimitedPart.isPresent()) {
            if ('[' == passThroughSQL.charAt(index) && delimitedPart.get().getValue().contains(" ")) {
                throw new UnsupportedEncryptSQLException(UNSUPPORTED_SPACE_DELIMITED_IDENTIFIER);
            }
            return delimitedPart.get();
        }
        int stopIndex = index;
        while (stopIndex < passThroughSQL.length()) {
            char current = passThroughSQL.charAt(stopIndex);
            if (Character.isLetterOrDigit(current) || '_' == current) {
                stopIndex++;
                continue;
            }
            break;
        }
        if (stopIndex == index) {
            throw new UnsupportedEncryptSQLException(UNSUPPORTED_SHAPE);
        }
        return new IdentifierPart(passThroughSQL.substring(index, stopIndex), index, stopIndex);
    }
    
    private static Optional<String> extractTableNameAfterFrom(final String passThroughSQL, final int startIndex) {
        int index = skipWhitespace(passThroughSQL, startIndex);
        Optional<IdentifierPart> identifierPart = readIdentifierPartIfPresent(passThroughSQL, index);
        if (!identifierPart.isPresent()) {
            return Optional.empty();
        }
        String tableName = identifierPart.get().getValue();
        index = identifierPart.get().getStopIndex();
        while (index < passThroughSQL.length() && '.' == passThroughSQL.charAt(index)) {
            Optional<IdentifierPart> nextPart = readIdentifierPartIfPresent(passThroughSQL, index + 1);
            if (!nextPart.isPresent()) {
                break;
            }
            tableName = nextPart.get().getValue();
            index = nextPart.get().getStopIndex();
        }
        return Optional.of(tableName);
    }
    
    private static Optional<Integer> findFromKeywordIndexIfPresent(final String passThroughSQL) {
        int index = 0;
        boolean inString = false;
        while (index <= passThroughSQL.length() - 4) {
            if (!inString && isFromKeywordAt(passThroughSQL, index)) {
                return Optional.of(index);
            }
            char current = passThroughSQL.charAt(index);
            if ('\'' != current) {
                index++;
                continue;
            }
            if (!inString) {
                inString = true;
                index++;
                continue;
            }
            if (index + 1 < passThroughSQL.length() && '\'' == passThroughSQL.charAt(index + 1)) {
                index += 2;
                continue;
            }
            inString = false;
            index++;
        }
        return Optional.empty();
    }
    
    private static Optional<IdentifierPart> readIdentifierPartIfPresent(final String passThroughSQL, final int startIndex) {
        int index = skipWhitespace(passThroughSQL, startIndex);
        if (index >= passThroughSQL.length()) {
            return Optional.empty();
        }
        Optional<IdentifierPart> delimitedPart = readDelimitedIdentifierPartIfPresent(passThroughSQL, index);
        if (delimitedPart.isPresent()) {
            return delimitedPart;
        }
        int stopIndex = index;
        while (stopIndex < passThroughSQL.length()) {
            char current = passThroughSQL.charAt(stopIndex);
            if (Character.isLetterOrDigit(current) || '_' == current) {
                stopIndex++;
                continue;
            }
            break;
        }
        if (stopIndex == index) {
            return Optional.empty();
        }
        return Optional.of(new IdentifierPart(passThroughSQL.substring(index, stopIndex), index, stopIndex));
    }
    
    private static Optional<IdentifierPart> readDelimitedIdentifierPartIfPresent(final String passThroughSQL, final int startIndex) {
        if (startIndex >= passThroughSQL.length()) {
            return Optional.empty();
        }
        char openDelimiter = passThroughSQL.charAt(startIndex);
        if ('[' != openDelimiter && '"' != openDelimiter) {
            return Optional.empty();
        }
        char closeDelimiter = '[' == openDelimiter ? ']' : '"';
        StringBuilder value = new StringBuilder();
        int index = startIndex + 1;
        while (index < passThroughSQL.length()) {
            char current = passThroughSQL.charAt(index);
            if (closeDelimiter == current) {
                if (index + 1 < passThroughSQL.length() && closeDelimiter == passThroughSQL.charAt(index + 1)) {
                    value.append(closeDelimiter);
                    index += 2;
                    continue;
                }
                return Optional.of(new IdentifierPart(value.toString(), startIndex, index + 1));
            }
            value.append(current);
            index++;
        }
        return Optional.empty();
    }
    
    private static boolean isClosedDelimitedIdentifier(final String identifier, final char delimiter) {
        if (identifier.length() < 2 || delimiter != identifier.charAt(0) || delimiter != identifier.charAt(identifier.length() - 1)) {
            return false;
        }
        int index = 1;
        while (index < identifier.length() - 1) {
            char current = identifier.charAt(index);
            if (delimiter == current) {
                if (index + 1 < identifier.length() - 1 && delimiter == identifier.charAt(index + 1)) {
                    index += 2;
                    continue;
                }
                return false;
            }
            index++;
        }
        return true;
    }
    
    private static int findFromKeywordIndex(final String passThroughSQL) {
        Optional<Integer> result = findFromKeywordIndexIfPresent(passThroughSQL);
        if (!result.isPresent()) {
            throw new UnsupportedEncryptSQLException(UNSUPPORTED_SHAPE);
        }
        return result.get();
    }
    
    private static boolean isFromKeywordAt(final String passThroughSQL, final int index) {
        return matchesKeyword(passThroughSQL, index, "FROM") && isWordBoundary(passThroughSQL, index - 1) && isWordBoundary(passThroughSQL, index + 4);
    }
    
    private static boolean startsWithKeyword(final String passThroughSQL, final String keyword) {
        if (passThroughSQL.length() < keyword.length()) {
            return false;
        }
        if (!passThroughSQL.regionMatches(true, 0, keyword, 0, keyword.length())) {
            return false;
        }
        return passThroughSQL.length() == keyword.length() || isWordBoundary(passThroughSQL, keyword.length());
    }
    
    private static boolean matchesKeyword(final String passThroughSQL, final int startIndex, final String keyword) {
        return passThroughSQL.regionMatches(true, startIndex, keyword, 0, keyword.length());
    }
    
    private static boolean isWordBoundary(final String passThroughSQL, final int index) {
        if (index < 0 || index >= passThroughSQL.length()) {
            return true;
        }
        char current = passThroughSQL.charAt(index);
        return !Character.isLetterOrDigit(current) && '_' != current;
    }
    
    private static int skipWhitespace(final String passThroughSQL, final int startIndex) {
        int index = startIndex;
        while (index < passThroughSQL.length() && Character.isWhitespace(passThroughSQL.charAt(index))) {
            index++;
        }
        return index;
    }
    
    private static List<String> splitSelectList(final String selectList) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (int index = 0; index < selectList.length(); index++) {
            char currentChar = selectList.charAt(index);
            if (',' == currentChar) {
                result.add(current.toString());
                current = new StringBuilder();
                continue;
            }
            current.append(currentChar);
        }
        result.add(current.toString());
        return result;
    }
    
    private static String unwrapIdentifier(final String identifier) {
        if (identifier.startsWith("[") && identifier.endsWith("]")) {
            return unescapeDelimitedContent(identifier.substring(1, identifier.length() - 1), ']');
        }
        if (identifier.startsWith("\"") && identifier.endsWith("\"")) {
            return unescapeDelimitedContent(identifier.substring(1, identifier.length() - 1), '"');
        }
        return identifier;
    }
    
    private static String unescapeDelimitedContent(final String content, final char delimiter) {
        String escapedDelimiter = String.valueOf(delimiter) + delimiter;
        return content.replace(escapedDelimiter, String.valueOf(delimiter));
    }
    
    private static Optional<EncryptColumn> findEncryptColumn(final Collection<EncryptColumn> encryptColumns, final String logicColumnName) {
        for (EncryptColumn each : encryptColumns) {
            if (each.getName().equalsIgnoreCase(logicColumnName)) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
    }
    
    private static String getPhysicalColumnNames(final EncryptColumn encryptColumn) {
        StringBuilder result = new StringBuilder(quotePhysicalColumnName(encryptColumn.getCipher().getName()));
        encryptColumn.getAssistedQuery().ifPresent(optional -> result.append(", ").append(quotePhysicalColumnName(optional.getName())));
        encryptColumn.getLikeQuery().ifPresent(optional -> result.append(", ").append(quotePhysicalColumnName(optional.getName())));
        return result.toString();
    }
    
    private static String quotePhysicalColumnName(final String physicalColumnName) {
        if (physicalColumnName.contains("]")) {
            throw new UnsupportedEncryptSQLException(UNSUPPORTED_PHYSICAL_COLUMN_NAME);
        }
        return QuoteCharacter.BRACKETS.wrap(physicalColumnName);
    }
    
    @Getter
    private static final class TableReference {
        
        private final String expression;
        
        private final String tableName;
        
        private final Optional<String> schemaName;
        
        private final int stopIndex;
        
        private TableReference(final String expression, final String tableName, final Optional<String> schemaName, final int stopIndex) {
            this.expression = expression;
            this.tableName = tableName;
            this.schemaName = schemaName;
            this.stopIndex = stopIndex;
        }
    }
    
    @Getter
    private static final class IdentifierPart {
        
        private final String value;
        
        private final int startIndex;
        
        private final int stopIndex;
        
        private IdentifierPart(final String value, final int startIndex, final int stopIndex) {
            this.value = value;
            this.startIndex = startIndex;
            this.stopIndex = stopIndex;
        }
    }
}
