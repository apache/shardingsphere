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

package org.apache.shardingsphere.data.pipeline.core.check.consistency.algorithm;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCalculateParameter;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCalculatedResult;
import org.apache.shardingsphere.data.pipeline.core.check.consistency.DataConsistencyCheckUtils;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineSQLException;
import org.apache.shardingsphere.data.pipeline.core.exception.data.PipelineTableDataConsistencyCheckLoadingFailedException;
import org.apache.shardingsphere.data.pipeline.core.util.CloseUtil;
import org.apache.shardingsphere.data.pipeline.core.util.JDBCStreamQueryUtil;
import org.apache.shardingsphere.data.pipeline.spi.ingest.dumper.ColumnValueReader;
import org.apache.shardingsphere.data.pipeline.spi.sqlbuilder.PipelineSQLBuilder;
import org.apache.shardingsphere.data.pipeline.util.spi.PipelineTypedSPILoader;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.util.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.util.spi.annotation.SPIDescription;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Data match data consistency calculate algorithm.
 */
@SPIDescription("Match raw data of records.")
@Slf4j
public final class DataMatchDataConsistencyCalculateAlgorithm extends AbstractStreamingDataConsistencyCalculateAlgorithm {
    
    private static final Collection<String> SUPPORTED_DATABASE_TYPES = ShardingSphereServiceLoader
            .getServiceInstances(DatabaseType.class).stream().map(DatabaseType::getType).collect(Collectors.toList());
    
    private static final String CHUNK_SIZE_KEY = "chunk-size";
    
    private static final int DEFAULT_CHUNK_SIZE = 1000;
    
    private int chunkSize;
    
    @Override
    public void init(final Properties props) {
        chunkSize = getChunkSize(props);
    }
    
    private int getChunkSize(final Properties props) {
        int result = Integer.parseInt(props.getProperty(CHUNK_SIZE_KEY, DEFAULT_CHUNK_SIZE + ""));
        if (result <= 0) {
            log.warn("Invalid result={}, use default value", result);
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
            ColumnValueReader columnValueReader = PipelineTypedSPILoader.getDatabaseTypedService(ColumnValueReader.class, param.getDatabaseType());
            ResultSet resultSet = calculationContext.getResultSet();
            while (resultSet.next()) {
                if (isCanceling()) {
                    throw new PipelineTableDataConsistencyCheckLoadingFailedException(param.getSchemaName(), param.getLogicTableName());
                }
                ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
                int columnCount = resultSetMetaData.getColumnCount();
                Collection<Object> record = new LinkedList<>();
                for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                    record.add(columnValueReader.readValue(resultSet, resultSetMetaData, columnIndex));
                }
                records.add(record);
                maxUniqueKeyValue = columnValueReader.readValue(resultSet, resultSetMetaData, param.getUniqueKey().getOrdinalPosition());
                if (records.size() == chunkSize) {
                    break;
                }
            }
            if (records.isEmpty()) {
                calculationContext.close();
            }
            return records.isEmpty() ? Optional.empty() : Optional.of(new CalculatedResult(maxUniqueKeyValue, records.size(), records));
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            calculationContext.close();
            if (ex instanceof PipelineSQLException) {
                throw (PipelineSQLException) ex;
            }
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
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            throw new PipelineTableDataConsistencyCheckLoadingFailedException(param.getSchemaName(), param.getLogicTableName(), ex);
        }
        try {
            fulfillCalculationContext(result, param);
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            result.close();
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
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, param.getDatabaseType());
        PreparedStatement preparedStatement = setCurrentStatement(JDBCStreamQueryUtil.generateStreamQueryPreparedStatement(databaseType, calculationContext.getConnection(), sql));
        if (!(databaseType instanceof MySQLDatabaseType)) {
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
        PipelineSQLBuilder sqlBuilder = PipelineTypedSPILoader.getDatabaseTypedService(PipelineSQLBuilder.class, param.getDatabaseType());
        boolean firstQuery = null == param.getTableCheckPosition();
        return sqlBuilder.buildQueryAllOrderingSQL(param.getSchemaName(), param.getLogicTableName(), param.getColumnNames(), param.getUniqueKey().getName(), firstQuery);
    }
    
