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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkflowSqlUtilsTest {
    
    @Test
    void assertCheckSafeIdentifierAllowsSafeIdentifier() {
        assertDoesNotThrow(() -> WorkflowSqlUtils.checkSafeIdentifier("table", "orders_01"));
        assertTrue(WorkflowSqlUtils.isSafeIdentifier("orders_01"));
    }
    
    @Test
    void assertCheckSafeIdentifierRejectsUnsafeIdentifier() {
        Exception actual = assertThrows(RuntimeException.class, () -> WorkflowSqlUtils.checkSafeIdentifier("table", "bad table"));
        assertThat(actual.getMessage(), is("table `bad table` contains unsupported characters. Only unquoted SQL identifiers are supported in V1."));
    }
    
    @Test
    void assertCreateAlgorithmFragmentFormatsProperties() {
        String actual = WorkflowSqlUtils.createAlgorithmFragment(" AES ", Map.of("aes-key-value", " 123456 "));
        assertThat(actual, is("TYPE(NAME='aes', PROPERTIES('aes-key-value'='123456'))"));
    }
    
    @Test
    void assertCreateAlgorithmFragmentReturnsEmptyForBlankType() {
        String actual = WorkflowSqlUtils.createAlgorithmFragment(" ", Map.of("aes-key-value", "123456"));
        assertThat(actual, is(""));
    }
    
    @Test
    void assertParsePropertyEntriesSkipsMalformedEntriesAndTrimsValues() {
        Map<String, String> actual = WorkflowSqlUtils.parsePropertyEntries(List.of("aes-key-value = 123456 ", " malformed ", " iv = abc "));
        assertThat(actual.size(), is(2));
        assertThat(actual.get("aes-key-value"), is("123456"));
        assertThat(actual.get("iv"), is("abc"));
    }
}
