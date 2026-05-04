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

package org.apache.shardingsphere.mcp.feature.encrypt.tool.service;

import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EncryptRuleInspectionServiceTest {
    
    private final EncryptRuleInspectionService service = new EncryptRuleInspectionService();
    
    @Test
    void assertQueryEncryptRulesForDatabase() {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.query("logic_db", "", "SHOW ENCRYPT RULES FROM logic_db"))
                .thenReturn(List.of(Map.of("logic_column", "phone", "assisted_query_column", "phone_assisted")));
        List<Map<String, Object>> actual = service.queryEncryptRules(queryFacade, "logic_db");
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).get("assisted_query_column"), is("phone_assisted"));
    }
    
    @Test
    void assertQueryEncryptRules() {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.query("logic_db", "", "SHOW ENCRYPT TABLE RULE orders FROM logic_db"))
                .thenReturn(List.of(Map.of("logic_column", "phone", "like_query_column", "phone_like")));
        List<Map<String, Object>> actual = service.queryEncryptRules(queryFacade, "logic_db", "orders");
        assertThat(actual.get(0).get("like_query_column"), is("phone_like"));
    }
    
    @Test
    void assertQueryEncryptRulesQuotesUnicodeNames() {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.query("逻辑库", "", "SHOW ENCRYPT TABLE RULE `订单` FROM `逻辑库`"))
                .thenReturn(List.of(Map.of("logic_column", "phone")));
        List<Map<String, Object>> actual = service.queryEncryptRules(queryFacade, "逻辑库", "订单");
        assertThat(actual.get(0).get("logic_column"), is("phone"));
    }
    
    @Test
    void assertQueryEncryptRulesEscapesQuoteDelimiter() {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.query("逻`辑库", "", "SHOW ENCRYPT TABLE RULE `订``单` FROM `逻``辑库`"))
                .thenReturn(List.of(Map.of("logic_column", "phone")));
        List<Map<String, Object>> actual = service.queryEncryptRules(queryFacade, "逻`辑库", "订`单");
        assertThat(actual.get(0).get("logic_column"), is("phone"));
    }
    
    @Test
    void assertQueryEncryptAlgorithms() {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.queryWithAnyDatabase("SHOW ENCRYPT ALGORITHM PLUGINS")).thenReturn(List.of(Map.of("type", "AES"), Map.of("type", "CUSTOM")));
        List<Map<String, Object>> actual = service.queryEncryptAlgorithms(queryFacade);
        assertTrue((Boolean) actual.get(0).get("supports_decrypt"));
        assertThat(actual.get(0).get("required_properties"), is(List.of("aes-key-value")));
        assertThat(actual.get(0).get("optional_properties"), is(List.of("digest-algorithm-name")));
        assertThat(actual.get(0).get("secret_properties"), is(List.of("aes-key-value")));
        List<?> propertyTemplates = (List<?>) actual.get(0).get("property_templates");
        assertThat(((Map<?, ?>) propertyTemplates.get(0)).get("property_key"), is("aes-key-value"));
        assertThat(actual.get(1).get("type"), is("CUSTOM"));
        assertNull(actual.get(1).get("supports_like"));
        assertThat(actual.get(1).get("property_templates"), is(List.of()));
    }
}
