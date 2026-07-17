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

package org.apache.shardingsphere.mcp.feature.broadcast.tool.model;

import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class BroadcastWorkflowRequestTest {
    
    @Test
    void assertSetTablesFromCommaSeparatedValue() {
        BroadcastWorkflowRequest request = new BroadcastWorkflowRequest();
        request.setTables(" t_order, t_order_item ");
        assertThat(request.getTables(), is(List.of("t_order", "t_order_item")));
        assertThat(request.getTargetTables(), is(List.of("t_order", "t_order_item")));
    }
    
    @Test
    void assertGetTargetTablesFallsBackToTable() {
        BroadcastWorkflowRequest request = new BroadcastWorkflowRequest();
        request.setTable("t_order");
        assertThat(request.getTargetTables(), is(List.of("t_order")));
    }
    
    @Test
    void assertCopy() {
        BroadcastWorkflowRequest request = new BroadcastWorkflowRequest();
        request.setDatabase("logic_db");
        request.setTables("t_order");
        BroadcastWorkflowRequest actual = request.copy();
        assertThat(actual.getDatabase(), is("logic_db"));
        assertThat(actual.getTables(), is(List.of("t_order")));
    }
    
    @Test
    void assertMergePreservesPreviousTablesWhenCurrentEmpty() {
        BroadcastWorkflowRequest previous = new BroadcastWorkflowRequest();
        previous.setTables("t_order");
        BroadcastWorkflowRequest current = new BroadcastWorkflowRequest();
        current.setDatabase("logic_db");
        BroadcastWorkflowRequest actual = BroadcastWorkflowRequest.merge(previous, current);
        assertThat(actual.getDatabase(), is("logic_db"));
        assertThat(actual.getTables(), is(List.of("t_order")));
    }
    
    @Test
    void assertMergeSwitchesFromTablesToTable() {
        BroadcastWorkflowRequest previous = new BroadcastWorkflowRequest();
        previous.setTables("t_order,t_order_item");
        BroadcastWorkflowRequest current = new BroadcastWorkflowRequest();
        current.setTable("t_customer");
        BroadcastWorkflowRequest actual = BroadcastWorkflowRequest.merge(previous, current);
        assertThat(actual.getTable(), is("t_customer"));
        assertThat(actual.getTables(), is(List.of()));
        assertThat(actual.getTargetTables(), is(List.of("t_customer")));
    }
    
    @Test
    void assertMergeSwitchesFromTableToTables() {
        BroadcastWorkflowRequest previous = new BroadcastWorkflowRequest();
        previous.setTable("t_order");
        BroadcastWorkflowRequest current = new BroadcastWorkflowRequest();
        current.setTables("t_customer,t_address");
        BroadcastWorkflowRequest actual = BroadcastWorkflowRequest.merge(previous, current);
        assertThat(actual.getTable(), is("t_customer"));
        assertThat(actual.getTables(), is(List.of("t_customer", "t_address")));
        assertThat(actual.getTargetTables(), is(List.of("t_customer", "t_address")));
    }
    
    @Test
    void assertMergeCopiesPlainPreviousRequest() {
        WorkflowRequest previous = new WorkflowRequest();
        previous.setDatabase("logic_db");
        previous.setTable("t_order");
        BroadcastWorkflowRequest actual = BroadcastWorkflowRequest.merge(previous, new BroadcastWorkflowRequest());
        assertThat(actual.getDatabase(), is("logic_db"));
        assertThat(actual.getTargetTables(), is(List.of("t_order")));
    }
}
