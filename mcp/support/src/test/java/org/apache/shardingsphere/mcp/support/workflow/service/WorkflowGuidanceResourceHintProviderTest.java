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

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class WorkflowGuidanceResourceHintProviderTest {
    
    @Test
    void assertCreateResourcesToReadWithDescriptorResources() {
        List<Map<String, Object>> actual = new WorkflowGuidanceResourceHintProvider().createResourcesToRead(createSnapshot("encrypt.rule", "logic_db", "", "t_order"));
        assertThat(actual, is(List.of(Map.of(
                "uri", "shardingsphere://workflow/test-resource",
                "resource_kind", "detail",
                "purpose", "read_first",
                "reason", "Workflow descriptor resource used by descriptor catalog loader tests.",
                "source_field", "resources_to_read"))));
    }
    
    @Test
    void assertCreateResourcesToReadWithSnapshotResources() {
        WorkflowContextSnapshot snapshot = createSnapshot("test.workflow", "logic_db", "", "t_order");
        snapshot.getResourceUriTemplates().add("shardingsphere://workflow/test-resource");
        List<Map<String, Object>> actual = new WorkflowGuidanceResourceHintProvider().createResourcesToRead(snapshot);
        assertThat(actual.getFirst().get("uri"), is("shardingsphere://workflow/test-resource"));
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
