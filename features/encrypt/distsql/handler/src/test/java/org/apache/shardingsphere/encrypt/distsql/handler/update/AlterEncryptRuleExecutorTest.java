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
import org.apache.shardingsphere.encrypt.distsql.statement.AlterEncryptRuleStatement;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.InvalidRuleConfigurationException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.persist.service.MetaDataManagerPersistService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AlterEncryptRuleExecutorTest {
    
    @Test
    void assertExecuteUpdateWithoutToBeAlteredRules() {
        EncryptRule rule = mock(EncryptRule.class);
        when(rule.getConfiguration()).thenReturn(new EncryptRuleConfiguration(Collections.emptyList(), Collections.emptyMap()));
        assertThrows(MissingRequiredRuleException.class,
                () -> new DistSQLUpdateExecuteEngine(createSQLStatementWithAssistQueryAndLikeColumns(), "foo_db", mockContextManager(rule), null).executeUpdate());
    }
    
    @Test
    void assertExecuteUpdateWithConflictAssistQueryColumnNames() {
        EncryptRule rule = mock(EncryptRule.class);
        when(rule.getAllTableNames()).thenReturn(Collections.singleton("t_encrypt"));
        assertThrows(InvalidRuleConfigurationException.class,
                () -> new DistSQLUpdateExecuteEngine(createColumnNameConflictedSQLStatement("user_id", "like_column"), "foo_db", mockContextManager(rule), null).executeUpdate());
    }
    
    @Test
    void assertExecuteUpdateWithConflictLikeColumnNames() {
        EncryptRule rule = mock(EncryptRule.class);
        when(rule.getAllTableNames()).thenReturn(Collections.singleton("t_encrypt"));
        assertThrows(InvalidRuleConfigurationException.class,
                () -> new DistSQLUpdateExecuteEngine(createColumnNameConflictedSQLStatement("assisted_column", "user_id"), "foo_db", mockContextManager(rule), null).executeUpdate());
    }
    
    private AlterEncryptRuleStatement createColumnNameConflictedSQLStatement(final String assistQueryColumnName, final String likeColumnName) {
        EncryptColumnSegment columnSegment = new EncryptColumnSegment("user_id",
                new EncryptColumnItemSegment("user_cipher", new AlgorithmSegment("MD5", new Properties())),
                new EncryptColumnItemSegment(assistQueryColumnName, new AlgorithmSegment("MD5", new Properties())),
                new EncryptColumnItemSegment(likeColumnName, new AlgorithmSegment("MD5", new Properties())));
        return new AlterEncryptRuleStatement(Collections.singleton(new EncryptRuleSegment("t_encrypt", Collections.singleton(columnSegment))));
    }
    
    @Test
    void assertExecuteUpdateWithAssistQueryAndLikeColumns() throws SQLException {
        EncryptRule rule = mock(EncryptRule.class);
        when(rule.getAllTableNames()).thenReturn(Collections.singleton("t_encrypt"));
        when(rule.getConfiguration()).thenReturn(createCurrentRuleConfiguration());
        ContextManager contextManager = mockContextManager(rule);
        MetaDataManagerPersistService metaDataManagerPersistService = contextManager.getPersistServiceFacade().getModeFacade().getMetaDataManagerService();
        new DistSQLUpdateExecuteEngine(createSQLStatementWithAssistQueryAndLikeColumns(), "foo_db", contextManager, null).executeUpdate();
        metaDataManagerPersistService.removeRuleConfigurationItem(any(), ArgumentMatchers.argThat(this::assertToBeDroppedRuleConfiguration));
        metaDataManagerPersistService.alterRuleConfiguration(any(), ArgumentMatchers.argThat(this::assertToBeAlteredRuleConfiguration));
    }
    
    @Test
    void assertExecuteUpdateWithoutAssistQueryAndLikeColumns() throws SQLException {
        EncryptRule rule = mock(EncryptRule.class);
        when(rule.getAllTableNames()).thenReturn(Collections.singleton("t_encrypt"));
        when(rule.getConfiguration()).thenReturn(createCurrentRuleConfiguration());
        ContextManager contextManager = mockContextManager(rule);
        new DistSQLUpdateExecuteEngine(createSQLStatementWithoutAssistQueryAndLikeColumns(), "foo_db", contextManager, null).executeUpdate();
        MetaDataManagerPersistService metaDataManagerPersistService = contextManager.getPersistServiceFacade().getModeFacade().getMetaDataManagerService();
        metaDataManagerPersistService.removeRuleConfigurationItem(any(), ArgumentMatchers.argThat(this::assertToBeDroppedRuleConfiguration));
        metaDataManagerPersistService.alterRuleConfiguration(any(), ArgumentMatchers.argThat(this::assertToBeAlteredRuleConfiguration));
    }
    
    private ContextManager mockContextManager(final EncryptRule rule) {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(result.getDatabase("foo_db")).thenReturn(new ShardingSphereDatabase("foo_db", mock(), mock(), new RuleMetaData(Collections.singleton(rule)), Collections.emptyList()));
        return result;
    }
    
    private AlterEncryptRuleStatement createSQLStatementWithAssistQueryAndLikeColumns() {
        EncryptColumnSegment columnSegment = new EncryptColumnSegment("user_id",
                new EncryptColumnItemSegment("user_cipher", new AlgorithmSegment("MD5", new Properties())),
                new EncryptColumnItemSegment("assisted_column", new AlgorithmSegment("MD5", new Properties())),
                new EncryptColumnItemSegment("like_column", new AlgorithmSegment("MD5", new Properties())));
        EncryptRuleSegment ruleSegment = new EncryptRuleSegment("t_encrypt", Collections.singleton(columnSegment));
        return new AlterEncryptRuleStatement(Collections.singleton(ruleSegment));
    }
    
    private AlterEncryptRuleStatement createSQLStatementWithoutAssistQueryAndLikeColumns() {
        EncryptColumnSegment columnSegment = new EncryptColumnSegment("user_id", new EncryptColumnItemSegment("user_cipher", new AlgorithmSegment("MD5", new Properties())), null, null);
        EncryptRuleSegment ruleSegment = new EncryptRuleSegment("t_encrypt", Collections.singleton(columnSegment));
        return new AlterEncryptRuleStatement(Collections.singleton(ruleSegment));
    }
    
    private EncryptRuleConfiguration createCurrentRuleConfiguration() {
        EncryptTableRuleConfiguration tableRuleConfig = new EncryptTableRuleConfiguration("t_encrypt", Collections.emptyList());
        return new EncryptRuleConfiguration(new LinkedList<>(Collections.singleton(tableRuleConfig)), Collections.emptyMap());
    }
    
    private boolean assertToBeDroppedRuleConfiguration(final EncryptRuleConfiguration actual) {
        assertTrue(actual.getTables().isEmpty());
        assertTrue(actual.getEncryptors().isEmpty());
        return true;
    }
    
    private boolean assertToBeAlteredRuleConfiguration(final EncryptRuleConfiguration actual) {
        assertThat(actual.getTables().size(), is(1));
        assertThat(actual.getTables().iterator().next().getName(), is("t_encrypt"));
        assertThat(actual.getTables().iterator().next().getColumns().iterator().next().getName(), is("user_id"));
        assertThat(actual.getEncryptors().size(), is(3));
        return true;
    }
}
