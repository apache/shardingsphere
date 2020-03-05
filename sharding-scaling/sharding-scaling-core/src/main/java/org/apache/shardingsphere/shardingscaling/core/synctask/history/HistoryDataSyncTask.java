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

package org.apache.shardingsphere.shardingscaling.core.synctask.history;

import lombok.extern.slf4j.Slf4j;

import org.apache.shardingsphere.shardingscaling.core.config.RdbmsConfiguration;
import org.apache.shardingsphere.shardingscaling.core.config.ScalingContext;
import org.apache.shardingsphere.shardingscaling.core.config.SyncConfiguration;
import org.apache.shardingsphere.shardingscaling.core.controller.task.ReportCallback;
import org.apache.shardingsphere.shardingscaling.core.controller.SyncProgress;
import org.apache.shardingsphere.shardingscaling.core.exception.SyncTaskExecuteException;
import org.apache.shardingsphere.shardingscaling.core.execute.engine.SyncTaskExecuteCallback;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.SyncExecutorGroup;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.channel.AckCallback;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.channel.MemoryChannel;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.reader.Reader;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.reader.ReaderFactory;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.record.DataRecord;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.record.Record;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.writer.Writer;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.writer.WriterFactory;
import org.apache.shardingsphere.shardingscaling.core.synctask.SyncTask;
import org.apache.shardingsphere.shardingscaling.core.datasource.DataSourceManager;
import org.apache.shardingsphere.spi.database.metadata.DataSourceMetaData;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Table slice execute task.
 */
@Slf4j
public final class HistoryDataSyncTask implements SyncTask {
    
    private final SyncConfiguration syncConfiguration;
    
    private final DataSourceManager dataSourceManager;
    
    private final String syncTaskId;
    
    private long estimatedRows;
    
    private AtomicLong syncedRows = new AtomicLong();
    
    private Reader reader;
    
    public HistoryDataSyncTask(final SyncConfiguration syncConfiguration, final DataSourceManager dataSourceManager) {
        this.syncConfiguration = syncConfiguration;
        this.dataSourceManager = dataSourceManager;
        syncTaskId = generateSyncTaskId(syncConfiguration.getReaderConfiguration());
    }
    
    private String generateSyncTaskId(final RdbmsConfiguration readerConfiguration) {
        DataSourceMetaData dataSourceMetaData = readerConfiguration.getDataSourceConfiguration().getDataSourceMetaData();
        String result = String.format("history-%s-%s", null != dataSourceMetaData.getCatalog() ? dataSourceMetaData.getCatalog() : dataSourceMetaData.getSchema(), readerConfiguration.getTableName());
        return null == readerConfiguration.getWhereCondition() ? result : result + "#" + readerConfiguration.getSpiltNum();
    }
    
    @Override
    public void prepare() {
    }
    
    @Override
    public void start(final ReportCallback callback) {
        getEstimatedRows();
        SyncExecutorGroup syncExecutorGroup = new SyncExecutorGroup(new SyncTaskExecuteCallback(this.getClass().getSimpleName(), syncTaskId, callback));
        instanceSyncExecutors(syncExecutorGroup);
        ScalingContext.getInstance().getSyncTaskExecuteEngine().submitGroup(syncExecutorGroup);
    }
    
    private void getEstimatedRows() {
        DataSource dataSource = dataSourceManager.getDataSource(syncConfiguration.getReaderConfiguration().getDataSourceConfiguration());
        try (Connection connection = dataSource.getConnection()) {
            ResultSet resultSet = connection.prepareStatement(String.format("SELECT COUNT(*) FROM %s %s",
                    syncConfiguration.getReaderConfiguration().getTableName(),
                    syncConfiguration.getReaderConfiguration().getWhereCondition()))
                    .executeQuery();
            resultSet.next();
            estimatedRows = resultSet.getInt(1);
        } catch (SQLException e) {
            throw new SyncTaskExecuteException("get estimated rows error.", e);
        }
    }
    
    private void instanceSyncExecutors(final SyncExecutorGroup syncExecutorGroup) {
        syncConfiguration.getReaderConfiguration().setTableNameMap(syncConfiguration.getTableNameMap());
        reader = ReaderFactory.newInstanceJdbcReader(syncConfiguration.getReaderConfiguration(), dataSourceManager);
        Writer writer = WriterFactory.newInstance(syncConfiguration.getWriterConfiguration(), dataSourceManager);
        MemoryChannel channel = instanceChannel();
        reader.setChannel(channel);
        writer.setChannel(channel);
        syncExecutorGroup.setChannel(channel);
        syncExecutorGroup.addSyncExecutor(reader);
        syncExecutorGroup.addSyncExecutor(writer);
    }
    
    private MemoryChannel instanceChannel() {
        return new MemoryChannel(new AckCallback() {
    
            @Override
            public void onAck(final List<Record> records) {
                int count = 0;
                for (Record record : records) {
                    if (DataRecord.class.equals(record.getClass())) {
                        count++;
                    }
                }
                syncedRows.addAndGet(count);
            }
        });
    }
    
    @Override
    public void stop() {
        if (null != reader) {
            reader.stop();
            reader = null;
        }
    }
    
    @Override
    public SyncProgress getProgress() {
        return new HistoryDataSyncTaskProgress(syncTaskId, estimatedRows, syncedRows.get());
    }
}
