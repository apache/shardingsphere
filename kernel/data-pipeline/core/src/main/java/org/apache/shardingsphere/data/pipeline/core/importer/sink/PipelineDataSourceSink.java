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

package org.apache.shardingsphere.data.pipeline.core.importer.sink;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.Column;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.GroupedDataRecord;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.Record;
import org.apache.shardingsphere.data.pipeline.api.job.JobOperationType;
import org.apache.shardingsphere.data.pipeline.api.metadata.LogicTableName;
import org.apache.shardingsphere.data.pipeline.common.config.ImporterConfiguration;
import org.apache.shardingsphere.data.pipeline.common.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.common.ingest.IngestDataChangeType;
import org.apache.shardingsphere.data.pipeline.common.ingest.record.RecordUtils;
import org.apache.shardingsphere.data.pipeline.common.job.progress.listener.PipelineJobProgressUpdatedParameter;
import org.apache.shardingsphere.data.pipeline.common.sqlbuilder.PipelineImportSQLBuilder;
import org.apache.shardingsphere.data.pipeline.common.util.PipelineJdbcUtils;
import org.apache.shardingsphere.data.pipeline.core.exception.job.PipelineImporterJobWriteException;
import org.apache.shardingsphere.data.pipeline.core.importer.DataRecordMerger;
import org.apache.shardingsphere.data.pipeline.spi.ratelimit.JobRateLimitAlgorithm;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Pipeline data source sink.
 */
@Slf4j
public final class PipelineDataSourceSink implements PipelineSink {
    
    private static final DataRecordMerger MERGER = new DataRecordMerger();
    
    @Getter(AccessLevel.PROTECTED)
    private final ImporterConfiguration importerConfig;
    
    private final PipelineDataSourceManager dataSourceManager;
    
    private final JobRateLimitAlgorithm rateLimitAlgorithm;
    
    private final PipelineImportSQLBuilder importSQLBuilder;
    
    private final AtomicReference<Statement> batchInsertStatement = new AtomicReference<>();
    
    private final AtomicReference<Statement> updateStatement = new AtomicReference<>();
    
    private final AtomicReference<Statement> batchDeleteStatement = new AtomicReference<>();
    
    public PipelineDataSourceSink(final ImporterConfiguration importerConfig, final PipelineDataSourceManager dataSourceManager) {
        this.importerConfig = importerConfig;
        this.dataSourceManager = dataSourceManager;
        rateLimitAlgorithm = importerConfig.getRateLimitAlgorithm();
        importSQLBuilder = new PipelineImportSQLBuilder(importerConfig.getDataSourceConfig().getDatabaseType());
    }
    
    @Override
    public boolean identifierMatched(final Object identifier) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public PipelineJobProgressUpdatedParameter write(final String ackId, final List<Record> records) {
        return flush(dataSourceManager.getDataSource(importerConfig.getDataSourceConfig()), records);
    }
    
    private PipelineJobProgressUpdatedParameter flush(final DataSource dataSource, final List<Record> buffer) {
        List<DataRecord> dataRecords = buffer.stream().filter(DataRecord.class::isInstance).map(DataRecord.class::cast).collect(Collectors.toList());
        if (dataRecords.isEmpty()) {
            return new PipelineJobProgressUpdatedParameter(0);
        }
        int insertRecordNumber = 0;
        for (DataRecord each : dataRecords) {
            if (IngestDataChangeType.INSERT.equals(each.getType())) {
                insertRecordNumber++;
            }
        }
        List<GroupedDataRecord> result = MERGER.group(dataRecords);
        for (GroupedDataRecord each : result) {
            flushInternal(dataSource, each.getBatchDeleteDataRecords());
            flushInternal(dataSource, each.getBatchInsertDataRecords());
            flushInternal(dataSource, each.getBatchUpdateDataRecords());
            sequentialFlush(dataSource, each.getNonBatchRecords());
        }
        return new PipelineJobProgressUpdatedParameter(insertRecordNumber);
    }
    
