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

package org.apache.shardingsphere.sharding.distsql.query;

import org.apache.shardingsphere.distsql.handler.engine.DistSQLConnectionContext;
import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecuteEngine;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.HintShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.statement.ShowDefaultShardingStrategyStatement;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShowDefaultShardingStrategyExecutorTest {
    
    @Test
    void assertExecuteQueryWithNullShardingStrategy() throws SQLException {
        DistSQLQueryExecuteEngine engine = new DistSQLQueryExecuteEngine(
                mock(ShowDefaultShardingStrategyStatement.class), "foo_db", mockContextManager(new ShardingRuleConfiguration()), mock(DistSQLConnectionContext.class));
        engine.executeQuery();
        List<LocalDataQueryResultRow> actual = new ArrayList<>(engine.getRows());
        assertThat(actual.size(), is(2));
        assertThat(actual.get(0).getCell(1), is("TABLE"));
        assertThat(actual.get(0).getCell(2), is(""));
        assertThat(actual.get(0).getCell(3), is(""));
        assertThat(actual.get(0).getCell(4), is(""));
        assertThat(actual.get(0).getCell(5), is(""));
        assertThat(actual.get(0).getCell(6), is(""));
        assertThat(actual.get(1).getCell(1), is("DATABASE"));
        assertThat(actual.get(1).getCell(2), is(""));
        assertThat(actual.get(1).getCell(3), is(""));
        assertThat(actual.get(1).getCell(4), is(""));
        assertThat(actual.get(1).getCell(5), is(""));
        assertThat(actual.get(1).getCell(6), is(""));
    }
    
    @Test
    void assertExecuteQueryWithNoneShardingStrategyType() throws SQLException {
        DistSQLQueryExecuteEngine engine = new DistSQLQueryExecuteEngine(
                mock(ShowDefaultShardingStrategyStatement.class), "foo_db", mockContextManager(createRuleConfigurationWithNoneShardingStrategyType()), mock(DistSQLConnectionContext.class));
        engine.executeQuery();
        List<LocalDataQueryResultRow> actual = new ArrayList<>(engine.getRows());
        assertThat(actual.size(), is(2));
        assertThat(actual.get(0).getCell(1), is("TABLE"));
        assertThat(actual.get(0).getCell(2), is("NONE"));
        assertThat(actual.get(0).getCell(3), is(""));
        assertThat(actual.get(0).getCell(4), is(""));
        assertThat(actual.get(0).getCell(5), is(""));
        assertThat(actual.get(0).getCell(6), is(""));
        assertThat(actual.get(1).getCell(1), is("DATABASE"));
        assertThat(actual.get(1).getCell(2), is("NONE"));
        assertThat(actual.get(1).getCell(3), is(""));
        assertThat(actual.get(1).getCell(4), is(""));
        assertThat(actual.get(1).getCell(5), is(""));
        assertThat(actual.get(1).getCell(6), is(""));
    }
    
    private ShardingRuleConfiguration createRuleConfigurationWithNoneShardingStrategyType() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.setDefaultTableShardingStrategy(new NoneShardingStrategyConfiguration());
        result.setDefaultDatabaseShardingStrategy(new NoneShardingStrategyConfiguration());
        return result;
    }
    
    @Test
    void assertExecuteQueryWithShardingStrategyType() throws SQLException {
        DistSQLQueryExecuteEngine engine = new DistSQLQueryExecuteEngine(
                mock(ShowDefaultShardingStrategyStatement.class), "foo_db", mockContextManager(createRuleConfigurationWithShardingStrategyType()), mock(DistSQLConnectionContext.class));
        engine.executeQuery();
        List<LocalDataQueryResultRow> actual = new ArrayList<>(engine.getRows());
        assertThat(actual.size(), is(2));
        assertThat(actual.get(0).getCell(1), is("TABLE"));
        assertThat(actual.get(0).getCell(2), is("STANDARD"));
        assertThat(actual.get(0).getCell(3), is("use_id"));
        assertThat(actual.get(0).getCell(4), is("database_inline"));
        assertThat(actual.get(0).getCell(5), is("INLINE"));
        assertThat(actual.get(0).getCell(6), is("{\"algorithm-expression\":\"ds_${user_id % 2}\"}"));
        assertThat(actual.get(1).getCell(1), is("DATABASE"));
        assertThat(actual.get(1).getCell(2), is("HINT"));
        assertThat(actual.get(1).getCell(3), is(""));
        assertThat(actual.get(1).getCell(4), is("database_inline"));
        assertThat(actual.get(1).getCell(5), is("INLINE"));
        assertThat(actual.get(1).getCell(6), is("{\"algorithm-expression\":\"ds_${user_id % 2}\"}"));
    }
    
    private ShardingRuleConfiguration createRuleConfigurationWithShardingStrategyType() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.getShardingAlgorithms().put("database_inline", new AlgorithmConfiguration("INLINE", PropertiesBuilder.build(new Property("algorithm-expression", "ds_${user_id % 2}"))));
        result.setDefaultTableShardingStrategy(new StandardShardingStrategyConfiguration("use_id", "database_inline"));
        result.setDefaultDatabaseShardingStrategy(new HintShardingStrategyConfiguration("database_inline"));
        return result;
    }
    
    private ContextManager mockContextManager(final ShardingRuleConfiguration ruleConfig) {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getDatabase("foo_db")).thenReturn(database);
        ShardingRule rule = mock(ShardingRule.class);
        when(rule.getConfiguration()).thenReturn(ruleConfig);
        when(database.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(rule)));
        return result;
    }
}
