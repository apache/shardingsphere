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

package org.apache.shardingsphere.core.routing.type.broadcast;

import org.apache.shardingsphere.api.config.rule.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.rule.TableRuleConfiguration;
import org.apache.shardingsphere.core.parsing.antlr.sql.statement.ddl.DDLStatement;
import org.apache.shardingsphere.core.parsing.parser.sql.dql.DQLStatement;
import org.apache.shardingsphere.core.parsing.parser.token.IndexToken;
import org.apache.shardingsphere.core.routing.type.RoutingResult;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

public final class TableBroadcastRoutingEngineTest {
    
    private ShardingRule shardingRule;
    
    @Before
    public void setEngineContext() {
        TableRuleConfiguration tableRuleConfig = new TableRuleConfiguration("t_order", "ds${0..1}.t_order_${0..2}");
        tableRuleConfig.setLogicIndex("t_order_index");
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        shardingRule = new ShardingRule(shardingRuleConfig, Arrays.asList("ds0", "ds1"));
    }
    
    @Test
    public void assertRoutingResultForDQLStatement() {
        assertThat(createDQLStatementRoutingResult(), instanceOf(RoutingResult.class));
    }
    
    @Test
    public void assertIsSingleRoutingForDQLStatement() {
        assertFalse(createDQLStatementRoutingResult().isSingleRouting());
    }
    
    @Test
    public void assertTableUnitsForDQLStatement() {
        RoutingResult routingResult = createDQLStatementRoutingResult();
        assertThat(routingResult.getTableUnits().getTableUnits().size(), is(0));
    }
    
    @Test
    public void assertRoutingResultForDDLStatement() {
        assertThat(createDDLStatementRoutingResult(), instanceOf(RoutingResult.class));
    }
    
    @Test
    public void assertIsSingleRoutingForDDLStatement() {
        assertFalse(createDDLStatementRoutingResult().isSingleRouting());
    }
    
    @Test
    public void assertTableUnitsForDDLStatement() {
        RoutingResult routingResult = createDDLStatementRoutingResult();
        assertThat(routingResult.getTableUnits().getTableUnits().size(), is(6));
    }
    
    private RoutingResult createDQLStatementRoutingResult() {
        TableBroadcastRoutingEngine tableBroadcastRoutingEngine = new TableBroadcastRoutingEngine(shardingRule, new DQLStatement());
        return tableBroadcastRoutingEngine.route();
    }
    
    private RoutingResult createDDLStatementRoutingResult() {
        DDLStatement ddlStatement = new DDLStatement();
        ddlStatement.setLogicSQL("CREATE INDEX t_order_index on t_order");
        ddlStatement.addSQLToken(new IndexToken(13, 25, "t_order"));
        TableBroadcastRoutingEngine tableBroadcastRoutingEngine = new TableBroadcastRoutingEngine(shardingRule, ddlStatement);
        return tableBroadcastRoutingEngine.route();
    }
}
