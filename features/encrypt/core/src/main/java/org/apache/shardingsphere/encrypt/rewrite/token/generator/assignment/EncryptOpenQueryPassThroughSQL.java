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
import org.apache.shardingsphere.encrypt.exception.syntax.UnsupportedEncryptSQLException;
import org.apache.shardingsphere.encrypt.rule.column.EncryptColumn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
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
     */
    String rewrite(final Collection<EncryptColumn> encryptColumns) {
        List<String> rewrittenItems = new ArrayList<>();
        for (String each : splitSelectList(selectList)) {
            String trimmedItem = each.trim();
            Optional<EncryptColumn> matchedEncryptColumn = findEncryptColumn(encryptColumns, unwrapIdentifier(trimmedItem));
            rewrittenItems.add(matchedEncryptColumn.map(EncryptOpenQueryPassThroughSQL::getPhysicalColumnNames).orElse(trimmedItem));
        }
        return "SELECT " + String.join(", ", rewrittenItems) + " FROM " + tableExpression + remainder;
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
        for (int index = 0; index < identifier.length(); index++) {
            char current = identifier.charAt(index);
            if (!Character.isLetterOrDigit(current) && '_' != current) {
                throw new UnsupportedEncryptSQLException(UNSUPPORTED_SELECT_EXPRESSION);
            }
        }
    }
    
    private static void validateRemainder(final String remainder) {
        String trimmedRemainder = remainder.trim().toUpperCase(Locale.ENGLISH);
        if (trimmedRemainder.startsWith("JOIN ") || trimmedRemainder.contains(" JOIN ")) {
            throw new UnsupportedEncryptSQLException(UNSUPPORTED_JOIN);
        }
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
        if ('[' == passThroughSQL.charAt(index)) {
            int closeIndex = passThroughSQL.indexOf(']', index + 1);
            if (-1 == closeIndex) {
                throw new UnsupportedEncryptSQLException(UNSUPPORTED_SHAPE);
            }
            String inner = passThroughSQL.substring(index + 1, closeIndex);
            if (inner.contains(" ")) {
                throw new UnsupportedEncryptSQLException(UNSUPPORTED_SPACE_DELIMITED_IDENTIFIER);
            }
            return new IdentifierPart(inner, index, closeIndex + 1);
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
        if ('[' == passThroughSQL.charAt(index)) {
            int closeIndex = passThroughSQL.indexOf(']', index + 1);
            if (-1 == closeIndex) {
                return Optional.empty();
            }
            return Optional.of(new IdentifierPart(passThroughSQL.substring(index + 1, closeIndex), index, closeIndex + 1));
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
            return identifier.substring(1, identifier.length() - 1);
        }
        return identifier;
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
        StringBuilder result = new StringBuilder(encryptColumn.getCipher().getName());
        encryptColumn.getAssistedQuery().ifPresent(optional -> result.append(", ").append(optional.getName()));
        encryptColumn.getLikeQuery().ifPresent(optional -> result.append(", ").append(optional.getName()));
        return result.toString();
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
