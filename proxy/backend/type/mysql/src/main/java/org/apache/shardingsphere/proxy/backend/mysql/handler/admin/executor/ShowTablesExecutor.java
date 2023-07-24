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

package org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.metadata.RawQueryResultColumnMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.metadata.RawQueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.type.RawMemoryQueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.type.memory.row.MemoryQueryResultDataRow;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.transparent.TransparentMergedResult;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminQueryExecutor;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.infra.util.regular.RegularUtils;
import org.apache.shardingsphere.sql.parser.sql.common.util.SQLUtils;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowTablesStatement;

import java.sql.Types;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Show tables executor.
 */
@RequiredArgsConstructor
public final class ShowTablesExecutor implements DatabaseAdminQueryExecutor {
    
    private static final String TABLE_TYPE = "BASE TABLE";
    
    private final MySQLShowTablesStatement showTablesStatement;
    
    private final DatabaseType databaseType;
    
    @Getter
    private QueryResultMetaData queryResultMetaData;
    
    @Getter
    private MergedResult mergedResult;
    
    @Override
    public void execute(final ConnectionSession connectionSession) {
        String databaseName = showTablesStatement.getFromSchema().map(schema -> schema.getSchema().getIdentifier().getValue()).orElseGet(connectionSession::getDatabaseName);
        queryResultMetaData = createQueryResultMetaData(databaseName);
        mergedResult = new TransparentMergedResult(getQueryResult(databaseName));
    }
    
    private QueryResultMetaData createQueryResultMetaData(final String databaseName) {
        List<RawQueryResultColumnMetaData> columnNames = new LinkedList<>();
        String tableColumnName = String.format("Tables_in_%s", databaseName);
        columnNames.add(new RawQueryResultColumnMetaData("", tableColumnName, tableColumnName, Types.VARCHAR, "VARCHAR", 255, 0));
        if (showTablesStatement.isContainsFull()) {
            columnNames.add(new RawQueryResultColumnMetaData("", "Table_type", "Table_type", Types.VARCHAR, "VARCHAR", 20, 0));
        }
        return new RawQueryResultMetaData(columnNames);
    }
    
    private QueryResult getQueryResult(final String databaseName) {
        if (!databaseType.getSystemSchemas().contains(databaseName) && !ProxyContext.getInstance().getDatabase(databaseName).isComplete()) {
            return new RawMemoryQueryResult(queryResultMetaData, Collections.emptyList());
        }
        List<MemoryQueryResultDataRow> rows = getAllTableNames(databaseName).stream().map(each -> {
            List<Object> rowValues = new LinkedList<>();
            rowValues.add(each);
            if (showTablesStatement.isContainsFull()) {
                rowValues.add(TABLE_TYPE);
            }
            return new MemoryQueryResultDataRow(rowValues);
        }).collect(Collectors.toList());
        return new RawMemoryQueryResult(queryResultMetaData, rows);
    }
    
    private Collection<String> getAllTableNames(final String databaseName) {
        Collection<String> result = ProxyContext.getInstance()
                .getDatabase(databaseName).getSchema(databaseName).getTables().values().stream().map(ShardingSphereTable::getName).collect(Collectors.toList());
        if (showTablesStatement.getFilter().isPresent()) {
            Optional<String> pattern = showTablesStatement.getFilter().get().getLike().map(optional -> SQLUtils.convertLikePatternToRegex(optional.getPattern()));
            return pattern.isPresent() ? result.stream().filter(each -> RegularUtils.matchesCaseInsensitive(pattern.get(), each)).collect(Collectors.toList()) : result;
        }
        return result;
    }
}
