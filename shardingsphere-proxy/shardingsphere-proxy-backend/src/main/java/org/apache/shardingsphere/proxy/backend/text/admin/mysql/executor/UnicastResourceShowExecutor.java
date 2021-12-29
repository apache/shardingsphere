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

package org.apache.shardingsphere.proxy.backend.text.admin.mysql.executor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
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
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngine;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngineFactory;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.JDBCBackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.NoDatabaseSelectedException;
import org.apache.shardingsphere.proxy.backend.exception.RuleNotExistedException;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.impl.QueryHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.text.admin.executor.DatabaseAdminQueryExecutor;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;

import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Unicast resource show executor.
 */
@Getter
@RequiredArgsConstructor
public final class UnicastResourceShowExecutor implements DatabaseAdminQueryExecutor {
    
    private final DatabaseCommunicationEngineFactory databaseCommunicationEngineFactory = DatabaseCommunicationEngineFactory.getInstance();
    
    private final SelectStatement sqlStatement;
    
    private final String sql;
    
    private MergedResult mergedResult;
    
    private DatabaseCommunicationEngine databaseCommunicationEngine;
    
    private ResponseHeader responseHeader;
    
    @Override
    public void execute(final ConnectionSession connectionSession) throws SQLException {
        String originSchema = connectionSession.getSchemaName();
        String schemaName = null == originSchema ? getFirstSchemaName() : originSchema;
        if (!ProxyContext.getInstance().getMetaData(schemaName).hasDataSource()) {
            throw new RuleNotExistedException();
        }
        try {
            connectionSession.setCurrentSchema(schemaName);
            SQLStatementContext<?> sqlStatementContext = SQLStatementContextFactory.newInstance(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaDataMap(),
                    Collections.emptyList(), sqlStatement, connectionSession.getDefaultSchemaName());
            databaseCommunicationEngine = databaseCommunicationEngineFactory.newTextProtocolInstance(sqlStatementContext, sql, (JDBCBackendConnection) connectionSession.getBackendConnection());
            responseHeader = databaseCommunicationEngine.execute();
            mergedResult = new TransparentMergedResult(createQueryResult());
        } finally {
            connectionSession.setCurrentSchema(originSchema);
            databaseCommunicationEngine.close();
        }
    }
    
    private String getFirstSchemaName() {
        Collection<String> schemaNames = ProxyContext.getInstance().getAllSchemaNames();
        if (schemaNames.isEmpty()) {
            throw new NoDatabaseSelectedException();
        }
        Optional<String> result = schemaNames.stream().filter(each -> ProxyContext.getInstance().getMetaData(each).hasDataSource()).findFirst();
        if (!result.isPresent()) {
            throw new RuleNotExistedException();
        }
        return result.get();
    }
    
    @Override
    public QueryResultMetaData getQueryResultMetaData() {
        LinkedList<RawQueryResultColumnMetaData> raws = ((QueryResponseHeader) responseHeader).getQueryHeaders().stream().map(QueryHeader::getColumnLabel)
                .map(each -> new RawQueryResultColumnMetaData("", each.toString(), each.toString(), Types.VARCHAR, "VARCHAR", 100, 0))
                .collect(Collectors.toCollection(LinkedList::new));
        return new RawQueryResultMetaData(raws);
    }
    
    private QueryResult createQueryResult() throws SQLException {
        List<MemoryQueryResultDataRow> rows = new LinkedList<>();
        while (databaseCommunicationEngine.next()) {
            Collection<Object> data = databaseCommunicationEngine.getQueryResponseRow().getData();
            rows.add(new MemoryQueryResultDataRow(new ArrayList<>(data)));
        }
        return new RawMemoryQueryResult(getQueryResultMetaData(), rows);
    }
    
}
