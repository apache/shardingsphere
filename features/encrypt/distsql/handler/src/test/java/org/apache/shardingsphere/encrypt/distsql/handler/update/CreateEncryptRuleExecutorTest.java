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

import org.apache.shardingsphere.distsql.handler.engine.update.DistSQLUpdateExecuteEngine;
import org.apache.shardingsphere.distsql.segment.AlgorithmSegment;
import org.apache.shardingsphere.encrypt.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.distsql.segment.EncryptColumnItemSegment;
import org.apache.shardingsphere.encrypt.distsql.segment.EncryptColumnSegment;
import org.apache.shardingsphere.encrypt.distsql.segment.EncryptRuleSegment;
import org.apache.shardingsphere.encrypt.distsql.statement.CreateEncryptRuleStatement;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.algorithm.core.exception.AlgorithmInitializationException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.InvalidRuleConfigurationException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.spi.exception.ServiceProviderNotFoundException;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.persist.service.MetaDataManagerPersistService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CreateEncryptRuleExecutorTest {
    
    @Test
    void assertExecuteUpdateWithDuplicateEncryptRule() {
        EncryptRule rule = mock(EncryptRule.class);
        when(rule.getAllTableNames()).thenReturn(Arrays.asList("t_user", "t_order"));
        assertThrows(DuplicateRuleException.class,
                () -> new DistSQLUpdateExecuteEngine(createSQLStatement(false, "MD5"), "foo_db", mockContextManager(rule), null).executeUpdate());
    }
    
    @Test
    void assertExecuteUpdateWithoutToBeCreatedEncryptors() {
        EncryptRule rule = mock(EncryptRule.class);
        assertThrows(ServiceProviderNotFoundException.class,
                () -> new DistSQLUpdateExecuteEngine(createSQLStatement(false, "INVALID_TYPE"), "foo_db", mockContextManager(rule), null).executeUpdate());
    }
    
    @Test
    void assertExecuteUpdateWithConflictedColumnNames() {
        EncryptRule rule = mock(EncryptRule.class);
        when(rule.getConfiguration()).thenReturn(getCurrentRuleConfiguration());
        assertThrows(InvalidRuleConfigurationException.class,
                () -> new DistSQLUpdateExecuteEngine(createConflictColumnNameSQLStatement(), "foo_db", mockContextManager(rule), null).executeUpdate());
    }
    
    @Test
    void assertExecuteUpdateAESEncryptRuleWithPropertiesNotExists() {
        CreateEncryptRuleStatement sqlStatement = createWrongAESEncryptorSQLStatement();
        EncryptRule rule = mock(EncryptRule.class);
        when(rule.getConfiguration()).thenReturn(getCurrentRuleConfiguration());
        assertThrows(AlgorithmInitializationException.class, () -> new DistSQLUpdateExecuteEngine(sqlStatement, "foo_db", mockContextManager(rule), null).executeUpdate());
    }
    
    private CreateEncryptRuleStatement createWrongAESEncryptorSQLStatement() {
        EncryptColumnSegment tUserColumnSegment = new EncryptColumnSegment("user_id",
                new EncryptColumnItemSegment("user_cipher", new AlgorithmSegment("AES", new Properties())),
                new EncryptColumnItemSegment("assisted_column", new AlgorithmSegment("AES", new Properties())),
                new EncryptColumnItemSegment("like_column", new AlgorithmSegment("CHAR_DIGEST_LIKE", new Properties())));
        EncryptColumnSegment tOrderColumnSegment = new EncryptColumnSegment("order_id",
                new EncryptColumnItemSegment("order_cipher", new AlgorithmSegment("AES", new Properties())),
                new EncryptColumnItemSegment("assisted_column", new AlgorithmSegment("AES", new Properties())),
                new EncryptColumnItemSegment("like_column", new AlgorithmSegment("CHAR_DIGEST_LIKE", new Properties())));
        EncryptRuleSegment userRuleSegment = new EncryptRuleSegment("t_user", Collections.singleton(tUserColumnSegment));
        EncryptRuleSegment orderRuleSegment = new EncryptRuleSegment("t_order", Collections.singleton(tOrderColumnSegment));
        return new CreateEncryptRuleStatement(true, Arrays.asList(userRuleSegment, orderRuleSegment));
    }
    
    @Test
    void assertExecuteUpdateWithIfNotExists() throws SQLException {
        EncryptRule rule = mock(EncryptRule.class);
        CreateEncryptRuleStatement sqlStatement = createAESEncryptRuleSQLStatement(true);
        ContextManager contextManager = mockContextManager(rule);
        new DistSQLUpdateExecuteEngine(sqlStatement, "foo_db", contextManager, null).executeUpdate();
        MetaDataManagerPersistService metaDataManagerPersistService = contextManager.getPersistServiceFacade().getModeFacade().getMetaDataManagerService();
        metaDataManagerPersistService.alterRuleConfiguration(any(), ArgumentMatchers.argThat(this::assertIfNotExistsRuleConfiguration));
    }
    
    private CreateEncryptRuleStatement createAESEncryptRuleSQLStatement(final boolean ifNotExists) {
        EncryptColumnSegment encryptColumnSegment = new EncryptColumnSegment("user_id",
                new EncryptColumnItemSegment("user_cipher", new AlgorithmSegment("AES", PropertiesBuilder.build(new Property("aes-key-value", "abc"), new Property("digest-algorithm-name", "SHA-1")))),
                new EncryptColumnItemSegment("assisted_column", null),
                new EncryptColumnItemSegment("like_column", null));
        Collection<EncryptRuleSegment> rules = new LinkedList<>();
        rules.add(new EncryptRuleSegment("t_user", Collections.singleton(encryptColumnSegment)));
        return new CreateEncryptRuleStatement(ifNotExists, rules);
    }
    
    private CreateEncryptRuleStatement createSQLStatement(final boolean ifNotExists, final String encryptorName) {
        EncryptColumnSegment tUserColumnSegment = new EncryptColumnSegment("user_id",
                new EncryptColumnItemSegment("user_cipher", new AlgorithmSegment(encryptorName, new Properties())),
                new EncryptColumnItemSegment("assisted_column", new AlgorithmSegment(encryptorName, new Properties())),
                new EncryptColumnItemSegment("like_column", new AlgorithmSegment(encryptorName, new Properties())));
        EncryptColumnSegment tOrderColumnSegment = new EncryptColumnSegment("order_id",
                new EncryptColumnItemSegment("order_cipher", new AlgorithmSegment(encryptorName, new Properties())),
                new EncryptColumnItemSegment("assisted_column", new AlgorithmSegment(encryptorName, new Properties())),
                new EncryptColumnItemSegment("like_column", new AlgorithmSegment(encryptorName, new Properties())));
        EncryptRuleSegment tUserRuleSegment = new EncryptRuleSegment("t_user", Collections.singleton(tUserColumnSegment));
        EncryptRuleSegment tOrderRuleSegment = new EncryptRuleSegment("t_order", Collections.singleton(tOrderColumnSegment));
        Collection<EncryptRuleSegment> rules = new LinkedList<>();
        rules.add(tUserRuleSegment);
        rules.add(tOrderRuleSegment);
        return new CreateEncryptRuleStatement(ifNotExists, rules);
    }
    
    private CreateEncryptRuleStatement createConflictColumnNameSQLStatement() {
        EncryptColumnSegment columnSegment = new EncryptColumnSegment("user_id",
                new EncryptColumnItemSegment("user_cipher", new AlgorithmSegment("MD5", new Properties())),
                new EncryptColumnItemSegment("user_id", new AlgorithmSegment("test", new Properties())),
                new EncryptColumnItemSegment("like_column", new AlgorithmSegment("test", new Properties())));
        EncryptRuleSegment ruleSegment = new EncryptRuleSegment("t_encrypt", Collections.singleton(columnSegment));
        return new CreateEncryptRuleStatement(false, Collections.singleton(ruleSegment));
    }
    
    private EncryptRuleConfiguration getCurrentRuleConfiguration() {
        Collection<EncryptTableRuleConfiguration> rules = new LinkedList<>();
        rules.add(new EncryptTableRuleConfiguration("t_user", Collections.emptyList()));
        rules.add(new EncryptTableRuleConfiguration("t_order", Collections.emptyList()));
        return new EncryptRuleConfiguration(rules, Collections.emptyMap());
    }
    
    private boolean assertIfNotExistsRuleConfiguration(final EncryptRuleConfiguration actual) {
        assertFalse(actual.getTables().isEmpty());
        assertFalse(actual.getEncryptors().isEmpty());
        return true;
    }
    
    private ContextManager mockContextManager(final EncryptRule rule) {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("foo_db");
        when(database.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(rule)));
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(result.getDatabase("foo_db")).thenReturn(database);
        return result;
    }
}
