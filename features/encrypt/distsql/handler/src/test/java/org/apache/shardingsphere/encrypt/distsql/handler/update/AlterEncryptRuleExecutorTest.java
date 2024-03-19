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

import org.apache.shardingsphere.distsql.handler.exception.rule.InvalidRuleConfigurationException;
import org.apache.shardingsphere.distsql.handler.exception.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.distsql.segment.AlgorithmSegment;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnItemRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.distsql.segment.EncryptColumnItemSegment;
import org.apache.shardingsphere.encrypt.distsql.segment.EncryptColumnSegment;
import org.apache.shardingsphere.encrypt.distsql.segment.EncryptRuleSegment;
import org.apache.shardingsphere.encrypt.distsql.statement.AlterEncryptRuleStatement;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.spi.exception.ServiceProviderNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AlterEncryptRuleExecutorTest {
    
    private final AlterEncryptRuleExecutor executor = new AlterEncryptRuleExecutor();
    
    @BeforeEach
    void setUp() {
        executor.setDatabase(mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS));
    }
    
    @Test
    void assertCheckSQLStatementWithoutToBeAlteredRules() {
        EncryptRule rule = mock(EncryptRule.class);
        when(rule.getConfiguration()).thenReturn(new EncryptRuleConfiguration(Collections.emptyList(), Collections.emptyMap()));
        executor.setRule(rule);
        assertThrows(MissingRequiredRuleException.class,
                () -> executor.checkBeforeUpdate(createSQLStatement("MD5")));
    }
    
    @Test
    void assertCheckSQLStatementWithoutToBeAlteredEncryptors() {
        EncryptRule rule = mock(EncryptRule.class);
        when(rule.getConfiguration()).thenReturn(createCurrentRuleConfiguration());
        executor.setRule(rule);
        assertThrows(ServiceProviderNotFoundException.class, () -> executor.checkBeforeUpdate(createSQLStatement("INVALID_TYPE")));
    }
    
    @Test
    void assertCheckSQLStatementWithConflictColumnNames() {
        EncryptRule rule = mock(EncryptRule.class);
        when(rule.getConfiguration()).thenReturn(createCurrentRuleConfiguration());
        executor.setRule(rule);
        assertThrows(InvalidRuleConfigurationException.class, () -> executor.checkBeforeUpdate(createConflictColumnNameSQLStatement()));
    }
    
    @Test
    void assertUpdateCurrentRuleConfigurationWithInUsedEncryptor() {
        EncryptRule rule = mock(EncryptRule.class);
        when(rule.getConfiguration()).thenReturn(createCurrentRuleConfiguration());
        executor.setRule(rule);
        EncryptRuleConfiguration toBeAlteredRuleConfig = executor.buildToBeAlteredRuleConfiguration(createSQLStatement("MD5"));
        assertThat(toBeAlteredRuleConfig.getEncryptors().size(), is(3));
    }
    
    private AlterEncryptRuleStatement createSQLStatement(final String encryptorName) {
        EncryptColumnSegment columnSegment = new EncryptColumnSegment("user_id",
                new EncryptColumnItemSegment("user_cipher", new AlgorithmSegment(encryptorName, new Properties())),
                new EncryptColumnItemSegment("assisted_column", new AlgorithmSegment("test", new Properties())),
                new EncryptColumnItemSegment("like_column", new AlgorithmSegment("test", new Properties())));
        EncryptRuleSegment ruleSegment = new EncryptRuleSegment("t_encrypt", Collections.singleton(columnSegment));
        return new AlterEncryptRuleStatement(Collections.singleton(ruleSegment));
    }
    
    private AlterEncryptRuleStatement createConflictColumnNameSQLStatement() {
        EncryptColumnSegment columnSegment = new EncryptColumnSegment("user_id",
                new EncryptColumnItemSegment("user_cipher", new AlgorithmSegment("MD5", new Properties())),
                new EncryptColumnItemSegment("user_id", new AlgorithmSegment("test", new Properties())),
                new EncryptColumnItemSegment("like_column", new AlgorithmSegment("test", new Properties())));
        EncryptRuleSegment ruleSegment = new EncryptRuleSegment("t_encrypt", Collections.singleton(columnSegment));
        return new AlterEncryptRuleStatement(Collections.singleton(ruleSegment));
    }
    
    private EncryptRuleConfiguration createCurrentRuleConfiguration() {
        EncryptTableRuleConfiguration tableRuleConfig = new EncryptTableRuleConfiguration("t_encrypt", Collections.emptyList());
        return new EncryptRuleConfiguration(new LinkedList<>(Collections.singleton(tableRuleConfig)), Collections.emptyMap());
    }
    
    private EncryptRuleConfiguration createCurrentRuleConfigurationWithMultipleTableRules() {
        EncryptColumnRuleConfiguration columnRuleConfig = new EncryptColumnRuleConfiguration("user_id", new EncryptColumnItemRuleConfiguration("user_id", "t_encrypt_user_id_MD5"));
        EncryptTableRuleConfiguration tableRuleConfig = new EncryptTableRuleConfiguration("t_encrypt", Collections.singleton(columnRuleConfig));
        Map<String, AlgorithmConfiguration> encryptors = Collections.singletonMap("t_encrypt_user_id_MD5", new AlgorithmConfiguration("TEST", new Properties()));
        return new EncryptRuleConfiguration(new LinkedList<>(Arrays.asList(tableRuleConfig,
                new EncryptTableRuleConfiguration("t_encrypt_another", Collections.singleton(columnRuleConfig)))), encryptors);
    }
    
    private EncryptRuleConfiguration createToBeAlteredRuleConfiguration() {
        EncryptTableRuleConfiguration tableRuleConfig = new EncryptTableRuleConfiguration("t_encrypt", Collections.emptyList());
        return new EncryptRuleConfiguration(new LinkedList<>(Collections.singleton(tableRuleConfig)), Collections.emptyMap());
    }
}
