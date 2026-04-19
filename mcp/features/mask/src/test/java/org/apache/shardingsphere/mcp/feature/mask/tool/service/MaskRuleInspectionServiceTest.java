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

import org.apache.shardingsphere.mcp.context.MCPFeatureContext;
import org.apache.shardingsphere.mcp.feature.spi.MCPFeatureQueryFacade;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MaskRuleInspectionServiceTest {
    
    private final MaskRuleInspectionService service = new MaskRuleInspectionService();
    
    @Test
    void assertQueryMaskRulesForDatabase() {
        MCPFeatureContext requestContext = mock(MCPFeatureContext.class);
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(requestContext.getQueryFacade()).thenReturn(queryFacade);
        when(queryFacade.query("logic_db", "", "SHOW MASK RULES FROM logic_db"))
                .thenReturn(List.of(Map.of("logic_column", "phone", "mask_algorithm", "MASK_FROM_X_TO_Y", "props", "from-x=4")));
        List<Map<String, Object>> actual = service.queryMaskRules(requestContext, "logic_db", "");
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).get("column"), is("phone"));
        assertThat(actual.get(0).get("algorithm_type"), is("MASK_FROM_X_TO_Y"));
    }
    
    @Test
    void assertQueryMaskRulesForTable() {
        MCPFeatureContext requestContext = mock(MCPFeatureContext.class);
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(requestContext.getQueryFacade()).thenReturn(queryFacade);
        when(queryFacade.query("logic_db", "", "SHOW MASK RULE orders FROM logic_db"))
                .thenReturn(List.of(Map.of("column", "phone", "algorithm_type", "MD5")));
        List<Map<String, Object>> actual = service.queryMaskRules(requestContext, "logic_db", "orders");
        assertThat(actual.get(0).get("column"), is("phone"));
    }
    
    @Test
    void assertQueryMaskAlgorithms() {
        MCPFeatureContext requestContext = mock(MCPFeatureContext.class);
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(requestContext.getQueryFacade()).thenReturn(queryFacade);
        when(queryFacade.queryWithAnyDatabase("SHOW MASK ALGORITHM PLUGINS")).thenReturn(List.of(Map.of("type", "MD5")));
        List<Map<String, Object>> actual = service.queryMaskAlgorithms(requestContext);
        assertThat(actual.size(), is(1));
        verify(queryFacade).queryWithAnyDatabase("SHOW MASK ALGORITHM PLUGINS");
    }
    
    @Test
    void assertEnrichMaskAlgorithms() {
        List<Map<String, Object>> actual = service.enrichMaskAlgorithms(List.of(Map.of("type", "MD5"), Map.of("type", "CUSTOM")));
        assertThat(actual.get(0).get("source"), is("builtin"));
        assertThat(actual.get(1).get("source"), is("custom-spi"));
    }
}
