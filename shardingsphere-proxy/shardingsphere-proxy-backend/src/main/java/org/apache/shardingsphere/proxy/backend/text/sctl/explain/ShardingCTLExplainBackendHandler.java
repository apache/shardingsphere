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
import org.apache.shardingsphere.infra.context.kernel.KernelProcessor;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.raw.execute.result.query.QueryHeader;
import org.apache.shardingsphere.infra.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.sql.LogicSQL;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.NoDatabaseSelectedException;
import org.apache.shardingsphere.proxy.backend.exception.RuleNotExistsException;
import org.apache.shardingsphere.proxy.backend.response.BackendResponse;
import org.apache.shardingsphere.proxy.backend.response.query.QueryData;
import org.apache.shardingsphere.proxy.backend.response.query.QueryResponse;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.sctl.exception.InvalidShardingCTLFormatException;
import org.apache.shardingsphere.infra.binder.SQLStatementContextFactory;
import org.apache.shardingsphere.infra.binder.metadata.schema.SchemaMetaData;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
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
    
    private final KernelProcessor kernelProcessor = new KernelProcessor();
    
    private List<QueryHeader> queryHeaders;
    
    private Iterator<ExecutionUnit> executionUnits;
    
    @Override
    public BackendResponse execute() {
        Optional<ShardingCTLExplainStatement> explainStatement = new ShardingCTLExplainParser(sql).doParse();
        if (!explainStatement.isPresent()) {
            throw new InvalidShardingCTLFormatException(sql);
        }
        ShardingSphereSchema schema = ProxyContext.getInstance().getSchema(backendConnection.getSchemaName());
        if (null == schema) {
            throw new NoDatabaseSelectedException();
        }
        if (!schema.isComplete()) {
            throw new RuleNotExistsException();
        }
        LogicSQL logicSQL = createLogicSQL(schema, explainStatement.get());
        executionUnits = kernelProcessor.generateExecutionContext(logicSQL, ProxyContext.getInstance().getSchemaContexts().getProps()).getExecutionUnits().iterator();
        queryHeaders = new ArrayList<>(2);
        queryHeaders.add(new QueryHeader("", "", "datasource_name", "", 255, Types.CHAR, 0, false, false, false, false));
        queryHeaders.add(new QueryHeader("", "", "sql", "", 255, Types.CHAR, 0, false, false, false, false));
        return new QueryResponse(queryHeaders);
    }
    
    private LogicSQL createLogicSQL(final ShardingSphereSchema schema, final ShardingCTLExplainStatement explainStatement) {
        SchemaMetaData schemaMetaData = schema.getMetaData().getRuleSchemaMetaData().getSchemaMetaData();
        SQLStatement sqlStatement = ProxyContext.getInstance().getSchemaContexts().getSqlParserEngine().parse(explainStatement.getSql(), false);
        SQLStatementContext<?> sqlStatementContext = SQLStatementContextFactory.newInstance(schemaMetaData, Collections.emptyList(), sqlStatement);
        return new LogicSQL(schema, sqlStatementContext, explainStatement.getSql(), Collections.emptyList());
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
