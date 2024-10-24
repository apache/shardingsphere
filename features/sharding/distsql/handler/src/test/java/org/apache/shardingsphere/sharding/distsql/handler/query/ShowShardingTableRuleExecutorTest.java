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

package org.apache.shardingsphere.sharding.distsql.handler.query;

import org.apache.shardingsphere.distsql.statement.DistSQLStatement;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.rule.scope.DatabaseRuleConfiguration;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.audit.ShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.statement.ShowShardingTableRulesStatement;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.test.it.distsql.handler.engine.query.DistSQLDatabaseRuleQueryExecutorTest;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.util.stream.Stream;

import static org.mockito.Mockito.mock;

class ShowShardingTableRuleExecutorTest extends DistSQLDatabaseRuleQueryExecutorTest {
    
    ShowShardingTableRuleExecutorTest() {
        super(mock(ShardingRule.class));
    }
    
    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    void assertExecuteQuery(final String name, final DatabaseRuleConfiguration ruleConfig, final DistSQLStatement sqlStatement,
                            final Collection<LocalDataQueryResultRow> expected) throws SQLException {
        assertQueryResultRows(ruleConfig, sqlStatement, expected);
    }
    
    private static class TestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
            return Stream.of(Arguments.arguments("normal", createRuleConfiguration(), new ShowShardingTableRulesStatement(null, null),
                    Collections.singleton(new LocalDataQueryResultRow("t_order", "ds_${0..1}.t_order_${0..1}", "",
                            "STANDARD", "user_id",
                            "INLINE", "{\"algorithm-expression\":\"ds_${user_id % 2}\"}", "STANDARD", "order_id",
                            "INLINE", "{\"algorithm-expression\":\"t_order_${order_id % 2}\"}",
                            "order_id", "SNOWFLAKE",
                            "", "DML_SHARDING_CONDITIONS", "true"))));
        }
        
        private ShardingRuleConfiguration createRuleConfiguration() {
            ShardingRuleConfiguration result = new ShardingRuleConfiguration();
            result.getTables().add(createShardingTableRuleConfiguration());
            result.setDefaultDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("user_id", "database_inline"));
            result.setDefaultTableShardingStrategy(new NoneShardingStrategyConfiguration());
            result.getShardingAlgorithms().put("database_inline", new AlgorithmConfiguration("INLINE", PropertiesBuilder.build(new Property("algorithm-expression", "ds_${user_id % 2}"))));
            result.getShardingAlgorithms().put("t_order_inline", new AlgorithmConfiguration("INLINE", PropertiesBuilder.build(new Property("algorithm-expression", "t_order_${order_id % 2}"))));
            result.getKeyGenerators().put("snowflake", new AlgorithmConfiguration("SNOWFLAKE", new Properties()));
            result.getAuditors().put("sharding_key_required_auditor", new AlgorithmConfiguration("DML_SHARDING_CONDITIONS", new Properties()));
            return result;
        }
        
        private ShardingTableRuleConfiguration createShardingTableRuleConfiguration() {
            ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration("t_order", "ds_${0..1}.t_order_${0..1}");
            result.setTableShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "t_order_inline"));
            result.setKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("order_id", "snowflake"));
            result.setAuditStrategy(new ShardingAuditStrategyConfiguration(Collections.singleton("sharding_key_required_auditor"), true));
            return result;
        }
    }
}
