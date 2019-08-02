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

package org.apache.shardingsphere.core.route.router;

import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.HintShardingStrategyConfiguration;
import org.apache.shardingsphere.api.hint.HintManager;
import org.apache.shardingsphere.core.database.DatabaseTypes;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.route.fixture.HintShardingAlgorithmFixture;
import org.apache.shardingsphere.core.route.router.sharding.DatabaseHintSQLRouter;
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
        shardingRuleConfig.getTableRuleConfigs().add(new TableRuleConfiguration("LOGIC_TABLE", "ds${0..1}.table_${0..2}"));
        shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(new HintShardingStrategyConfiguration(new HintShardingAlgorithmFixture()));
        ShardingRule shardingRule = new ShardingRule(shardingRuleConfig, Arrays.asList("ds0", "ds1"));
        databaseHintSQLRouter = new DatabaseHintSQLRouter(DatabaseTypes.getActualDatabaseType("MySQL"), shardingRule);
    }
    
    @Test
    public void assertParse() {
        assertThat(databaseHintSQLRouter.parse("select t from tbl t", false), instanceOf(SelectStatement.class));
    }
    
    @Test
    public void assertRoute() {
        hintManager.addDatabaseShardingValue("", 1);
        SelectStatement dqlStatement = new SelectStatement();
        assertNotNull(databaseHintSQLRouter.route("select t from tbl t", Collections.emptyList(), dqlStatement));
    }
}
