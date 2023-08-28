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

import org.apache.shardingsphere.distsql.handler.query.RQLExecutor;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ComplexShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.HintShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.query.ShowDefaultShardingStrategyExecutor;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowDefaultShardingStrategyStatement;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShowDefaultShardingStrategyExecutorTest {
    
    @Test
    void assertGetRowData() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        ShardingRule rule1 = mock(ShardingRule.class);
        when(rule1.getConfiguration()).thenReturn(createRuleConfiguration1());
        when(database.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(rule1)));
        RQLExecutor<ShowDefaultShardingStrategyStatement> executor = new ShowDefaultShardingStrategyExecutor();
        Collection<LocalDataQueryResultRow> actual = executor.getRows(database, mock(ShowDefaultShardingStrategyStatement.class));
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
        assertThat(row.getCell(6), is("{algorithm-expression=ds_${user_id % 2}}"));
        ShardingRule rule2 = mock(ShardingRule.class);
        when(rule2.getConfiguration()).thenReturn(createRuleConfiguration2());
        when(database.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(rule2)));
        executor = new ShowDefaultShardingStrategyExecutor();
        actual = executor.getRows(database, mock(ShowDefaultShardingStrategyStatement.class));
        assertThat(actual.size(), is(2));
        iterator = actual.iterator();
        row = iterator.next();
        assertThat(row.getCell(1), is("TABLE"));
        assertThat(row.getCell(2), is("STANDARD"));
        assertThat(row.getCell(3), is("use_id"));
        assertThat(row.getCell(4), is("database_inline"));
        assertThat(row.getCell(5), is("INLINE"));
        assertThat(row.getCell(6), is("{algorithm-expression=ds_${user_id % 2}}"));
        row = iterator.next();
        assertThat(row.getCell(1), is("DATABASE"));
        assertThat(row.getCell(2), is("HINT"));
        assertThat(row.getCell(3), is(""));
        assertThat(row.getCell(4), is("database_inline"));
        assertThat(row.getCell(5), is("INLINE"));
        assertThat(row.getCell(6), is("{algorithm-expression=ds_${user_id % 2}}"));
    }
    
    @Test
    void assertGetColumnNames() {
        RQLExecutor<ShowDefaultShardingStrategyStatement> executor = new ShowDefaultShardingStrategyExecutor();
        Collection<String> columns = executor.getColumnNames();
        assertThat(columns.size(), is(6));
        Iterator<String> iterator = columns.iterator();
        assertThat(iterator.next(), is("name"));
        assertThat(iterator.next(), is("type"));
        assertThat(iterator.next(), is("sharding_column"));
        assertThat(iterator.next(), is("sharding_algorithm_name"));
        assertThat(iterator.next(), is("sharding_algorithm_type"));
        assertThat(iterator.next(), is("sharding_algorithm_props"));
    }
    
    private RuleConfiguration createRuleConfiguration1() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.getShardingAlgorithms().put("database_inline", new AlgorithmConfiguration("INLINE", PropertiesBuilder.build(new Property("algorithm-expression", "ds_${user_id % 2}"))));
        result.setDefaultTableShardingStrategy(new NoneShardingStrategyConfiguration());
        result.setDefaultDatabaseShardingStrategy(new ComplexShardingStrategyConfiguration("use_id, order_id", "database_inline"));
        return result;
    }
    
    private RuleConfiguration createRuleConfiguration2() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.getShardingAlgorithms().put("database_inline", new AlgorithmConfiguration("INLINE", PropertiesBuilder.build(new Property("algorithm-expression", "ds_${user_id % 2}"))));
        result.setDefaultTableShardingStrategy(new StandardShardingStrategyConfiguration("use_id", "database_inline"));
        result.setDefaultDatabaseShardingStrategy(new HintShardingStrategyConfiguration("database_inline"));
        return result;
    }
}
