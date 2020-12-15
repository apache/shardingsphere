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

package org.apache.shardingsphere.proxy.backend.text.admin.mysql.schema.impl;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.metadata.RawQueryResultColumnMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.metadata.RawQueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.type.RawMemoryQueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.type.memory.row.MemoryQueryResultDataRow;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.impl.QueryHeaderBuilder;
import org.apache.shardingsphere.proxy.backend.text.admin.mysql.schema.SchemaBackendHandler;

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
public final class ShowTablesBackendHandler implements SchemaBackendHandler {
    
    private final BackendConnection backendConnection;
    
    private QueryResult queryResult;
    
    @Override
    public ResponseHeader execute() throws SQLException {
        RawQueryResultMetaData queryResultMetaData = createQueryResultMetaData();
        ShardingSphereMetaData metaData = ProxyContext.getInstance().getMetaData(backendConnection.getSchemaName());
        if (ProxyContext.getInstance().getMetaData(backendConnection.getSchemaName()).isComplete()) {
            Collection<String> allTableNames = ProxyContext.getInstance().getMetaData(backendConnection.getSchemaName()).getSchema().getAllTableNames();
            List<MemoryQueryResultDataRow> rows = allTableNames.stream().map(each -> new MemoryQueryResultDataRow(Collections.singletonList(each))).collect(Collectors.toList());
            queryResult = new RawMemoryQueryResult(queryResultMetaData, rows);
        } else {
            queryResult = new RawMemoryQueryResult(queryResultMetaData, Collections.emptyList());
        }
        return new QueryResponseHeader(Collections.singletonList(QueryHeaderBuilder.build(queryResult, metaData, 1)));
    }
    
    private RawQueryResultMetaData createQueryResultMetaData() {
        String column = String.format("Tables_in_%s", backendConnection.getSchemaName());
        return new RawQueryResultMetaData(Collections.singletonList(new RawQueryResultColumnMetaData("", column, column, Types.VARCHAR, "VARCHAR", 255, 0, false, false, false)));
    }
    
    @Override
    public boolean next() throws SQLException {
        return queryResult.next();
    }
    
    @Override
    public Collection<Object> getRowData() throws SQLException {
        Collection<Object> result = new LinkedList<>();
        for (int columnIndex = 1; columnIndex <= queryResult.getMetaData().getColumnCount(); columnIndex++) {
            result.add(queryResult.getValue(columnIndex, Object.class));
        }
        return result;
    }
}
