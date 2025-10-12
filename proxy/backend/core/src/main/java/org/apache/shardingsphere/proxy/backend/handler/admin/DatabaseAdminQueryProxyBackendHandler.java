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

package org.apache.shardingsphere.proxy.backend.handler.admin;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminQueryExecutor;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseCell;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseRow;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeaderBuilderEngine;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Database admin query proxy backend handler.
 */
@RequiredArgsConstructor
public final class DatabaseAdminQueryProxyBackendHandler implements ProxyBackendHandler {
    
    private final ContextManager contextManager;
    
    private final ConnectionSession connectionSession;
    
    private final DatabaseAdminQueryExecutor executor;
    
    private QueryResultMetaData queryResultMetaData;
    
    private MergedResult mergedResult;
    
    @Override
    public ResponseHeader execute() throws SQLException {
        executor.execute(connectionSession, contextManager.getMetaDataContexts().getMetaData());
        queryResultMetaData = executor.getQueryResultMetaData();
        mergedResult = executor.getMergedResult();
        return new QueryResponseHeader(createResponseHeader());
    }
    
    private List<QueryHeader> createResponseHeader() throws SQLException {
        List<QueryHeader> result = new ArrayList<>(queryResultMetaData.getColumnCount());
        ShardingSphereDatabase database = null == connectionSession.getUsedDatabaseName() ? null : contextManager.getDatabase(connectionSession.getUsedDatabaseName());
        DatabaseType databaseType = null == database ? connectionSession.getProtocolType() : database.getProtocolType();
        QueryHeaderBuilderEngine queryHeaderBuilderEngine = new QueryHeaderBuilderEngine(databaseType);
        for (int columnIndex = 1; columnIndex <= queryResultMetaData.getColumnCount(); columnIndex++) {
            result.add(queryHeaderBuilderEngine.build(queryResultMetaData, database, columnIndex));
        }
        return result;
    }
    
    @Override
    public boolean next() throws SQLException {
        return mergedResult.next();
    }
    
    @Override
    public QueryResponseRow getRowData() throws SQLException {
        List<QueryResponseCell> result = new ArrayList<>(queryResultMetaData.getColumnCount());
        for (int columnIndex = 1; columnIndex <= queryResultMetaData.getColumnCount(); columnIndex++) {
            result.add(new QueryResponseCell(queryResultMetaData.getColumnType(columnIndex), mergedResult.getValue(columnIndex, Object.class)));
        }
        return new QueryResponseRow(result);
    }
}
