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

package org.apache.shardingsphere.mcp.feature.encrypt.resource.handler;

import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.api.resource.MCPUriVariables;
import org.apache.shardingsphere.mcp.feature.encrypt.EncryptFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.encrypt.tool.service.EncryptRuleInspectionService;
import org.apache.shardingsphere.mcp.support.database.MCPDatabaseHandlerContext;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EncryptAlgorithmsHandlerTest {
    
    @Test
    void assertGetContextType() {
        assertThat(new EncryptAlgorithmsHandler().getContextType(), is(MCPDatabaseHandlerContext.class));
    }
    
    @Test
    void assertGetResourceUriTemplate() {
        assertThat(new EncryptAlgorithmsHandler().getResourceUriOrTemplate(), is(EncryptFeatureDefinition.ALGORITHMS_RESOURCE_URI));
    }
    
    @Test
    void assertHandle() throws ReflectiveOperationException {
        EncryptAlgorithmsHandler handler = new EncryptAlgorithmsHandler();
        EncryptRuleInspectionService ruleInspectionService = mock(EncryptRuleInspectionService.class);
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        MCPDatabaseHandlerContext databaseContext = mock(MCPDatabaseHandlerContext.class);
        when(databaseContext.getQueryFacade()).thenReturn(queryFacade);
        when(ruleInspectionService.queryEncryptAlgorithms(queryFacade)).thenReturn(List.of(Map.of("type", "AES")));
        Plugins.getMemberAccessor().set(EncryptAlgorithmsHandler.class.getDeclaredField("ruleInspectionService"), handler, ruleInspectionService);
        MCPResponse actual = handler.handle(databaseContext, new MCPUriVariables(Map.of()));
        verify(ruleInspectionService).queryEncryptAlgorithms(queryFacade);
        assertThat(((Collection<?>) actual.toPayload().get("items")).size(), is(1));
        assertThat(actual.toPayload().get("self_uri"), is("shardingsphere://features/encrypt/algorithms"));
    }
}
