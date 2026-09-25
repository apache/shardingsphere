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

package org.apache.shardingsphere.proxy.backend.connector.jdbc.transaction;

import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DeferredMetaDataRefreshContextTest {
    
    private final ContextManager contextManager = mock(ContextManager.class, Answers.RETURNS_DEEP_STUBS);
    
    private final ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
    
    @Test
    void assertReconcileEachDeferredTable() {
        when(contextManager.getMetaDataContexts().getMetaData().getDatabase("foo_db")).thenReturn(database);
        IdentifierValue originalTable = new IdentifierValue("t_order");
        IdentifierValue renamedTable = new IdentifierValue("t_order_new");
        DeferredMetaDataRefreshContext deferredContext = new DeferredMetaDataRefreshContext();
        deferredContext.add("foo_db", "foo_schema", "ds_0", Arrays.asList(originalTable, renamedTable));
        deferredContext.reconcile(contextManager);
        verify(contextManager).reconcileTable(database, "foo_schema", "ds_0", originalTable);
        verify(contextManager).reconcileTable(database, "foo_schema", "ds_0", renamedTable);
    }
    
    @Test
    void assertReconcileKeepsQuotedIdentifier() {
        when(contextManager.getMetaDataContexts().getMetaData().getDatabase("foo_db")).thenReturn(database);
        IdentifierValue quotedTable = new IdentifierValue("\"MixedCase\"");
        DeferredMetaDataRefreshContext deferredContext = new DeferredMetaDataRefreshContext();
        deferredContext.add("foo_db", "foo_schema", "ds_0", Collections.singletonList(quotedTable));
        deferredContext.reconcile(contextManager);
        verify(contextManager).reconcileTable(database, "foo_schema", "ds_0", quotedTable);
    }
    
    @Test
    void assertReconcileDeduplicatesRepeatedTable() {
        when(contextManager.getMetaDataContexts().getMetaData().getDatabase("foo_db")).thenReturn(database);
        DeferredMetaDataRefreshContext deferredContext = new DeferredMetaDataRefreshContext();
        deferredContext.add("foo_db", "foo_schema", "ds_0", Collections.singletonList(new IdentifierValue("t_order")));
        deferredContext.add("foo_db", "foo_schema", "ds_0", Collections.singletonList(new IdentifierValue("t_order")));
        deferredContext.reconcile(contextManager);
        verify(contextManager).reconcileTable(database, "foo_schema", "ds_0", new IdentifierValue("t_order"));
    }
    
    @Test
    void assertReconcileAfterClear() {
        DeferredMetaDataRefreshContext deferredContext = new DeferredMetaDataRefreshContext();
        deferredContext.add("foo_db", "foo_schema", "ds_0", Collections.singletonList(new IdentifierValue("t_order")));
        deferredContext.clear();
        deferredContext.reconcile(contextManager);
        verify(contextManager, never()).reconcileTable(any(ShardingSphereDatabase.class), anyString(), anyString(), any(IdentifierValue.class));
    }
}
