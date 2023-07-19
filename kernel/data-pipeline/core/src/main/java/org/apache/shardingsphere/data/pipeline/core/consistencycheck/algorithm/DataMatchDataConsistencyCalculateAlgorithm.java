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

package org.apache.shardingsphere.data.pipeline.core.consistencycheck.algorithm;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.common.sqlbuilder.PipelineDataConsistencyCalculateSQLBuilder;
import org.apache.shardingsphere.data.pipeline.common.util.JDBCStreamQueryUtils;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.DataConsistencyCalculateParameter;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.DataConsistencyCalculatedResult;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.DataMatchCalculatedResult;
import org.apache.shardingsphere.data.pipeline.core.dumper.ColumnValueReaderEngine;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineSQLException;
import org.apache.shardingsphere.data.pipeline.core.exception.data.PipelineTableDataConsistencyCheckLoadingFailedException;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.database.mysql.MySQLDatabaseType;
import org.apache.shardingsphere.infra.util.close.QuietlyCloser;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.util.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.util.spi.annotation.SPIDescription;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Data match data consistency calculate algorithm.
 */
@SPIDescription("Match raw data of records.")
@Slf4j
public final class DataMatchDataConsistencyCalculateAlgorithm extends AbstractStreamingDataConsistencyCalculateAlgorithm {
    
    private static final String CHUNK_SIZE_KEY = "chunk-size";
    
    private static final int DEFAULT_CHUNK_SIZE = 1000;
    
    private int chunkSize;
    
    @Override
    public void init(final Properties props) {
        chunkSize = getChunkSize(props);
    }
    
    private int getChunkSize(final Properties props) {
        int result;
        try {
            result = Integer.parseInt(props.getProperty(CHUNK_SIZE_KEY, Integer.toString(DEFAULT_CHUNK_SIZE)));
        } catch (final NumberFormatException ignore) {
            log.warn("'chunk-size' is not a valid number, use default value {}", DEFAULT_CHUNK_SIZE);
            return DEFAULT_CHUNK_SIZE;
        }
        if (result <= 0) {
            log.warn("Invalid 'chunk-size': {}, use default value {}", result, DEFAULT_CHUNK_SIZE);
            return DEFAULT_CHUNK_SIZE;
        }
        return result;
    }
    
