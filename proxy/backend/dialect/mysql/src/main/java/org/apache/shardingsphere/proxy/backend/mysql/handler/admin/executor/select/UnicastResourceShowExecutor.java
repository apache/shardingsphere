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

package org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.select;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.NoDatabaseSelectedException;
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.engine.SQLBindEngine;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.kernel.metadata.resource.storageunit.EmptyStorageUnitException;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.metadata.RawQueryResultColumnMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.metadata.RawQueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.type.RawMemoryQueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.type.memory.row.MemoryQueryResultDataRow;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.hint.SQLHintUtils;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.transparent.TransparentMergedResult;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.proxy.backend.connector.DatabaseProxyConnector;
import org.apache.shardingsphere.proxy.backend.connector.DatabaseProxyConnectorFactory;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminQueryExecutor;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;

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
public final class UnicastResourceShowExecutor implements DatabaseAdminQueryExecutor {
    
    private final SelectStatement sqlStatement;
    
    private final String sql;
    
    private DatabaseProxyConnector databaseProxyConnector;
    
    private ResponseHeader responseHeader;
    
    @Getter
    private MergedResult mergedResult;
    
    @Override
    public void execute(final ConnectionSession connectionSession, final ShardingSphereMetaData metaData) throws SQLException {
        String originDatabase = connectionSession.getUsedDatabaseName();
        String databaseName = null == originDatabase ? getFirstDatabaseName(metaData) : originDatabase;
        ShardingSpherePreconditions.checkState(metaData.containsDatabase(databaseName), () -> new UnknownDatabaseException(databaseName));
        ShardingSpherePreconditions.checkState(metaData.getDatabase(databaseName).containsDataSource(), () -> new EmptyStorageUnitException(databaseName));
        HintValueContext hintValueContext = SQLHintUtils.extractHint(sql);
        try {
            connectionSession.setCurrentDatabaseName(databaseName);
            SQLStatementContext sqlStatementContext = new SQLBindEngine(metaData, connectionSession.getCurrentDatabaseName(), hintValueContext).bind(sqlStatement);
            databaseProxyConnector = DatabaseProxyConnectorFactory.newInstance(new QueryContext(
                    sqlStatementContext, sql, Collections.emptyList(), hintValueContext, connectionSession.getConnectionContext(), metaData), connectionSession.getDatabaseConnectionManager(), false);
            responseHeader = databaseProxyConnector.execute();
            mergedResult = new TransparentMergedResult(createQueryResult());
        } finally {
            connectionSession.setCurrentDatabaseName(originDatabase);
            databaseProxyConnector.close();
        }
    }
    
    private String getFirstDatabaseName(final ShardingSphereMetaData metaData) {
        Collection<ShardingSphereDatabase> databases = metaData.getAllDatabases();
        if (databases.isEmpty()) {
            throw new NoDatabaseSelectedException();
        }
        Optional<ShardingSphereDatabase> result = databases.stream().filter(ShardingSphereDatabase::containsDataSource).findFirst();
        ShardingSpherePreconditions.checkState(result.isPresent(), EmptyStorageUnitException::new);
        return result.get().getName();
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
        while (databaseProxyConnector.next()) {
            List<Object> data = databaseProxyConnector.getRowData().getData();
            rows.add(new MemoryQueryResultDataRow(data));
        }
        return new RawMemoryQueryResult(getQueryResultMetaData(), rows);
    }
}
