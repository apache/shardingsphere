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
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.authority.checker.AuthorityChecker;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.metadata.RawQueryResultColumnMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.metadata.RawQueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.type.RawMemoryQueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.type.memory.row.MemoryQueryResultDataRow;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.transparent.TransparentMergedResult;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.StorageUnitNotExistedException;
import org.apache.shardingsphere.proxy.backend.handler.admin.FunctionWithException;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;

import javax.sql.DataSource;
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
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The abstract class of database meta data, used to define the template.
 */
@Getter
public abstract class AbstractDatabaseMetaDataExecutor implements DatabaseAdminQueryExecutor {
    
    private QueryResultMetaData queryResultMetaData;
    
    private MergedResult mergedResult;
    
    private final LinkedList<Map<String, Object>> rows = new LinkedList<>();
    
    private final Collection<String> labels = new LinkedList<>();
    
    @Override
    public final void execute(final ConnectionSession connectionSession) throws SQLException {
        List<String> databaseNames = getDatabaseNames(connectionSession);
        for (String databaseName : databaseNames) {
            initDatabaseData(databaseName);
            getSourceData(databaseName, resultSet -> handleResultSet(databaseName, resultSet));
        }
        createPreProcessing();
        queryResultMetaData = createQueryResultMetaData();
        mergedResult = createMergedResult();
    }
    
    private void handleResultSet(final String databaseName, final ResultSet resultSet) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        while (resultSet.next()) {
            Map<String, Object> rowMap = new LinkedHashMap<>();
            Map<String, String> aliasMap = new LinkedHashMap<>();
            for (int i = 1; i < metaData.getColumnCount() + 1; i++) {
                aliasMap.put(metaData.getColumnName(i), metaData.getColumnLabel(i));
                rowMap.put(metaData.getColumnLabel(i), resultSet.getString(i));
            }
            rowPostProcessing(databaseName, rowMap, aliasMap);
            if (!rowMap.isEmpty()) {
                rows.addFirst(rowMap);
            }
        }
        if (rows.isEmpty()) {
            for (int i = 1; i < metaData.getColumnCount() + 1; i++) {
                labels.add(metaData.getColumnLabel(i));
            }
        }
    }
    
    protected abstract void initDatabaseData(String databaseName);
    
    protected abstract List<String> getDatabaseNames(ConnectionSession connectionSession);
    
    protected abstract void createPreProcessing();
    
    protected abstract void getSourceData(String databaseName, FunctionWithException<ResultSet, SQLException> callback) throws SQLException;
    
    protected abstract void rowPostProcessing(String databaseName, Map<String, Object> rowMap, Map<String, String> aliasMap);
    
    private MergedResult createMergedResult() {
        List<MemoryQueryResultDataRow> resultDataRows = rows.stream().map(each -> new MemoryQueryResultDataRow(new LinkedList<>(each.values()))).collect(Collectors.toList());
        return new TransparentMergedResult(new RawMemoryQueryResult(queryResultMetaData, resultDataRows));
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
    
    protected static Boolean hasDataSource(final String databaseName) {
        return ProxyContext.getInstance().getDatabase(databaseName).containsDataSource();
    }
    
    protected static boolean isAuthorized(final String databaseName, final Grantee grantee) {
        AuthorityRule authorityRule = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getSingleRule(AuthorityRule.class);
        return new AuthorityChecker(authorityRule, grantee).isAuthorized(databaseName);
    }
    
    /**
     * Default database meta data executor, execute sql directly in the database to obtain the result source data.
     */
    @RequiredArgsConstructor
    @Slf4j
    public static class DefaultDatabaseMetaDataExecutor extends AbstractDatabaseMetaDataExecutor {
        
        private final String sql;
        
        @Override
        protected void initDatabaseData(final String databaseName) {
        }
        
        @Override
        protected List<String> getDatabaseNames(final ConnectionSession connectionSession) {
            Optional<String> database = ProxyContext.getInstance().getAllDatabaseNames().stream().filter(each -> isAuthorized(each, connectionSession.getGrantee()))
                    .filter(AbstractDatabaseMetaDataExecutor::hasDataSource).findFirst();
            return database.map(Collections::singletonList).orElse(Collections.emptyList());
        }
        
        @Override
        protected void getSourceData(final String databaseName, final FunctionWithException<ResultSet, SQLException> callback) throws SQLException {
            ShardingSphereResourceMetaData resourceMetaData = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getDatabase(databaseName).getResourceMetaData();
            Optional<Entry<String, DataSource>> dataSourceEntry = resourceMetaData.getDataSources().entrySet().stream().findFirst();
            log.info("Actual SQL: {} ::: {}", dataSourceEntry.orElseThrow(() -> new StorageUnitNotExistedException(databaseName)).getKey(), sql);
            try (
                    Connection connection = dataSourceEntry.get().getValue().getConnection();
                    PreparedStatement preparedStatement = connection.prepareStatement(sql);
                    ResultSet resultSet = preparedStatement.executeQuery()) {
                callback.apply(resultSet);
            }
        }
        
        @Override
        protected void rowPostProcessing(final String databaseName, final Map<String, Object> rowMap, final Map<String, String> aliasMap) {
        }
        
        @Override
        protected void createPreProcessing() {
        }
    }
}
