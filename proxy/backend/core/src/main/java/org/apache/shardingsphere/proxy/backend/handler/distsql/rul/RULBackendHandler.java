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

package org.apache.shardingsphere.proxy.backend.handler.distsql.rul;

import org.apache.shardingsphere.distsql.handler.type.rul.RULExecuteEngine;
import org.apache.shardingsphere.distsql.statement.rul.RULStatement;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DatabaseConnectionManager;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.ExecutorStatementManager;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataMergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.distsql.DistSQLBackendHandler;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseCell;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseRow;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;

import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * RUL backend handler.
 */
public final class RULBackendHandler extends RULExecuteEngine implements DistSQLBackendHandler {
    
    private final ConnectionSession connectionSession;
    
    private List<QueryHeader> queryHeaders;
    
    private MergedResult mergedResult;
    
    public RULBackendHandler(final RULStatement sqlStatement, final ConnectionSession connectionSession) {
        super(sqlStatement, connectionSession.getDatabaseName(), ProxyContext.getInstance().getContextManager());
        this.connectionSession = connectionSession;
    }
    
    @Override
    public ResponseHeader execute() throws SQLException {
        executeQuery();
        queryHeaders = createQueryHeader(getColumnNames());
        mergedResult = createMergedResult(getRows());
        return new QueryResponseHeader(queryHeaders);
    }
    
    private List<QueryHeader> createQueryHeader(final Collection<String> columnNames) {
        return columnNames.stream().map(each -> new QueryHeader("", "", each, each, Types.CHAR, "CHAR", 255, 0, false, false, false, false)).collect(Collectors.toList());
    }
    
    private MergedResult createMergedResult(final Collection<LocalDataQueryResultRow> rows) {
        return new LocalDataMergedResult(rows);
    }
    
    @Override
    public boolean next() throws SQLException {
        return null != mergedResult && mergedResult.next();
    }
    
    @Override
    public QueryResponseRow getRowData() throws SQLException {
        List<QueryResponseCell> cells = new ArrayList<>(queryHeaders.size());
        for (int i = 0; i < queryHeaders.size(); i++) {
            cells.add(new QueryResponseCell(queryHeaders.get(i).getColumnType(), mergedResult.getValue(i + 1, Object.class)));
        }
        return new QueryResponseRow(cells);
    }
    
    @Override
    protected ShardingSphereDatabase getDatabase(final String databaseName) {
        return ProxyContext.getInstance().getDatabase(databaseName);
    }
    
    @Override
    protected DatabaseType getDatabaseProtocolType() {
        return connectionSession.getProtocolType();
    }
    
    @Override
    protected ConnectionContext getConnectionContext() {
        return connectionSession.getConnectionContext();
    }
    
    @SuppressWarnings("rawtypes")
    @Override
    protected DatabaseConnectionManager getDatabaseConnectionManager() {
        return connectionSession.getDatabaseConnectionManager();
    }
    
    @SuppressWarnings("rawtypes")
    @Override
    protected ExecutorStatementManager getStatementManager() {
        return connectionSession.getStatementManager();
    }
}
