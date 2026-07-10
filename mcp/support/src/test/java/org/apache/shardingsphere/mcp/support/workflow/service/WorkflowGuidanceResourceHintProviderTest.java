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

package org.apache.shardingsphere.mcp.support.workflow.service;

import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowKind;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowRequest;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkflowGuidanceResourceHintProviderTest {
    
    @Test
    void assertCreateResourcesToReadWithEncryptRule() {
        List<String> actual = extractResourceUris(new WorkflowGuidanceResourceHintProvider().createResourcesToRead(createSnapshot("encrypt.rule", "logic_db", "", "t_order")));
        assertTrue(actual.contains("shardingsphere://features/encrypt/algorithms"));
        assertTrue(actual.contains("shardingsphere://features/encrypt/databases/logic_db/rules"));
        assertTrue(actual.contains("shardingsphere://features/encrypt/databases/logic_db/tables/t_order/rules"));
    }
    
    @Test
    void assertCreateResourcesToReadWithShardingTableRule() {
        List<String> actual = extractResourceUris(new WorkflowGuidanceResourceHintProvider().createResourcesToRead(createSnapshot("sharding.table.rule", "logic_db", "", "t_order")));
        assertTrue(actual.contains("shardingsphere://features/sharding/algorithm-plugins"));
        assertTrue(actual.contains("shardingsphere://features/sharding/databases/logic_db/table-rules"));
        assertTrue(actual.contains("shardingsphere://features/sharding/databases/logic_db/table-nodes"));
        assertTrue(actual.contains("shardingsphere://databases/logic_db/storage-units"));
        assertTrue(actual.contains("shardingsphere://databases/logic_db/single-tables"));
        assertTrue(actual.contains("shardingsphere://databases/logic_db/single-tables/t_order"));
        assertTrue(actual.contains("shardingsphere://features/sharding/databases/logic_db/tables/t_order/table-rule"));
        assertTrue(actual.contains("shardingsphere://features/sharding/databases/logic_db/tables/t_order/nodes"));
    }
    
    @Test
    void assertCreateResourcesToReadWithDescriptorResources() {
        List<String> actual = extractResourceUris(new WorkflowGuidanceResourceHintProvider().createResourcesToRead(createSnapshot("encrypt.rule", "logic_db", "", "t_order")));
        assertTrue(actual.contains("shardingsphere://workflow/test-resource"));
    }
    
    @Test
    void assertCreateResourcesToReadWithShardingComponentCleanup() {
        List<String> actual = extractResourceUris(new WorkflowGuidanceResourceHintProvider().createResourcesToRead(createSnapshot("sharding.component.cleanup", "logic_db", "", "")));
        assertThat(actual, is(List.of(
                "shardingsphere://features/sharding/databases/logic_db/algorithms",
                "shardingsphere://features/sharding/databases/logic_db/key-generators",
                "shardingsphere://features/sharding/databases/logic_db/auditors",
                "shardingsphere://features/sharding/databases/logic_db/unused-algorithms",
                "shardingsphere://features/sharding/databases/logic_db/unused-key-generators",
                "shardingsphere://features/sharding/databases/logic_db/unused-auditors")));
    }
    
    @Test
    void assertCreateResourcesToReadWithColumnWorkflow() {
        List<String> actual = extractResourceUris(new WorkflowGuidanceResourceHintProvider().createResourcesToRead(createSnapshot("encrypt.table", "logic_db", "public", "t_order")));
        assertThat(actual, is(List.of("shardingsphere://databases/logic_db/schemas/public/tables/t_order/columns")));
    }
    
    private List<String> extractResourceUris(final List<Map<String, Object>> resourcesToRead) {
        List<String> result = new LinkedList<>();
        for (Map<String, Object> each : resourcesToRead) {
            result.add((String) each.get("uri"));
        }
        return result;
    }
    
    private WorkflowContextSnapshot createSnapshot(final String workflowKind, final String database, final String schema, final String table) {
        WorkflowContextSnapshot result = new WorkflowContextSnapshot();
        result.setWorkflowKind(WorkflowKind.valueOf(workflowKind));
        WorkflowRequest request = new WorkflowRequest();
        request.setDatabase(database);
        request.setSchema(schema);
        request.setTable(table);
        result.setRequest(request);
        return result;
    }
}
