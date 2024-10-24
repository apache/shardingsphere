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
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.HintShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.statement.ShowDefaultShardingStrategyStatement;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

import static org.mockito.Mockito.mock;

class ShowDefaultShardingStrategyExecutorTest extends DistSQLDatabaseRuleQueryExecutorTest {
    
    ShowDefaultShardingStrategyExecutorTest() {
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
            return Stream.of(Arguments.arguments("withNullShardingStrategy", new ShardingRuleConfiguration(), new ShowDefaultShardingStrategyStatement(null),
                    Arrays.asList(new LocalDataQueryResultRow("TABLE", "", "", "", "", ""), new LocalDataQueryResultRow("DATABASE", "", "", "", "", ""))),
                    Arguments.arguments("withNoneShardingStrategyType", createRuleConfigurationWithNoneShardingStrategyType(), new ShowDefaultShardingStrategyStatement(null),
                            Arrays.asList(new LocalDataQueryResultRow("TABLE", "NONE", "", "", "", ""), new LocalDataQueryResultRow("DATABASE", "NONE", "", "", "", ""))),
                    Arguments.arguments("withShardingStrategyType", createRuleConfigurationWithShardingStrategyType(), new ShowDefaultShardingStrategyStatement(null),
                            Arrays.asList(
                                    new LocalDataQueryResultRow("TABLE", "STANDARD", "use_id", "database_inline", "INLINE", "{\"algorithm-expression\":\"ds_${user_id % 2}\"}"),
                                    new LocalDataQueryResultRow("DATABASE", "HINT", "", "database_inline", "INLINE", "{\"algorithm-expression\":\"ds_${user_id % 2}\"}"))));
        }
        
        private ShardingRuleConfiguration createRuleConfigurationWithNoneShardingStrategyType() {
            ShardingRuleConfiguration result = new ShardingRuleConfiguration();
            result.setDefaultTableShardingStrategy(new NoneShardingStrategyConfiguration());
            result.setDefaultDatabaseShardingStrategy(new NoneShardingStrategyConfiguration());
            return result;
        }
        
        private ShardingRuleConfiguration createRuleConfigurationWithShardingStrategyType() {
            ShardingRuleConfiguration result = new ShardingRuleConfiguration();
            result.getShardingAlgorithms().put("database_inline", new AlgorithmConfiguration("INLINE", PropertiesBuilder.build(new Property("algorithm-expression", "ds_${user_id % 2}"))));
            result.setDefaultTableShardingStrategy(new StandardShardingStrategyConfiguration("use_id", "database_inline"));
            result.setDefaultDatabaseShardingStrategy(new HintShardingStrategyConfiguration("database_inline"));
            return result;
        }
    }
}