    @Override
    public Optional<DataConsistencyCalculatedResult> calculateChunk(final DataConsistencyCalculateParameter param) {
        CalculationContext calculationContext = getOrCreateCalculationContext(param);
        try {
            Collection<Collection<Object>> records = new LinkedList<>();
            Object maxUniqueKeyValue = null;
            ColumnValueReaderEngine columnValueReaderEngine = new ColumnValueReaderEngine(param.getDatabaseType());
            ResultSet resultSet = calculationContext.getResultSet();
            while (resultSet.next()) {
                ShardingSpherePreconditions.checkState(!isCanceling(), () -> new PipelineTableDataConsistencyCheckLoadingFailedException(param.getSchemaName(), param.getLogicTableName()));
                ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
                int columnCount = resultSetMetaData.getColumnCount();
                Collection<Object> columnRecord = new LinkedList<>();
                for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                    columnRecord.add(columnValueReaderEngine.read(resultSet, resultSetMetaData, columnIndex));
                }
                records.add(columnRecord);
                maxUniqueKeyValue = columnValueReaderEngine.read(resultSet, resultSetMetaData, param.getUniqueKey().getOrdinalPosition());
                if (records.size() == chunkSize) {
                    break;
                }
            }
            if (records.isEmpty()) {
                calculationContext.close();
            }
            return records.isEmpty() ? Optional.empty() : Optional.of(new DataMatchCalculatedResult(maxUniqueKeyValue, records));
        } catch (final PipelineSQLException ex) {
            calculationContext.close();
            throw ex;
            // CHECKSTYLE:OFF
        } catch (final SQLException | RuntimeException ex) {
            // CHECKSTYLE:ON
            calculationContext.close();
            throw new PipelineTableDataConsistencyCheckLoadingFailedException(param.getSchemaName(), param.getLogicTableName(), ex);
        }
    }
    
    private CalculationContext getOrCreateCalculationContext(final DataConsistencyCalculateParameter param) {
        CalculationContext result = (CalculationContext) param.getCalculationContext();
        if (null != result) {
            return result;
        }
        try {
            result = createCalculationContext(param);
            fulfillCalculationContext(result, param);
            // CHECKSTYLE:OFF
        } catch (final SQLException | RuntimeException ex) {
            // CHECKSTYLE:ON
            QuietlyCloser.close(result);
            throw new PipelineTableDataConsistencyCheckLoadingFailedException(param.getSchemaName(), param.getLogicTableName(), ex);
        }
        return result;
    }
    
    private CalculationContext createCalculationContext(final DataConsistencyCalculateParameter param) throws SQLException {
        Connection connection = param.getDataSource().getConnection();
        CalculationContext result = new CalculationContext(connection);
        param.setCalculationContext(result);
        return result;
    }
    
    private void fulfillCalculationContext(final CalculationContext calculationContext, final DataConsistencyCalculateParameter param) throws SQLException {
        String sql = getQuerySQL(param);
        PreparedStatement preparedStatement = JDBCStreamQueryUtils.generateStreamQueryPreparedStatement(param.getDatabaseType(), calculationContext.getConnection(), sql);
        setCurrentStatement(preparedStatement);
        if (!(param.getDatabaseType() instanceof MySQLDatabaseType)) {
            preparedStatement.setFetchSize(chunkSize);
        }
        calculationContext.setPreparedStatement(preparedStatement);
        Object tableCheckPosition = param.getTableCheckPosition();
        if (null != tableCheckPosition) {
            preparedStatement.setObject(1, tableCheckPosition);
        }
        ResultSet resultSet = preparedStatement.executeQuery();
        calculationContext.setResultSet(resultSet);
    }
    
    private String getQuerySQL(final DataConsistencyCalculateParameter param) {
        if (null == param.getUniqueKey()) {
            throw new UnsupportedOperationException("Data consistency of DATA_MATCH type not support table without unique key and primary key now");
        }
        PipelineDataConsistencyCalculateSQLBuilder pipelineSQLBuilder = new PipelineDataConsistencyCalculateSQLBuilder(param.getDatabaseType());
        Collection<String> columnNames = param.getColumnNames().isEmpty() ? Collections.singleton("*") : param.getColumnNames();
        boolean firstQuery = null == param.getTableCheckPosition();
        return pipelineSQLBuilder.buildQueryAllOrderingSQL(param.getSchemaName(), param.getLogicTableName(), columnNames, param.getUniqueKey().getName(), firstQuery);
    }
    
    @Override
    public String getType() {
        return "DATA_MATCH";
    }
    
    @Override
    public Collection<DatabaseType> getSupportedDatabaseTypes() {
        return ShardingSphereServiceLoader.getServiceInstances(DatabaseType.class);
    }
    
    @RequiredArgsConstructor
    private static final class CalculationContext implements AutoCloseable {
        
        @Getter
        private final Connection connection;
        
        private final AtomicReference<PreparedStatement> preparedStatement = new AtomicReference<>();
        
        private final AtomicReference<ResultSet> resultSet = new AtomicReference<>();
        
        /**
         * Get result set.
         *
         * @return result set
         */
        public ResultSet getResultSet() {
            return resultSet.get();
        }
        
        /**
         * Set prepared statement.
         *
         * @param preparedStatement prepared statement
         */
        public void setPreparedStatement(final PreparedStatement preparedStatement) {
            this.preparedStatement.set(preparedStatement);
        }
        
        /**
         * Set result set.
         *
         * @param resultSet result set
         */
        public void setResultSet(final ResultSet resultSet) {
            this.resultSet.set(resultSet);
        }
        
        @Override
        public void close() {
            QuietlyCloser.close(resultSet.get());
            QuietlyCloser.close(preparedStatement.get());
            QuietlyCloser.close(connection);
        }
    }
}