    private void flushInternal(final DataSource dataSource, final List<DataRecord> buffer) {
        if (null == buffer || buffer.isEmpty()) {
            return;
        }
        tryFlush(dataSource, buffer);
    }
    
    @SneakyThrows(InterruptedException.class)
    private void tryFlush(final DataSource dataSource, final List<DataRecord> buffer) {
        for (int i = 0; !Thread.interrupted() && i <= importerConfig.getRetryTimes(); i++) {
            try {
                doFlush(dataSource, buffer);
                return;
            } catch (final SQLException ex) {
                log.error("flush failed {}/{} times.", i, importerConfig.getRetryTimes(), ex);
                if (i == importerConfig.getRetryTimes()) {
                    throw new PipelineImporterJobWriteException(ex);
                }
                Thread.sleep(Math.min(5 * 60 * 1000L, 1000L << i));
            }
        }
    }
    
    private void doFlush(final DataSource dataSource, final List<DataRecord> buffer) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            switch (buffer.get(0).getType()) {
                case IngestDataChangeType.INSERT:
                    if (null != rateLimitAlgorithm) {
                        rateLimitAlgorithm.intercept(JobOperationType.INSERT, 1);
                    }
                    executeBatchInsert(connection, buffer);
                    break;
                case IngestDataChangeType.UPDATE:
                    if (null != rateLimitAlgorithm) {
                        rateLimitAlgorithm.intercept(JobOperationType.UPDATE, 1);
                    }
                    executeUpdate(connection, buffer);
                    break;
                case IngestDataChangeType.DELETE:
                    if (null != rateLimitAlgorithm) {
                        rateLimitAlgorithm.intercept(JobOperationType.DELETE, 1);
                    }
                    executeBatchDelete(connection, buffer);
                    break;
                default:
                    break;
            }
            connection.commit();
        }
    }
    
    private void doFlush(final Connection connection, final List<DataRecord> buffer) {
        // TODO it's better use transaction, but execute delete maybe not effect when open transaction of PostgreSQL sometimes
        for (DataRecord each : buffer) {
            try {
                doFlush(connection, each);
            } catch (final SQLException ex) {
                throw new PipelineImporterJobWriteException(String.format("Write failed, record=%s", each), ex);
            }
        }
    }
    
    private void doFlush(final Connection connection, final DataRecord dataRecord) throws SQLException {
        switch (dataRecord.getType()) {
            case IngestDataChangeType.INSERT:
                if (null != rateLimitAlgorithm) {
                    rateLimitAlgorithm.intercept(JobOperationType.INSERT, 1);
                }
                executeBatchInsert(connection, Collections.singletonList(dataRecord));
                break;
            case IngestDataChangeType.UPDATE:
                if (null != rateLimitAlgorithm) {
                    rateLimitAlgorithm.intercept(JobOperationType.UPDATE, 1);
                }
                executeUpdate(connection, dataRecord);
                break;
            case IngestDataChangeType.DELETE:
                if (null != rateLimitAlgorithm) {
                    rateLimitAlgorithm.intercept(JobOperationType.DELETE, 1);
                }
                executeBatchDelete(connection, Collections.singletonList(dataRecord));
                break;
            default:
        }
    }
    
    private void executeBatchInsert(final Connection connection, final List<DataRecord> dataRecords) throws SQLException {
        DataRecord dataRecord = dataRecords.get(0);
        String insertSql = importSQLBuilder.buildInsertSQL(getSchemaName(dataRecord.getTableName()), dataRecord);
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertSql)) {
            batchInsertStatement.set(preparedStatement);
            preparedStatement.setQueryTimeout(30);
            for (DataRecord each : dataRecords) {
                for (int i = 0; i < each.getColumnCount(); i++) {
                    preparedStatement.setObject(i + 1, each.getColumn(i).getValue());
                }
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        } finally {
            batchInsertStatement.set(null);
        }
    }
    
    private String getSchemaName(final String logicTableName) {
        return getImporterConfig().getSchemaName(new LogicTableName(logicTableName));
    }
    
    private void executeUpdate(final Connection connection, final List<DataRecord> dataRecords) throws SQLException {
        for (DataRecord each : dataRecords) {
            executeUpdate(connection, each);
        }
    }
    
    private void executeUpdate(final Connection connection, final DataRecord dataRecord) throws SQLException {
        Set<String> shardingColumns = importerConfig.getShardingColumns(dataRecord.getTableName());
        List<Column> conditionColumns = RecordUtils.extractConditionColumns(dataRecord, shardingColumns);
        List<Column> setColumns = dataRecord.getColumns().stream().filter(Column::isUpdated).collect(Collectors.toList());
        String updateSql = importSQLBuilder.buildUpdateSQL(getSchemaName(dataRecord.getTableName()), dataRecord, conditionColumns);
        try (PreparedStatement preparedStatement = connection.prepareStatement(updateSql)) {
            updateStatement.set(preparedStatement);
            for (int i = 0; i < setColumns.size(); i++) {
                preparedStatement.setObject(i + 1, setColumns.get(i).getValue());
            }
            for (int i = 0; i < conditionColumns.size(); i++) {
                Column keyColumn = conditionColumns.get(i);
                // TODO There to be compatible with PostgreSQL before value is null except primary key and unsupported updating sharding value now.
                if (shardingColumns.contains(keyColumn.getName()) && keyColumn.getOldValue() == null) {
                    preparedStatement.setObject(setColumns.size() + i + 1, keyColumn.getValue());
                    continue;
                }
                preparedStatement.setObject(setColumns.size() + i + 1, keyColumn.getOldValue());
            }
            // TODO if table without unique key the conditionColumns before values is null, so update will fail at PostgreSQL
            int updateCount = preparedStatement.executeUpdate();
            if (1 != updateCount) {
                log.warn("executeUpdate failed, updateCount={}, updateSql={}, updatedColumns={}, conditionColumns={}", updateCount, updateSql, setColumns, conditionColumns);
            }
        } finally {
            updateStatement.set(null);
        }
    }
    
    private void executeBatchDelete(final Connection connection, final List<DataRecord> dataRecords) throws SQLException {
        DataRecord dataRecord = dataRecords.get(0);
        String deleteSQL = importSQLBuilder.buildDeleteSQL(getSchemaName(dataRecord.getTableName()), dataRecord,
                RecordUtils.extractConditionColumns(dataRecord, importerConfig.getShardingColumns(dataRecord.getTableName())));
        try (PreparedStatement preparedStatement = connection.prepareStatement(deleteSQL)) {
            batchDeleteStatement.set(preparedStatement);
            preparedStatement.setQueryTimeout(30);
            for (DataRecord each : dataRecords) {
                List<Column> conditionColumns = RecordUtils.extractConditionColumns(each, importerConfig.getShardingColumns(dataRecord.getTableName()));
                for (int i = 0; i < conditionColumns.size(); i++) {
                    Object oldValue = conditionColumns.get(i).getOldValue();
                    if (null == oldValue) {
                        log.warn("Record old value is null, record={}", each);
                    }
                    preparedStatement.setObject(i + 1, oldValue);
                }
                preparedStatement.addBatch();
            }
            int[] counts = preparedStatement.executeBatch();
            if (IntStream.of(counts).anyMatch(value -> 1 != value)) {
                log.warn("batchDelete failed, counts={}, sql={}, dataRecords={}", Arrays.toString(counts), deleteSQL, dataRecords);
            }
        } finally {
            batchDeleteStatement.set(null);
        }
    }
    
    private void sequentialFlush(final DataSource dataSource, final List<DataRecord> buffer) {
        if (buffer.isEmpty()) {
            return;
        }
        try (Connection connection = dataSource.getConnection()) {
            doFlush(connection, buffer);
        } catch (final SQLException ex) {
            throw new PipelineImporterJobWriteException(ex);
        }
    }
    
    @Override
    public void close() {
        PipelineJdbcUtils.cancelStatement(batchInsertStatement.get());
        PipelineJdbcUtils.cancelStatement(updateStatement.get());
        PipelineJdbcUtils.cancelStatement(batchDeleteStatement.get());
    }
}
