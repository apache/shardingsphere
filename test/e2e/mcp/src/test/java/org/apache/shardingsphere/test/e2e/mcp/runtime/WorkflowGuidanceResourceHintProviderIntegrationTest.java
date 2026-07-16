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

package org.apache.shardingsphere.test.e2e.mcp.runtime;

import org.apache.shardingsphere.mcp.feature.readwritesplitting.ReadwriteSplittingFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.shadow.ShadowFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.sharding.ShardingFeatureDefinition;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowKind;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowRequest;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowGuidanceResourceHintProvider;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkflowGuidanceResourceHintProviderIntegrationTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("workflowResourceHints")
    void assertResolveCrossModuleResourceHints(final String scenarioName, final WorkflowKind workflowKind,
                                               final List<String> resourceUriTemplates, final List<String> expectedUris) {
        WorkflowContextSnapshot snapshot = createSnapshot(workflowKind);
        snapshot.getResourceUriTemplates().addAll(resourceUriTemplates);
        List<String> actual = new WorkflowGuidanceResourceHintProvider().createResourcesToRead(snapshot).stream()
                .map(each -> String.valueOf(each.get("uri"))).toList();
        assertTrue(actual.containsAll(expectedUris));
    }
    
    private static Stream<Arguments> workflowResourceHints() {
        List<String> tableResourceUriTemplates = List.of(
                ShadowFeatureDefinition.STORAGE_UNITS_RESOURCE_URI,
                ShadowFeatureDefinition.SINGLE_TABLES_RESOURCE_URI,
                ShadowFeatureDefinition.SINGLE_TABLE_RESOURCE_URI);
        List<String> expectedTableResourceUris = List.of(
                "shardingsphere://databases/logic_db/storage-units",
                "shardingsphere://databases/logic_db/single-tables",
                "shardingsphere://databases/logic_db/single-tables/orders");
        return Stream.of(
                Arguments.of("readwrite-splitting", ReadwriteSplittingFeatureDefinition.RULE_WORKFLOW_KIND,
                        List.of(ReadwriteSplittingFeatureDefinition.STORAGE_UNITS_RESOURCE_URI), List.of("shardingsphere://databases/logic_db/storage-units")),
                Arguments.of("shadow", ShadowFeatureDefinition.RULE_WORKFLOW_KIND, tableResourceUriTemplates, expectedTableResourceUris),
                Arguments.of("sharding", ShardingFeatureDefinition.TABLE_RULE_WORKFLOW_KIND,
                        List.of(ShardingFeatureDefinition.STORAGE_UNITS_RESOURCE_URI, ShardingFeatureDefinition.SINGLE_TABLES_RESOURCE_URI,
                                ShardingFeatureDefinition.SINGLE_TABLE_RESOURCE_URI),
                        expectedTableResourceUris));
    }
    
    private WorkflowContextSnapshot createSnapshot(final WorkflowKind workflowKind) {
        WorkflowContextSnapshot result = new WorkflowContextSnapshot();
        result.setWorkflowKind(workflowKind);
        WorkflowRequest request = new WorkflowRequest();
        request.setDatabase("logic_db");
        request.setTable("orders");
        result.setRequest(request);
        return result;
    }
}
