/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.core.routing.type.broadcast;

import io.shardingjdbc.core.api.config.ShardingRuleConfiguration;
import io.shardingjdbc.core.api.config.TableRuleConfiguration;
import io.shardingjdbc.core.parsing.parser.sql.ddl.DDLStatement;
import io.shardingjdbc.core.parsing.parser.sql.dql.DQLStatement;
import io.shardingjdbc.core.parsing.parser.token.IndexToken;
import io.shardingjdbc.core.rule.ShardingRule;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

public final class TableBroadcastRoutingEngineTest {
    
    private ShardingRule shardingRule;
    
    private DDLStatement ddlStatement;
    
    @Before
    public void setEngineContext() {
        TableRuleConfiguration tableRuleConfig = new TableRuleConfiguration();
        tableRuleConfig.setLogicTable("t_order");
        tableRuleConfig.setActualDataNodes("ds${0..1}.t_order_${0..2}");
        tableRuleConfig.setLogicIndex("t_order_index");
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        shardingRule = new ShardingRule(shardingRuleConfig, Arrays.asList("ds0", "ds1"));
        ddlStatement = new DDLStatement();
    }
    
    @Test
    public void assertRouteWithoutLogicTableNames() {
        TableBroadcastRoutingEngine tableBroadcastRoutingEngine = new TableBroadcastRoutingEngine(shardingRule, new DQLStatement());
        tableBroadcastRoutingEngine.route();
    }
    
    @Test
    public void assertRouteWithLogicTableNames() {
        ddlStatement.getSqlTokens().add(new IndexToken(13, "t_order_index", "t_order"));
        TableBroadcastRoutingEngine tableBroadcastRoutingEngine = new TableBroadcastRoutingEngine(shardingRule, ddlStatement);
        tableBroadcastRoutingEngine.route();
    }
}

