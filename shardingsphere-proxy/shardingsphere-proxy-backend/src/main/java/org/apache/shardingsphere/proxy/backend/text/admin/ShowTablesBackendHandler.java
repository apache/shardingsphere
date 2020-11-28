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

package org.apache.shardingsphere.proxy.backend.text.admin;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.raw.QueryResultRow;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.raw.RawQueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.raw.metadata.QueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.raw.metadata.QueryResultRowMetaData;
import org.apache.shardingsphere.proxy.backend.response.query.QueryHeader;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.BackendResponse;
import org.apache.shardingsphere.proxy.backend.response.query.QueryResponse;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;

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
public final class ShowTablesBackendHandler implements TextProtocolBackendHandler {
    
    private final BackendConnection backendConnection;
    
    private QueryResponse queryResponse;
    
    @Override
    public BackendResponse execute() {
        QueryResponse result = createQueryResponse(backendConnection.getSchemaName());
        if (!ProxyContext.getInstance().getMetaData(backendConnection.getSchemaName()).isComplete()) {
            return result;
        }
        QueryResultMetaData metaData = new QueryResultMetaData(Collections.singletonList(new QueryResultRowMetaData(
                null, result.getQueryHeaders().get(0).getColumnName(), result.getQueryHeaders().get(0).getColumnLabel(), Types.VARCHAR, "VARCHAR", 255, 0, false, false, false)));
        Collection<String> allTableNames = ProxyContext.getInstance().getMetaData(backendConnection.getSchemaName()).getSchema().getAllTableNames();
        List<QueryResultRow> rows = allTableNames.stream().map(each -> new QueryResultRow(Collections.singletonList(each))).collect(Collectors.toList());
        QueryResult queryResult = new RawQueryResult(metaData, rows);
        result.getQueryResults().add(queryResult);
        queryResponse = result;
        return result;
    }
    
    private QueryResponse createQueryResponse(final String schemaName) {
        String column = String.format("Tables_in_%s", schemaName);
        return new QueryResponse(Collections.singletonList(new QueryHeader(schemaName, "", column, column, 64, Types.VARCHAR, 0, false, false, false, false)));
    }
    
    @Override
    public boolean next() throws SQLException {
        return null != queryResponse && queryResponse.getQueryResults().get(0).next();
    }
    
    @Override
    public Collection<Object> getRowData() throws SQLException {
        Collection<Object> result = new LinkedList<>();
        for (int columnIndex = 1; columnIndex <= queryResponse.getQueryHeaders().size(); columnIndex++) {
            result.add(queryResponse.getQueryResults().get(0).getValue(columnIndex, Object.class));
        }
        return result;
    }
}
