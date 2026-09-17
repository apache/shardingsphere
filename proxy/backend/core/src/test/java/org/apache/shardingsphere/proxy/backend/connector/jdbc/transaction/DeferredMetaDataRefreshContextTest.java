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

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.refresher.pushdown.PushDownMetaDataRefreshEngine;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.MockedConstruction;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;

class DeferredMetaDataRefreshContextTest {
    
    private final ContextManager contextManager = mock(ContextManager.class, Answers.RETURNS_DEEP_STUBS);
    
    private final SQLStatementContext firstStatementContext = mock(SQLStatementContext.class);
    
    private final SQLStatementContext secondStatementContext = mock(SQLStatementContext.class);
    
    @Test
    void assertRefreshWithoutSavepoint() throws SQLException {
        DeferredMetaDataRefreshContext deferredContext = new DeferredMetaDataRefreshContext();
        deferredContext.add("foo_db", firstStatementContext, Collections.emptyList());
        deferredContext.add("foo_db", secondStatementContext, Collections.emptyList());
        assertThat(getRefreshedStatementContexts(deferredContext), is(Arrays.asList(firstStatementContext, secondStatementContext)));
    }
    
    @Test
    void assertRefreshAfterClear() throws SQLException {
        DeferredMetaDataRefreshContext deferredContext = new DeferredMetaDataRefreshContext();
        deferredContext.add("foo_db", firstStatementContext, Collections.emptyList());
        deferredContext.clear();
        assertTrue(getRefreshedStatementContexts(deferredContext).isEmpty());
    }
    
    @Test
    void assertRollbackToSavepoint() throws SQLException {
        DeferredMetaDataRefreshContext deferredContext = new DeferredMetaDataRefreshContext();
        deferredContext.add("foo_db", firstStatementContext, Collections.emptyList());
        deferredContext.markSavepoint("foo_savepoint");
        deferredContext.add("foo_db", secondStatementContext, Collections.emptyList());
        deferredContext.rollbackToSavepoint("foo_savepoint");
        assertThat(getRefreshedStatementContexts(deferredContext), is(Collections.singletonList(firstStatementContext)));
    }
    
    @Test
    void assertRollbackToNotMarkedSavepoint() throws SQLException {
        DeferredMetaDataRefreshContext deferredContext = new DeferredMetaDataRefreshContext();
        deferredContext.add("foo_db", firstStatementContext, Collections.emptyList());
        deferredContext.rollbackToSavepoint("foo_savepoint");
        assertThat(getRefreshedStatementContexts(deferredContext), is(Collections.singletonList(firstStatementContext)));
    }
    
    @Test
    void assertRollbackToSavepointReleasedByEarlierRollback() throws SQLException {
        DeferredMetaDataRefreshContext deferredContext = new DeferredMetaDataRefreshContext();
        deferredContext.add("foo_db", firstStatementContext, Collections.emptyList());
        deferredContext.markSavepoint("foo_savepoint");
        deferredContext.add("foo_db", secondStatementContext, Collections.emptyList());
        deferredContext.markSavepoint("bar_savepoint");
        deferredContext.rollbackToSavepoint("foo_savepoint");
        deferredContext.rollbackToSavepoint("bar_savepoint");
        assertThat(getRefreshedStatementContexts(deferredContext), is(Collections.singletonList(firstStatementContext)));
    }
    
    private Collection<Object> getRefreshedStatementContexts(final DeferredMetaDataRefreshContext deferredContext) throws SQLException {
        List<Object> result = new LinkedList<>();
        try (MockedConstruction<PushDownMetaDataRefreshEngine> ignored = mockConstruction(PushDownMetaDataRefreshEngine.class, (mock, context) -> result.add(context.arguments().get(0)))) {
            deferredContext.refresh(contextManager);
        }
        return result;
    }
}
