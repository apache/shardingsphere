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

package org.apache.shardingsphere.proxy.backend.text.data.impl;

import com.google.common.base.Preconditions;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.binder.aware.CursorDefinitionAware;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.CloseStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.CursorStatementContext;
import org.apache.shardingsphere.infra.binder.type.CursorAvailable;
import org.apache.shardingsphere.infra.distsql.exception.resource.RequiredResourceMissedException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.util.SystemSchemaUtil;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngine;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngineFactory;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.JDBCDatabaseCommunicationEngine;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.RuleNotExistedException;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseRow;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.text.data.DatabaseBackendHandler;
import org.apache.shardingsphere.sharding.merge.ddl.fetch.FetchOrderByValueGroupsHolder;

import java.sql.SQLException;

/**
 * Database backend handler with assigned schema.
 */
@RequiredArgsConstructor
public final class SchemaAssignedDatabaseBackendHandler implements DatabaseBackendHandler {
    
    private final DatabaseCommunicationEngineFactory databaseCommunicationEngineFactory = DatabaseCommunicationEngineFactory.getInstance();
    
    private final SQLStatementContext<?> sqlStatementContext;
    
    private final String sql;
    
    private final ConnectionSession connectionSession;
    
    private DatabaseCommunicationEngine<?> databaseCommunicationEngine;
    
    @Override
    public ResponseHeader execute() throws SQLException {
        prepareDatabaseCommunicationEngine();
        return (ResponseHeader) databaseCommunicationEngine.execute();
    }
    
    @Override
    public Future<ResponseHeader> executeFuture() {
        try {
            prepareDatabaseCommunicationEngine();
            return (Future<ResponseHeader>) databaseCommunicationEngine.execute();
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            return Future.failedFuture(ex);
        }
    }
    
    private void prepareDatabaseCommunicationEngine() throws RequiredResourceMissedException {
        ShardingSphereDatabase database = ProxyContext.getInstance().getDatabase(connectionSession.getDatabaseName());
        boolean isSystemSchema = SystemSchemaUtil.containsSystemSchema(sqlStatementContext.getDatabaseType(), sqlStatementContext.getTablesContext().getSchemaNames(), database);
        if (!isSystemSchema && !database.containsDataSource()) {
            throw new RequiredResourceMissedException(connectionSession.getDatabaseName());
        }
        if (!isSystemSchema && !database.isComplete()) {
            throw new RuleNotExistedException();
        }
        if (sqlStatementContext instanceof CursorAvailable) {
            prepareCursorStatementContext((CursorAvailable) sqlStatementContext, connectionSession);
        }
        databaseCommunicationEngine = databaseCommunicationEngineFactory.newTextProtocolInstance(sqlStatementContext, sql, connectionSession.getBackendConnection());
    }
    
    private void prepareCursorStatementContext(final CursorAvailable statementContext, final ConnectionSession connectionSession) {
        if (statementContext.getCursorName().isPresent()) {
            String cursorName = statementContext.getCursorName().get().getIdentifier().getValue().toLowerCase();
            prepareCursorStatementContext(statementContext, connectionSession, cursorName);
        }
        if (statementContext instanceof CloseStatementContext && ((CloseStatementContext) statementContext).getSqlStatement().isCloseAll()) {
            FetchOrderByValueGroupsHolder.remove();
            connectionSession.getCursorDefinitions().clear();
        }
    }
    
    private void prepareCursorStatementContext(final CursorAvailable statementContext, final ConnectionSession connectionSession, final String cursorName) {
        if (statementContext instanceof CursorStatementContext) {
            connectionSession.getCursorDefinitions().put(cursorName, (CursorStatementContext) statementContext);
        }
        if (statementContext instanceof CursorDefinitionAware) {
            CursorStatementContext cursorStatementContext = connectionSession.getCursorDefinitions().get(cursorName);
            Preconditions.checkArgument(null != cursorStatementContext, "Cursor %s does not exist.", cursorName);
            ((CursorDefinitionAware) statementContext).setUpCursorDefinition(cursorStatementContext);
        }
        if (statementContext instanceof CloseStatementContext) {
            FetchOrderByValueGroupsHolder.getOrderByValueGroups().remove(cursorName);
            FetchOrderByValueGroupsHolder.getMinGroupRowCounts().remove(cursorName);
            connectionSession.getCursorDefinitions().remove(cursorName);
        }
    }
    
    @Override
    public boolean next() throws SQLException {
        return databaseCommunicationEngine.next();
    }
    
    @Override
    public QueryResponseRow getRowData() throws SQLException {
        return databaseCommunicationEngine.getQueryResponseRow();
    }
    
    @Override
    public void close() throws SQLException {
        if (databaseCommunicationEngine instanceof JDBCDatabaseCommunicationEngine) {
            ((JDBCDatabaseCommunicationEngine) databaseCommunicationEngine).close();
        }
    }
}
