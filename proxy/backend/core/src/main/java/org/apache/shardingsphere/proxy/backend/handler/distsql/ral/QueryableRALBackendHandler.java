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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.distsql.handler.type.ral.query.ConnectionSizeAwareQueryableRALExecutor;
import org.apache.shardingsphere.distsql.handler.type.ral.query.DatabaseAwareQueryableRALExecutor;
import org.apache.shardingsphere.distsql.handler.type.ral.query.InstanceContextAwareQueryableRALExecutor;
import org.apache.shardingsphere.distsql.handler.type.ral.query.QueryableRALExecutor;
import org.apache.shardingsphere.distsql.statement.ral.QueryableRALStatement;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataMergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.distsql.DistSQLBackendHandler;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseCell;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseRow;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.util.DatabaseNameUtils;

import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Queryable RAL backend handler.
 *
 * @param <T> type of queryable RAL statement
 */
@RequiredArgsConstructor
public final class QueryableRALBackendHandler<T extends QueryableRALStatement> implements DistSQLBackendHandler {
    
    private final T sqlStatement;
    
    private final ConnectionSession connectionSession;
    
    private List<QueryHeader> queryHeaders;
    
    private MergedResult mergedResult;
    
    @SuppressWarnings("unchecked")
    @Override
    public ResponseHeader execute() {
        QueryableRALExecutor<T> executor = TypedSPILoader.getService(QueryableRALExecutor.class, sqlStatement.getClass());
        mergedResult = getMergedResult(executor);
        queryHeaders = createQueryHeader(executor.getColumnNames());
        return new QueryResponseHeader(queryHeaders);
    }
    
    private MergedResult getMergedResult(final QueryableRALExecutor<T> executor) {
        if (executor instanceof InstanceContextAwareQueryableRALExecutor) {
            ((InstanceContextAwareQueryableRALExecutor<T>) executor).setInstanceContext(ProxyContext.getInstance().getContextManager().getInstanceContext());
        }
        if (executor instanceof DatabaseAwareQueryableRALExecutor) {
            ((DatabaseAwareQueryableRALExecutor<T>) executor).setCurrentDatabase(ProxyContext.getInstance().getDatabase(DatabaseNameUtils.getDatabaseName(sqlStatement, connectionSession)));
        }
        if (executor instanceof ConnectionSizeAwareQueryableRALExecutor) {
            ((ConnectionSizeAwareQueryableRALExecutor<T>) executor).setConnectionSize(connectionSession.getDatabaseConnectionManager().getConnectionSize());
        }
        return createMergedResult(executor.getRows(sqlStatement, ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData()));
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
