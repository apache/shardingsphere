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

package org.apache.shardingsphere.data.pipeline.core.importer.sink.type;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.core.constant.PipelineSQLOperationType;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.exception.job.PipelineImporterJobWriteException;
import org.apache.shardingsphere.data.pipeline.core.importer.ImporterConfiguration;
import org.apache.shardingsphere.data.pipeline.core.importer.sink.PipelineSink;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.Column;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.Record;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.RecordUtils;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.group.DataRecordGroupEngine;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.group.GroupedDataRecord;
import org.apache.shardingsphere.data.pipeline.core.job.progress.listener.PipelineJobUpdateProgress;
import org.apache.shardingsphere.data.pipeline.core.sqlbuilder.sql.PipelineImportSQLBuilder;
import org.apache.shardingsphere.data.pipeline.core.util.PipelineJdbcUtils;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.util.json.JsonUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Pipeline data source sink.
 */
@HighFrequencyInvocation
@Slf4j
public final class PipelineDataSourceSink implements PipelineSink {
    
    private final ImporterConfiguration importerConfig;
    
    private final DataSource dataSource;
    
    private final PipelineImportSQLBuilder importSQLBuilder;
    
    private final DataRecordGroupEngine groupEngine;
    
    private final AtomicReference<PreparedStatement> runningStatement;
    
    public PipelineDataSourceSink(final ImporterConfiguration importerConfig, final PipelineDataSourceManager dataSourceManager) {
        this.importerConfig = importerConfig;
        dataSource = dataSourceManager.getDataSource(importerConfig.getDataSourceConfig());
        importSQLBuilder = new PipelineImportSQLBuilder(importerConfig.getDataSourceConfig().getDatabaseType());
        groupEngine = new DataRecordGroupEngine();
        runningStatement = new AtomicReference<>();
    }
    
    @Override
    public PipelineJobUpdateProgress write(final String ackId, final Collection<Record> records) {
        List<DataRecord> dataRecords = records.stream().filter(DataRecord.class::isInstance).map(DataRecord.class::cast).collect(Collectors.toList());
        if (dataRecords.isEmpty()) {
            return new PipelineJobUpdateProgress(0);
        }
        if (dataRecords.iterator().next().getUniqueKeyValue().isEmpty()) {
            sequentialWrite(dataRecords);
            return new PipelineJobUpdateProgress(dataRecords.size());
        }
        for (GroupedDataRecord each : groupEngine.group(dataRecords)) {
            batchWrite(each.getDeleteDataRecords());
            batchWrite(each.getInsertDataRecords());
            batchWrite(each.getUpdateDataRecords());
        }
        return new PipelineJobUpdateProgress((int) dataRecords.stream().filter(each -> PipelineSQLOperationType.INSERT == each.getType()).count());
    }
    
    private void sequentialWrite(final List<DataRecord> buffer) {
        // TODO It's better to use transaction, but delete operation may not take effect on PostgreSQL sometimes
        try {
            for (DataRecord each : buffer) {
                doWrite(Collections.singletonList(each), true);
            }
        } catch (final SQLException ex) {
            throw new PipelineImporterJobWriteException(ex);
        }
    }
    
    @SuppressWarnings("BusyWait")
    @SneakyThrows(InterruptedException.class)
    private void batchWrite(final Collection<DataRecord> records) {
        if (records.isEmpty()) {
            return;
        }
        for (int i = 0; !Thread.interrupted() && i <= importerConfig.getRetryTimes(); i++) {
            try {
                doWrite(records, 0 == i);
                break;
            } catch (final SQLException ex) {
                log.error("Flush failed {}/{} times.", i, importerConfig.getRetryTimes(), ex);
                if (i == importerConfig.getRetryTimes()) {
                    throw new PipelineImporterJobWriteException(ex);
                }
                Thread.sleep(Math.min(5L * 60L * 1000L, 1000L << i));
            }
        }
    }
    
    private void doWrite(final Collection<DataRecord> records, final boolean firstTimeRun) throws SQLException {
        switch (records.iterator().next().getType()) {
            case INSERT:
                Optional.ofNullable(importerConfig.getRateLimitAlgorithm()).ifPresent(optional -> optional.intercept(PipelineSQLOperationType.INSERT, 1));
                executeBatchInsert(records, firstTimeRun);
                break;
            case UPDATE:
                Optional.ofNullable(importerConfig.getRateLimitAlgorithm()).ifPresent(optional -> optional.intercept(PipelineSQLOperationType.UPDATE, 1));
                executeUpdate(records, firstTimeRun);
                break;
            case DELETE:
                Optional.ofNullable(importerConfig.getRateLimitAlgorithm()).ifPresent(optional -> optional.intercept(PipelineSQLOperationType.DELETE, 1));
                executeBatchDelete(records);
                break;
            default:
                break;
        }
    }
    
