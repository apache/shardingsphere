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

package org.apache.shardingsphere.data.pipeline.core.importer;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.config.ImporterConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.api.executor.AbstractLifecycleExecutor;
import org.apache.shardingsphere.data.pipeline.api.importer.Importer;
import org.apache.shardingsphere.data.pipeline.api.ingest.channel.PipelineChannel;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.Column;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.FinishedRecord;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.GroupedDataRecord;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.Record;
import org.apache.shardingsphere.data.pipeline.api.job.JobOperationType;
import org.apache.shardingsphere.data.pipeline.api.job.progress.listener.PipelineJobProgressUpdatedParameter;
import org.apache.shardingsphere.data.pipeline.api.job.progress.listener.PipelineJobProgressListener;
import org.apache.shardingsphere.data.pipeline.api.metadata.LogicTableName;
import org.apache.shardingsphere.data.pipeline.core.exception.job.PipelineImporterJobWriteException;
import org.apache.shardingsphere.data.pipeline.core.ingest.IngestDataChangeType;
import org.apache.shardingsphere.data.pipeline.core.record.RecordUtil;
import org.apache.shardingsphere.data.pipeline.core.sqlbuilder.PipelineSQLBuilderFactory;
import org.apache.shardingsphere.data.pipeline.core.util.ThreadUtil;
import org.apache.shardingsphere.data.pipeline.spi.ratelimit.JobRateLimitAlgorithm;
import org.apache.shardingsphere.data.pipeline.spi.sqlbuilder.PipelineSQLBuilder;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Default importer.
 */
@Slf4j
public final class DefaultImporter extends AbstractLifecycleExecutor implements Importer {
    
    private static final DataRecordMerger MERGER = new DataRecordMerger();
    
    @Getter(AccessLevel.PROTECTED)
    private final ImporterConfiguration importerConfig;
    
    private final PipelineDataSourceManager dataSourceManager;
    
    private final PipelineSQLBuilder pipelineSqlBuilder;
    
    private final PipelineChannel channel;
    
    private final PipelineJobProgressListener jobProgressListener;
    
    private final JobRateLimitAlgorithm rateLimitAlgorithm;
    
    public DefaultImporter(final ImporterConfiguration importerConfig, final PipelineDataSourceManager dataSourceManager, final PipelineChannel channel,
                           final PipelineJobProgressListener jobProgressListener) {
        this.importerConfig = importerConfig;
        rateLimitAlgorithm = importerConfig.getRateLimitAlgorithm();
        this.dataSourceManager = dataSourceManager;
        this.channel = channel;
        pipelineSqlBuilder = PipelineSQLBuilderFactory.getInstance(importerConfig.getDataSourceConfig().getDatabaseType().getType());
        this.jobProgressListener = jobProgressListener;
    }
    
    @Override
    protected void runBlocking() {
        write();
    }
    
    private void write() {
        log.info("importer write");
        int round = 1;
        int rowCount = 0;
        boolean finishedByBreak = false;
        int batchSize = importerConfig.getBatchSize() * 2;
        while (isRunning()) {
            List<Record> records = channel.fetchRecords(batchSize, 3);
            if (null != records && !records.isEmpty()) {
                round++;
                rowCount += records.size();
                PipelineJobProgressUpdatedParameter updatedParameter = flush(dataSourceManager.getDataSource(importerConfig.getDataSourceConfig()), records);
                channel.ack(records);
                jobProgressListener.onProgressUpdated(updatedParameter);
                if (0 == round % 50) {
                    log.info("importer write, round={}, rowCount={}", round, rowCount);
                }
                if (FinishedRecord.class.equals(records.get(records.size() - 1).getClass())) {
                    log.info("write, get FinishedRecord, break");
                    finishedByBreak = true;
                    break;
                }
            }
        }
        log.info("importer write done, rowCount={}, finishedByBreak={}", rowCount, finishedByBreak);
    }
    
    private PipelineJobProgressUpdatedParameter flush(final DataSource dataSource, final List<Record> buffer) {
        List<GroupedDataRecord> result = MERGER.group(buffer.stream().filter(each -> each instanceof DataRecord).map(each -> (DataRecord) each).collect(Collectors.toList()));
        int insertRecordNumber = 0;
        int deleteRecordNumber = 0;
        for (GroupedDataRecord each : result) {
            deleteRecordNumber += null != each.getDeleteDataRecords() ? each.getDeleteDataRecords().size() : 0;
            flushInternal(dataSource, each.getDeleteDataRecords());
            insertRecordNumber += null != each.getInsertDataRecords() ? each.getInsertDataRecords().size() : 0;
            flushInternal(dataSource, each.getInsertDataRecords());
            flushInternal(dataSource, each.getUpdateDataRecords());
        }
        return new PipelineJobProgressUpdatedParameter(insertRecordNumber, deleteRecordNumber);
    }
    
