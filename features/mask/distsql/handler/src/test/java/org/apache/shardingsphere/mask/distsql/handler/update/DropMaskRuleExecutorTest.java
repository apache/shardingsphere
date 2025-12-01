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

package org.apache.shardingsphere.mask.distsql.handler.update;

import org.apache.shardingsphere.distsql.handler.engine.update.DistSQLUpdateExecuteEngine;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.mask.config.MaskRuleConfiguration;
import org.apache.shardingsphere.mask.config.rule.MaskColumnRuleConfiguration;
import org.apache.shardingsphere.mask.config.rule.MaskTableRuleConfiguration;
import org.apache.shardingsphere.mask.distsql.statement.DropMaskRuleStatement;
import org.apache.shardingsphere.mask.rule.MaskRule;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.persist.service.MetaDataManagerPersistService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DropMaskRuleExecutorTest {
    
    @Test
    void assertExecuteUpdateWithoutToBeDroppedRule() {
        MaskRule rule = mock(MaskRule.class);
        when(rule.getConfiguration()).thenReturn(new MaskRuleConfiguration(Collections.emptyList(), Collections.emptyMap()));
        assertThrows(MissingRequiredRuleException.class, () -> new DistSQLUpdateExecuteEngine(createSQLStatement(false, "t_mask"), "foo_db", mockContextManager(rule)).executeUpdate());
    }
    
    @Test
    void assertExecuteUpdateWithoutIfExists() throws SQLException {
        MaskRule rule = mock(MaskRule.class);
        MaskRuleConfiguration ruleConfig = createCurrentRuleConfiguration();
        when(rule.getConfiguration()).thenReturn(ruleConfig);
        ContextManager contextManager = mockContextManager(rule);
        new DistSQLUpdateExecuteEngine(createSQLStatement(false, "T_MASK"), "foo_db", contextManager).executeUpdate();
        MetaDataManagerPersistService metaDataManagerPersistService = contextManager.getPersistServiceFacade().getModeFacade().getMetaDataManagerService();
        metaDataManagerPersistService.alterRuleConfiguration(any(), ArgumentMatchers.argThat(this::assertRuleConfigurationWithoutIfExists));
    }
    
    private boolean assertRuleConfigurationWithoutIfExists(final MaskRuleConfiguration actual) {
        assertThat(actual.getTables().size(), is(1));
        assertTrue(actual.getMaskAlgorithms().isEmpty());
        return true;
    }
    
    @Test
    void assertExecuteUpdateWithIfExists() throws SQLException {
        MaskRule rule = mock(MaskRule.class);
        when(rule.getConfiguration()).thenReturn(new MaskRuleConfiguration(Collections.emptyList(), Collections.emptyMap()));
        MaskRuleConfiguration ruleConfig = createCurrentRuleConfiguration();
        when(rule.getConfiguration()).thenReturn(ruleConfig);
        ContextManager contextManager = mockContextManager(rule);
        new DistSQLUpdateExecuteEngine(createSQLStatement(true, "T_USER"), "foo_db", contextManager).executeUpdate();
        MetaDataManagerPersistService metaDataManagerPersistService = contextManager.getPersistServiceFacade().getModeFacade().getMetaDataManagerService();
        metaDataManagerPersistService.alterRuleConfiguration(any(), ArgumentMatchers.argThat(this::assertRuleConfigurationWithoutIfExists));
        metaDataManagerPersistService.alterRuleConfiguration(any(), ArgumentMatchers.argThat(this::assertRuleConfigurationWithIfExists));
    }
    
    private DropMaskRuleStatement createSQLStatement(final boolean ifExists, final String tableName) {
        return new DropMaskRuleStatement(ifExists, Collections.singleton(tableName));
    }
    
    private MaskRuleConfiguration createCurrentRuleConfiguration() {
        MaskColumnRuleConfiguration columnRuleConfig = new MaskColumnRuleConfiguration("user_id", "MD5");
        MaskTableRuleConfiguration tableRuleConfig = new MaskTableRuleConfiguration("t_mask", Collections.singleton(columnRuleConfig));
        return new MaskRuleConfiguration(new LinkedList<>(Collections.singleton(tableRuleConfig)), Collections.emptyMap());
    }
    
    private boolean assertRuleConfigurationWithIfExists(final MaskRuleConfiguration actual) {
        assertThat(actual.getTables().size(), is(1));
        return true;
    }
    
    private ContextManager mockContextManager(final MaskRule rule) {
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db", mock(), mock(), new RuleMetaData(Collections.singleton(rule)), Collections.emptyList());
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(result.getDatabase("foo_db")).thenReturn(database);
        return result;
    }
}
