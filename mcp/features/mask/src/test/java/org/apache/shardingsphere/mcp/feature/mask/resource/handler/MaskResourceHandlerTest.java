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

package org.apache.shardingsphere.mcp.feature.mask.resource.handler;

import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.api.resource.MCPResourceHandler;
import org.apache.shardingsphere.mcp.api.resource.MCPUriVariables;
import org.apache.shardingsphere.mcp.feature.mask.tool.service.MaskRuleInspectionService;
import org.apache.shardingsphere.mcp.support.database.MCPDatabaseHandlerContext;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.internal.configuration.plugins.Plugins;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MaskResourceHandlerTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("assertGetUriPatternArguments")
    void assertGetUriPattern(final String name, final MCPResourceHandler<MCPDatabaseHandlerContext> handler, final String expectedUriPattern) {
        assertThat(handler.getResourceDescriptor().getUriTemplate(), is(expectedUriPattern));
    }

    @Test
    void assertGetMaskRuleResourceKind() {
        assertThat(new MaskRuleHandler().getResourceDescriptor().getResourceKind(), is("list"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("assertHandleArguments")
    void assertHandle(final String name, final MCPResourceHandler<MCPDatabaseHandlerContext> handler, final Map<String, String> uriVariables,
                      final String expectedDatabase, final String expectedTable, final List<Map<String, Object>> maskRules,
                      final List<Map<String, Object>> maskAlgorithms, final String expectedSelfUri, final String expectedParentUri) throws ReflectiveOperationException {
        MaskRuleInspectionService ruleInspectionService = mock(MaskRuleInspectionService.class);
        Plugins.getMemberAccessor().set(((Object) handler).getClass().getDeclaredField("ruleInspectionService"), handler, ruleInspectionService);
        MCPDatabaseHandlerContext databaseContext = mock(MCPDatabaseHandlerContext.class);
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(databaseContext.getQueryFacade()).thenReturn(queryFacade);
        when(ruleInspectionService.queryMaskRules(queryFacade, expectedDatabase)).thenReturn(maskRules);
        when(ruleInspectionService.queryMaskRules(queryFacade, expectedDatabase, expectedTable)).thenReturn(maskRules);
        when(ruleInspectionService.queryMaskAlgorithms(queryFacade)).thenReturn(maskAlgorithms);
        MCPResponse actual = handler.handle(databaseContext, new MCPUriVariables(uriVariables));
        assertThat(((List<?>) actual.toPayload().get("items")).size(), is(1));
        assertThat(actual.toPayload().get("self_uri"), is(expectedSelfUri));
        if (expectedParentUri.isEmpty()) {
            return;
        }
        assertThat(((Map<?, ?>) actual.toPayload().get("parent_resource")).get("uri"), is(expectedParentUri));
    }

    private static Stream<Arguments> assertGetUriPatternArguments() {
        return Stream.of(
                Arguments.of("mask algorithms URI", new MaskAlgorithmsHandler(), "shardingsphere://features/mask/algorithms"),
                Arguments.of("mask rules URI", new MaskRulesHandler(), "shardingsphere://features/mask/databases/{database}/rules"),
                Arguments.of("mask table rule URI", new MaskRuleHandler(), "shardingsphere://features/mask/databases/{database}/tables/{table}/rules"));
    }

    private static Stream<Arguments> assertHandleArguments() {
        return Stream.of(
                Arguments.of("mask algorithms", new MaskAlgorithmsHandler(), Map.of(), "", "", List.of(), List.of(Map.of("type", "MD5")),
                        "shardingsphere://features/mask/algorithms", ""),
                Arguments.of("mask rules", new MaskRulesHandler(), Map.of("database", "logic_db"), "logic_db", "", List.of(Map.of("column", "phone")), List.of(),
                        "shardingsphere://features/mask/databases/logic_db/rules", ""),
                Arguments.of("mask table rule", new MaskRuleHandler(), Map.of("database", "logic_db", "table", "orders"), "logic_db", "orders", List.of(Map.of("column", "phone")), List.of(),
                        "shardingsphere://features/mask/databases/logic_db/tables/orders/rules", "shardingsphere://features/mask/databases/logic_db/rules"));
    }
}
