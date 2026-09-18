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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.refresher.pushdown.PushDownMetaDataRefreshEngine;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Deferred meta data refresh context.
 *
 * <p>Meta data refresh loads the altered schema through a new physical connection, which cannot see DDL that the current transaction
 * has not committed yet. Refreshes collected here are therefore replayed after the transaction commits, and discarded when it rolls back.</p>
 */
public final class DeferredMetaDataRefreshContext {
    
    private final List<DeferredMetaDataRefresh> deferredRefreshes = new LinkedList<>();
    
    private final Map<String, Integer> savepointMarks = new LinkedHashMap<>();
    
    /**
     * Add deferred meta data refresh.
     *
     * @param databaseName database name
     * @param sqlStatementContext SQL statement context
     * @param routeUnits route units
     */
    public void add(final String databaseName, final SQLStatementContext sqlStatementContext, final Collection<RouteUnit> routeUnits) {
        deferredRefreshes.add(new DeferredMetaDataRefresh(databaseName, sqlStatementContext, routeUnits));
    }
    
    /**
     * Mark current deferred meta data refresh position of savepoint.
     *
     * @param savepointName savepoint name
     */
    public void markSavepoint(final String savepointName) {
        savepointMarks.put(savepointName, deferredRefreshes.size());
    }
    
    /**
     * Roll back deferred meta data refreshes collected after savepoint was marked.
     *
     * @param savepointName savepoint name
     */
    public void rollbackToSavepoint(final String savepointName) {
        if (!savepointMarks.containsKey(savepointName)) {
            return;
        }
        int mark = savepointMarks.get(savepointName);
        deferredRefreshes.subList(mark, deferredRefreshes.size()).clear();
        savepointMarks.values().removeIf(each -> each > mark);
    }
    
    /**
     * Refresh deferred meta data.
     *
     * @param contextManager context manager
     * @throws SQLException SQL exception
     */
    public void refresh(final ContextManager contextManager) throws SQLException {
        for (DeferredMetaDataRefresh each : deferredRefreshes) {
            each.refresh(contextManager);
        }
    }
    
    /**
     * Clear deferred meta data refreshes.
     */
    public void clear() {
        deferredRefreshes.clear();
        savepointMarks.clear();
    }
    
    @RequiredArgsConstructor
    private static final class DeferredMetaDataRefresh {
        
        private final String databaseName;
        
        private final SQLStatementContext sqlStatementContext;
        
        private final Collection<RouteUnit> routeUnits;
        
        private void refresh(final ContextManager contextManager) throws SQLException {
            new PushDownMetaDataRefreshEngine(sqlStatementContext).refresh(contextManager.getPersistServiceFacade().getModeFacade().getMetaDataManagerService(),
                    contextManager.getMetaDataContexts().getMetaData().getDatabase(databaseName), contextManager.getMetaDataContexts().getMetaData().getProps(), routeUnits);
        }
    }
}
