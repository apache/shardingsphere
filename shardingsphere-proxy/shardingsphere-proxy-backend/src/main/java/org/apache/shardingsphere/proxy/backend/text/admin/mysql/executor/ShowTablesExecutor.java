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
import org.apache.shardingsphere.proxy.backend.text.admin.executor.DatabaseAdminQueryExecutor;

import java.sql.Types;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Show tables executor.
 */
@Getter
public final class ShowTablesExecutor implements DatabaseAdminQueryExecutor {
    
    private QueryResultMetaData queryResultMetaData;
    
    private MergedResult mergedResult;
    
    @Override
    public void execute(final BackendConnection backendConnection) {
        queryResultMetaData = createQueryResultMetaData(backendConnection.getSchemaName());
        mergedResult = new TransparentMergedResult(getQueryResult(backendConnection.getSchemaName()));
    }
    
    private QueryResult getQueryResult(final String schemaName) {
        if (!ProxyContext.getInstance().getMetaData(schemaName).isComplete()) {
            return new RawMemoryQueryResult(queryResultMetaData, Collections.emptyList());
        }
        Collection<String> allTableNames = ProxyContext.getInstance().getMetaData(schemaName).getSchema().getAllTableNames();
        List<MemoryQueryResultDataRow> rows = allTableNames.stream().map(each -> new MemoryQueryResultDataRow(Collections.singletonList(each))).collect(Collectors.toList());
        return new RawMemoryQueryResult(queryResultMetaData, rows);
    }
    
    private QueryResultMetaData createQueryResultMetaData(final String schemaName) {
        String columnName = String.format("Tables_in_%s", schemaName);
        return new RawQueryResultMetaData(Collections.singletonList(new RawQueryResultColumnMetaData("", columnName, columnName, Types.VARCHAR, "VARCHAR", 255, 0)));
    }
}
