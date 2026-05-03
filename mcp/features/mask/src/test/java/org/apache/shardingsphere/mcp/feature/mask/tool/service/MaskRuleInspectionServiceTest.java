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

package org.apache.shardingsphere.mcp.feature.mask.tool.service;

import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MaskRuleInspectionServiceTest {
    
    private final MaskRuleInspectionService service = new MaskRuleInspectionService();
    
    @Test
    void assertQueryMaskRulesForDatabase() {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.query("logic_db", "", "SHOW MASK RULES FROM logic_db"))
                .thenReturn(List.of(Map.of("column", "phone", "algorithm_type", "MASK_FROM_X_TO_Y", "algorithm_props", "from-x=4")));
        List<Map<String, Object>> actual = service.queryMaskRules(queryFacade, "logic_db");
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).get("column"), is("phone"));
        assertThat(actual.get(0).get("algorithm_type"), is("MASK_FROM_X_TO_Y"));
        assertThat(actual.get(0).get("algorithm_props"), is("from-x=4"));
    }
    
    @Test
    void assertQueryMaskRule() {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.query("logic_db", "", "SHOW MASK RULE orders FROM logic_db"))
                .thenReturn(List.of(Map.of("column", "phone", "algorithm_type", "MD5")));
        List<Map<String, Object>> actual = service.queryMaskRule(queryFacade, "logic_db", "orders");
        assertThat(actual.get(0).get("column"), is("phone"));
    }
    
    @Test
    void assertQueryMaskRuleQuotesUnicodeNames() {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.query("逻辑库", "", "SHOW MASK RULE `订单` FROM `逻辑库`"))
                .thenReturn(List.of(Map.of("column", "phone", "algorithm_type", "MD5")));
        List<Map<String, Object>> actual = service.queryMaskRule(queryFacade, "逻辑库", "订单");
        assertThat(actual.get(0).get("column"), is("phone"));
    }
    
    @Test
    void assertQueryMaskRuleEscapesQuoteDelimiter() {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.query("逻`辑库", "", "SHOW MASK RULE `订``单` FROM `逻``辑库`"))
                .thenReturn(List.of(Map.of("column", "phone", "algorithm_type", "MD5")));
        List<Map<String, Object>> actual = service.queryMaskRule(queryFacade, "逻`辑库", "订`单");
        assertThat(actual.get(0).get("column"), is("phone"));
    }
    
    @Test
    void assertQueryMaskAlgorithms() {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.queryWithAnyDatabase("SHOW MASK ALGORITHM PLUGINS")).thenReturn(List.of(Map.of("type", "MD5"), Map.of("type", "CUSTOM")));
        List<Map<String, Object>> actual = service.queryMaskAlgorithms(queryFacade);
        assertThat(actual.get(0).get("type"), is("MD5"));
        assertThat(actual.get(1).get("type"), is("CUSTOM"));
    }
}
