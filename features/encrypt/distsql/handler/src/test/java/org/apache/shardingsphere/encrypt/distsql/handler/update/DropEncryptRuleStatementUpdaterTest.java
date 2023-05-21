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

package org.apache.shardingsphere.encrypt.distsql.handler.update;

import org.apache.shardingsphere.distsql.handler.exception.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnItemRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.distsql.parser.statement.DropEncryptRuleStatement;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class DropEncryptRuleStatementUpdaterTest {
    
    private final DropEncryptRuleStatementUpdater updater = new DropEncryptRuleStatementUpdater();
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Test
    void assertCheckSQLStatementWithoutCurrentRule() {
        assertThrows(MissingRequiredRuleException.class, () -> updater.checkSQLStatement(database, createSQLStatement("t_encrypt"), null));
    }
    
    @Test
    void assertCheckSQLStatementWithoutToBeDroppedRule() {
        assertThrows(MissingRequiredRuleException.class,
                () -> updater.checkSQLStatement(database, createSQLStatement("t_encrypt"), new EncryptRuleConfiguration(Collections.emptyList(), Collections.emptyMap())));
    }
    
    @Test
    void assertUpdateCurrentRuleConfiguration() {
        EncryptRuleConfiguration ruleConfig = createCurrentRuleConfiguration();
        assertTrue(updater.updateCurrentRuleConfiguration(createSQLStatement("t_encrypt"), ruleConfig));
        assertTrue(ruleConfig.getEncryptors().isEmpty());
    }
    
    @Test
    void assertUpdateCurrentRuleConfigurationWithInUsedEncryptor() {
        EncryptRuleConfiguration ruleConfig = createCurrentRuleConfigurationWithMultipleTableRules();
        assertFalse(updater.updateCurrentRuleConfiguration(createSQLStatement("t_encrypt"), ruleConfig));
        assertThat(ruleConfig.getEncryptors().size(), is(1));
    }
    
    @Test
    void assertUpdateCurrentRuleConfigurationWithIfExists() {
        EncryptRuleConfiguration ruleConfig = createCurrentRuleConfiguration();
        DropEncryptRuleStatement statement = createSQLStatement(true, "t_encrypt_1");
        updater.checkSQLStatement(database, statement, mock(EncryptRuleConfiguration.class));
        assertFalse(updater.updateCurrentRuleConfiguration(statement, ruleConfig));
        assertThat(ruleConfig.getEncryptors().size(), is(3));
    }
    
    private DropEncryptRuleStatement createSQLStatement(final String tableName) {
        return new DropEncryptRuleStatement(false, Collections.singleton(tableName));
    }
    
    private DropEncryptRuleStatement createSQLStatement(final boolean ifExists, final String tableName) {
        return new DropEncryptRuleStatement(ifExists, Collections.singleton(tableName));
    }
    
    private EncryptRuleConfiguration createCurrentRuleConfiguration() {
        EncryptColumnRuleConfiguration columnRuleConfig = new EncryptColumnRuleConfiguration("user_id", new EncryptColumnItemRuleConfiguration("user_cipher", "t_encrypt_user_id_MD5"));
        columnRuleConfig.setAssistedQuery(new EncryptColumnItemRuleConfiguration("user_assisted", "t_encrypt_test_assisted"));
        columnRuleConfig.setLikeQuery(new EncryptColumnItemRuleConfiguration("user_like", "t_encrypt_test_like"));
        Map<String, AlgorithmConfiguration> encryptors = new HashMap<>(3, 1F);
        encryptors.put("t_encrypt_user_id_MD5", new AlgorithmConfiguration("TEST", new Properties()));
        encryptors.put("t_encrypt_test_assisted", new AlgorithmConfiguration("TEST", new Properties()));
        encryptors.put("t_encrypt_test_like", new AlgorithmConfiguration("TEST", new Properties()));
        EncryptTableRuleConfiguration tableRuleConfig = new EncryptTableRuleConfiguration("t_encrypt", Collections.singleton(columnRuleConfig));
        return new EncryptRuleConfiguration(new LinkedList<>(Collections.singleton(tableRuleConfig)), encryptors);
    }
    
    private EncryptRuleConfiguration createCurrentRuleConfigurationWithMultipleTableRules() {
        EncryptColumnRuleConfiguration columnRuleConfig = new EncryptColumnRuleConfiguration("user_id", new EncryptColumnItemRuleConfiguration("user_cipher", "t_encrypt_user_id_MD5"));
        EncryptTableRuleConfiguration tableRuleConfig = new EncryptTableRuleConfiguration("t_encrypt", Collections.singleton(columnRuleConfig));
        Map<String, AlgorithmConfiguration> encryptors = Collections.singletonMap("t_encrypt_user_id_MD5", new AlgorithmConfiguration("TEST", new Properties()));
        return new EncryptRuleConfiguration(new LinkedList<>(Arrays.asList(tableRuleConfig,
                new EncryptTableRuleConfiguration("t_encrypt_another", Collections.singleton(columnRuleConfig)))), encryptors);
    }
}
