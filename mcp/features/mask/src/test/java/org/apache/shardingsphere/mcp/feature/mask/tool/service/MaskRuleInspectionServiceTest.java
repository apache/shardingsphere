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

import org.apache.shardingsphere.mcp.api.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.api.exception.MCPQueryFailedException;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowQueryResult;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MaskRuleInspectionServiceTest {
    
    private final MaskRuleInspectionService service = new MaskRuleInspectionService();
    
    @Test
    void assertQueryMaskRulesForDatabase() {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.query("logic_db", "SHOW MASK RULES FROM logic_db"))
                .thenReturn(List.of(Map.of("column", "phone", "algorithm_type", "MASK_FROM_X_TO_Y", "algorithm_props", "from-x=4")));
        List<Map<String, Object>> actual = service.queryMaskRules(queryFacade, "logic_db");
        assertThat(actual.size(), is(1));
        assertThat(actual.getFirst().get("column"), is("phone"));
        assertThat(actual.getFirst().get("algorithm_type"), is("MASK_FROM_X_TO_Y"));
        assertThat(actual.getFirst().get("algorithm_props"), is("from-x=4"));
    }
    
    @Test
    void assertQueryMaskRulesForDatabaseWithUnavailableDistSQL() {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.query("logic_db", "SHOW MASK RULES FROM logic_db"))
                .thenThrow(new MCPQueryFailedException("syntax error near 'MASK RULES FROM logic_db'", new SQLSyntaxErrorException("syntax error")));
        assertTrue(service.queryMaskRules(queryFacade, "logic_db").isEmpty());
    }
    
    @Test
    void assertQueryMaskRules() {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.query("logic_db", "SHOW MASK RULE orders FROM logic_db"))
                .thenReturn(List.of(Map.of("column", "phone", "algorithm_type", "MD5")));
        List<Map<String, Object>> actual = service.queryMaskRules(queryFacade, "logic_db", "orders");
        assertThat(actual.getFirst().get("column"), is("phone"));
    }
    
    @Test
    void assertQueryMaskRulesWithUnavailableDistSQL() {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.query("logic_db", "SHOW MASK RULE orders FROM logic_db"))
                .thenThrow(new MCPQueryFailedException("syntax error near 'MASK RULE orders FROM logic_db'", new SQLSyntaxErrorException("syntax error")));
        assertTrue(service.queryMaskRules(queryFacade, "logic_db", "orders").isEmpty());
    }
    
    @Test
    void assertQueryMaskRulesPropagatesQueryFailure() {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.query("logic_db", "SHOW MASK RULE orders FROM logic_db"))
                .thenThrow(new MCPQueryFailedException("Connection refused.", new SQLException("Connection refused.")));
        assertThrows(MCPQueryFailedException.class, () -> service.queryMaskRules(queryFacade, "logic_db", "orders"));
    }
    
    @Test
    void assertQueryMaskRulesQuotesUnicodeNames() {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.query("逻辑库", "SHOW MASK RULE `订单` FROM `逻辑库`"))
                .thenReturn(List.of(Map.of("column", "phone", "algorithm_type", "MD5")));
        List<Map<String, Object>> actual = service.queryMaskRules(queryFacade, "逻辑库", "订单");
        assertThat(actual.getFirst().get("column"), is("phone"));
    }
    
    @Test
    void assertQueryMaskRulesRejectsBackQuote() {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        assertThrows(MCPInvalidRequestException.class, () -> service.queryMaskRules(queryFacade, "逻`辑库", "订`单"));
    }
    
    @Test
    void assertQueryMaskAlgorithms() {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.queryWithAnyDatabase("SHOW MASK ALGORITHM PLUGINS")).thenReturn(List.of(Map.of("type", "MASK_FROM_X_TO_Y"), Map.of("type", "CUSTOM")));
        WorkflowQueryResult actual = service.queryMaskAlgorithms(queryFacade);
        assertThat(actual.getRows().getFirst().get("type"), is("MASK_FROM_X_TO_Y"));
        assertThat(actual.getRows().getFirst().get("required_properties"), is(List.of("from-x", "to-y", "replace-char")));
        assertThat(actual.getRows().getFirst().get("optional_properties"), is(List.of()));
        List<?> propertyTemplates = (List<?>) actual.getRows().getFirst().get("property_templates");
        assertThat(((Map<?, ?>) propertyTemplates.getFirst()).get("property_key"), is("from-x"));
        assertThat(actual.getRows().get(1).get("type"), is("CUSTOM"));
        assertThat(actual.getRows().get(1).get("property_templates"), is(List.of()));
        assertTrue(actual.isAvailabilityConfirmed());
    }
    
    @Test
    void assertQueryMaskAlgorithmsWithUnavailableDistSQL() {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.queryWithAnyDatabase("SHOW MASK ALGORITHM PLUGINS"))
                .thenThrow(new MCPQueryFailedException("syntax error near 'MASK ALGORITHM PLUGINS'", new SQLSyntaxErrorException("syntax error")));
        WorkflowQueryResult actual = service.queryMaskAlgorithms(queryFacade);
        assertThat(actual.getRows().getFirst().get("type"), is("KEEP_FIRST_N_LAST_M"));
        assertThat(actual.getRows().getFirst().get("required_properties"), is(List.of("first-n", "last-m", "replace-char")));
        assertFalse(actual.isAvailabilityConfirmed());
    }
    
    @Test
    void assertQueryMaskAlgorithmsPropagatesQueryFailure() {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.queryWithAnyDatabase("SHOW MASK ALGORITHM PLUGINS"))
                .thenThrow(new MCPQueryFailedException("Connection refused.", new SQLException("Connection refused.")));
        assertThrows(MCPQueryFailedException.class, () -> service.queryMaskAlgorithms(queryFacade));
    }
}
