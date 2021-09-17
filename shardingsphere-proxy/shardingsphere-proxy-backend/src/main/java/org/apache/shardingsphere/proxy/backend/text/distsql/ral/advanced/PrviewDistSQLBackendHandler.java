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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.advanced;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.distsql.parser.statement.ral.advanced.preview.PreviewStatement;
import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.infra.binder.SQLStatementContextFactory;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.type.TableAvailable;
import org.apache.shardingsphere.infra.context.kernel.KernelProcessor;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.proxy.backend.communication.SQLStatementSchemaHolder;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.RuleNotExistedException;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.impl.QueryHeader;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Preview dist sql backend handler.
 */
@RequiredArgsConstructor
@Getter
public final class PrviewDistSQLBackendHandler implements TextProtocolBackendHandler {
    
    private final PreviewStatement previewStatement;
    
    private final BackendConnection backendConnection;
    
    private final KernelProcessor kernelProcessor = new KernelProcessor();
    
    private List<QueryHeader> queryHeaders;
    
    private Iterator<ExecutionUnit> executionUnits;
    
    @Override
    public ResponseHeader execute() {
        MetaDataContexts metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        String defaultSchemaName = backendConnection.getDefaultSchemaName();
        String databaseType = DatabaseTypeRegistry.getTrunkDatabaseTypeName(metaDataContexts.getMetaData(defaultSchemaName).getResource().getDatabaseType());
        SQLStatement sqlStatement = new ShardingSphereSQLParserEngine(databaseType, metaDataContexts.getProps()).parse(previewStatement.getSql(), false);
        SQLStatementContext<?> sqlStatementContext = SQLStatementContextFactory.newInstance(metaDataContexts.getMetaDataMap(), Collections.emptyList(), sqlStatement, defaultSchemaName);
        // TODO optimize SQLStatementSchemaHolder
        if (sqlStatementContext instanceof TableAvailable) {
            ((TableAvailable) sqlStatementContext).getTablesContext().getSchemaName().ifPresent(SQLStatementSchemaHolder::set);
        }
        ShardingSphereMetaData metaData = ProxyContext.getInstance().getMetaData(backendConnection.getSchemaName());
        if (!metaData.isComplete()) {
            throw new RuleNotExistedException();
        }
        LogicSQL logicSQL = new LogicSQL(sqlStatementContext, previewStatement.getSql(), Collections.emptyList());
        executionUnits = kernelProcessor.generateExecutionContext(logicSQL, metaData, metaDataContexts.getProps()).getExecutionUnits().iterator();
        queryHeaders = new ArrayList<>(2);
        queryHeaders.add(new QueryHeader("", "", "data_source_name", "", Types.CHAR, "CHAR", 255, 0, false, false, false, false));
        queryHeaders.add(new QueryHeader("", "", "sql", "", Types.CHAR, "CHAR", 255, 0, false, false, false, false));
        return new QueryResponseHeader(queryHeaders);
    }
    
    @Override
    public boolean next() {
        return null != executionUnits && executionUnits.hasNext();
    }
    
    @Override
    public Collection<Object> getRowData() {
        ExecutionUnit executionUnit = executionUnits.next();
        Collection<Object> result = new LinkedList<>();
        result.add(executionUnit.getDataSourceName());
        result.add(executionUnit.getSqlUnit().getSql());
        return result;
    }
}
