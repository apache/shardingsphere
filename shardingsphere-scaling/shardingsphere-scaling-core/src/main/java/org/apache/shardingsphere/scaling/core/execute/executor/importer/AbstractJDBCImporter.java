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

package org.apache.shardingsphere.scaling.core.execute.executor.importer;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.shardingsphere.scaling.core.config.ImporterConfiguration;
import org.apache.shardingsphere.scaling.core.constant.ScalingConstant;
import org.apache.shardingsphere.scaling.core.datasource.DataSourceManager;
import org.apache.shardingsphere.scaling.core.exception.SyncTaskExecuteException;
import org.apache.shardingsphere.scaling.core.execute.executor.AbstractShardingScalingExecutor;
import org.apache.shardingsphere.scaling.core.execute.executor.channel.Channel;
import org.apache.shardingsphere.scaling.core.execute.executor.record.Column;
import org.apache.shardingsphere.scaling.core.execute.executor.record.DataRecord;
import org.apache.shardingsphere.scaling.core.execute.executor.record.FinishedRecord;
import org.apache.shardingsphere.scaling.core.execute.executor.record.GroupedDataRecord;
import org.apache.shardingsphere.scaling.core.execute.executor.record.Record;
import org.apache.shardingsphere.scaling.core.execute.executor.record.RecordUtil;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Abstract JDBC importer implementation.
 */
@Slf4j
public abstract class AbstractJDBCImporter extends AbstractShardingScalingExecutor implements Importer {
    
    private static final DataRecordMerger MERGER = new DataRecordMerger();
    
    private final ImporterConfiguration importerConfig;
    
    private final DataSourceManager dataSourceManager;
    
    private final AbstractSQLBuilder sqlBuilder;
    
    @Setter
    private Channel channel;
    
    protected AbstractJDBCImporter(final ImporterConfiguration importerConfig, final DataSourceManager dataSourceManager) {
        this.importerConfig = importerConfig;
        this.dataSourceManager = dataSourceManager;
        sqlBuilder = createSQLBuilder(importerConfig.getShardingColumnsMap());
    }
    
    /**
     * Create SQL builder.
     *
     * @param shardingColumnsMap sharding columns map
     * @return SQL builder
     */
    protected abstract AbstractSQLBuilder createSQLBuilder(Map<String, Set<String>> shardingColumnsMap);
    
    @Override
    public final void start() {
        super.start();
        write();
    }
    
    @Override
    public final void write() {
        while (isRunning()) {
            List<Record> records = channel.fetchRecords(1024, 3);
            if (null != records && !records.isEmpty()) {
                flush(dataSourceManager.getDataSource(importerConfig.getDataSourceConfig()), records);
                if (FinishedRecord.class.equals(records.get(records.size() - 1).getClass())) {
                    channel.ack();
                    break;
                }
            }
            channel.ack();
        }
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
            throw new SyncTaskExecuteException("write failed.");
        }
    }
    
    private boolean tryFlush(final DataSource dataSource, final List<DataRecord> buffer) {
        int retryTimes = importerConfig.getRetryTimes();
        do {
            try {
                doFlush(dataSource, buffer);
                return true;
            } catch (final SQLException ex) {
                log.error("flush failed: ", ex);
            }
        } while (isRunning() && retryTimes-- > 0);
        return false;
    }
    
    private void doFlush(final DataSource dataSource, final List<DataRecord> buffer) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            switch (buffer.get(0).getType()) {
                case ScalingConstant.INSERT:
                    executeBatchInsert(connection, buffer);
                    break;
                case ScalingConstant.UPDATE:
                    executeUpdate(connection, buffer);
                    break;
                case ScalingConstant.DELETE:
                    executeBatchDelete(connection, buffer);
                    break;
                default:
                    break;
            }
            connection.commit();
        }
    }
    
    private void executeBatchInsert(final Connection connection, final List<DataRecord> dataRecords) throws SQLException {
        String insertSql = sqlBuilder.buildInsertSQL(dataRecords.get(0));
        PreparedStatement ps = connection.prepareStatement(insertSql);
        ps.setQueryTimeout(30);
        for (DataRecord each : dataRecords) {
            for (int i = 0; i < each.getColumnCount(); i++) {
                ps.setObject(i + 1, each.getColumn(i).getValue());
            }
            ps.addBatch();
        }
        ps.executeBatch();
    }
    
    private void executeUpdate(final Connection connection, final List<DataRecord> dataRecords) throws SQLException {
        for (DataRecord each : dataRecords) {
            executeUpdate(connection, each);
        }
    }
    
    private void executeUpdate(final Connection connection, final DataRecord record) throws SQLException {
        List<Column> conditionColumns = RecordUtil.extractConditionColumns(record, importerConfig.getShardingColumnsMap().get(record.getTableName()));
        List<Column> updatedColumns = RecordUtil.extractUpdatedColumns(record);
        String updateSql = sqlBuilder.buildUpdateSQL(record, conditionColumns);
        PreparedStatement ps = connection.prepareStatement(updateSql);
        for (int i = 0; i < updatedColumns.size(); i++) {
            ps.setObject(i + 1, updatedColumns.get(i).getValue());
        }
        for (int i = 0; i < conditionColumns.size(); i++) {
            Column keyColumn = conditionColumns.get(i);
            ps.setObject(updatedColumns.size() + i + 1,
                    // sharding column can not be updated
                    (keyColumn.isPrimaryKey() && keyColumn.isUpdated()) ? keyColumn.getOldValue() : keyColumn.getValue());
        }
        ps.execute();
    }
    
    private void executeBatchDelete(final Connection connection, final List<DataRecord> dataRecords) throws SQLException {
        List<Column> conditionColumns = RecordUtil.extractConditionColumns(dataRecords.get(0), importerConfig.getShardingColumnsMap().get(dataRecords.get(0).getTableName()));
        String deleteSQL = sqlBuilder.buildDeleteSQL(dataRecords.get(0), conditionColumns);
        PreparedStatement ps = connection.prepareStatement(deleteSQL);
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
