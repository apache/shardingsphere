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

package org.apache.shardingsphere.globalclock.distsql.handler.update;

import org.apache.shardingsphere.distsql.statement.DistSQLStatement;
import org.apache.shardingsphere.globalclock.config.GlobalClockRuleConfiguration;
import org.apache.shardingsphere.globalclock.distsql.statement.updatable.AlterGlobalClockRuleStatement;
import org.apache.shardingsphere.globalclock.rule.GlobalClockRule;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.config.rule.scope.GlobalRuleConfiguration;
import org.apache.shardingsphere.test.it.distsql.handler.engine.update.GlobalRuleDefinitionExecutorTest;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.support.ParameterDeclarations;

import java.sql.SQLException;
import java.util.Properties;
import java.util.stream.Stream;

import static org.mockito.Mockito.mock;

class AlterGlobalClockRuleExecutorTest extends GlobalRuleDefinitionExecutorTest {
    
    AlterGlobalClockRuleExecutorTest() {
        super(mock(GlobalClockRule.class));
    }
    
    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    void assertExecuteUpdate(final String name, final GlobalRuleConfiguration ruleConfig, final DistSQLStatement sqlStatement, final RuleConfiguration matchedRuleConfig) throws SQLException {
        assertExecuteUpdate(ruleConfig, sqlStatement, matchedRuleConfig, null);
    }
    
    private static final class TestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ParameterDeclarations parameters, final ExtensionContext context) {
            return Stream.of(Arguments.arguments("normal",
                    new GlobalClockRuleConfiguration("TSO", "local", false, new Properties()),
                    new AlterGlobalClockRuleStatement("TSO", "redis", true, new Properties()),
                    new GlobalClockRuleConfiguration("TSO", "redis", true, new Properties())));
        }
    }
}
