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

package org.apache.shardingsphere.core.routing.router;

import org.apache.shardingsphere.api.HintManager;
import org.apache.shardingsphere.api.config.rule.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.rule.TableRuleConfiguration;
import org.apache.shardingsphere.api.config.strategy.HintShardingStrategyConfiguration;
import org.apache.shardingsphere.core.fixture.OrderDatabaseHintShardingAlgorithm;
import org.apache.shardingsphere.core.hint.HintManagerHolder;
import org.apache.shardingsphere.core.parsing.parser.sql.dql.DQLStatement;
import org.apache.shardingsphere.core.parsing.parser.sql.dql.select.SelectStatement;
import org.apache.shardingsphere.core.routing.router.sharding.DatabaseHintSQLRouter;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public final class DatabaseHintSQLRouterTest {

    private final HintManager hintManager = HintManager.getInstance();

    private DatabaseHintSQLRouter databaseHintSQLRouter;

    @After
    public void tearDown() {
        hintManager.close();
    }

    @Before
    public void setRouterContext() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfig = new TableRuleConfiguration();
        tableRuleConfig.setLogicTable("LOGIC_TABLE");
        tableRuleConfig.setActualDataNodes("ds${0..1}.table_${0..2}");
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(new HintShardingStrategyConfiguration(new OrderDatabaseHintShardingAlgorithm()));
        ShardingRule shardingRule = new ShardingRule(shardingRuleConfig, Arrays.asList("ds0", "ds1"));
        databaseHintSQLRouter = new DatabaseHintSQLRouter(shardingRule, true);
    }

    @Test
    public void assertParse() {
        assertThat(databaseHintSQLRouter.parse("select t from table t", false), instanceOf(SelectStatement.class));
    }

    @Test
    public void assertRoute() {
        hintManager.addDatabaseShardingValue(HintManagerHolder.DB_TABLE_NAME, 1);
        assertNotNull(databaseHintSQLRouter.route("select t from table t", Collections.emptyList(), new DQLStatement()));
    }
}

