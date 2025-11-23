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

package org.apache.shardingsphere.single.distsql.handler.update;

import org.apache.shardingsphere.distsql.handler.engine.update.DistSQLUpdateExecuteEngine;
import org.apache.shardingsphere.infra.exception.kernel.metadata.resource.storageunit.MissingRequiredStorageUnitsException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.rule.attribute.datasource.DataSourceMapperRuleAttribute;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.persist.service.MetaDataManagerPersistService;
import org.apache.shardingsphere.single.config.SingleRuleConfiguration;
import org.apache.shardingsphere.single.distsql.statement.rdl.SetDefaultSingleTableStorageUnitStatement;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SetDefaultSingleTableStorageUnitExecutorTest {
    
    @Test
    void assertExecuteUpdateWithNotExistedDefaultStorageUnit() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getResourceMetaData().getStorageUnits().keySet()).thenReturn(Collections.emptySet());
        when(database.getRuleMetaData().getAttributes(DataSourceMapperRuleAttribute.class)).thenReturn(Collections.emptyList());
        SingleRule rule = mock(SingleRule.class, RETURNS_DEEP_STUBS);
        when(rule.getAttributes().findAttribute(DataSourceMapperRuleAttribute.class)).thenReturn(Optional.empty());
        DistSQLUpdateExecuteEngine engine = new DistSQLUpdateExecuteEngine(new SetDefaultSingleTableStorageUnitStatement("foo_ds"), "foo_db", mockContextManager(database, rule, "foo_ds"));
        assertThrows(MissingRequiredStorageUnitsException.class, engine::executeUpdate);
    }
    
    @Test
    void assertExecuteUpdateWithoutDefaultStorageUnit() throws SQLException {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        SingleRule rule = mock(SingleRule.class);
        when(rule.getConfiguration()).thenReturn(new SingleRuleConfiguration(Collections.emptyList(), "foo_ds"));
        ContextManager contextManager = mockContextManager(database, rule, null);
        new DistSQLUpdateExecuteEngine(new SetDefaultSingleTableStorageUnitStatement(null), "foo_db", contextManager).executeUpdate();
        MetaDataManagerPersistService metaDataManagerPersistService = contextManager.getPersistServiceFacade().getModeFacade().getMetaDataManagerService();
        verify(metaDataManagerPersistService).removeRuleConfigurationItem(any(), ArgumentMatchers.<SingleRuleConfiguration>argThat(x -> x.getDefaultDataSource().equals(Optional.of("foo_ds"))));
        verify(metaDataManagerPersistService).alterRuleConfiguration(any(), ArgumentMatchers.<SingleRuleConfiguration>argThat(x -> !x.getDefaultDataSource().isPresent()));
    }
    
    @Test
    void assertExecuteUpdateWithDefaultStorageUnit() throws SQLException {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("foo_db");
        when(database.getResourceMetaData().getStorageUnits().keySet()).thenReturn(new HashSet<>(Arrays.asList("foo_ds", "bar_ds")));
        when(database.getRuleMetaData().getAttributes(DataSourceMapperRuleAttribute.class)).thenReturn(Collections.emptyList());
        SingleRule rule = mock(SingleRule.class, RETURNS_DEEP_STUBS);
        when(rule.getConfiguration()).thenReturn(new SingleRuleConfiguration(Collections.emptyList(), "foo_ds"));
        when(rule.getAttributes().findAttribute(DataSourceMapperRuleAttribute.class)).thenReturn(Optional.empty());
        ContextManager contextManager = mockContextManager(database, rule, "bar_ds");
        new DistSQLUpdateExecuteEngine(new SetDefaultSingleTableStorageUnitStatement("bar_ds"), "foo_db", contextManager).executeUpdate();
        MetaDataManagerPersistService metaDataManagerPersistService = contextManager.getPersistServiceFacade().getModeFacade().getMetaDataManagerService();
        verify(metaDataManagerPersistService).alterRuleConfiguration(any(), ArgumentMatchers.<SingleRuleConfiguration>argThat(x -> x.getDefaultDataSource().equals(Optional.of("bar_ds"))));
    }
    
    private ContextManager mockContextManager(final ShardingSphereDatabase database, final SingleRule rule, final String defaultDataSource) {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("foo_db");
        when(database.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(rule)));
        when(database.decorateRuleConfiguration(any())).thenReturn(new SingleRuleConfiguration(Collections.emptyList(), defaultDataSource));
        when(result.getDatabase("foo_db")).thenReturn(database);
        return result;
    }
}
