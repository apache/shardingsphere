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

package org.apache.shardingsphere.mcp.feature.broadcast.resource.handler;

import org.apache.shardingsphere.mcp.api.payload.MCPSuccessPayload;
import org.apache.shardingsphere.mcp.api.resource.MCPUriVariables;
import org.apache.shardingsphere.mcp.feature.broadcast.tool.service.BroadcastRuleInspectionService;
import org.apache.shardingsphere.mcp.support.MCPFeatureRequestContext;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BroadcastRulesHandlerTest {
    
    @Test
    void assertHandle() {
        try (MockedConstruction<BroadcastRuleInspectionService> mocked = mockConstruction(BroadcastRuleInspectionService.class)) {
            BroadcastRulesHandler handler = new BroadcastRulesHandler();
            BroadcastRuleInspectionService ruleInspectionService = mocked.constructed().getFirst();
            MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
            MCPFeatureRequestContext requestContext = mock(MCPFeatureRequestContext.class);
            when(requestContext.getQueryFacade()).thenReturn(queryFacade);
            when(ruleInspectionService.queryBroadcastRules(queryFacade, "logic_db"))
                    .thenReturn(List.of(Map.of("broadcast_table", "t_order"), Map.of("broadcast_table", "t_order_item")));
            MCPSuccessPayload actual = handler.handle(requestContext, new MCPUriVariables(Map.of("database", "logic_db")));
            verify(ruleInspectionService).queryBroadcastRules(queryFacade, "logic_db");
            assertThat(((Collection<?>) actual.toPayload().get("items")).size(), is(2));
            assertThat(actual.toPayload().get("self_uri"), is("shardingsphere://features/broadcast/databases/logic_db/rules"));
        }
    }
}
