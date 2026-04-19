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

import org.apache.shardingsphere.mcp.context.MCPFeatureContext;
import org.apache.shardingsphere.mcp.feature.spi.MCPFeatureQueryFacade;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EncryptRuleInspectionServiceTest {
    
    private final EncryptRuleInspectionService service = new EncryptRuleInspectionService();
    
    @Test
    void assertQueryEncryptRulesForDatabase() {
        MCPFeatureContext requestContext = mock(MCPFeatureContext.class);
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(requestContext.getQueryFacade()).thenReturn(queryFacade);
        when(queryFacade.query("logic_db", "", "SHOW ENCRYPT RULES FROM logic_db"))
                .thenReturn(List.of(Map.of("logic_column", "phone", "assisted_query", "phone_assisted")));
        List<Map<String, Object>> actual = service.queryEncryptRules(requestContext, "logic_db", "");
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).get("assisted_query_column"), is("phone_assisted"));
    }
    
    @Test
    void assertQueryEncryptRulesForTable() {
        MCPFeatureContext requestContext = mock(MCPFeatureContext.class);
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(requestContext.getQueryFacade()).thenReturn(queryFacade);
        when(queryFacade.query("logic_db", "", "SHOW ENCRYPT TABLE RULE orders FROM logic_db"))
                .thenReturn(List.of(Map.of("logic_column", "phone", "like_query", "phone_like")));
        List<Map<String, Object>> actual = service.queryEncryptRules(requestContext, "logic_db", "orders");
        assertThat(actual.get(0).get("like_query_column"), is("phone_like"));
    }
    
    @Test
    void assertQueryEncryptAlgorithms() {
        MCPFeatureContext requestContext = mock(MCPFeatureContext.class);
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(requestContext.getQueryFacade()).thenReturn(queryFacade);
        when(queryFacade.queryWithAnyDatabase("SHOW ENCRYPT ALGORITHM PLUGINS")).thenReturn(List.of(Map.of("type", "AES")));
        List<Map<String, Object>> actual = service.queryEncryptAlgorithms(requestContext);
        assertThat(actual.size(), is(1));
        verify(queryFacade).queryWithAnyDatabase("SHOW ENCRYPT ALGORITHM PLUGINS");
    }
    
    @Test
    void assertEnrichEncryptAlgorithms() {
        List<Map<String, Object>> actual = service.enrichEncryptAlgorithms(List.of(Map.of("type", "AES"), Map.of("type", "CUSTOM")));
        assertThat(actual.get(0).get("source"), is("builtin"));
        assertTrue((Boolean) actual.get(0).get("supports_decrypt"));
        assertThat(actual.get(1).get("source"), is("custom-spi"));
        assertThat(actual.get(1).get("supports_like"), is((Object) null));
    }
}
