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

package org.apache.shardingsphere.proxy.backend.handler.admin.mysql.executor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.dialect.exception.syntax.database.NoDatabaseSelectedException;
import org.apache.shardingsphere.infra.binder.QueryContext;
import org.apache.shardingsphere.infra.binder.SQLStatementContextFactory;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.metadata.RawQueryResultColumnMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.metadata.RawQueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.type.RawMemoryQueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.type.memory.row.MemoryQueryResultDataRow;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.transparent.TransparentMergedResult;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngineFactory;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.JDBCDatabaseCommunicationEngine;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.RuleNotExistedException;
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
    
    private final DatabaseCommunicationEngineFactory databaseCommunicationEngineFactory = DatabaseCommunicationEngineFactory.getInstance();
    
    private final SelectStatement sqlStatement;
    
    private final String sql;
    
    private MergedResult mergedResult;
    
    private JDBCDatabaseCommunicationEngine databaseCommunicationEngine;
    
    private ResponseHeader responseHeader;
    
    @Override
    public void execute(final ConnectionSession connectionSession) throws SQLException {
        String originDatabase = connectionSession.getDatabaseName();
        String databaseName = null == originDatabase ? getFirstDatabaseName() : originDatabase;
        if (!ProxyContext.getInstance().getDatabase(databaseName).containsDataSource()) {
            throw new RuleNotExistedException();
        }
        try {
            connectionSession.setCurrentDatabase(databaseName);
            SQLStatementContext<?> sqlStatementContext = SQLStatementContextFactory.newInstance(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData(),
                    sqlStatement, connectionSession.getDefaultDatabaseName());
            databaseCommunicationEngine = databaseCommunicationEngineFactory.newDatabaseCommunicationEngine(new QueryContext(sqlStatementContext, sql, Collections.emptyList()),
                    connectionSession.getBackendConnection(), false);
            responseHeader = databaseCommunicationEngine.execute();
            mergedResult = new TransparentMergedResult(createQueryResult());
        } finally {
            connectionSession.setCurrentDatabase(originDatabase);
            databaseCommunicationEngine.close();
        }
    }
    
    private String getFirstDatabaseName() {
        Collection<String> databaseNames = ProxyContext.getInstance().getAllDatabaseNames();
        if (databaseNames.isEmpty()) {
            throw new NoDatabaseSelectedException();
        }
        Optional<String> result = databaseNames.stream().filter(each -> ProxyContext.getInstance().getDatabase(each).containsDataSource()).findFirst();
        if (!result.isPresent()) {
            throw new RuleNotExistedException();
        }
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
        while (databaseCommunicationEngine.next()) {
            List<Object> data = databaseCommunicationEngine.getRowData().getData();
            rows.add(new MemoryQueryResultDataRow(data));
        }
        return new RawMemoryQueryResult(getQueryResultMetaData(), rows);
    }
}
