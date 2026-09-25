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

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * Deferred meta data refresh context.
 *
 * <p>Meta data refresh loads the altered schema through a new physical connection, which cannot see DDL that the current transaction
 * has not committed yet. Tables touched by DDL are therefore collected here and reloaded once the transaction ends. Reloading reads the
 * committed state of each table, so it reports the tables a renamed or dropped object left behind, and it stays correct whether the
 * backend keeps DDL transactional. Each table is created, altered or dropped in meta data according to its committed state, so a
 * sequence such as create and then rename is applied by its final effect. Table names keep their parsed identifier so that a quoted
 * identifier is not reconciled as an unquoted one.</p>
 */
public final class DeferredMetaDataRefreshContext {
    
    private final Collection<DeferredTable> deferredTables = new LinkedHashSet<>();
    
    /**
     * Add tables to be reloaded when current transaction ends.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param logicDataSourceName logic data source name the tables are routed to
     * @param tableNames table names
     */
    public void add(final String databaseName, final String schemaName, final String logicDataSourceName, final Collection<IdentifierValue> tableNames) {
        for (IdentifierValue each : tableNames) {
            deferredTables.add(new DeferredTable(databaseName, schemaName, logicDataSourceName, each));
        }
    }
    
    /**
     * Reconcile meta data of deferred tables against their committed state.
     *
     * @param contextManager context manager
     */
    public void reconcile(final ContextManager contextManager) {
        for (DeferredTable each : deferredTables) {
            contextManager.reconcileTable(contextManager.getMetaDataContexts().getMetaData().getDatabase(each.databaseName), each.schemaName, each.logicDataSourceName, each.tableName);
        }
    }
    
    /**
     * Clear deferred tables.
     */
    public void clear() {
        deferredTables.clear();
    }
    
    @RequiredArgsConstructor
    @EqualsAndHashCode
    private static final class DeferredTable {
        
        private final String databaseName;
        
        private final String schemaName;
        
        private final String logicDataSourceName;
        
        private final IdentifierValue tableName;
    }
}
