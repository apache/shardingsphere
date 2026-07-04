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

package org.apache.shardingsphere.mcp.feature.readwritesplitting.resource.handler;

import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.api.resource.MCPUriVariables;
import org.apache.shardingsphere.mcp.feature.readwritesplitting.ReadwriteSplittingFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.readwritesplitting.tool.service.ReadwriteSplittingInspectionService;
import org.apache.shardingsphere.mcp.support.database.MCPDatabaseHandlerContext;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReadwriteSplittingRuleStatusHandlerTest {
    
    @Test
    void assertGetContextType() {
        assertThat(new ReadwriteSplittingRuleStatusHandler().getContextType(), is(MCPDatabaseHandlerContext.class));
    }
    
    @Test
    void assertGetResourceUriTemplate() {
        assertThat(new ReadwriteSplittingRuleStatusHandler().getResourceUriOrTemplate(), is(ReadwriteSplittingFeatureDefinition.RULE_STATUS_RESOURCE_URI));
    }
    
    @Test
    void assertHandle() {
        ReadwriteSplittingInspectionService inspectionService = mock(ReadwriteSplittingInspectionService.class);
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        MCPDatabaseHandlerContext databaseContext = mock(MCPDatabaseHandlerContext.class);
        when(databaseContext.getQueryFacade()).thenReturn(queryFacade);
        when(inspectionService.queryRuleStatus(queryFacade, "logic_db", "readwrite_ds")).thenReturn(List.of(Map.of("storage_unit", "read_ds_0")));
        MCPResponse actual = new ReadwriteSplittingRuleStatusHandler(inspectionService).handle(databaseContext, new MCPUriVariables(Map.of("database", "logic_db", "rule", "readwrite_ds")));
        verify(inspectionService).queryRuleStatus(queryFacade, "logic_db", "readwrite_ds");
        assertThat(((Collection<?>) actual.toPayload().get("items")).size(), is(1));
        assertThat(actual.toPayload().get("self_uri"), is("shardingsphere://features/readwrite-splitting/databases/logic_db/rules/readwrite_ds/status"));
        assertThat(((Map<?, ?>) actual.toPayload().get("parent_resource")).get("uri"), is("shardingsphere://features/readwrite-splitting/databases/logic_db/status"));
    }
}
