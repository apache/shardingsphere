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
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ComplexShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.HintShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.statement.ShowDefaultShardingStrategyStatement;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShowDefaultShardingStrategyExecutorTest {
    
    private DistSQLQueryExecuteEngine engine;
    
    DistSQLQueryExecuteEngine setUp(final ShardingRuleConfiguration configuration) {
        return new DistSQLQueryExecuteEngine(mock(ShowDefaultShardingStrategyStatement.class), "foo_db", mockContextManager(configuration), mock(DistSQLConnectionContext.class));
    }
    
    private ContextManager mockContextManager(final ShardingRuleConfiguration configuration) {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getDatabase("foo_db")).thenReturn(database);
        ShardingRule rule = mock(ShardingRule.class);
        when(rule.getConfiguration()).thenReturn(configuration);
        when(database.getRuleMetaData().findSingleRule(ShardingRule.class)).thenReturn(Optional.of(rule));
        return result;
    }
    
    @Test
    void assertGetRowData1() throws SQLException {
        engine = setUp(createRuleConfiguration1());
        engine.executeQuery();
        Collection<LocalDataQueryResultRow> actual = engine.getRows();
        assertThat(actual.size(), is(2));
        Iterator<LocalDataQueryResultRow> iterator = actual.iterator();
        LocalDataQueryResultRow row = iterator.next();
        assertThat(row.getCell(1), is("TABLE"));
        assertThat(row.getCell(2), is("NONE"));
        assertThat(row.getCell(3), is(""));
        assertThat(row.getCell(4), is(""));
        assertThat(row.getCell(5), is(""));
        assertThat(row.getCell(6), is(""));
        row = iterator.next();
        assertThat(row.getCell(1), is("DATABASE"));
        assertThat(row.getCell(2), is("COMPLEX"));
        assertThat(row.getCell(3), is("use_id, order_id"));
        assertThat(row.getCell(4), is("database_inline"));
        assertThat(row.getCell(5), is("INLINE"));
        assertThat(row.getCell(6), is("{\"algorithm-expression\":\"ds_${user_id % 2}\"}"));
    }
    
    private ShardingRuleConfiguration createRuleConfiguration1() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.getShardingAlgorithms().put("database_inline", new AlgorithmConfiguration("INLINE", PropertiesBuilder.build(new Property("algorithm-expression", "ds_${user_id % 2}"))));
        result.setDefaultTableShardingStrategy(new NoneShardingStrategyConfiguration());
        result.setDefaultDatabaseShardingStrategy(new ComplexShardingStrategyConfiguration("use_id, order_id", "database_inline"));
        return result;
    }
    
    @Test
    void assertGetRowData2() throws SQLException {
        engine = setUp(createRuleConfiguration2());
        engine.executeQuery();
        Collection<LocalDataQueryResultRow> actual = engine.getRows();
        assertThat(actual.size(), is(2));
        Iterator<LocalDataQueryResultRow> iterator = actual.iterator();
        LocalDataQueryResultRow row = iterator.next();
        assertThat(row.getCell(1), is("TABLE"));
        assertThat(row.getCell(2), is("STANDARD"));
        assertThat(row.getCell(3), is("use_id"));
        assertThat(row.getCell(4), is("database_inline"));
        assertThat(row.getCell(5), is("INLINE"));
        assertThat(row.getCell(6), is("{\"algorithm-expression\":\"ds_${user_id % 2}\"}"));
        row = iterator.next();
        assertThat(row.getCell(1), is("DATABASE"));
        assertThat(row.getCell(2), is("HINT"));
        assertThat(row.getCell(3), is(""));
        assertThat(row.getCell(4), is("database_inline"));
        assertThat(row.getCell(5), is("INLINE"));
        assertThat(row.getCell(6), is("{\"algorithm-expression\":\"ds_${user_id % 2}\"}"));
    }
    
    private ShardingRuleConfiguration createRuleConfiguration2() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.getShardingAlgorithms().put("database_inline", new AlgorithmConfiguration("INLINE", PropertiesBuilder.build(new Property("algorithm-expression", "ds_${user_id % 2}"))));
        result.setDefaultTableShardingStrategy(new StandardShardingStrategyConfiguration("use_id", "database_inline"));
        result.setDefaultDatabaseShardingStrategy(new HintShardingStrategyConfiguration("database_inline"));
        return result;
    }
}
