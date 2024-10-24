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
import org.apache.shardingsphere.infra.config.rule.scope.DatabaseRuleConfiguration;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableReferenceRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.statement.ShowShardingTableReferenceRulesStatement;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.test.it.distsql.handler.engine.query.DistSQLDatabaseRuleQueryExecutorTest;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;

import static org.mockito.Mockito.mock;

class ShowShardingTableReferenceRuleExecutorTest extends DistSQLDatabaseRuleQueryExecutorTest {
    
    ShowShardingTableReferenceRuleExecutorTest() {
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
            return Stream.of(Arguments.arguments("normal", createRuleConfiguration(), new ShowShardingTableReferenceRulesStatement(null, null),
                    Collections.singleton(new LocalDataQueryResultRow("foo", "t_order,t_order_item"))),
                    Arguments.arguments("withSpecifiedRuleName", createRuleConfiguration(), new ShowShardingTableReferenceRulesStatement("foo", null),
                            Collections.singleton(new LocalDataQueryResultRow("foo", "t_order,t_order_item"))));
        }
        
        private ShardingRuleConfiguration createRuleConfiguration() {
            ShardingRuleConfiguration result = new ShardingRuleConfiguration();
            result.getTables().add(new ShardingTableRuleConfiguration("t_order", null));
            result.getTables().add(new ShardingTableRuleConfiguration("t_order_item", null));
            result.getTables().add(new ShardingTableRuleConfiguration("t_1", null));
            result.getTables().add(new ShardingTableRuleConfiguration("t_2", null));
            result.getBindingTableGroups().add(new ShardingTableReferenceRuleConfiguration("foo", "t_order,t_order_item"));
            return result;
        }
    }
}
