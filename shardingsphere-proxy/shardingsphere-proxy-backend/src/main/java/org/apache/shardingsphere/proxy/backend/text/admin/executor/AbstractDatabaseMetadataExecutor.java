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

package org.apache.shardingsphere.proxy.backend.text.admin.executor;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.metadata.RawQueryResultColumnMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.metadata.RawQueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.type.RawMemoryQueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.type.memory.row.MemoryQueryResultDataRow;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.transparent.TransparentMergedResult;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.DatabaseNotExistedException;
import org.apache.shardingsphere.proxy.backend.text.admin.FunctionWithException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
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
    public final void execute(final BackendConnection backendConnection) throws SQLException {
        List<String> schemaNames = getSchemaNames();
        for (String schemaName : schemaNames) {
            initSchemaData(schemaName);
            getSourceData(schemaName, resultSet -> handleResultSet(schemaName, resultSet));
        }
        createPreProcessing();
        queryResultMetaData = createQueryResultMetaData();
        mergedResult = createMergedResult();
    }
    
    private void handleResultSet(final String schemaName, final ResultSet resultSet) throws SQLException {
        while (resultSet.next()) {
            Map<String, Object> rowMap = new LinkedHashMap<>();
            Map<String, String> aliasMap = new LinkedHashMap<>();
            ResultSetMetaData metaData = resultSet.getMetaData();
            for (int i = 1; i < metaData.getColumnCount() + 1; i++) {
                aliasMap.put(metaData.getColumnName(i), metaData.getColumnLabel(i));
                rowMap.put(metaData.getColumnLabel(i), resultSet.getString(i));
            }
            rowPostProcessing(schemaName, rowMap, aliasMap);
            if (!rowMap.isEmpty()) {
                rows.addFirst(rowMap);
            }
        }
    }
    
    /**
     * Initialize the schema data.
     */
    protected abstract void initSchemaData(String schemaName);
    
    /**
     * Get the schema names as a condition for SQL execution.
     *
     * @return schema names
     */
    protected abstract List<String> getSchemaNames();
    
    /**
     * Add default row data.
     *
     */
    protected abstract void createPreProcessing();
    
    /**
     * Get the source object of the row data.
     *
     * @param schemaName schema name
     * @param callback callback for processing source data of information_schema
     * @throws SQLException SQLException
     */
    protected abstract void getSourceData(String schemaName, FunctionWithException<ResultSet, SQLException> callback) throws SQLException;
    
    /**
     * Get the source object of the row data.
     *
     * @param schemaName schema name
     * @param rowMap row
     * @param aliasMap alias
     */
    protected abstract void rowPostProcessing(String schemaName, Map<String, Object> rowMap, Map<String, String> aliasMap);
    
    private MergedResult createMergedResult() {
        List<MemoryQueryResultDataRow> resultDataRows = rows.stream()
                .map(each -> new MemoryQueryResultDataRow(new LinkedList<>(each.values()))).collect(Collectors.toCollection(LinkedList::new));
        return new TransparentMergedResult(new RawMemoryQueryResult(queryResultMetaData, resultDataRows));
    }
    
    private RawQueryResultMetaData createQueryResultMetaData() {
        List<RawQueryResultColumnMetaData> columns = rows.stream().flatMap(each -> each.keySet().stream()).collect(Collectors.toCollection(LinkedHashSet::new))
                .stream().map(each -> new RawQueryResultColumnMetaData("", each, each, Types.VARCHAR, "VARCHAR", 20, 0)).collect(Collectors.toCollection(LinkedList::new));
        return new RawQueryResultMetaData(columns);
    }
    
    /**
     * Determine whether the current schema has a data source.
     *
     * @param schemaName schema name
     * @return has datasource or not
     */
    protected static Boolean hasDatasource(final String schemaName) {
        return ProxyContext.getInstance().getMetaData(schemaName).hasDataSource();
    }
    
    /**
     * Default database metadata executor, execute sql directly in the database to obtain the result source data.
     */
    @Slf4j
    public static class DefaultDatabaseMetadataExecutor extends AbstractDatabaseMetadataExecutor {
        
        @Getter
        private final String sql;
        
        public DefaultDatabaseMetadataExecutor(final String sql) {
            this.sql = sql;
        }
    
        @Override
        protected void initSchemaData(final String schemaName) {
        }
    
        /**
         * Get the schema names as a condition for SQL execution.
         *
         * @return schema names
         */
        @Override
        protected List<String> getSchemaNames() {
            String schema = ProxyContext.getInstance().getAllSchemaNames().stream().filter(AbstractDatabaseMetadataExecutor::hasDatasource).findFirst().orElseThrow(DatabaseNotExistedException::new);
            return Collections.singletonList(schema);
        }
        
        /**
         * Get the source data of the row data.
         *
         * @param schemaName schema name
         * @throws SQLException SQLException
         */
        @Override
        protected void getSourceData(final String schemaName, final FunctionWithException<ResultSet, SQLException> callback) throws SQLException {
            ShardingSphereResource resource = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData(schemaName).getResource();
            Optional<Entry<String, DataSource>> dataSourceEntry = resource.getDataSources().entrySet().stream().findFirst();
            log.info("Actual SQL: {} ::: {}", dataSourceEntry.orElseThrow(DatabaseNotExistedException::new).getKey(), sql);
            try (Connection conn = dataSourceEntry.get().getValue().getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                callback.apply(ps.executeQuery());
            }
        }
        
        /**
         * Custom processing.
         *
         * @param schemaName schema name
         * @param rowMap row
         * @param aliasMap alias
         */
        @Override
        protected void rowPostProcessing(final String schemaName, final Map<String, Object> rowMap, final Map<String, String> aliasMap) {
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
