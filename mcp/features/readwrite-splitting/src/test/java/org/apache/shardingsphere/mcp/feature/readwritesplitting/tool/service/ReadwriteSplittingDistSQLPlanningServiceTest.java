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

package org.apache.shardingsphere.mcp.feature.readwritesplitting.tool.service;

import org.apache.shardingsphere.mcp.feature.readwritesplitting.tool.model.ReadwriteSplittingRuleWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.readwritesplitting.tool.model.ReadwriteSplittingStatusWorkflowRequest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ReadwriteSplittingDistSQLPlanningServiceTest {
    
    @Test
    void assertPlanCreateRule() {
        ReadwriteSplittingRuleWorkflowRequest request = createRuleRequest();
        assertThat(new ReadwriteSplittingRuleDistSQLPlanningService().planCreateRule(request).getSql(),
                is("CREATE READWRITE_SPLITTING RULE readwrite_ds (WRITE_STORAGE_UNIT=write_ds, READ_STORAGE_UNITS(read_ds_0, read_ds_1), "
                        + "TRANSACTIONAL_READ_QUERY_STRATEGY='DYNAMIC', TYPE(NAME='weight', PROPERTIES('read_ds_0'='2')))"));
    }
    
    @Test
    void assertPlanAlterRule() {
        assertThat(new ReadwriteSplittingRuleDistSQLPlanningService().planAlterRule(createRuleRequest()).getOperationType(), is("alter"));
    }
    
    @Test
    void assertPlanDropRule() {
        assertThat(new ReadwriteSplittingRuleDistSQLPlanningService().planDropRule("readwrite_ds").getSql(), is("DROP READWRITE_SPLITTING RULE readwrite_ds"));
    }
    
    @Test
    void assertPlanStatus() {
        ReadwriteSplittingStatusWorkflowRequest request = new ReadwriteSplittingStatusWorkflowRequest();
        request.setDatabase("logic_db");
        request.setRuleName("readwrite_ds");
        request.setStorageUnit("read_ds_0");
        request.setTargetStatus("enable");
        assertThat(new ReadwriteSplittingStatusDistSQLPlanningService().planStatus(request).getSql(),
                is("ALTER READWRITE_SPLITTING RULE readwrite_ds ENABLE read_ds_0 FROM logic_db"));
    }
    
    private ReadwriteSplittingRuleWorkflowRequest createRuleRequest() {
        ReadwriteSplittingRuleWorkflowRequest result = new ReadwriteSplittingRuleWorkflowRequest();
        result.setRuleName("readwrite_ds");
        result.setWriteStorageUnit("write_ds");
        result.setReadStorageUnits("read_ds_0,read_ds_1");
        result.setTransactionalReadQueryStrategy("dynamic");
        result.setLoadBalancerType("WEIGHT");
        result.putLoadBalancerProperties(Map.of("read_ds_0", "2"));
        return result;
    }
}
