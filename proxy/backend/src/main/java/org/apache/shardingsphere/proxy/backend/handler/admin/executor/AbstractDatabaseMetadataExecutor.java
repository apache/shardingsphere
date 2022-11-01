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
import org.apache.shardingsphere.infra.executor.check.SQLCheckEngine;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.metadata.RawQueryResultColumnMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.metadata.RawQueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.type.RawMemoryQueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.type.memory.row.MemoryQueryResultDataRow;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.transparent.TransparentMergedResult;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.ResourceNotExistedException;
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
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * The abstract class of database metadata, used to define the template.
 */
public abstract class AbstractDatabaseMetadataExecutor implements DatabaseAdminQueryExecutor {
    
    @Getter
    private QueryResultMetaData queryResultMetaData;
    
    @Getter
    private MergedResult mergedResult;
    
    @Getter
    private final LinkedList<Map<String, Object>> rows = new LinkedList<>();
    
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
        while (resultSet.next()) {
            Map<String, Object> rowMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            Map<String, String> aliasMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            ResultSetMetaData metaData = resultSet.getMetaData();
            for (int i = 1; i < metaData.getColumnCount() + 1; i++) {
                aliasMap.put(metaData.getColumnName(i), metaData.getColumnLabel(i));
                rowMap.put(metaData.getColumnLabel(i), resultSet.getString(i));
            }
            rowPostProcessing(databaseName, rowMap, aliasMap);
            if (!rowMap.isEmpty()) {
                rows.addFirst(rowMap);
            }
        }
    }
    
    /**
     * Initialize the database data.
     *
     * @param databaseName database name
     */
    protected abstract void initDatabaseData(String databaseName);
    
    /**
     * Get the database names as a condition for SQL execution.
     *
     * @param connectionSession connection session
     * @return database names
     */
    protected abstract List<String> getDatabaseNames(ConnectionSession connectionSession);
    
    /**
     * Add default row data.
     *
     */
    protected abstract void createPreProcessing();
    
    /**
     * Get the source object of the row data.
     *
     * @param databaseName database name
     * @param callback callback for processing source data of information_schema
     * @throws SQLException SQLException
     */
    protected abstract void getSourceData(String databaseName, FunctionWithException<ResultSet, SQLException> callback) throws SQLException;
    
    /**
     * Get the source object of the row data.
     *
     * @param databaseName database name
     * @param rowMap row
     * @param aliasMap alias
     */
    protected abstract void rowPostProcessing(String databaseName, Map<String, Object> rowMap, Map<String, String> aliasMap);
    
    private MergedResult createMergedResult() {
        List<MemoryQueryResultDataRow> resultDataRows = rows.stream().map(each -> new MemoryQueryResultDataRow(new LinkedList<>(each.values()))).collect(Collectors.toList());
        return new TransparentMergedResult(new RawMemoryQueryResult(queryResultMetaData, resultDataRows));
    }
    
    private RawQueryResultMetaData createQueryResultMetaData() {
        List<RawQueryResultColumnMetaData> columns = rows.stream().flatMap(each -> each.keySet().stream()).collect(Collectors.toCollection(LinkedHashSet::new))
                .stream().map(each -> new RawQueryResultColumnMetaData("", each, each, Types.VARCHAR, "VARCHAR", 20, 0)).collect(Collectors.toList());
        return new RawQueryResultMetaData(columns);
    }
    
    /**
     * Determine whether the current database has a data source.
     *
     * @param databaseName database name
     * @return has data source or not
     */
    protected static Boolean hasDataSource(final String databaseName) {
        return ProxyContext.getInstance().getDatabase(databaseName).containsDataSource();
    }
    
    /**
     * Determine whether there is authority.
     *
     * @param databaseName database name
     * @param grantee grantee
     * @return has authority or not
     */
    protected static boolean hasAuthority(final String databaseName, final Grantee grantee) {
        return SQLCheckEngine.check(databaseName, getRules(databaseName), grantee);
    }
    
    private static Collection<ShardingSphereRule> getRules(final String databaseName) {
        Collection<ShardingSphereRule> result;
        result = new LinkedList<>(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getDatabase(databaseName).getRuleMetaData().getRules());
        result.addAll(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getRules());
        return result;
    }
    
    /**
     * Default database metadata executor, execute sql directly in the database to obtain the result source data.
     */
    @RequiredArgsConstructor
    @Slf4j
    public static class DefaultDatabaseMetadataExecutor extends AbstractDatabaseMetadataExecutor {
        
        private final String sql;
        
        @Override
        protected void initDatabaseData(final String databaseName) {
        }
        
        /**
         * Get the database names as a condition for SQL execution.
         *
         * @return database names
         */
        @Override
        protected List<String> getDatabaseNames(final ConnectionSession connectionSession) {
            Optional<String> database = ProxyContext.getInstance().getAllDatabaseNames().stream().filter(each -> hasAuthority(each, connectionSession.getGrantee()))
                    .filter(AbstractDatabaseMetadataExecutor::hasDataSource).findFirst();
            return database.map(Collections::singletonList).orElse(Collections.emptyList());
        }
        
        /**
         * Get the source data of the row data.
         *
         * @param databaseName database name
         * @throws SQLException SQLException
         */
        @Override
        protected void getSourceData(final String databaseName, final FunctionWithException<ResultSet, SQLException> callback) throws SQLException {
            ShardingSphereResourceMetaData resourceMetaData = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getDatabase(databaseName).getResourceMetaData();
            Optional<Entry<String, DataSource>> dataSourceEntry = resourceMetaData.getDataSources().entrySet().stream().findFirst();
            log.info("Actual SQL: {} ::: {}", dataSourceEntry.orElseThrow(ResourceNotExistedException::new).getKey(), sql);
            try (
                    Connection connection = dataSourceEntry.get().getValue().getConnection();
                    PreparedStatement preparedStatement = connection.prepareStatement(sql);
                    ResultSet resultSet = preparedStatement.executeQuery()) {
                callback.apply(resultSet);
            }
        }
        
        /**
         * Custom processing.
         *
         * @param databaseName database name
         * @param rowMap row
         * @param aliasMap alias
         */
        @Override
        protected void rowPostProcessing(final String databaseName, final Map<String, Object> rowMap, final Map<String, String> aliasMap) {
        }
        
        /**
         * Add default row data.
         *
         */
        @Override
        protected void createPreProcessing() {
        }
    }
}
