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

package org.apache.shardingsphere.core.route.type.broadcast;

import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.core.optimize.transparent.statement.TransparentOptimizedStatement;
import org.apache.shardingsphere.core.parse.sql.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.core.parse.sql.segment.generic.TableSegment;
import org.apache.shardingsphere.core.parse.sql.statement.ddl.CreateIndexStatement;
import org.apache.shardingsphere.core.route.type.RoutingResult;
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
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        shardingRule = new ShardingRule(shardingRuleConfig, Arrays.asList("ds0", "ds1"));
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
        assertThat(routingResult.getRoutingUnits().size(), is(6));
    }
    
    private RoutingResult createDDLStatementRoutingResult() {
        CreateIndexStatement createIndexStatement = new CreateIndexStatement();
        createIndexStatement.getAllSQLSegments().add(new TableSegment(0, 0, "t_order"));
        createIndexStatement.setIndex(new IndexSegment(0, 0, "t_order_index"));
        return new TableBroadcastRoutingEngine(shardingRule, new TransparentOptimizedStatement(createIndexStatement)).route();
    }
}
