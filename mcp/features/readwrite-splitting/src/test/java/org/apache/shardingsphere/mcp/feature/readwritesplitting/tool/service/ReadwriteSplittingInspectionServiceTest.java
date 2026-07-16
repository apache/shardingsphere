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

package org.apache.shardingsphere.mcp.feature.readwritesplitting.tool.service;

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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReadwriteSplittingInspectionServiceTest {
    
    @Test
    void assertQueryRules() {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        new ReadwriteSplittingInspectionService().queryRules(queryFacade, "logic_db");
        verify(queryFacade).query(eq("logic_db"), eq("SHOW READWRITE_SPLITTING RULES FROM logic_db"));
    }
    
    @Test
    void assertQueryRuleStatus() {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        new ReadwriteSplittingInspectionService().queryRuleStatus(queryFacade, "logic_db", "readwrite_ds");
        verify(queryFacade).query(eq("logic_db"), eq("SHOW STATUS FROM READWRITE_SPLITTING RULE readwrite_ds FROM logic_db"));
    }
    
    @Test
    void assertQueryLoadBalanceAlgorithmPlugins() {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.queryWithAnyDatabase("SHOW LOAD BALANCE ALGORITHM PLUGINS")).thenReturn(List.of(Map.of("type", "WEIGHT")));
        List<Map<String, Object>> actual = new ReadwriteSplittingInspectionService().queryLoadBalanceAlgorithmPlugins(queryFacade);
        assertThat(actual.getFirst().get("property_guidance").toString(), containsString("numeric property"));
    }
    
    @Test
    void assertQueryLoadBalanceAlgorithmPluginsWithUnavailableDistSQL() {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.queryWithAnyDatabase("SHOW LOAD BALANCE ALGORITHM PLUGINS"))
                .thenThrow(new MCPQueryFailedException("syntax error near 'LOAD BALANCE ALGORITHM PLUGINS'", new SQLSyntaxErrorException("syntax error")));
        List<Map<String, Object>> actual = new ReadwriteSplittingInspectionService().queryLoadBalanceAlgorithmPlugins(queryFacade);
        assertTrue(actual.stream().anyMatch(each -> "ROUND_ROBIN".equals(each.get("type"))));
        assertTrue(actual.stream().anyMatch(each -> "ROUND_ROBIN".equals(each.get("type")) && "No required properties.".equals(each.get("property_guidance"))));
    }
    
    @Test
    void assertQueryLoadBalanceAlgorithmPluginsPropagatesQueryFailure() {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.queryWithAnyDatabase("SHOW LOAD BALANCE ALGORITHM PLUGINS"))
                .thenThrow(new MCPQueryFailedException("Connection refused.", new SQLException("Connection refused.")));
        assertThrows(MCPQueryFailedException.class, () -> new ReadwriteSplittingInspectionService().queryLoadBalanceAlgorithmPlugins(queryFacade));
    }
    
    @Test
    void assertQueryRuleCount() {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.query(eq("logic_db"), eq("COUNT READWRITE_SPLITTING RULE FROM logic_db"))).thenReturn(List.of(Map.of("count", 1)));
        assertThat(new ReadwriteSplittingInspectionService().queryRuleCount(queryFacade, "logic_db"), is(List.of(Map.of("count", 1))));
    }
}
