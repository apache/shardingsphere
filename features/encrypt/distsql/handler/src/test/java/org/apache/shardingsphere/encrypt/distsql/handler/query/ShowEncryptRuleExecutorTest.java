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

package org.apache.shardingsphere.encrypt.distsql.handler.query;

import org.apache.shardingsphere.distsql.statement.DistSQLStatement;
import org.apache.shardingsphere.encrypt.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.config.rule.EncryptColumnItemRuleConfiguration;
import org.apache.shardingsphere.encrypt.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.distsql.statement.ShowEncryptRulesStatement;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.rule.scope.DatabaseRuleConfiguration;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.test.it.distsql.handler.engine.query.DistSQLDatabaseRuleQueryExecutorTest;
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

class ShowEncryptRuleExecutorTest extends DistSQLDatabaseRuleQueryExecutorTest {
    
    ShowEncryptRuleExecutorTest() {
        super(mock(EncryptRule.class));
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
            return Stream.of(Arguments.arguments("normal", createRuleConfiguration(), new ShowEncryptRulesStatement("T_ENCRYPT", null),
                    Collections.singleton(new LocalDataQueryResultRow("t_encrypt", "user_id", "user_cipher", "user_assisted", "user_like", "md5", "", "", "", "", ""))));
        }
        
        private EncryptRuleConfiguration createRuleConfiguration() {
            EncryptColumnRuleConfiguration encryptColumnRuleConfig = new EncryptColumnRuleConfiguration("user_id", new EncryptColumnItemRuleConfiguration("user_cipher", "test"));
            encryptColumnRuleConfig.setAssistedQuery(new EncryptColumnItemRuleConfiguration("user_assisted", "foo_assist_query_encryptor"));
            encryptColumnRuleConfig.setLikeQuery(new EncryptColumnItemRuleConfiguration("user_like", "foo_like_encryptor"));
            EncryptTableRuleConfiguration encryptTableRuleConfig = new EncryptTableRuleConfiguration("t_encrypt", Collections.singleton(encryptColumnRuleConfig));
            AlgorithmConfiguration shardingSphereAlgorithmConfig = new AlgorithmConfiguration("md5", new Properties());
            return new EncryptRuleConfiguration(Collections.singleton(encryptTableRuleConfig), Collections.singletonMap("test", shardingSphereAlgorithmConfig));
        }
    }
}
