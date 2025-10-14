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

package org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.show;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.metadata.database.system.SystemDatabase;
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.metadata.RawQueryResultColumnMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.metadata.RawQueryResultMetaData;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataMergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.util.regex.RegexUtils;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminQueryExecutor;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.table.MySQLShowTablesStatement;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Show tables executor for MySQL.
 */
@RequiredArgsConstructor
public final class MySQLShowTablesExecutor implements DatabaseAdminQueryExecutor {
    
    private final MySQLShowTablesStatement sqlStatement;
    
    @Getter
    private QueryResultMetaData queryResultMetaData;
    
    @Getter
    private MergedResult mergedResult;
    
    @Override
    public void execute(final ConnectionSession connectionSession, final ShardingSphereMetaData metaData) {
        String databaseName = sqlStatement.getFromDatabase().map(optional -> optional.getDatabase().getIdentifier().getValue()).orElseGet(connectionSession::getUsedDatabaseName);
        queryResultMetaData = createQueryResultMetaData(databaseName);
        mergedResult = new LocalDataMergedResult(getQueryResultRows(databaseName, metaData));
    }
    
    private QueryResultMetaData createQueryResultMetaData(final String databaseName) {
        List<RawQueryResultColumnMetaData> columnNames = new ArrayList<>(2);
        String tableColumnName = String.format("Tables_in_%s", databaseName);
        columnNames.add(new RawQueryResultColumnMetaData("", tableColumnName, tableColumnName, Types.VARCHAR, "VARCHAR", 255, 0));
        if (sqlStatement.isContainsFull()) {
            columnNames.add(new RawQueryResultColumnMetaData("", "Table_type", "Table_type", Types.VARCHAR, "VARCHAR", 20, 0));
        }
        return new RawQueryResultMetaData(columnNames);
    }
    
    private Collection<LocalDataQueryResultRow> getQueryResultRows(final String databaseName, final ShardingSphereMetaData metaData) {
        SystemDatabase systemDatabase = new SystemDatabase(sqlStatement.getDatabaseType());
        ShardingSpherePreconditions.checkState(metaData.containsDatabase(databaseName), () -> new UnknownDatabaseException(databaseName));
        ShardingSphereDatabase database = metaData.getDatabase(databaseName);
        return systemDatabase.getSystemSchemas().contains(databaseName) || database.isComplete()
                ? getTables(database).stream().map(this::getQueryResultRow).collect(Collectors.toList())
                : Collections.emptyList();
    }
    
    private Collection<ShardingSphereTable> getTables(final ShardingSphereDatabase database) {
        if (null == database.getSchema(database.getName())) {
            return Collections.emptyList();
        }
        Collection<ShardingSphereTable> tables = database.getSchema(database.getName()).getAllTables();
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
    
    private LocalDataQueryResultRow getQueryResultRow(final ShardingSphereTable table) {
        return sqlStatement.isContainsFull() ? new LocalDataQueryResultRow(table.getName(), table.getType()) : new LocalDataQueryResultRow(table.getName());
    }
}
