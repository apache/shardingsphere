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

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkflowSqlUtilsTest {
    
    @Test
    void assertIsSafeIdentifier() {
        assertTrue(WorkflowSqlUtils.isSafeIdentifier("orders_01"));
    }
    
    @Test
    void assertCheckSafeIdentifierAllowsSafeIdentifier() {
        assertDoesNotThrow(() -> WorkflowSqlUtils.checkSafeIdentifier("table", "orders_01"));
    }
    
    @Test
    void assertCheckSafeIdentifierRejectsUnsafeIdentifier() {
        Exception actualException = assertThrows(RuntimeException.class, () -> WorkflowSqlUtils.checkSafeIdentifier("table", "bad table"));
        assertThat(actualException.getMessage(), is("table `bad table` contains unsupported characters. Only unquoted SQL identifiers are supported in V1."));
    }
    
    @Test
    void assertTrimToEmptyReturnsEmptyForNull() {
        String actualValue = WorkflowSqlUtils.trimToEmpty(null);
        assertThat(actualValue, is(""));
    }
    
    @Test
    void assertEscapeLiteralEscapesSingleQuote() {
        String actualValue = WorkflowSqlUtils.escapeLiteral("O'Brien");
        assertThat(actualValue, is("O''Brien"));
    }
    
    @Test
    void assertCreatePropertiesTrimsValues() {
        Properties actualProperties = WorkflowSqlUtils.createProperties(Map.of("aes-key-value", " 123456 "));
        assertThat(actualProperties.getProperty("aes-key-value"), is("123456"));
    }
    
    @Test
    void assertCreateAlgorithmFragmentFormatsProperties() {
        String actualFragment = WorkflowSqlUtils.createAlgorithmFragment(" AES ", Map.of("aes-key-value", " 123456 "));
        assertThat(actualFragment, is("TYPE(NAME='aes', PROPERTIES('aes-key-value'='123456'))"));
    }
    
    @Test
    void assertCreateAlgorithmFragmentReturnsEmptyForBlankType() {
        String actualFragment = WorkflowSqlUtils.createAlgorithmFragment(" ", Map.of("aes-key-value", "123456"));
        assertThat(actualFragment, is(""));
    }
    
    @Test
    void assertParsePropertyEntriesSkipsMalformedEntriesAndTrimsValues() {
        Map<String, String> actualEntries = WorkflowSqlUtils.parsePropertyEntries(List.of("aes-key-value = 123456 ", " malformed ", " iv = abc "));
        assertThat(actualEntries.size(), is(2));
        assertThat(actualEntries.get("aes-key-value"), is("123456"));
        assertThat(actualEntries.get("iv"), is("abc"));
    }
    
    @Test
    void assertCreatePropertyMapReturnsEmptyForNull() {
        Map<String, String> actualEntries = WorkflowSqlUtils.createPropertyMap(null);
        assertThat(actualEntries, is(Map.of()));
    }
    
    @Test
    void assertCreatePropertyMapHandlesProperties() {
        Properties props = new Properties();
        props.setProperty("aes-key-value", " 123456 ");
        Map<String, String> actualEntries = WorkflowSqlUtils.createPropertyMap(props);
        assertThat(actualEntries, is(Map.of("aes-key-value", "123456")));
    }
    
    @Test
    void assertCreatePropertyMapHandlesMap() {
        Map<String, String> actualEntries = WorkflowSqlUtils.createPropertyMap(Map.of("aes-key-value", " 123456 "));
        assertThat(actualEntries, is(Map.of("aes-key-value", "123456")));
    }
    
    @Test
    void assertCreatePropertyMapHandlesString() {
        Map<String, String> actualEntries = WorkflowSqlUtils.createPropertyMap("{'aes-key-value':'123456','iv':'abc'}");
        assertThat(actualEntries, is(Map.of("aes-key-value", "123456", "iv", "abc")));
    }
}
