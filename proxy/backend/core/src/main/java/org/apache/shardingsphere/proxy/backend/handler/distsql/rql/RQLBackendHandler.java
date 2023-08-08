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

package org.apache.shardingsphere.proxy.backend.handler.distsql.rql;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.exception.dialect.exception.syntax.database.NoDatabaseSelectedException;
import org.apache.shardingsphere.infra.exception.dialect.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.distsql.handler.query.RQLExecutor;
import org.apache.shardingsphere.distsql.parser.statement.rql.RQLStatement;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataMergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.distsql.DistSQLBackendHandler;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseCell;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseRow;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.DatabaseSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.available.FromDatabaseAvailable;

import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * RQL backend handler.
 *
 * @param <T> type of RQL statement
 */
@RequiredArgsConstructor
@Getter
public final class RQLBackendHandler<T extends RQLStatement> implements DistSQLBackendHandler {
    
    private final T sqlStatement;
    
    private final ConnectionSession connectionSession;
    
    private List<QueryHeader> queryHeaders;
    
    private MergedResult mergedResult;
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public ResponseHeader execute() throws SQLException {
        String databaseName = getDatabaseName(connectionSession, sqlStatement);
        checkDatabaseName(databaseName);
        RQLExecutor executor = TypedSPILoader.getService(RQLExecutor.class, sqlStatement.getClass());
        queryHeaders = createQueryHeader(executor.getColumnNames());
        mergedResult = createMergedResult(executor.getRows(ProxyContext.getInstance().getDatabase(databaseName), sqlStatement));
        return new QueryResponseHeader(queryHeaders);
    }
    
    private String getDatabaseName(final ConnectionSession connectionSession, final T sqlStatement) {
        Optional<DatabaseSegment> databaseSegment = sqlStatement instanceof FromDatabaseAvailable ? ((FromDatabaseAvailable) sqlStatement).getDatabase() : Optional.empty();
        return databaseSegment.isPresent() ? databaseSegment.get().getIdentifier().getValue() : connectionSession.getDatabaseName();
    }
    
    private void checkDatabaseName(final String databaseName) {
        ShardingSpherePreconditions.checkNotNull(databaseName, NoDatabaseSelectedException::new);
        ShardingSpherePreconditions.checkState(ProxyContext.getInstance().databaseExists(databaseName), () -> new UnknownDatabaseException(databaseName));
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
}
