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
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCalculateParameter;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCalculatedResult;
import org.apache.shardingsphere.data.pipeline.api.metadata.model.PipelineColumnMetaData;
import org.apache.shardingsphere.data.pipeline.core.exception.data.PipelineTableDataConsistencyCheckLoadingFailedException;
import org.apache.shardingsphere.data.pipeline.core.exception.data.UnsupportedCRC32DataConsistencyCalculateAlgorithmException;
import org.apache.shardingsphere.data.pipeline.core.sqlbuilder.PipelineSQLBuilderFactory;
import org.apache.shardingsphere.data.pipeline.core.util.PipelineJdbcUtils;
import org.apache.shardingsphere.data.pipeline.spi.sqlbuilder.PipelineSQLBuilder;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * CRC32 match data consistency calculate algorithm.
 */
@Slf4j
public final class CRC32MatchDataConsistencyCalculateAlgorithm extends AbstractStreamingDataConsistencyCalculateAlgorithm {
    
    private static final Collection<String> SUPPORTED_DATABASE_TYPES = Collections.singletonList(new MySQLDatabaseType().getType());
    
    private static final String CHUNK_SIZE_KEY = "chunk-size";
    
    private static final int DEFAULT_CHUNK_SIZE = 5000;
    
    private final Map<String, String> sqlCache = new ConcurrentHashMap<>();
    
    @Getter
    private Properties props;
    
    private int chunkSize;
    
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
    protected Optional<DataConsistencyCalculatedResult> calculateChunk(final DataConsistencyCalculateParameter parameter) {
        PipelineSQLBuilder sqlBuilder = PipelineSQLBuilderFactory.getInstance(parameter.getDatabaseType());
        PipelineColumnMetaData uniqueKey = parameter.getUniqueKey();
        CalculatedResult previousCalculatedResult = (CalculatedResult) parameter.getPreviousCalculatedResult();
        Object beginId;
        if (null == previousCalculatedResult) {
            beginId = getBeginIdFromUniqueKey(uniqueKey.getDataType());
        } else {
            beginId = previousCalculatedResult.getMaxUniqueKeyValue();
        }
        Object endId = getMaxUniqueKeyValue(sqlBuilder, parameter);
        if (null == endId) {
            return Optional.empty();
        }
        List<CalculatedItem> calculatedItems = parameter.getColumnNames().stream().map(each -> calculateCRC32(sqlBuilder, parameter, each, beginId, endId)).collect(Collectors.toList());
        int recordsCount = calculatedItems.get(0).getRecordsCount();
        return Optional.of(new CalculatedResult(endId, recordsCount, calculatedItems.stream().map(CalculatedItem::getCrc32).collect(Collectors.toList())));
    }
    
    private Object getBeginIdFromUniqueKey(final int columnType) {
        if (PipelineJdbcUtils.isStringColumn(columnType)) {
            return "!";
        } else {
            return Integer.MIN_VALUE;
        }
    }
    
