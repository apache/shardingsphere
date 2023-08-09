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

package org.apache.shardingsphere.data.pipeline.core.consistencycheck.table.calculator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.common.query.JDBCStreamQueryBuilder;
import org.apache.shardingsphere.data.pipeline.common.sqlbuilder.PipelineDataConsistencyCalculateSQLBuilder;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.RecordSingleTableInventoryCalculatedResult;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.SingleTableInventoryCalculatedResult;
import org.apache.shardingsphere.data.pipeline.core.dumper.ColumnValueReaderEngine;
import org.apache.shardingsphere.data.pipeline.core.exception.data.PipelineTableDataConsistencyCheckLoadingFailedException;
import org.apache.shardingsphere.infra.database.mysql.type.MySQLDatabaseType;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.core.external.sql.type.kernel.category.PipelineSQLException;
import org.apache.shardingsphere.infra.util.close.QuietlyCloser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Record single table inventory calculator.
 */
@RequiredArgsConstructor
@Slf4j
public final class RecordSingleTableInventoryCalculator extends AbstractStreamingSingleTableInventoryCalculator {
    
    private final int chunkSize;
    
    @Override
    public Optional<SingleTableInventoryCalculatedResult> calculateChunk(final SingleTableInventoryCalculateParameter param) {
        CalculationContext calculationContext = getOrCreateCalculationContext(param);
        try {
            List<Map<String, Object>> records = new LinkedList<>();
            Object maxUniqueKeyValue = null;
            ColumnValueReaderEngine columnValueReaderEngine = new ColumnValueReaderEngine(param.getDatabaseType());
            ResultSet resultSet = calculationContext.getResultSet();
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            while (resultSet.next()) {
                ShardingSpherePreconditions.checkState(!isCanceling(), () -> new PipelineTableDataConsistencyCheckLoadingFailedException(param.getSchemaName(), param.getLogicTableName()));
                Map<String, Object> columnRecord = new LinkedHashMap<>();
                for (int columnIndex = 1, columnCount = resultSetMetaData.getColumnCount(); columnIndex <= columnCount; columnIndex++) {
                    columnRecord.put(resultSetMetaData.getColumnLabel(columnIndex), columnValueReaderEngine.read(resultSet, resultSetMetaData, columnIndex));
                }
                records.add(columnRecord);
                maxUniqueKeyValue = columnValueReaderEngine.read(resultSet, resultSetMetaData, param.getFirstUniqueKey().getOrdinalPosition());
                if (records.size() == chunkSize) {
                    break;
                }
            }
            if (records.isEmpty()) {
                calculationContext.close();
            }
            return records.isEmpty() ? Optional.empty() : Optional.of(new RecordSingleTableInventoryCalculatedResult(maxUniqueKeyValue, records));
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
    
    private CalculationContext getOrCreateCalculationContext(final SingleTableInventoryCalculateParameter param) {
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
    
    private CalculationContext createCalculationContext(final SingleTableInventoryCalculateParameter param) throws SQLException {
        Connection connection = param.getDataSource().getConnection();
        CalculationContext result = new CalculationContext(connection);
        param.setCalculationContext(result);
        return result;
    }
    
    private void fulfillCalculationContext(final CalculationContext calculationContext, final SingleTableInventoryCalculateParameter param) throws SQLException {
        String sql = getQuerySQL(param);
        PreparedStatement preparedStatement = JDBCStreamQueryBuilder.build(param.getDatabaseType(), calculationContext.getConnection(), sql);
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
    
    private String getQuerySQL(final SingleTableInventoryCalculateParameter param) {
        if (null == param.getFirstUniqueKey()) {
            throw new UnsupportedOperationException("Data consistency of DATA_MATCH type not support table without unique key and primary key now");
        }
        PipelineDataConsistencyCalculateSQLBuilder pipelineSQLBuilder = new PipelineDataConsistencyCalculateSQLBuilder(param.getDatabaseType());
        Collection<String> columnNames = param.getColumnNames().isEmpty() ? Collections.singleton("*") : param.getColumnNames();
        boolean firstQuery = null == param.getTableCheckPosition();
        return pipelineSQLBuilder.buildQueryAllOrderingSQL(param.getSchemaName(), param.getLogicTableName(), columnNames, param.getFirstUniqueKey().getName(), firstQuery);
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
