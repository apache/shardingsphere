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

package org.apache.shardingsphere.sqlfederation.distsql.handler.update;

import org.apache.shardingsphere.distsql.statement.DistSQLStatement;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.config.rule.scope.GlobalRuleConfiguration;
import org.apache.shardingsphere.sqlfederation.config.SQLFederationCacheOption;
import org.apache.shardingsphere.sqlfederation.config.SQLFederationRuleConfiguration;
import org.apache.shardingsphere.sqlfederation.distsql.segment.CacheOptionSegment;
import org.apache.shardingsphere.sqlfederation.distsql.statement.updatable.AlterSQLFederationRuleStatement;
import org.apache.shardingsphere.sqlfederation.rule.SQLFederationRule;
import org.apache.shardingsphere.sqlfederation.rule.builder.DefaultSQLFederationRuleConfigurationBuilder;
import org.apache.shardingsphere.test.it.distsql.handler.engine.update.GlobalRuleDefinitionExecutorTest;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.sql.SQLException;
import java.util.stream.Stream;

import static org.mockito.Mockito.mock;

class AlterSQLFederationRuleExecutorTest extends GlobalRuleDefinitionExecutorTest {
    
    AlterSQLFederationRuleExecutorTest() {
        super(mock(SQLFederationRule.class));
    }
    
    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    void assertExecuteUpdate(final String name, final GlobalRuleConfiguration ruleConfig,
                             final DistSQLStatement sqlStatement, final RuleConfiguration matchedRuleConfig, final Class<? extends Exception> expectedException) throws SQLException {
        assertExecuteUpdate(ruleConfig, sqlStatement, matchedRuleConfig, expectedException);
    }
    
    private static class TestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
            return Stream.of(
                    Arguments.arguments("normal",
                            new DefaultSQLFederationRuleConfigurationBuilder().build(),
                            new AlterSQLFederationRuleStatement(true, true, new CacheOptionSegment(64, 512L)),
                            new SQLFederationRuleConfiguration(true, true, new SQLFederationCacheOption(64, 512L)), null),
                    Arguments.arguments("withNotExistedDistributedTransactionType",
                            new DefaultSQLFederationRuleConfigurationBuilder().build(),
                            new AlterSQLFederationRuleStatement(null, null, null),
                            new SQLFederationRuleConfiguration(false, false, new SQLFederationCacheOption(2000, 65535L)), null),
                    Arguments.arguments("withNotExistedXATransactionProvider",
                            new DefaultSQLFederationRuleConfigurationBuilder().build(),
                            new AlterSQLFederationRuleStatement(null, null, new CacheOptionSegment(null, null)),
                            new SQLFederationRuleConfiguration(false, false, new SQLFederationCacheOption(2000, 65535L)), null));
        }
    }
}
