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

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkflowSQLUtilsTest {
    
    @Test
    void assertIsSafeIdentifier() {
        assertTrue(WorkflowSQLUtils.isSafeIdentifier("orders_01"));
    }
    
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
    void assertNormalizeIdentifierUnwrapsDelimitedIdentifier() {
        assertThat(WorkflowSQLUtils.normalizeIdentifier("`bad table`"), is("bad table"));
        assertThat(WorkflowSQLUtils.normalizeIdentifier("\"Order Detail\""), is("Order Detail"));
        assertThat(WorkflowSQLUtils.normalizeIdentifier("[Order Detail]"), is("Order Detail"));
    }
    
    @Test
    void assertFormatDistSQLIdentifierQuotesSafeIdentifier() {
        String actualValue = WorkflowSQLUtils.formatDistSQLIdentifier("orders_01");
        assertThat(actualValue, is("`orders_01`"));
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
    void assertFormatDistSQLIdentifierQuotesMixedCaseIdentifier() {
        String actualValue = WorkflowSQLUtils.formatDistSQLIdentifier("Phone");
        assertThat(actualValue, is("`Phone`"));
    }
    
    @Test
    void assertFormatDistSQLIdentifierQuotesUnicodeIdentifier() {
        String actualValue = WorkflowSQLUtils.formatDistSQLIdentifier("订单");
        assertThat(actualValue, is("`订单`"));
    }
    
    @Test
    void assertFormatDistSQLIdentifierEscapesQuoteDelimiter() {
        String actualValue = WorkflowSQLUtils.formatDistSQLIdentifier("bad`table");
        assertThat(actualValue, is("`bad``table`"));
    }
    
    @Test
    void assertFormatSQLIdentifierUsesMysqlQuoteStyle() {
        String actualValue = WorkflowSQLUtils.formatSQLIdentifier("MySQL", "key");
        assertThat(actualValue, is("`key`"));
    }
    
    @Test
    void assertFormatSQLIdentifierUsesFallbackQuoteStyle() {
        String actualValue = WorkflowSQLUtils.formatSQLIdentifier("", "orders");
        assertThat(actualValue, is("`orders`"));
    }
    
    @Test
    void assertFormatSQLIdentifierUsesPostgreSQLQuoteStyle() {
        String actualValue = WorkflowSQLUtils.formatSQLIdentifier("PostgreSQL", "orders");
        assertThat(actualValue, is("\"orders\""));
    }
    
    @Test
    void assertFormatSQLIdentifierPreservesDelimitedSafeIdentifier() {
        String actualValue = WorkflowSQLUtils.formatSQLIdentifier("PostgreSQL", "\"orders\"");
        assertThat(actualValue, is("\"orders\""));
    }
    
    @Test
    void assertIsSameIdentifierWithCaseInsensitiveDatabase() {
        assertTrue(WorkflowSQLUtils.isSameIdentifier("MySQL", "Phone", "phone"));
    }
    
    @Test
    void assertIsSameIdentifierWithCaseSensitiveDatabase() {
        assertFalse(WorkflowSQLUtils.isSameIdentifier("PostgreSQL", "Phone", "phone"));
    }
    
    @Test
    void assertEscapeLiteralEscapesSingleQuote() {
        String actualValue = WorkflowSQLUtils.escapeLiteral("O'Brien");
        assertThat(actualValue, is("O''Brien"));
    }
    
    @Test
    void assertCreatePropertiesTrimsValues() {
        Properties actualProperties = WorkflowSQLUtils.createProperties(Map.of("aes-key-value", " 123456 "));
        assertThat(actualProperties.getProperty("aes-key-value"), is("123456"));
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
    void assertParsePropertyEntriesSkipsMalformedEntriesAndTrimsValues() {
        Map<String, String> actualEntries = WorkflowSQLUtils.parsePropertyEntries(List.of("aes-key-value = 123456 ", " malformed ", " iv = abc "));
        assertThat(actualEntries.size(), is(2));
        assertThat(actualEntries.get("aes-key-value"), is("123456"));
        assertThat(actualEntries.get("iv"), is("abc"));
    }
    
    @Test
    void assertCreatePropertyMapReturnsEmptyForNull() {
        Map<String, String> actualEntries = WorkflowSQLUtils.createPropertyMap(null);
        assertThat(actualEntries, is(Map.of()));
    }
    
    @Test
    void assertCreatePropertyMapHandlesProperties() {
        Properties props = new Properties();
        props.setProperty("aes-key-value", " 123456 ");
        Map<String, String> actualEntries = WorkflowSQLUtils.createPropertyMap(props);
        assertThat(actualEntries, is(Map.of("aes-key-value", "123456")));
    }
    
    @Test
    void assertCreatePropertyMapHandlesMap() {
        Map<String, String> actualEntries = WorkflowSQLUtils.createPropertyMap(Map.of("aes-key-value", " 123456 "));
        assertThat(actualEntries, is(Map.of("aes-key-value", "123456")));
    }
    
    @Test
    void assertCreatePropertyMapHandlesString() {
        Map<String, String> actualEntries = WorkflowSQLUtils.createPropertyMap("{'aes-key-value':'123456','iv':'abc'}");
        assertThat(actualEntries, is(Map.of("aes-key-value", "123456", "iv", "abc")));
    }
}
