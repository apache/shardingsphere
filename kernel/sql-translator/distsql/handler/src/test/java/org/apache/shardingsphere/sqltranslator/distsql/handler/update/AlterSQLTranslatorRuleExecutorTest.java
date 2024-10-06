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

package org.apache.shardingsphere.sqltranslator.distsql.handler.update;

import org.apache.shardingsphere.distsql.segment.AlgorithmSegment;
import org.apache.shardingsphere.distsql.statement.DistSQLStatement;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.config.rule.scope.GlobalRuleConfiguration;
import org.apache.shardingsphere.sqltranslator.config.SQLTranslatorRuleConfiguration;
import org.apache.shardingsphere.sqltranslator.distsql.statement.updateable.AlterSQLTranslatorRuleStatement;
import org.apache.shardingsphere.sqltranslator.rule.SQLTranslatorRule;
import org.apache.shardingsphere.test.it.distsql.handler.engine.update.GlobalRuleDefinitionExecutorTest;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.sql.SQLException;
import java.util.Properties;
import java.util.stream.Stream;

import static org.mockito.Mockito.mock;

class AlterSQLTranslatorRuleExecutorTest extends GlobalRuleDefinitionExecutorTest {
    
    AlterSQLTranslatorRuleExecutorTest() {
        super(mock(SQLTranslatorRule.class));
    }
    
    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    void assertExecuteUpdate(final String name, final GlobalRuleConfiguration ruleConfig, final DistSQLStatement sqlStatement, final RuleConfiguration matchedRuleConfig) throws SQLException {
        assertExecuteUpdate(ruleConfig, sqlStatement, matchedRuleConfig, null);
    }
    
    private static class TestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
            return Stream.of(Arguments.arguments("withTrueOriginalSQLWhenTranslatingFailed",
                    new SQLTranslatorRuleConfiguration("NATIVE", new Properties(), true),
                    new AlterSQLTranslatorRuleStatement(new AlgorithmSegment("NATIVE", PropertiesBuilder.build(new Property("foo", "bar"))), true),
                    new SQLTranslatorRuleConfiguration("NATIVE", PropertiesBuilder.build(new Property("foo", "bar")), true)),
                    Arguments.arguments("withNullOriginalSQLWhenTranslatingFailed",
                            new SQLTranslatorRuleConfiguration("NATIVE", new Properties(), true),
                            new AlterSQLTranslatorRuleStatement(new AlgorithmSegment("NATIVE", PropertiesBuilder.build(new Property("foo", "bar"))), null),
                            new SQLTranslatorRuleConfiguration("NATIVE", PropertiesBuilder.build(new Property("foo", "bar")), true)));
        }
    }
}
