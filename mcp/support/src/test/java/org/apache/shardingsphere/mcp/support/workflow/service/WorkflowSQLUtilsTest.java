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

import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicyFactory;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.infra.metadata.identifier.DatabaseIdentifierContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkflowSQLUtilsTest {
    
    @Test
    void assertCheckSafeIdentifierAllowsSafeIdentifier() {
        assertDoesNotThrow(() -> WorkflowSQLUtils.checkSupportedIdentifier("table", "orders_01"));
    }
    
    @Test
    void assertCheckSupportedIdentifierAllowsSpecialCharacterIdentifier() {
        assertDoesNotThrow(() -> WorkflowSQLUtils.checkSupportedIdentifier("table", "bad table"));
    }
    
    @Test
    void assertCheckSupportedIdentifierRejectsLineTerminator() {
        Exception actualException = assertThrows(RuntimeException.class, () -> WorkflowSQLUtils.checkSupportedIdentifier("table", "bad\ntable"));
        assertThat(actualException.getMessage(), is("table `bad\ntable` contains unsupported characters that cannot be rendered as a reviewable SQL identifier."));
    }
    
    @Test
    void assertCheckSupportedIdentifierRejectsBackQuote() {
        Exception actualException = assertThrows(RuntimeException.class, () -> WorkflowSQLUtils.checkSupportedIdentifier("table", "bad`table"));
        assertThat(actualException.getMessage(), is("table `bad`table` contains unsupported characters that cannot be rendered as a reviewable SQL identifier."));
    }
    
    @Test
    void assertNormalizeIdentifierUnwrapsDelimitedIdentifier() {
        assertThat(WorkflowSQLUtils.normalizeIdentifier("`bad table`"), is("bad table"));
        assertThat(WorkflowSQLUtils.normalizeIdentifier("\"Order Detail\""), is("Order Detail"));
        assertThat(WorkflowSQLUtils.normalizeIdentifier("[Order Detail]"), is("Order Detail"));
    }
    
    @Test
    void assertFormatDistSQLIdentifierKeepsSafeIdentifier() {
        String actualValue = WorkflowSQLUtils.formatDistSQLIdentifier("orders_01");
        assertThat(actualValue, is("orders_01"));
    }
    
    @Test
    void assertFormatDistSQLIdentifierQuotesDelimitedSafeIdentifier() {
        String actualValue = WorkflowSQLUtils.formatDistSQLIdentifier("`orders`");
        assertThat(actualValue, is("`orders`"));
    }
    
    @Test
    void assertFormatDistSQLIdentifierQuotesReservedIdentifier() {
        String actualValue = WorkflowSQLUtils.formatDistSQLIdentifier("key");
        assertThat(actualValue, is("`key`"));
    }
    
    @Test
    void assertFormatDistSQLIdentifierKeepsPlainIdentifier() {
        String actualValue = WorkflowSQLUtils.formatDistSQLIdentifier("rank");
        assertThat(actualValue, is("rank"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getDistSQLKeywordCases")
    void assertFormatDistSQLIdentifierQuotesDistSQLKeyword(final String name, final String identifier, final String expectedValue) {
        String actualValue = WorkflowSQLUtils.formatDistSQLIdentifier(identifier);
        assertThat(actualValue, is(expectedValue));
    }
    
    @Test
    void assertFormatDistSQLIdentifierKeepsMixedCaseIdentifier() {
        String actualValue = WorkflowSQLUtils.formatDistSQLIdentifier("Phone");
        assertThat(actualValue, is("Phone"));
    }
    
    @Test
    void assertFormatDistSQLIdentifierQuotesUnicodeIdentifier() {
        String actualValue = WorkflowSQLUtils.formatDistSQLIdentifier("订单");
        assertThat(actualValue, is("`订单`"));
    }
    
    @Test
    void assertFormatDistSQLIdentifierRejectsBackQuote() {
        Exception actualException = assertThrows(RuntimeException.class, () -> WorkflowSQLUtils.formatDistSQLIdentifier("bad`table"));
        assertThat(actualException.getMessage(), is("identifier `bad`table` contains unsupported characters that cannot be rendered as a reviewable SQL identifier."));
    }
    
    @Test
    void assertFormatGeneratedRuleDistSQLIdentifierQuotesSafeIdentifier() {
        String actualValue = WorkflowSQLUtils.formatGeneratedRuleDistSQLIdentifier("orders_01");
        assertThat(actualValue, is("`orders_01`"));
    }
    
    @Test
    void assertFormatGeneratedRuleDistSQLIdentifierQuotesDelimitedSafeIdentifier() {
        String actualValue = WorkflowSQLUtils.formatGeneratedRuleDistSQLIdentifier("`orders`");
        assertThat(actualValue, is("`orders`"));
    }
    
    @Test
    void assertFormatGeneratedRuleDistSQLIdentifierReturnsEmptyForBlankIdentifier() {
        String actualValue = WorkflowSQLUtils.formatGeneratedRuleDistSQLIdentifier("");
        assertThat(actualValue, is(""));
    }
    
    @Test
    void assertIsSameIdentifierWithCaseInsensitiveDatabase() {
        assertTrue(WorkflowSQLUtils.isSameIdentifier(new DatabaseIdentifierContext(IdentifierCasePolicyFactory.newInsensitivePolicySet()), IdentifierScope.TABLE, "Phone", "phone"));
    }
    
    @Test
    void assertIsSameIdentifierFoldsPostgreSQLUnquotedIdentifier() {
        assertTrue(WorkflowSQLUtils.isSameIdentifier(new DatabaseIdentifierContext(IdentifierCasePolicyFactory.newLowerCasePolicySet()), IdentifierScope.TABLE, "Phone", "phone"));
    }
    
    @Test
    void assertIsSameIdentifierPreservesPostgreSQLDelimitedIdentifier() {
        assertFalse(WorkflowSQLUtils.isSameIdentifier(new DatabaseIdentifierContext(IdentifierCasePolicyFactory.newLowerCasePolicySet()), IdentifierScope.TABLE, "\"Phone\"", "phone"));
    }
    
    @Test
    void assertIsSameIdentifierRejectsUnquotedPostgreSQLQuotedName() {
        assertFalse(WorkflowSQLUtils.isSameIdentifier(new DatabaseIdentifierContext(IdentifierCasePolicyFactory.newLowerCasePolicySet()), IdentifierScope.TABLE, "Phone", "Phone"));
    }
    
    @Test
    void assertIsSameIdentifierMatchesQuotedPostgreSQLName() {
        assertTrue(WorkflowSQLUtils.isSameIdentifier(new DatabaseIdentifierContext(IdentifierCasePolicyFactory.newLowerCasePolicySet()), IdentifierScope.TABLE, "\"Phone\"", "Phone"));
    }
    
    @Test
    void assertEscapeLiteralEscapesSingleQuote() {
        String actualValue = WorkflowSQLUtils.escapeLiteral("O'Brien");
        assertThat(actualValue, is("O''Brien"));
    }
    
    @Test
    void assertCreateAlgorithmFragmentFormatsProperties() {
        String actualFragment = WorkflowSQLUtils.createAlgorithmFragment(" AES ", Map.of("aes-key-value", " 123456 "));
        assertThat(actualFragment, is("TYPE(NAME='aes', PROPERTIES('aes-key-value'='123456'))"));
    }
    
    @Test
    void assertCreateAlgorithmFragmentEscapesLiterals() {
        String actualFragment = WorkflowSQLUtils.createAlgorithmFragment("AES'X", Map.of("a'b", "v'1"));
        assertThat(actualFragment, is("TYPE(NAME='aes''x', PROPERTIES('a''b'='v''1'))"));
    }
    
    @Test
    void assertCreateAlgorithmFragmentReturnsEmptyForBlankType() {
        String actualFragment = WorkflowSQLUtils.createAlgorithmFragment(" ", Map.of("aes-key-value", "123456"));
        assertThat(actualFragment, is(""));
    }
    
    @Test
    void assertCreateAlgorithmFragmentWithExactTypePreservesCase() {
        String actualFragment = WorkflowSQLUtils.createAlgorithmFragmentWithExactType(" SQL_HINT ", Map.of());
        assertThat(actualFragment, is("TYPE(NAME='SQL_HINT')"));
    }
    
    private static Stream<Arguments> getDistSQLKeywordCases() {
        return Stream.of(
                Arguments.of("quote if keyword", "if", "`if`"),
                Arguments.of("quote exists keyword", "exists", "`exists`"),
                Arguments.of("quote true keyword", "true", "`true`"),
                Arguments.of("quote false keyword", "false", "`false`"),
                Arguments.of("quote name keyword", "name", "`name`"),
                Arguments.of("quote cipher keyword", "cipher", "`cipher`"),
                Arguments.of("quote order keyword", "order", "`order`"),
                Arguments.of("quote type keyword", "type", "`type`"),
                Arguments.of("quote table keyword", "table", "`table`"),
                Arguments.of("quote from keyword", "from", "`from`"),
                Arguments.of("quote properties keyword", "properties", "`properties`"),
                Arguments.of("quote encrypt algorithm keyword", "encrypt_algorithm", "`encrypt_algorithm`"),
                Arguments.of("quote assisted query column keyword", "assisted_query_column", "`assisted_query_column`"),
                Arguments.of("quote mask algorithm keyword", "keep_from_x_to_y", "`keep_from_x_to_y`"),
                Arguments.of("quote uppercase algorithm keyword", "AES", "`AES`"));
    }
    
}
