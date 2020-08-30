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

package org.apache.shardingsphere.proxy.backend.text.sctl.explain;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.raw.execute.result.query.QueryHeader;
import org.apache.shardingsphere.kernel.context.SchemaContext;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.wrapper.StatementExecutorWrapper;
import org.apache.shardingsphere.proxy.backend.response.BackendResponse;
import org.apache.shardingsphere.proxy.backend.response.error.ErrorResponse;
import org.apache.shardingsphere.proxy.backend.response.query.QueryData;
import org.apache.shardingsphere.proxy.backend.response.query.QueryResponse;
import org.apache.shardingsphere.proxy.backend.schema.ProxySchemaContexts;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.sctl.exception.InvalidShardingCTLFormatException;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * Sharding CTL explain backend handler.
 */
@RequiredArgsConstructor
public final class ShardingCTLExplainBackendHandler implements TextProtocolBackendHandler {
    
    private final String sql;
    
    private final BackendConnection backendConnection;
    
    private List<QueryHeader> queryHeaders;
    
    private Iterator<ExecutionUnit> executionUnits;
    
    @Override
    public BackendResponse execute() {
        Optional<ShardingCTLExplainStatement> explainStatement = new ShardingCTLExplainParser(sql).doParse();
        if (!explainStatement.isPresent()) {
            return new ErrorResponse(new InvalidShardingCTLFormatException(sql));
        }
        SchemaContext schema = ProxySchemaContexts.getInstance().getSchema(backendConnection.getSchema());
        StatementExecutorWrapper statementExecutorWrapper =
                new StatementExecutorWrapper(schema, schema.getRuntimeContext().getSqlParserEngine().parse(explainStatement.get().getSql(), false));
        executionUnits = statementExecutorWrapper.generateExecutionContext(explainStatement.get().getSql()).getExecutionUnits().iterator();
        queryHeaders = new ArrayList<>(2);
        queryHeaders.add(new QueryHeader("", "", "datasource_name", "", 255, Types.CHAR, 0, false, false, false, false));
        queryHeaders.add(new QueryHeader("", "", "sql", "", 255, Types.CHAR, 0, false, false, false, false));
        return new QueryResponse(queryHeaders);
    }
    
    @Override
    public boolean next() {
        return null != executionUnits && executionUnits.hasNext();
    }
    
    @Override
    public QueryData getQueryData() {
        ExecutionUnit executionUnit = executionUnits.next();
        List<Object> row = new ArrayList<>(queryHeaders.size());
        row.add(executionUnit.getDataSourceName());
        row.add(executionUnit.getSqlUnit().getSql());
        List<Integer> columnTypes = new ArrayList<>(queryHeaders.size());
        columnTypes.add(queryHeaders.get(0).getColumnType());
        columnTypes.add(queryHeaders.get(1).getColumnType());
        return new QueryData(columnTypes, row);
    }
}
