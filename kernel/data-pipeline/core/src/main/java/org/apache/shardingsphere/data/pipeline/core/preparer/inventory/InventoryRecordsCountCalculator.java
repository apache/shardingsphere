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

package org.apache.shardingsphere.data.pipeline.core.preparer.inventory;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.InventoryDumperContext;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.core.sqlbuilder.sql.PipelinePrepareSQLBuilder;
import org.apache.shardingsphere.data.pipeline.core.exception.job.SplitPipelineJobByUniqueKeyException;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.database.mysql.type.MySQLDatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Inventory records count calculator.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class InventoryRecordsCountCalculator {
    
    /**
     * Get table records count.
     *
     * @param dumperContext inventory dumper context
     * @param dataSource data source
     * @return table records count
     * @throws SplitPipelineJobByUniqueKeyException if there's exception from database
     */
    public static long getTableRecordsCount(final InventoryDumperContext dumperContext, final PipelineDataSourceWrapper dataSource) {
        String schemaName = dumperContext.getCommonContext().getTableAndSchemaNameMapper().getSchemaName(dumperContext.getLogicTableName());
        String actualTableName = dumperContext.getActualTableName();
        PipelinePrepareSQLBuilder pipelineSQLBuilder = new PipelinePrepareSQLBuilder(dataSource.getDatabaseType());
        Optional<String> sql = pipelineSQLBuilder.buildEstimatedCountSQL(schemaName, actualTableName);
        try {
            if (sql.isPresent()) {
                DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, dataSource.getDatabaseType().getType());
                long result = getEstimatedCount(databaseType, dataSource, sql.get());
                return result > 0 ? result : getCount(dataSource, pipelineSQLBuilder.buildCountSQL(schemaName, actualTableName));
            }
            return getCount(dataSource, pipelineSQLBuilder.buildCountSQL(schemaName, actualTableName));
        } catch (final SQLException ex) {
            String uniqueKey = dumperContext.hasUniqueKey() ? dumperContext.getUniqueKeyColumns().get(0).getName() : "";
            throw new SplitPipelineJobByUniqueKeyException(dumperContext.getActualTableName(), uniqueKey, ex);
        }
    }
    
    private static long getEstimatedCount(final DatabaseType databaseType, final DataSource dataSource, final String estimatedCountSQL) throws SQLException {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(estimatedCountSQL)) {
            if (databaseType instanceof MySQLDatabaseType) {
                preparedStatement.setString(1, connection.getCatalog());
            }
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                resultSet.next();
                return resultSet.getLong(1);
            }
        }
    }
    
    private static long getCount(final DataSource dataSource, final String countSQL) throws SQLException {
        long startTimeMillis = System.currentTimeMillis();
        long result;
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(countSQL)) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                resultSet.next();
                result = resultSet.getLong(1);
            }
        }
        log.info("getCount cost {} ms, sql: {}, count: {}", System.currentTimeMillis() - startTimeMillis, countSQL, result);
        return result;
    }
}
