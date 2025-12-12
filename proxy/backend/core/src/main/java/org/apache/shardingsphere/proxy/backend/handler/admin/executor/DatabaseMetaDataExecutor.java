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

package org.apache.shardingsphere.proxy.backend.handler.admin.executor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.authority.checker.AuthorityChecker;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.metadata.RawQueryResultColumnMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.metadata.RawQueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.type.RawMemoryQueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.type.memory.row.MemoryQueryResultDataRow;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.transparent.TransparentMergedResult;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Database meta data executor.
 */
@RequiredArgsConstructor
@Getter
public class DatabaseMetaDataExecutor implements DatabaseAdminQueryExecutor {
    
    private QueryResultMetaData queryResultMetaData;
    
    private MergedResult mergedResult;
    
    private final String sql;
    
    private final List<Object> parameters;
    
    private final List<Map<String, Object>> rows = new LinkedList<>();
    
    private final Collection<String> labels = new LinkedList<>();
    
    @Override
    public final void execute(final ConnectionSession connectionSession, final ShardingSphereMetaData metaData) throws SQLException {
        Collection<ShardingSphereDatabase> databases = getDatabases(connectionSession, metaData);
        for (ShardingSphereDatabase each : databases) {
            loadMetaData(each);
        }
        postProcess();
        queryResultMetaData = createQueryResultMetaData();
        mergedResult = createMergedResult();
    }
    
    protected Collection<ShardingSphereDatabase> getDatabases(final ConnectionSession connectionSession, final ShardingSphereMetaData metaData) {
        AuthorityChecker authorityChecker = new AuthorityChecker(metaData.getGlobalRuleMetaData().getSingleRule(AuthorityRule.class), connectionSession.getConnectionContext().getGrantee());
        String databaseName = connectionSession.getCurrentDatabaseName();
        ShardingSphereDatabase database = metaData.getDatabase(databaseName);
        if (null != database && authorityChecker.isAuthorized(database.getName()) && database.containsDataSource()) {
            return Collections.singleton(database);
        }
        Collection<ShardingSphereDatabase> databases = metaData.getAllDatabases().stream()
                .filter(each -> authorityChecker.isAuthorized(each.getName())).filter(ShardingSphereDatabase::containsDataSource).collect(Collectors.toList());
        return databases.isEmpty() ? Collections.emptyList() : Collections.singleton(databases.iterator().next());
    }
    
    private void loadMetaData(final ShardingSphereDatabase database) throws SQLException {
        ResourceMetaData resourceMetaData = database.getResourceMetaData();
        Optional<StorageUnit> storageUnit = resourceMetaData.getStorageUnits().values().stream().findFirst();
        if (!storageUnit.isPresent()) {
            return;
        }
        try (
                Connection connection = storageUnit.get().getDataSource().getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            for (int i = 0; i < parameters.size(); i++) {
                preparedStatement.setObject(i + 1, parameters.get(i));
            }
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                handleResultSet(database, resultSet);
            }
        }
    }
    
    private void handleResultSet(final ShardingSphereDatabase database, final ResultSet resultSet) throws SQLException {
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        while (resultSet.next()) {
            int columnCount = resultSetMetaData.getColumnCount();
            Map<String, Object> rowMap = new LinkedHashMap<>(columnCount, 1F);
            Map<String, String> aliasMap = new LinkedHashMap<>(columnCount, 1F);
            for (int i = 1; i < columnCount + 1; i++) {
                aliasMap.put(resultSetMetaData.getColumnName(i), resultSetMetaData.getColumnLabel(i));
                rowMap.put(resultSetMetaData.getColumnLabel(i), resultSet.getString(i));
            }
            preProcess(database, rowMap, aliasMap);
            if (!rowMap.isEmpty()) {
                rows.add(rowMap);
            }
        }
        if (rows.isEmpty()) {
            for (int i = 1; i < resultSetMetaData.getColumnCount() + 1; i++) {
                labels.add(resultSetMetaData.getColumnLabel(i));
            }
        }
    }
    
    protected void preProcess(final ShardingSphereDatabase database, final Map<String, Object> rows, final Map<String, String> alias) throws SQLException {
    }
    
    protected void postProcess() {
    }
    
    private RawQueryResultMetaData createQueryResultMetaData() {
        if (rows.isEmpty() && !labels.isEmpty()) {
            List<RawQueryResultColumnMetaData> columns = labels.stream().map(each -> new RawQueryResultColumnMetaData("", each, each, Types.VARCHAR, "VARCHAR", 20, 0)).collect(Collectors.toList());
            return new RawQueryResultMetaData(columns);
        }
        List<RawQueryResultColumnMetaData> columns = rows.stream().flatMap(each -> each.keySet().stream()).collect(Collectors.toCollection(LinkedHashSet::new))
                .stream().map(each -> new RawQueryResultColumnMetaData("", each, each, Types.VARCHAR, "VARCHAR", 20, 0)).collect(Collectors.toList());
        return new RawQueryResultMetaData(columns);
    }
    
    private MergedResult createMergedResult() {
        List<MemoryQueryResultDataRow> resultDataRows = rows.stream().map(each -> new MemoryQueryResultDataRow(new LinkedList<>(each.values()))).collect(Collectors.toList());
        return new TransparentMergedResult(new RawMemoryQueryResult(queryResultMetaData, resultDataRows));
    }
}