    @Override
    public String getType() {
        return "DATA_MATCH";
    }
    
    @Override
    public Collection<String> getSupportedDatabaseTypes() {
        return SUPPORTED_DATABASE_TYPES;
    }
    
    @RequiredArgsConstructor
    @Getter
    private static final class CalculationContext implements AutoCloseable {
        
        private final Connection connection;
        
        @Setter
        private volatile PreparedStatement preparedStatement;
        
        @Setter
        private volatile ResultSet resultSet;
        
        @Override
        public void close() {
            CloseUtil.closeQuietly(resultSet);
            CloseUtil.closeQuietly(preparedStatement);
            CloseUtil.closeQuietly(connection);
        }
    }
    
    @RequiredArgsConstructor
    @Getter
    static final class CalculatedResult implements DataConsistencyCalculatedResult {
        
        @NonNull
        private final Object maxUniqueKeyValue;
        
        private final int recordsCount;
        
        private final Collection<Collection<Object>> records;
        
        public Optional<Object> getMaxUniqueKeyValue() {
            return Optional.of(maxUniqueKeyValue);
        }
        
        @SneakyThrows(SQLException.class)
        @Override
        public boolean equals(final Object o) {
            if (null == o) {
                return false;
            }
            if (this == o) {
                return true;
            }
            if (!(o instanceof CalculatedResult)) {
                log.warn("CalculatedResult type not match, o.className={}", o.getClass().getName());
                return false;
            }
            final CalculatedResult that = (CalculatedResult) o;
            if (recordsCount != that.recordsCount || !Objects.equals(maxUniqueKeyValue, that.maxUniqueKeyValue)) {
                log.warn("recordCount or maxUniqueKeyValue not match, recordCount1={}, recordCount2={}, maxUniqueKeyValue1={}, maxUniqueKeyValue2={}",
                        recordsCount, that.recordsCount, maxUniqueKeyValue, that.maxUniqueKeyValue);
                return false;
            }
            EqualsBuilder equalsBuilder = new EqualsBuilder();
            Iterator<Collection<Object>> thisIterator = records.iterator();
            Iterator<Collection<Object>> thatIterator = that.records.iterator();
            while (thisIterator.hasNext() && thatIterator.hasNext()) {
                equalsBuilder.reset();
                Collection<Object> thisNext = thisIterator.next();
                Collection<Object> thatNext = thatIterator.next();
                if (thisNext.size() != thatNext.size()) {
                    log.warn("record column size not match, size1={}, size2={}, record1={}, record2={}", thisNext.size(), thatNext.size(), thisNext, thatNext);
                    return false;
                }
                Iterator<Object> thisNextIterator = thisNext.iterator();
                Iterator<Object> thatNextIterator = thatNext.iterator();
                int columnIndex = 0;
                while (thisNextIterator.hasNext() && thatNextIterator.hasNext()) {
                    ++columnIndex;
                    Object thisResult = thisNextIterator.next();
                    Object thatResult = thatNextIterator.next();
                    boolean matched;
                    if (thisResult instanceof SQLXML && thatResult instanceof SQLXML) {
                        matched = ((SQLXML) thisResult).getString().equals(((SQLXML) thatResult).getString());
                    } else if (thisResult instanceof BigDecimal && thatResult instanceof BigDecimal) {
                        matched = DataConsistencyCheckUtils.isBigDecimalEquals((BigDecimal) thisResult, (BigDecimal) thatResult);
                    } else {
                        matched = equalsBuilder.append(thisResult, thatResult).isEquals();
                    }
                    if (!matched) {
                        log.warn("record column value not match, columnIndex={}, value1={}, value2={}, value1.class={}, value2.class={}, record1={}, record2={}", columnIndex, thisResult, thatResult,
                                null != thisResult ? thisResult.getClass().getName() : "", null != thatResult ? thatResult.getClass().getName() : "",
                                thisNext, thatNext);
                        return false;
                    }
                }
            }
            return true;
        }
        
        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37).append(getMaxUniqueKeyValue().orElse(null)).append(getRecordsCount()).append(getRecords()).toHashCode();
        }
    }
}
