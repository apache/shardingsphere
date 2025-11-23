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

package org.apache.shardingsphere.broadcast.distsql.handler.update;

import org.apache.shardingsphere.broadcast.config.BroadcastRuleConfiguration;
import org.apache.shardingsphere.broadcast.distsql.statement.CreateBroadcastTableRuleStatement;
import org.apache.shardingsphere.broadcast.rule.BroadcastRule;
import org.apache.shardingsphere.distsql.handler.engine.update.DistSQLUpdateExecuteEngine;
import org.apache.shardingsphere.infra.exception.kernel.metadata.resource.storageunit.EmptyStorageUnitException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.persist.service.MetaDataManagerPersistService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreateBroadcastTableRuleExecutorTest {
    
    @Test
    void assertExecuteUpdateWithEmptyStorageUnits() {
        CreateBroadcastTableRuleStatement sqlStatement = new CreateBroadcastTableRuleStatement(false, Collections.singleton("t_address"));
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getResourceMetaData().getStorageUnits()).thenReturn(Collections.emptyMap());
        BroadcastRule rule = mock(BroadcastRule.class);
        assertThrows(EmptyStorageUnitException.class, () -> new DistSQLUpdateExecuteEngine(sqlStatement, "foo_db", mockContextManager(database, rule)).executeUpdate());
    }
    
    @Test
    void assertExecuteUpdateWithDuplicateBroadcastTables() {
        CreateBroadcastTableRuleStatement sqlStatement = new CreateBroadcastTableRuleStatement(false, Collections.singleton("foo_tbl"));
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        BroadcastRule rule = mock(BroadcastRule.class);
        when(rule.getTables()).thenReturn(Collections.singleton("foo_tbl"));
        assertThrows(DuplicateRuleException.class, () -> new DistSQLUpdateExecuteEngine(sqlStatement, "foo_db", mockContextManager(database, rule)).executeUpdate());
    }
    
    @Test
    void assertExecuteUpdateWithIfNotExists() throws SQLException {
        CreateBroadcastTableRuleStatement sqlStatement = new CreateBroadcastTableRuleStatement(true, new ArrayList<>(Arrays.asList("foo_tbl", "bar_tbl")));
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        BroadcastRule rule = mock(BroadcastRule.class);
        when(rule.getTables()).thenReturn(Collections.singleton("foo_tbl"));
        ContextManager contextManager = mockContextManager(database, rule);
        new DistSQLUpdateExecuteEngine(sqlStatement, "foo_db", contextManager).executeUpdate();
        MetaDataManagerPersistService metaDataManagerPersistService = contextManager.getPersistServiceFacade().getModeFacade().getMetaDataManagerService();
        verify(metaDataManagerPersistService).alterRuleConfiguration(any(),
                ArgumentMatchers.<BroadcastRuleConfiguration>argThat(x -> x.getTables().equals(new HashSet<>(Arrays.asList("foo_tbl", "bar_tbl")))));
    }
    
    @Test
    void assertExecuteUpdateWithoutIfNotExists() throws SQLException {
        CreateBroadcastTableRuleStatement sqlStatement = new CreateBroadcastTableRuleStatement(false, Collections.singleton("bar_tbl"));
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        BroadcastRule rule = mock(BroadcastRule.class);
        when(rule.getTables()).thenReturn(Collections.singleton("foo_tbl"));
        ContextManager contextManager = mockContextManager(database, rule);
        new DistSQLUpdateExecuteEngine(sqlStatement, "foo_db", contextManager).executeUpdate();
        MetaDataManagerPersistService metaDataManagerPersistService = contextManager.getPersistServiceFacade().getModeFacade().getMetaDataManagerService();
        verify(metaDataManagerPersistService).alterRuleConfiguration(any(),
                ArgumentMatchers.<BroadcastRuleConfiguration>argThat(x -> x.getTables().equals(new HashSet<>(Arrays.asList("foo_tbl", "bar_tbl")))));
    }
    
    @Test
    void assertExecuteUpdateWithoutExistedRule() throws SQLException {
        CreateBroadcastTableRuleStatement sqlStatement = new CreateBroadcastTableRuleStatement(false, new ArrayList<>(Arrays.asList("foo_tbl", "bar_tbl")));
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        ContextManager contextManager = mockContextManager(database, null);
        new DistSQLUpdateExecuteEngine(sqlStatement, "foo_db", contextManager).executeUpdate();
        MetaDataManagerPersistService metaDataManagerPersistService = contextManager.getPersistServiceFacade().getModeFacade().getMetaDataManagerService();
        verify(metaDataManagerPersistService).alterRuleConfiguration(any(),
                ArgumentMatchers.<BroadcastRuleConfiguration>argThat(x -> x.getTables().equals(new HashSet<>(Arrays.asList("foo_tbl", "bar_tbl")))));
    }
    
    private ContextManager mockContextManager(final ShardingSphereDatabase database, final BroadcastRule rule) {
        when(database.getName()).thenReturn("foo_db");
        when(database.getRuleMetaData()).thenReturn(new RuleMetaData(null == rule ? Collections.emptyList() : Collections.singleton(rule)));
        when(database.decorateRuleConfiguration(any())).thenReturn(new BroadcastRuleConfiguration(new HashSet<>(Arrays.asList("foo_tbl", "bar_tbl"))));
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(result.getDatabase("foo_db")).thenReturn(database);
        return result;
    }
}
