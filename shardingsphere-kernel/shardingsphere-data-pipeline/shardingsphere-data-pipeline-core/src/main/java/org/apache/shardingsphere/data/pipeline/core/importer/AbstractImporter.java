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

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.ImporterConfiguration;
import org.apache.shardingsphere.data.pipeline.api.executor.AbstractLifecycleExecutor;
import org.apache.shardingsphere.data.pipeline.api.ingest.channel.Channel;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.Column;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.FinishedRecord;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.GroupedDataRecord;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.Record;
import org.apache.shardingsphere.data.pipeline.core.datasource.DataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineJobExecutionException;
import org.apache.shardingsphere.data.pipeline.core.ingest.IngestDataChangeType;
import org.apache.shardingsphere.data.pipeline.core.record.RecordUtil;
import org.apache.shardingsphere.data.pipeline.core.util.ThreadUtil;
import org.apache.shardingsphere.data.pipeline.spi.importer.Importer;
import org.apache.shardingsphere.data.pipeline.spi.importer.ImporterListener;
import org.apache.shardingsphere.data.pipeline.spi.sqlbuilder.PipelineSQLBuilder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Abstract importer.
 */
@Slf4j
public abstract class AbstractImporter extends AbstractLifecycleExecutor implements Importer {
    
    private static final DataRecordMerger MERGER = new DataRecordMerger();
    
    private final ImporterConfiguration importerConfig;
    
    private final DataSourceManager dataSourceManager;
    
    private final PipelineSQLBuilder pipelineSqlBuilder;
    
    @Setter
    private Channel channel;
    
    @Setter
    private ImporterListener importerListener;
    
    protected AbstractImporter(final ImporterConfiguration importerConfig, final DataSourceManager dataSourceManager) {
        this.importerConfig = importerConfig;
        this.dataSourceManager = dataSourceManager;
        pipelineSqlBuilder = createSQLBuilder(importerConfig.getShardingColumnsMap());
    }
    
    /**
     * Create SQL builder.
     *
     * @param shardingColumnsMap sharding columns map
     * @return SQL builder
     */
    protected abstract PipelineSQLBuilder createSQLBuilder(Map<String, Set<String>> shardingColumnsMap);
    
    @Override
    public final void start() {
        super.start();
        write();
    }
    
    @Override
    public final void write() {
        log.info("importer write");
        int rowCount = 0;
        while (isRunning()) {
            List<Record> records = channel.fetchRecords(1024, 3);
            if (null != records && !records.isEmpty()) {
                rowCount += records.size();
                flush(dataSourceManager.getDataSource(importerConfig.getDataSourceConfig()), records);
                if (null != importerListener) {
                    importerListener.recordsImported(records);
                }
                if (FinishedRecord.class.equals(records.get(records.size() - 1).getClass())) {
                    channel.ack();
                    break;
                }
            }
            channel.ack();
        }
        log.info("importer write, rowCount={}", rowCount);
    }
    
    private void flush(final DataSource dataSource, final List<Record> buffer) {
        List<GroupedDataRecord> groupedDataRecords = MERGER.group(buffer.stream()
                .filter(each -> each instanceof DataRecord)
                .map(each -> (DataRecord) each)
                .collect(Collectors.toList()));
        groupedDataRecords.forEach(each -> {
            if (CollectionUtils.isNotEmpty(each.getDeleteDataRecords())) {
                flushInternal(dataSource, each.getDeleteDataRecords());
            }
            if (CollectionUtils.isNotEmpty(each.getInsertDataRecords())) {
                flushInternal(dataSource, each.getInsertDataRecords());
            }
            if (CollectionUtils.isNotEmpty(each.getUpdateDataRecords())) {
                flushInternal(dataSource, each.getUpdateDataRecords());
            }
        });
    }
    
    private void flushInternal(final DataSource dataSource, final List<DataRecord> buffer) {
        boolean success = tryFlush(dataSource, buffer);
        if (isRunning() && !success) {
            throw new PipelineJobExecutionException("write failed.");
        }
    }
    
    private boolean tryFlush(final DataSource dataSource, final List<DataRecord> buffer) {
        for (int i = 0; isRunning() && i <= importerConfig.getRetryTimes(); i++) {
            try {
                doFlush(dataSource, buffer);
                return true;
            } catch (final SQLException ex) {
                log.error("flush failed {}/{} times.", i, importerConfig.getRetryTimes(), ex);
                ThreadUtil.sleep(Math.min(5 * 60 * 1000L, 1000 << i));
            }
        }
        return false;
    }
    
    private void doFlush(final DataSource dataSource, final List<DataRecord> buffer) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            switch (buffer.get(0).getType()) {
                case IngestDataChangeType.INSERT:
                    executeBatchInsert(connection, buffer);
                    break;
                case IngestDataChangeType.UPDATE:
                    executeUpdate(connection, buffer);
                    break;
                case IngestDataChangeType.DELETE:
                    executeBatchDelete(connection, buffer);
                    break;
                default:
                    break;
            }
            connection.commit();
        }
    }
    
    private void executeBatchInsert(final Connection connection, final List<DataRecord> dataRecords) throws SQLException {
        String insertSql = pipelineSqlBuilder.buildInsertSQL(dataRecords.get(0));
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
    
    private void executeUpdate(final Connection connection, final List<DataRecord> dataRecords) throws SQLException {
        for (DataRecord each : dataRecords) {
            executeUpdate(connection, each);
        }
    }
    
    private void executeUpdate(final Connection connection, final DataRecord record) throws SQLException {
        List<Column> conditionColumns = RecordUtil.extractConditionColumns(record, importerConfig.getShardingColumnsMap().get(record.getTableName()));
        List<Column> updatedColumns = pipelineSqlBuilder.extractUpdatedColumns(record.getColumns(), record);
        String updateSql = pipelineSqlBuilder.buildUpdateSQL(record, conditionColumns);
        try (PreparedStatement ps = connection.prepareStatement(updateSql)) {
            for (int i = 0; i < updatedColumns.size(); i++) {
                ps.setObject(i + 1, updatedColumns.get(i).getValue());
            }
            for (int i = 0; i < conditionColumns.size(); i++) {
                Column keyColumn = conditionColumns.get(i);
                ps.setObject(updatedColumns.size() + i + 1, (keyColumn.isPrimaryKey() && keyColumn.isUpdated()) ? keyColumn.getOldValue() : keyColumn.getValue());
            }
            ps.execute();
        }
    }
    
    private void executeBatchDelete(final Connection connection, final List<DataRecord> dataRecords) throws SQLException {
        List<Column> conditionColumns = RecordUtil.extractConditionColumns(dataRecords.get(0), importerConfig.getShardingColumnsMap().get(dataRecords.get(0).getTableName()));
        String deleteSQL = pipelineSqlBuilder.buildDeleteSQL(dataRecords.get(0), conditionColumns);
        try (PreparedStatement ps = connection.prepareStatement(deleteSQL)) {
            ps.setQueryTimeout(30);
            for (DataRecord each : dataRecords) {
                conditionColumns = RecordUtil.extractConditionColumns(each, importerConfig.getShardingColumnsMap().get(each.getTableName()));
                for (int i = 0; i < conditionColumns.size(); i++) {
                    ps.setObject(i + 1, conditionColumns.get(i).getValue());
                }
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
}
