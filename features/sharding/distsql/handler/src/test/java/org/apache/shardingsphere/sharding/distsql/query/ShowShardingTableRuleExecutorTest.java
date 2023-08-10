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
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.audit.ShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.query.ShowShardingTableRuleExecutor;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowShardingTableRulesStatement;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShowShardingTableRuleExecutorTest {
    
    @Test
    void assertGetRowData() {
        RQLExecutor<ShowShardingTableRulesStatement> executor = new ShowShardingTableRuleExecutor();
        Collection<LocalDataQueryResultRow> actual = executor.getRows(mockDatabase(), mock(ShowShardingTableRulesStatement.class));
        assertThat(actual.size(), is(1));
        Iterator<LocalDataQueryResultRow> iterator = actual.iterator();
        LocalDataQueryResultRow row = iterator.next();
        assertThat(row.getCell(1), is("t_order"));
        assertThat(row.getCell(2), is("ds_${0..1}.t_order_${0..1}"));
        assertThat(row.getCell(3), is(""));
        assertThat(row.getCell(4), is("STANDARD"));
        assertThat(row.getCell(5), is("user_id"));
        assertThat(row.getCell(6), is("INLINE"));
        assertThat(row.getCell(7), is("algorithm-expression=ds_${user_id % 2}"));
        assertThat(row.getCell(8), is("STANDARD"));
        assertThat(row.getCell(9), is("order_id"));
        assertThat(row.getCell(10), is("INLINE"));
        assertThat(row.getCell(11), is("algorithm-expression=t_order_${order_id % 2}"));
        assertThat(row.getCell(12), is("order_id"));
        assertThat(row.getCell(13), is("SNOWFLAKE"));
        assertThat(row.getCell(14), is(""));
        assertThat(row.getCell(15), is("DML_SHARDING_CONDITIONS"));
        assertThat(row.getCell(16), is("true"));
    }
    
    @Test
    void assertGetColumnNames() {
        RQLExecutor<ShowShardingTableRulesStatement> executor = new ShowShardingTableRuleExecutor();
        Collection<String> columns = executor.getColumnNames();
        assertThat(columns.size(), is(16));
        Iterator<String> iterator = columns.iterator();
        assertThat(iterator.next(), is("table"));
        assertThat(iterator.next(), is("actual_data_nodes"));
        assertThat(iterator.next(), is("actual_data_sources"));
        assertThat(iterator.next(), is("database_strategy_type"));
        assertThat(iterator.next(), is("database_sharding_column"));
        assertThat(iterator.next(), is("database_sharding_algorithm_type"));
        assertThat(iterator.next(), is("database_sharding_algorithm_props"));
        assertThat(iterator.next(), is("table_strategy_type"));
        assertThat(iterator.next(), is("table_sharding_column"));
        assertThat(iterator.next(), is("table_sharding_algorithm_type"));
        assertThat(iterator.next(), is("table_sharding_algorithm_props"));
        assertThat(iterator.next(), is("key_generate_column"));
        assertThat(iterator.next(), is("key_generator_type"));
        assertThat(iterator.next(), is("key_generator_props"));
        assertThat(iterator.next(), is("auditor_types"));
        assertThat(iterator.next(), is("allow_hint_disable"));
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        ShardingRule rule = mock(ShardingRule.class);
        when(rule.getConfiguration()).thenReturn(createRuleConfiguration());
        when(result.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(rule)));
        return result;
    }
    
    private RuleConfiguration createRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.getTables().add(createShardingTableRuleConfiguration());
        result.setDefaultDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("user_id", "database_inline"));
        result.setDefaultTableShardingStrategy(new NoneShardingStrategyConfiguration());
        result.getShardingAlgorithms().put("database_inline", createShardingInlineAlgorithmConfiguration("ds_${user_id % 2}"));
        result.getShardingAlgorithms().put("t_order_inline", createShardingInlineAlgorithmConfiguration("t_order_${order_id % 2}"));
        result.getKeyGenerators().put("snowflake", createKeyGeneratorConfiguration());
        result.getAuditors().put("sharding_key_required_auditor", createAuditorConfiguration());
        return result;
    }
    
    private ShardingTableRuleConfiguration createShardingTableRuleConfiguration() {
        ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration("t_order", "ds_${0..1}.t_order_${0..1}");
        result.setTableShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "t_order_inline"));
        result.setKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("order_id", "snowflake"));
        result.setAuditStrategy(new ShardingAuditStrategyConfiguration(Collections.singleton("sharding_key_required_auditor"), true));
        return result;
    }
    
    private AlgorithmConfiguration createShardingInlineAlgorithmConfiguration(final String algorithmExpression) {
        return new AlgorithmConfiguration("INLINE", PropertiesBuilder.build(new Property("algorithm-expression", algorithmExpression)));
    }
    
    private AlgorithmConfiguration createKeyGeneratorConfiguration() {
        return new AlgorithmConfiguration("SNOWFLAKE", new Properties());
    }
    
    private AlgorithmConfiguration createAuditorConfiguration() {
        return new AlgorithmConfiguration("DML_SHARDING_CONDITIONS", new Properties());
    }
}
