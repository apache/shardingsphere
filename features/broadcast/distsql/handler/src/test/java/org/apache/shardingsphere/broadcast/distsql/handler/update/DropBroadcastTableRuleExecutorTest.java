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

import org.apache.shardingsphere.broadcast.distsql.statement.DropBroadcastTableRuleStatement;
import org.apache.shardingsphere.broadcast.rule.BroadcastRule;
import org.apache.shardingsphere.distsql.handler.engine.update.DistSQLUpdateExecuteEngine;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.persist.service.MetaDataManagerPersistService;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DropBroadcastTableRuleExecutorTest {
    
    @Test
    void assertExecuteUpdateWithoutToBeDroppedTables() {
        DropBroadcastTableRuleStatement sqlStatement = new DropBroadcastTableRuleStatement(false, Collections.singleton("foo_tbl"));
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        BroadcastRule rule = mock(BroadcastRule.class, RETURNS_DEEP_STUBS);
        assertThrows(MissingRequiredRuleException.class, () -> new DistSQLUpdateExecuteEngine(sqlStatement, "foo_db", mockContextManager(database, rule), null).executeUpdate());
    }
    
    @Test
    void assertExecuteUpdate() throws SQLException {
        DropBroadcastTableRuleStatement sqlStatement = new DropBroadcastTableRuleStatement(true, new ArrayList<>(Arrays.asList("foo_tbl", "bar_tbl")));
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("foo_db");
        BroadcastRule rule = mock(BroadcastRule.class, RETURNS_DEEP_STUBS);
        when(rule.getConfiguration().getTables()).thenReturn(Collections.singleton("foo_tbl"));
        ContextManager contextManager = mockContextManager(database, rule);
        new DistSQLUpdateExecuteEngine(sqlStatement, "foo_db", contextManager, null).executeUpdate();
        MetaDataManagerPersistService metaDataManagerPersistService = contextManager.getPersistServiceFacade().getModeFacade().getMetaDataManagerService();
        verify(metaDataManagerPersistService).removeRuleConfiguration(eq(database), any(RuleConfiguration.class), eq("broadcast"));
    }
    
    private ContextManager mockContextManager(final ShardingSphereDatabase database, final BroadcastRule rule) {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("foo_db");
        when(database.getRuleMetaData()).thenReturn(new RuleMetaData(null == rule ? Collections.emptyList() : Collections.singleton(rule)));
        when(result.getDatabase("foo_db")).thenReturn(database);
        return result;
    }
}