    private Object getMaxUniqueKeyValue(final PipelineSQLBuilder sqlBuilder, final DataConsistencyCalculateParameter parameter) {
        String schemaName = parameter.getSchemaName();
        String logicTableName = parameter.getLogicTableName();
        String cacheKeyPrefix = "uniqueKey-" + (null == parameter.getPreviousCalculatedResult() ? "first" : "later") + "-";
        String cacheKey = cacheKeyPrefix + parameter.getDatabaseType() + "-" + (null != schemaName && DatabaseTypeFactory.getInstance(parameter.getDatabaseType()).isSchemaAvailable()
                ? schemaName + "." + logicTableName
                : logicTableName);
        String sql = sqlCache.computeIfAbsent(cacheKey, s -> sqlBuilder.buildChunkedQueryUniqueKeySQL(schemaName, logicTableName, parameter.getUniqueKey().getName(),
                null == parameter.getPreviousCalculatedResult()));
        CalculatedResult previousCalculatedResult = (CalculatedResult) parameter.getPreviousCalculatedResult();
        try (
                Connection connection = parameter.getDataSource().getConnection();
                PreparedStatement preparedStatement = setCurrentStatement(connection.prepareStatement(sql))) {
            preparedStatement.setFetchSize(chunkSize);
            if (null == previousCalculatedResult) {
                preparedStatement.setInt(1, chunkSize);
            } else {
                preparedStatement.setObject(1, previousCalculatedResult.getMaxUniqueKeyValue());
                preparedStatement.setInt(2, chunkSize);
            }
            Object maxUniqueKeyValue = null;
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    maxUniqueKeyValue = resultSet.getObject(1);
                }
            }
            return maxUniqueKeyValue;
        } catch (final SQLException ex) {
            log.error("get max unique key value failed", ex);
            throw new PipelineTableDataConsistencyCheckLoadingFailedException(logicTableName);
        }
    }
    
    private CalculatedItem calculateCRC32(final PipelineSQLBuilder sqlBuilder, final DataConsistencyCalculateParameter parameter, final String columnName, final Object beginId, final Object endId) {
        String logicTableName = parameter.getLogicTableName();
        String schemaName = parameter.getSchemaName();
        String cacheKey = "crc32-" + parameter.getDatabaseType() + "-" + (null != schemaName && DatabaseTypeFactory.getInstance(parameter.getDatabaseType()).isSchemaAvailable()
                ? schemaName + "." + logicTableName
                : logicTableName);
        String sql = sqlCache.get(cacheKey);
        if (null == sql) {
            Optional<String> optional = sqlBuilder.buildCRC32SQL(schemaName, logicTableName, columnName, parameter.getUniqueKey().getName());
            ShardingSpherePreconditions.checkState(optional.isPresent(), () -> new UnsupportedCRC32DataConsistencyCalculateAlgorithmException(parameter.getDatabaseType()));
            sql = optional.get();
            sqlCache.put(cacheKey, sql);
        }
        return calculateCRC32(parameter.getDataSource(), logicTableName, sql, beginId, endId);
    }
    
    private CalculatedItem calculateCRC32(final DataSource dataSource, final String logicTableName, final String sql, final Object beginId, final Object endId) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = setCurrentStatement(connection.prepareStatement(sql))) {
            preparedStatement.setObject(1, beginId);
            preparedStatement.setObject(2, endId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                resultSet.next();
                long crc32 = resultSet.getLong(1);
                int recordsCount = resultSet.getInt(2);
                return new CalculatedItem(crc32, recordsCount);
            }
        } catch (final SQLException ex) {
            log.error("calculate CRC32 failed", ex);
            throw new PipelineTableDataConsistencyCheckLoadingFailedException(logicTableName);
        }
    }
    
    @Override
    public String getType() {
        return "CRC32_MATCH";
    }
    
    @Override
    public Collection<String> getSupportedDatabaseTypes() {
        return SUPPORTED_DATABASE_TYPES;
    }
    
    @Override
    public String getDescription() {
        return "Match CRC32 of records.";
    }
    
    @RequiredArgsConstructor
    @Getter
    private static final class CalculatedItem {
        
        private final long crc32;
        
        private final int recordsCount;
    }
    
    @RequiredArgsConstructor
    @Getter
    private static final class CalculatedResult implements DataConsistencyCalculatedResult {
        
        private final Object maxUniqueKeyValue;
        
        private final int recordsCount;
        
        @NonNull
        private final Collection<Long> columnsCrc32;
        
        @Override
        public boolean equals(final @NonNull Object o) {
            if (this == o) {
                return true;
            }
            if (getClass() != o.getClass()) {
                log.warn("CalculatedResult type not match, o.className={}", o.getClass().getName());
                return false;
            }
            final CalculatedResult that = (CalculatedResult) o;
            if (recordsCount != that.recordsCount) {
                log.info("recordsCount not match, recordsCount={}, that.recordsCount={}", recordsCount, that.recordsCount);
                return false;
            }
            if (!columnsCrc32.equals(that.columnsCrc32)) {
                log.info("columnsCrc32 not match, columnsCrc32={}, that.columnsCrc32={}", columnsCrc32, that.columnsCrc32);
                return false;
            } else {
                return true;
            }
        }
        
        @Override
        public int hashCode() {
            int result = recordsCount;
            result = 31 * result + columnsCrc32.hashCode();
            return result;
        }
    }
}
