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
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCalculateParameter;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCalculatedResult;
import org.apache.shardingsphere.data.pipeline.core.check.consistency.DataConsistencyCheckUtils;
import org.apache.shardingsphere.data.pipeline.core.exception.data.PipelineTableDataConsistencyCheckLoadingFailedException;
import org.apache.shardingsphere.data.pipeline.spi.ingest.dumper.ColumnValueReader;
import org.apache.shardingsphere.data.pipeline.spi.sqlbuilder.PipelineSQLBuilder;
import org.apache.shardingsphere.infra.algorithm.AlgorithmDescription;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.util.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.util.spi.type.required.RequiredSPIRegistry;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPIRegistry;

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
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Data match data consistency calculate algorithm.
 */
@AlgorithmDescription("Match raw data of records.")
@Slf4j
public final class DataMatchDataConsistencyCalculateAlgorithm extends AbstractStreamingDataConsistencyCalculateAlgorithm {
    
    private static final Collection<String> SUPPORTED_DATABASE_TYPES = ShardingSphereServiceLoader
            .getServiceInstances(DatabaseType.class).stream().map(DatabaseType::getType).collect(Collectors.toList());
    
    private static final String CHUNK_SIZE_KEY = "chunk-size";
    
    private static final int DEFAULT_CHUNK_SIZE = 1000;
    
    @Getter
    private Properties props;
    
    private int chunkSize;
    
    private final Map<String, String> firstSQLCache = new ConcurrentHashMap<>();
    
    private final Map<String, String> laterSQLCache = new ConcurrentHashMap<>();
    
    @Override
    public void init(final Properties props) {
        this.props = props;
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
        CalculatedResult previousCalculatedResult = (CalculatedResult) param.getPreviousCalculatedResult();
        String sql = getQuerySQL(param);
        try (
                Connection connection = param.getDataSource().getConnection();
                PreparedStatement preparedStatement = setCurrentStatement(connection.prepareStatement(sql))) {
            preparedStatement.setFetchSize(chunkSize);
            Object tableCheckPosition = param.getTableCheckPosition();
            if (null == previousCalculatedResult) {
                if (null == tableCheckPosition) {
                    preparedStatement.setInt(1, chunkSize);
                } else {
                    preparedStatement.setObject(1, tableCheckPosition);
                    preparedStatement.setInt(2, chunkSize);
                }
            } else {
                preparedStatement.setObject(1, previousCalculatedResult.getMaxUniqueKeyValue().orElse(null));
                preparedStatement.setInt(2, chunkSize);
            }
            Collection<Collection<Object>> records = new LinkedList<>();
            Object maxUniqueKeyValue = null;
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                ColumnValueReader columnValueReader = TypedSPIRegistry.findRegisteredService(ColumnValueReader.class, param.getDatabaseType())
                        .orElseGet(() -> RequiredSPIRegistry.getRegisteredService(ColumnValueReader.class));
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
                }
            }
            return records.isEmpty() ? Optional.empty() : Optional.of(new CalculatedResult(maxUniqueKeyValue, records.size(), records));
        } catch (final SQLException ex) {
            throw new PipelineTableDataConsistencyCheckLoadingFailedException(param.getSchemaName(), param.getLogicTableName(), ex);
        }
    }
    
    private String getQuerySQL(final DataConsistencyCalculateParameter param) {
        PipelineSQLBuilder sqlBuilder = TypedSPIRegistry.findRegisteredService(PipelineSQLBuilder.class, param.getDatabaseType(), null)
                .orElseGet(() -> RequiredSPIRegistry.getRegisteredService(PipelineSQLBuilder.class));
        String logicTableName = param.getLogicTableName();
        String schemaName = param.getSchemaName();
        String uniqueKey = param.getUniqueKey().getName();
        String cacheKey = param.getDatabaseType() + "-" + (null != schemaName && TypedSPIRegistry.getRegisteredService(DatabaseType.class, param.getDatabaseType()).isSchemaAvailable()
                ? schemaName + "." + logicTableName
                : logicTableName);
        if (null == param.getPreviousCalculatedResult() && null == param.getTableCheckPosition()) {
            return firstSQLCache.computeIfAbsent(cacheKey, s -> sqlBuilder.buildChunkedQuerySQL(schemaName, logicTableName, uniqueKey, true));
        }
        return laterSQLCache.computeIfAbsent(cacheKey, s -> sqlBuilder.buildChunkedQuerySQL(schemaName, logicTableName, uniqueKey, false));
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