    private void flushInternal(final DataSource dataSource, final List<DataRecord> buffer) {
        if (null == buffer || buffer.isEmpty()) {
            return;
        }
        boolean success = tryFlush(dataSource, buffer);
        ShardingSpherePreconditions.checkState(!isRunning() || success, PipelineImporterJobWriteException::new);
    }
    
    private boolean tryFlush(final DataSource dataSource, final List<DataRecord> buffer) {
        for (int i = 0; isRunning() && i <= importerConfig.getRetryTimes(); i++) {
            try {
                doFlush(dataSource, buffer);
                return true;
            } catch (final SQLException ex) {
                log.error("flush failed {}/{} times", i, importerConfig.getRetryTimes(), ex);
                ThreadUtil.sleep(Math.min(5 * 60 * 1000L, 1000L << i));
            }
        }
        return false;
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
    
    private void executeBatchInsert(final Connection connection, final List<DataRecord> dataRecords) throws SQLException {
        DataRecord dataRecord = dataRecords.get(0);
        String insertSql = pipelineSqlBuilder.buildInsertSQL(getSchemaName(dataRecord.getTableName()), dataRecord, importerConfig.getShardingColumnsMap());
        try (PreparedStatement ps = connection.prepareStatement(insertSql)) {
            ps.setQueryTimeout(30);
            for (DataRecord each : dataRecords) {
                for (int i = 0; i < each.getColumnCount(); i++) {
                    ps.setObject(i + 1, each.getColumn(i).getValue());
                }
                ps.addBatch();
            }
            ps.executeBatch();
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
    
    private void executeUpdate(final Connection connection, final DataRecord record) throws SQLException {
        Set<String> shardingColumns = importerConfig.getShardingColumns(record.getTableName());
        if (null == shardingColumns) {
            log.error("executeUpdate, could not get shardingColumns, tableName={}, logicTableNames={}", record.getTableName(), importerConfig.getLogicTableNames());
        }
        List<Column> conditionColumns = RecordUtil.extractConditionColumns(record, shardingColumns);
        List<Column> updatedColumns = pipelineSqlBuilder.extractUpdatedColumns(record, importerConfig.getShardingColumnsMap());
        String updateSql = pipelineSqlBuilder.buildUpdateSQL(getSchemaName(record.getTableName()), record, conditionColumns, importerConfig.getShardingColumnsMap());
        try (PreparedStatement ps = connection.prepareStatement(updateSql)) {
            for (int i = 0; i < updatedColumns.size(); i++) {
                ps.setObject(i + 1, updatedColumns.get(i).getValue());
            }
            for (int i = 0; i < conditionColumns.size(); i++) {
                Column keyColumn = conditionColumns.get(i);
                ps.setObject(updatedColumns.size() + i + 1, (keyColumn.isUniqueKey() && keyColumn.isUpdated()) ? keyColumn.getOldValue() : keyColumn.getValue());
            }
            int updateCount = ps.executeUpdate();
            if (1 != updateCount) {
                log.warn("executeUpdate failed, updateCount={}, updateSql={}, updatedColumns={}, conditionColumns={}", updateCount, updateSql, updatedColumns, conditionColumns);
            }
        }
    }
    
    private void executeBatchDelete(final Connection connection, final List<DataRecord> dataRecords) throws SQLException {
        DataRecord dataRecord = dataRecords.get(0);
        List<Column> conditionColumns = RecordUtil.extractConditionColumns(dataRecord, importerConfig.getShardingColumns(dataRecord.getTableName()));
        String deleteSQL = pipelineSqlBuilder.buildDeleteSQL(getSchemaName(dataRecord.getTableName()), dataRecord, conditionColumns);
        try (PreparedStatement ps = connection.prepareStatement(deleteSQL)) {
            ps.setQueryTimeout(30);
            for (DataRecord each : dataRecords) {
                conditionColumns = RecordUtil.extractConditionColumns(each, importerConfig.getShardingColumns(each.getTableName()));
                for (int i = 0; i < conditionColumns.size(); i++) {
                    ps.setObject(i + 1, conditionColumns.get(i).getValue());
                }
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
    
    @Override
    protected void doStop() {
    }
}
