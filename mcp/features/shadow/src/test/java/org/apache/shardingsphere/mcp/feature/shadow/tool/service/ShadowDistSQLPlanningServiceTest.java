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

package org.apache.shardingsphere.mcp.feature.shadow.tool.service;

import org.apache.shardingsphere.mcp.feature.shadow.tool.model.ShadowDefaultAlgorithmWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.shadow.tool.model.ShadowRuleWorkflowRequest;
import org.apache.shardingsphere.mcp.support.workflow.model.RuleArtifact;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShadowDistSQLPlanningServiceTest {
    
    @Test
    void assertPlanCreateRule() {
        RuleArtifact actual = new ShadowDistSQLPlanningService().planCreateRule(createRuleRequest());
        assertThat(actual.getSql(), is("CREATE SHADOW RULE shadow_rule(SOURCE=demo_ds, SHADOW=demo_ds_shadow, "
                + "t_order(TYPE(NAME='value_match', PROPERTIES('column'='user_id', 'operation'='insert', 'value'='1'))))"));
        assertNoForbiddenArtifacts(actual.getSql());
    }
    
    @Test
    void assertPlanCreateRuleFormatsReservedIdentifiers() {
        ShadowRuleWorkflowRequest request = createRuleRequest();
        request.setRuleName("type");
        request.setSourceStorageUnit("from");
        request.setShadowStorageUnit("table");
        request.setTableName("order");
        RuleArtifact actual = new ShadowDistSQLPlanningService().planCreateRule(request);
        assertThat(actual.getSql(), is("CREATE SHADOW RULE `type`(SOURCE=`from`, SHADOW=`table`, "
                + "`order`(TYPE(NAME='value_match', PROPERTIES('column'='user_id', 'operation'='insert', 'value'='1'))))"));
    }
    
    @Test
    void assertPlanAlterRule() {
        assertTrue(new ShadowDistSQLPlanningService().planAlterRule(createRuleRequest()).getSql().startsWith("ALTER SHADOW RULE shadow_rule"));
    }
    
    @Test
    void assertPlanDropRule() {
        assertThat(new ShadowDistSQLPlanningService().planDropRule("shadow_rule").getSql(), is("DROP SHADOW RULE shadow_rule"));
    }
    
    @Test
    void assertPlanDefaultAlgorithm() {
        ShadowDefaultAlgorithmWorkflowRequest request = new ShadowDefaultAlgorithmWorkflowRequest();
        request.setAlgorithmType("SQL_HINT");
        String actual = new ShadowDistSQLPlanningService().planCreateDefaultAlgorithm(request).getSql();
        assertThat(actual, is("CREATE DEFAULT SHADOW ALGORITHM TYPE(NAME='sql_hint')"));
        assertFalse(actual.contains(" FROM "));
    }
    
    @Test
    void assertPlanCleanup() {
        assertThat(new ShadowDistSQLPlanningService().planDropAlgorithm("unused_algorithm").getSql(), is("DROP SHADOW ALGORITHM unused_algorithm"));
    }
    
    private ShadowRuleWorkflowRequest createRuleRequest() {
        ShadowRuleWorkflowRequest result = new ShadowRuleWorkflowRequest();
        result.setRuleName("shadow_rule");
        result.setSourceStorageUnit("demo_ds");
        result.setShadowStorageUnit("demo_ds_shadow");
        result.setTableName("t_order");
        result.setAlgorithmType("VALUE_MATCH");
        result.putAlgorithmProperties(Map.of("operation", "insert", "column", "user_id", "value", "1"));
        return result;
    }
    
    private void assertNoForbiddenArtifacts(final String sql) {
        assertFalse(sql.contains("CREATE TABLE"));
        assertFalse(sql.contains("CREATE INDEX"));
        assertFalse(sql.contains("REGISTER STORAGE UNIT"));
        assertFalse(sql.contains("MIGRATE"));
    }
}
