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
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineDataConsistencyCheckFailedException;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.ColumnValueReaderFactory;
import org.apache.shardingsphere.data.pipeline.core.sqlbuilder.PipelineSQLBuilderFactory;
import org.apache.shardingsphere.data.pipeline.spi.ingest.dumper.ColumnValueReader;
import org.apache.shardingsphere.data.pipeline.spi.sqlbuilder.PipelineSQLBuilder;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeFactory;

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
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Data match data consistency calculate algorithm.
 */
@Slf4j
public final class DataMatchDataConsistencyCalculateAlgorithm extends AbstractStreamingDataConsistencyCalculateAlgorithm {
    
    private static final Collection<String> SUPPORTED_DATABASE_TYPES = DatabaseTypeFactory.getInstances().stream().map(DatabaseType::getType).collect(Collectors.toList());
    
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
    protected Optional<Object> calculateChunk(final DataConsistencyCalculateParameter parameter) {
        CalculatedResult previousCalculatedResult = (CalculatedResult) parameter.getPreviousCalculatedResult();
        String sql = getQuerySQL(parameter);
        try (
                Connection connection = parameter.getDataSource().getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            if (null == previousCalculatedResult) {
                preparedStatement.setInt(1, chunkSize);
            } else {
                preparedStatement.setObject(1, previousCalculatedResult.getMaxUniqueKeyValue());
                preparedStatement.setInt(2, chunkSize);
            }
            Collection<Collection<Object>> records = new LinkedList<>();
            Object maxUniqueKeyValue = null;
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                ColumnValueReader columnValueReader = ColumnValueReaderFactory.getInstance(parameter.getDatabaseType());
                while (resultSet.next()) {
                    ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
                    int columnCount = resultSetMetaData.getColumnCount();
                    Collection<Object> record = new LinkedList<>();
                    for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                        record.add(columnValueReader.readValue(resultSet, resultSetMetaData, columnIndex));
                    }
                    records.add(record);
                    maxUniqueKeyValue = columnValueReader.readValue(resultSet, resultSetMetaData, parameter.getUniqueKey().getOrdinalPosition());
                }
            }
            return records.isEmpty() ? Optional.empty() : Optional.of(new CalculatedResult(maxUniqueKeyValue, records.size(), records));
        } catch (final SQLException ex) {
            throw new PipelineDataConsistencyCheckFailedException(String.format("table %s data check failed.", parameter.getLogicTableName()), ex);
        }
    }
    
    private String getQuerySQL(final DataConsistencyCalculateParameter parameter) {
        PipelineSQLBuilder sqlBuilder = PipelineSQLBuilderFactory.getInstance(parameter.getDatabaseType());
        String logicTableName = parameter.getLogicTableName();
        String schemaName = parameter.getTableNameSchemaNameMapping().getSchemaName(logicTableName);
        String uniqueKey = parameter.getUniqueKey().getName();
        String cacheKey = parameter.getDatabaseType() + "-" + (DatabaseTypeFactory.getInstance(parameter.getDatabaseType()).isSchemaAvailable()
                ? schemaName.toLowerCase() + "." + logicTableName.toLowerCase() : logicTableName.toLowerCase());
        if (null == parameter.getPreviousCalculatedResult()) {
            return firstSQLCache.computeIfAbsent(cacheKey, s -> sqlBuilder.buildChunkedQuerySQL(schemaName, logicTableName, uniqueKey, true));
        } else {
            return laterSQLCache.computeIfAbsent(cacheKey, s -> sqlBuilder.buildChunkedQuerySQL(schemaName, logicTableName, uniqueKey, false));
        }
    }
    
    @Override
    public String getType() {
        return "DATA_MATCH";
    }
    
    @Override
    public Collection<String> getSupportedDatabaseTypes() {
        return SUPPORTED_DATABASE_TYPES;
    }
    
    @Override
    public String getDescription() {
        return "Match raw data of records.";
    }
    
    @RequiredArgsConstructor
    @Getter
    private static final class CalculatedResult {
        
        @NonNull
        private final Object maxUniqueKeyValue;
        
        private final int recordCount;
        
        private final Collection<Collection<Object>> records;
        
        @SneakyThrows
        @Override
        public boolean equals(final @NonNull Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof CalculatedResult)) {
                log.warn("CalculatedResult type not match, o.className={}", o.getClass().getName());
                return false;
            }
            final CalculatedResult that = (CalculatedResult) o;
            boolean equalsFirst = new EqualsBuilder().append(getRecordCount(), that.getRecordCount()).append(getMaxUniqueKeyValue(), that.getMaxUniqueKeyValue()).isEquals();
            if (!equalsFirst) {
                log.warn("recordCount or maxUniqueKeyValue not match, recordCount1={}, recordCount2={}, maxUniqueKeyValue1={}, maxUniqueKeyValue2={}",
                        getRecordCount(), that.getRecordCount(), getMaxUniqueKeyValue(), that.getMaxUniqueKeyValue());
                return false;
            }
            Iterator<Collection<Object>> thisIterator = this.records.iterator();
            Iterator<Collection<Object>> thatIterator = that.records.iterator();
            while (thisIterator.hasNext() && thatIterator.hasNext()) {
                Collection<Object> thisNext = thisIterator.next();
                Collection<Object> thatNext = thatIterator.next();
                if (thisNext.size() != thatNext.size()) {
                    log.warn("record column size not match, size1={}, size2={}, record1={}, record2={}", thisNext.size(), thatNext.size(), thisNext, thatNext);
                    return false;
                }
                Iterator<Object> thisNextIterator = thisNext.iterator();
                Iterator<Object> thatNextIterator = thatNext.iterator();
                while (thisNextIterator.hasNext() && thatNextIterator.hasNext()) {
                    Object thisResult = thisNextIterator.next();
                    Object thatResult = thatNextIterator.next();
                    if (thisResult instanceof SQLXML && thatResult instanceof SQLXML) {
                        return ((SQLXML) thisResult).getString().equals(((SQLXML) thatResult).getString());
                    }
                    // TODO The standard MySQL JDBC will convert unsigned mediumint to Integer, but proxy convert it to Long
                    if (thisResult instanceof Integer && thatResult instanceof Long) {
                        return ((Integer) thisResult).longValue() == (Long) thatResult;
                    }
                    if (!new EqualsBuilder().append(thisResult, thatResult).isEquals()) {
                        log.warn("record column value not match, value1={}, value2={}, value1.class={}, value2.class={}, record1={}, record2={}", thisResult, thatResult,
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
            return new HashCodeBuilder(17, 37).append(getMaxUniqueKeyValue()).append(getRecordCount()).append(getRecords()).toHashCode();
        }
    }
}