    private void executeBatchInsert(final Collection<DataRecord> dataRecords, final boolean firstTimeRun) throws SQLException {
        DataRecord dataRecord = dataRecords.iterator().next();
        String sql = importSQLBuilder.buildInsertSQL(importerConfig.findSchemaName(dataRecord.getTableName()).orElse(null), dataRecord);
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            runningStatement.set(preparedStatement);
            if (firstTimeRun) {
                executeBatchInsertFirstTime(connection, preparedStatement, dataRecords);
            } else {
                retryBatchInsert(preparedStatement, dataRecords);
            }
        } finally {
            runningStatement.set(null);
        }
    }
    
    private void executeBatchInsertFirstTime(final Connection connection, final PreparedStatement preparedStatement, final Collection<DataRecord> dataRecords) throws SQLException {
        boolean transactionEnabled = dataRecords.size() > 1;
        if (transactionEnabled) {
            connection.setAutoCommit(false);
        }
        preparedStatement.setQueryTimeout(30);
        for (DataRecord each : dataRecords) {
            for (int i = 0; i < each.getColumnCount(); i++) {
                preparedStatement.setObject(i + 1, each.getColumn(i).getValue());
            }
            preparedStatement.addBatch();
        }
        preparedStatement.executeBatch();
        if (transactionEnabled) {
            connection.commit();
        }
    }
    
    private void retryBatchInsert(final PreparedStatement preparedStatement, final Collection<DataRecord> dataRecords) throws SQLException {
        for (DataRecord each : dataRecords) {
            for (int i = 0; i < each.getColumnCount(); i++) {
                preparedStatement.setObject(i + 1, each.getColumn(i).getValue());
            }
            preparedStatement.executeUpdate();
        }
    }
    
    private void executeUpdate(final Collection<DataRecord> dataRecords, final boolean firstTimeRun) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            boolean transactionEnabled = dataRecords.size() > 1 && firstTimeRun;
            if (transactionEnabled) {
                connection.setAutoCommit(false);
            }
            for (DataRecord each : dataRecords) {
                executeUpdate(connection, each);
            }
            if (transactionEnabled) {
                connection.commit();
            }
        }
    }
    
    private void executeUpdate(final Connection connection, final DataRecord dataRecord) throws SQLException {
        Collection<String> shardingColumns = importerConfig.getShardingColumns(dataRecord.getTableName());
        List<Column> conditionColumns = RecordUtils.extractConditionColumns(dataRecord, shardingColumns);
        List<Column> setColumns = dataRecord.getColumns().stream().filter(Column::isUpdated).collect(Collectors.toList());
        String sql = importSQLBuilder.buildUpdateSQL(importerConfig.findSchemaName(dataRecord.getTableName()).orElse(null), dataRecord, conditionColumns);
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            runningStatement.set(preparedStatement);
            for (int i = 0; i < setColumns.size(); i++) {
                preparedStatement.setObject(i + 1, setColumns.get(i).getValue());
            }
            for (int i = 0; i < conditionColumns.size(); i++) {
                Column keyColumn = conditionColumns.get(i);
                // TODO There to be compatible with PostgreSQL before value is null except primary key and unsupported updating sharding value now.
                if (shardingColumns.contains(keyColumn.getName()) && null == keyColumn.getOldValue()) {
                    preparedStatement.setObject(setColumns.size() + i + 1, keyColumn.getValue());
                    continue;
                }
                preparedStatement.setObject(setColumns.size() + i + 1, keyColumn.getOldValue());
            }
            // TODO if table without unique key the conditionColumns before values is null, so update will fail at PostgreSQL
            int updateCount = preparedStatement.executeUpdate();
            if (1 != updateCount) {
                log.warn("Update failed, update count: {}, sql: {}, set columns: {}, sharding columns: {}, condition columns: {}",
                        updateCount, sql, setColumns, JsonUtils.toJsonString(shardingColumns), JsonUtils.toJsonString(conditionColumns));
            }
        } catch (final SQLException ex) {
            log.error("execute update failed, sql: {}, set columns: {}, sharding columns: {}, condition columns: {}, error message: {}, data record: {}",
                    sql, setColumns, JsonUtils.toJsonString(shardingColumns), JsonUtils.toJsonString(conditionColumns), ex.getMessage(), dataRecord);
            throw ex;
        } finally {
            runningStatement.set(null);
        }
    }
    
    private void executeBatchDelete(final Collection<DataRecord> dataRecords) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            boolean transactionEnabled = dataRecords.size() > 1;
            if (transactionEnabled) {
                connection.setAutoCommit(false);
            }
            executeBatchDelete(connection, dataRecords, importerConfig.getShardingColumns(dataRecords.iterator().next().getTableName()));
            if (transactionEnabled) {
                connection.commit();
            }
        }
    }
    
    private void executeBatchDelete(final Connection connection, final Collection<DataRecord> dataRecords, final Collection<String> shardingColumns) throws SQLException {
        DataRecord dataRecord = dataRecords.iterator().next();
        String deleteSQL = importSQLBuilder.buildDeleteSQL(importerConfig.findSchemaName(dataRecord.getTableName()).orElse(null), dataRecord,
                RecordUtils.extractConditionColumns(dataRecord, shardingColumns));
        try (PreparedStatement preparedStatement = connection.prepareStatement(deleteSQL)) {
            runningStatement.set(preparedStatement);
            preparedStatement.setQueryTimeout(30);
            for (DataRecord each : dataRecords) {
                List<Column> conditionColumns = RecordUtils.extractConditionColumns(each, importerConfig.getShardingColumns(each.getTableName()));
                for (int i = 0; i < conditionColumns.size(); i++) {
                    Object oldValue = conditionColumns.get(i).getOldValue();
                    if (null == oldValue) {
                        log.warn("Record old value is null, record: {}", each);
                    }
                    preparedStatement.setObject(i + 1, oldValue);
                }
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        } finally {
            runningStatement.set(null);
        }
    }
    
    @Override
    public void close() {
        Optional.ofNullable(runningStatement.get()).ifPresent(PipelineJdbcUtils::cancelStatement);
    }
}
