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

package org.apache.shardingsphere.proxy.backend.text.admin.mysql.executor.information;

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
import org.apache.shardingsphere.proxy.backend.text.admin.executor.DatabaseAdminQueryExecutor;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The abstract class of select information schema, used to define the template.
 */
public abstract class AbstractSelectInformationExecutor implements DatabaseAdminQueryExecutor {
    
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
            constructRowData(schemaName, getSourceData(schemaName));
        }
        queryResultMetaData = createQueryResultMetaData();
        mergedResult = createMergedResult();
    }
    
    /**
     * Get the schema names as a condition for SQL execution.
     *
     * @return schema names
     */
    protected abstract List<String> getSchemaNames();
    
    /**
     * Get the source object of the row data.
     *
     * @param schemaName schema name
     * @return source object of row data
     * @throws SQLException SQLException
     */
    protected abstract Object getSourceData(String schemaName) throws SQLException;
    
    /**
     * Construct row data from source data.
     *
     * @param schemaName schema name
     * @param sourceData source data of row data
     * @throws SQLException SQLException
     */
    protected abstract void constructRowData(String schemaName, Object sourceData) throws SQLException;
    
    private MergedResult createMergedResult() {
        List<MemoryQueryResultDataRow> resultDataRows = rows.stream()
                .map(each -> new MemoryQueryResultDataRow(new LinkedList<>(each.values()))).collect(Collectors.toList());
        return new TransparentMergedResult(new RawMemoryQueryResult(queryResultMetaData, resultDataRows));
    }
    
    private RawQueryResultMetaData createQueryResultMetaData() {
        List<RawQueryResultColumnMetaData> columns = rows.stream().flatMap(each -> each.keySet().stream()).collect(Collectors.toSet())
                .stream().map(each -> new RawQueryResultColumnMetaData("", each, each, Types.VARCHAR, "VARCHAR", 20, 0)).collect(Collectors.toList());
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
     * Default select information executor, execute sql directly in the database to obtain the result source data.
     */
    @Slf4j
    public static class DefaultSelectInformationExecutor extends AbstractSelectInformationExecutor {
        
        @Getter
        private final String sql;
        
        public DefaultSelectInformationExecutor(final String sql) {
            this.sql = sql;
        }
        
        /**
         * Get the schema names as a condition for SQL execution.
         *
         * @return schema names
         */
        @Override
        protected List<String> getSchemaNames() {
            String schema = ProxyContext.getInstance().getAllSchemaNames().stream().filter(AbstractSelectInformationExecutor::hasDatasource).findFirst().orElseThrow(DatabaseNotExistedException::new);
            return Collections.singletonList(schema);
        }
        
        /**
         * Get the source data of the row data.
         *
         * @param schemaName schema name
         * @return source data of row data
         * @throws SQLException SQLException
         */
        @Override
        protected Object getSourceData(final String schemaName) throws SQLException {
            ShardingSphereResource resource = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData(schemaName).getResource();
            Optional<Entry<String, DataSource>> dataSourceEntry = resource.getDataSources().entrySet().stream().findFirst();
            log.info("Actual SQL: {} ::: {}", dataSourceEntry.orElseThrow(DatabaseNotExistedException::new).getKey(), sql);
            return dataSourceEntry.get().getValue().getConnection().prepareStatement(sql).executeQuery();
        }
        
        /**
         * Construct row data from source data.
         *
         * @param schemaName schema name
         * @param sourceData source data of row data
         * @throws SQLException SQLException
         */
        @Override
        protected void constructRowData(final String schemaName, final Object sourceData) throws SQLException {
            ResultSet resultSet = (ResultSet) sourceData;
            while (resultSet.next()) {
                Map<String, Object> row = new HashMap<>();
                ResultSetMetaData metaData = resultSet.getMetaData();
                for (int i = 1; i < metaData.getColumnCount() + 1; i++) {
                    row.put(resultSet.getMetaData().getColumnName(i), resultSet.getString(i));
                }
                rowPostProcessing(schemaName, row);
                if (!row.isEmpty()) {
                    getRows().addFirst(row);
                }
            }
        }
        
        /**
         * Custom processing.
         *
         * @param schemaName schema name
         * @param rows row data
         */
        protected void rowPostProcessing(final String schemaName, final Map<String, Object> rows) {
        }
    }
}
