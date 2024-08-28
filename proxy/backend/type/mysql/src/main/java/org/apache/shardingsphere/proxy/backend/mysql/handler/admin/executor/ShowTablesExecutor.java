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
import org.apache.shardingsphere.infra.database.core.metadata.database.system.SystemDatabase;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.metadata.RawQueryResultColumnMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.metadata.RawQueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.type.RawMemoryQueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.type.memory.row.MemoryQueryResultDataRow;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.transparent.TransparentMergedResult;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.util.regex.RegexUtils;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminQueryExecutor;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowTablesStatement;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Show tables executor.
 */
@RequiredArgsConstructor
public final class ShowTablesExecutor implements DatabaseAdminQueryExecutor {
    
    private final ShowTablesStatement sqlStatement;
    
    private final DatabaseType databaseType;
    
    @Getter
    private QueryResultMetaData queryResultMetaData;
    
    @Getter
    private MergedResult mergedResult;
    
    @Override
    public void execute(final ConnectionSession connectionSession) {
        String databaseName = sqlStatement.getFromDatabase().map(schema -> schema.getDatabase().getIdentifier().getValue()).orElseGet(connectionSession::getUsedDatabaseName);
        queryResultMetaData = createQueryResultMetaData(databaseName);
        mergedResult = new TransparentMergedResult(getQueryResult(databaseName));
    }
    
    private QueryResultMetaData createQueryResultMetaData(final String databaseName) {
        List<RawQueryResultColumnMetaData> columnNames = new LinkedList<>();
        String tableColumnName = String.format("Tables_in_%s", databaseName);
        columnNames.add(new RawQueryResultColumnMetaData("", tableColumnName, tableColumnName, Types.VARCHAR, "VARCHAR", 255, 0));
        if (sqlStatement.isContainsFull()) {
            columnNames.add(new RawQueryResultColumnMetaData("", "Table_type", "Table_type", Types.VARCHAR, "VARCHAR", 20, 0));
        }
        return new RawQueryResultMetaData(columnNames);
    }
    
    private QueryResult getQueryResult(final String databaseName) {
        SystemDatabase systemDatabase = new SystemDatabase(databaseType);
        if (!systemDatabase.getSystemSchemas().contains(databaseName) && !ProxyContext.getInstance().getContextManager().getDatabase(databaseName).isComplete()) {
            return new RawMemoryQueryResult(queryResultMetaData, Collections.emptyList());
        }
        List<MemoryQueryResultDataRow> rows = getTables(databaseName).stream().map(this::getRow).collect(Collectors.toList());
        return new RawMemoryQueryResult(queryResultMetaData, rows);
    }
    
    private MemoryQueryResultDataRow getRow(final ShardingSphereTable table) {
        return sqlStatement.isContainsFull()
                ? new MemoryQueryResultDataRow(Arrays.asList(table.getName(), table.getType()))
                : new MemoryQueryResultDataRow(Collections.singletonList(table.getName()));
    }
    
    private Collection<ShardingSphereTable> getTables(final String databaseName) {
        Collection<ShardingSphereTable> tables = ProxyContext.getInstance().getContextManager().getDatabase(databaseName).getSchema(databaseName).getTables().values();
        Collection<ShardingSphereTable> filteredTables = filterByLike(tables);
        return filteredTables.stream().sorted(Comparator.comparing(ShardingSphereTable::getName)).collect(Collectors.toList());
    }
    
    private Collection<ShardingSphereTable> filterByLike(final Collection<ShardingSphereTable> tables) {
        Optional<Pattern> likePattern = getLikePattern();
        return likePattern.isPresent() ? tables.stream().filter(each -> likePattern.get().matcher(each.getName()).matches()).collect(Collectors.toList()) : tables;
    }
    
    private Optional<Pattern> getLikePattern() {
        if (!sqlStatement.getFilter().isPresent()) {
            return Optional.empty();
        }
        Optional<String> regex = sqlStatement.getFilter().get().getLike().map(optional -> RegexUtils.convertLikePatternToRegex(optional.getPattern()));
        return regex.map(optional -> Pattern.compile(optional, Pattern.CASE_INSENSITIVE));
    }
}
