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

package org.apache.shardingsphere.mcp.feature.shadow.tool.service;

import org.apache.shardingsphere.mcp.api.protocol.exception.MCPQueryFailedException;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ShadowInspectionServiceTest {
    
    @Test
    void assertQueryRules() {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        new ShadowInspectionService().queryRules(queryFacade, "logic_db");
        verify(queryFacade).query("logic_db", "SHOW SHADOW RULES FROM logic_db");
    }
    
    @Test
    void assertQueryRule() {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        new ShadowInspectionService().queryRule(queryFacade, "logic_db", "shadow_rule");
        verify(queryFacade).query("logic_db", "SHOW SHADOW RULE shadow_rule FROM logic_db");
    }
    
    @Test
    void assertQueryTableRule() {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        new ShadowInspectionService().queryTableRule(queryFacade, "logic_db", "t_order");
        verify(queryFacade).query("logic_db", "SHOW SHADOW TABLE RULE t_order FROM logic_db");
    }
    
    @Test
    void assertQueryAlgorithmPlugins() {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.queryWithAnyDatabase("SHOW SHADOW ALGORITHM PLUGINS")).thenReturn(List.of(Map.of("type", "VALUE_MATCH")));
        List<Map<String, Object>> actual = new ShadowInspectionService().queryAlgorithmPlugins(queryFacade);
        assertThat(actual.size(), is(1));
        assertThat(String.valueOf(actual.getFirst().get("property_guidance")), containsString("operation"));
    }
    
    @Test
    void assertQueryAlgorithmPluginsWithUnavailableDistSQL() {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.queryWithAnyDatabase("SHOW SHADOW ALGORITHM PLUGINS"))
                .thenThrow(new MCPQueryFailedException("syntax error near 'SHADOW ALGORITHM PLUGINS'", new SQLSyntaxErrorException("syntax error")));
        List<Map<String, Object>> actual = new ShadowInspectionService().queryAlgorithmPlugins(queryFacade);
        assertTrue(actual.stream().anyMatch(each -> "VALUE_MATCH".equals(each.get("type"))));
        assertTrue(actual.stream().anyMatch(each -> "VALUE_MATCH".equals(each.get("type")) && String.valueOf(each.get("property_guidance")).contains("operation")));
    }
    
    @Test
    void assertQueryAlgorithmPluginsPropagatesQueryFailure() {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.queryWithAnyDatabase("SHOW SHADOW ALGORITHM PLUGINS"))
                .thenThrow(new MCPQueryFailedException("Connection refused.", new SQLException("Connection refused.")));
        assertThrows(MCPQueryFailedException.class, () -> new ShadowInspectionService().queryAlgorithmPlugins(queryFacade));
    }
}
