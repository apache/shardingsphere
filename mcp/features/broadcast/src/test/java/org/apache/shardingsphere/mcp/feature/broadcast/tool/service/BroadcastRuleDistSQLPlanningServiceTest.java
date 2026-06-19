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

package org.apache.shardingsphere.mcp.feature.broadcast.tool.service;

import org.apache.shardingsphere.mcp.api.protocol.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.support.workflow.model.RuleArtifact;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BroadcastRuleDistSQLPlanningServiceTest {
    
    private final BroadcastRuleDistSQLPlanningService service = new BroadcastRuleDistSQLPlanningService();
    
    @Test
    void assertPlanCreateRule() {
        RuleArtifact actual = service.planCreateRule(List.of("t_order", "order"));
        assertThat(actual.getOperationType(), is("create"));
        assertThat(actual.getSql(), is("CREATE BROADCAST TABLE RULE t_order, `order`"));
    }
    
    @Test
    void assertPlanCreateRuleFormatsReservedIdentifiers() {
        RuleArtifact actual = service.planCreateRule(List.of("from", "table", "name"));
        assertThat(actual.getSql(), is("CREATE BROADCAST TABLE RULE `from`, `table`, `name`"));
    }
    
    @Test
    void assertPlanDropRule() {
        RuleArtifact actual = service.planDropRule(List.of("t_order"));
        assertThat(actual.getOperationType(), is("drop"));
        assertThat(actual.getSql(), is("DROP BROADCAST TABLE RULE t_order"));
    }
    
    @Test
    void assertRejectUnsupportedIdentifier() {
        assertThrows(MCPInvalidRequestException.class, () -> service.planCreateRule(List.of("bad`table")));
    }
}
