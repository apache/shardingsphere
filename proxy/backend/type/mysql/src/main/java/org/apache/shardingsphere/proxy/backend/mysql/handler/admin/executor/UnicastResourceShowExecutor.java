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

package org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.exception.dialect.exception.syntax.database.NoDatabaseSelectedException;
import org.apache.shardingsphere.infra.binder.engine.SQLBindEngine;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.metadata.RawQueryResultColumnMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.metadata.RawQueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.type.RawMemoryQueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.type.memory.row.MemoryQueryResultDataRow;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.transparent.TransparentMergedResult;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.proxy.backend.connector.DatabaseConnector;
import org.apache.shardingsphere.proxy.backend.connector.DatabaseConnectorFactory;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.StorageUnitNotExistedException;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminQueryExecutor;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;

import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Unicast resource show executor.
 */
@RequiredArgsConstructor
@Getter
public final class UnicastResourceShowExecutor implements DatabaseAdminQueryExecutor {
    
    private final DatabaseConnectorFactory databaseConnectorFactory = DatabaseConnectorFactory.getInstance();
    
    private final SelectStatement sqlStatement;
    
    private final String sql;
    
    private MergedResult mergedResult;
    
    private DatabaseConnector databaseConnector;
    
    private ResponseHeader responseHeader;
    
    @Override
    public void execute(final ConnectionSession connectionSession) throws SQLException {
        String originDatabase = connectionSession.getDatabaseName();
        String databaseName = null == originDatabase ? getFirstDatabaseName() : originDatabase;
        ShardingSpherePreconditions.checkState(ProxyContext.getInstance().getDatabase(databaseName).containsDataSource(), () -> new StorageUnitNotExistedException(databaseName));
        try {
            connectionSession.setCurrentDatabase(databaseName);
            SQLStatementContext sqlStatementContext = new SQLBindEngine(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData(),
                    connectionSession.getDefaultDatabaseName()).bind(sqlStatement, Collections.emptyList());
            databaseConnector = databaseConnectorFactory.newInstance(new QueryContext(sqlStatementContext, sql, Collections.emptyList()),
                    connectionSession.getDatabaseConnectionManager(), false);
            responseHeader = databaseConnector.execute();
            mergedResult = new TransparentMergedResult(createQueryResult());
        } finally {
            connectionSession.setCurrentDatabase(originDatabase);
            databaseConnector.close();
        }
    }
    
    private String getFirstDatabaseName() {
        Collection<String> databaseNames = ProxyContext.getInstance().getAllDatabaseNames();
        if (databaseNames.isEmpty()) {
            throw new NoDatabaseSelectedException();
        }
        Optional<String> result = databaseNames.stream().filter(each -> ProxyContext.getInstance().getDatabase(each).containsDataSource()).findFirst();
        ShardingSpherePreconditions.checkState(result.isPresent(), StorageUnitNotExistedException::new);
        return result.get();
    }
    
    @Override
    public QueryResultMetaData getQueryResultMetaData() {
        List<RawQueryResultColumnMetaData> columns = ((QueryResponseHeader) responseHeader).getQueryHeaders().stream().map(QueryHeader::getColumnLabel)
                .map(each -> new RawQueryResultColumnMetaData("", each, each, Types.VARCHAR, "VARCHAR", 100, 0))
                .collect(Collectors.toList());
        return new RawQueryResultMetaData(columns);
    }
    
    private QueryResult createQueryResult() throws SQLException {
        List<MemoryQueryResultDataRow> rows = new LinkedList<>();
        while (databaseConnector.next()) {
            List<Object> data = databaseConnector.getRowData().getData();
            rows.add(new MemoryQueryResultDataRow(data));
        }
        return new RawMemoryQueryResult(getQueryResultMetaData(), rows);
    }
}
