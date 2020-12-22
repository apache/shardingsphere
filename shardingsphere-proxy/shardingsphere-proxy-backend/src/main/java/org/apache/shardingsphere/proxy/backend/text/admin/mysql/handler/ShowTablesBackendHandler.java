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

package org.apache.shardingsphere.proxy.backend.text.admin.mysql.handler;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.metadata.RawQueryResultColumnMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.metadata.RawQueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.type.RawMemoryQueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.type.memory.row.MemoryQueryResultDataRow;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.transparent.TransparentMergedResult;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.impl.QueryHeaderBuilder;
import org.apache.shardingsphere.proxy.backend.text.admin.DatabaseAdminBackendHandler;

import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Backend handler for show tables.
 */
@RequiredArgsConstructor
public final class ShowTablesBackendHandler implements DatabaseAdminBackendHandler {
    
    private final BackendConnection backendConnection;
    
    private QueryResultMetaData queryResultMetaData;
    
    private MergedResult mergedResult;
    
    @Override
    public ResponseHeader execute() throws SQLException {
        queryResultMetaData = createQueryResultMetaData();
        QueryResult queryResult = getQueryResult();
        mergedResult = new TransparentMergedResult(queryResult);
        return new QueryResponseHeader(Collections.singletonList(QueryHeaderBuilder.build(queryResult, ProxyContext.getInstance().getMetaData(backendConnection.getSchemaName()), 1)));
    }
    
    private QueryResult getQueryResult() {
        if (!ProxyContext.getInstance().getMetaData(backendConnection.getSchemaName()).isComplete()) {
            return new RawMemoryQueryResult(queryResultMetaData, Collections.emptyList());
        }
        Collection<String> allTableNames = ProxyContext.getInstance().getMetaData(backendConnection.getSchemaName()).getSchema().getAllTableNames();
        List<MemoryQueryResultDataRow> rows = allTableNames.stream().map(each -> new MemoryQueryResultDataRow(Collections.singletonList(each))).collect(Collectors.toList());
        return new RawMemoryQueryResult(queryResultMetaData, rows);
    }
    
    private RawQueryResultMetaData createQueryResultMetaData() {
        return new RawQueryResultMetaData(
                Collections.singletonList(new RawQueryResultColumnMetaData("", String.format("Tables_in_%s", backendConnection.getSchemaName()), Types.VARCHAR, "VARCHAR", 255, 0)));
    }
    
    @Override
    public boolean next() throws SQLException {
        return mergedResult.next();
    }
    
    @Override
    public Collection<Object> getRowData() throws SQLException {
        Collection<Object> result = new LinkedList<>();
        for (int columnIndex = 1; columnIndex <= queryResultMetaData.getColumnCount(); columnIndex++) {
            result.add(mergedResult.getValue(columnIndex, Object.class));
        }
        return result;
    }
}
