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
import org.apache.shardingsphere.shardingscaling.core.execute.executor.SyncRunnerGroup;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.channel.AckCallback;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.channel.MemoryChannel;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.reader.Reader;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.reader.ReaderFactory;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.record.DataRecord;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.record.Record;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.writer.Writer;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.writer.WriterFactory;
import org.apache.shardingsphere.shardingscaling.core.synctask.SyncTask;
import org.apache.shardingsphere.shardingscaling.core.util.DataSourceFactory;
import org.apache.shardingsphere.spi.database.DataSourceMetaData;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Table slice execute task.
 *
 * @author avalon566
 * @author yangyi
 */
@Slf4j
public final class HistoryDataSyncTask implements SyncTask {

    private final SyncConfiguration syncConfiguration;
    
    private final DataSourceFactory dataSourceFactory;
    
    private final String syncTaskId;

    private long estimatedRows;

    private AtomicLong syncedRows = new AtomicLong();
    
    private Reader reader;
    
    public HistoryDataSyncTask(final SyncConfiguration syncConfiguration, final DataSourceFactory dataSourceFactory) {
        this.syncConfiguration = syncConfiguration;
        this.dataSourceFactory = dataSourceFactory;
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
        SyncRunnerGroup syncRunnerGroup = new SyncRunnerGroup(new SyncTaskExecuteCallback(this.getClass().getSimpleName(), syncTaskId, callback));
        instanceSyncRunners(syncRunnerGroup);
        ScalingContext.getInstance().getSyncTaskExecuteEngine().submitGroup(syncRunnerGroup);
    }
    
    private void getEstimatedRows() {
        DataSource dataSource = dataSourceFactory.getDataSource(syncConfiguration.getReaderConfiguration().getDataSourceConfiguration());
        try (Connection connection = dataSource.getConnection()) {
            ResultSet resultSet = connection.prepareStatement(String.format("select count(*) from %s %s",
                    syncConfiguration.getReaderConfiguration().getTableName(),
                    syncConfiguration.getReaderConfiguration().getWhereCondition()))
                    .executeQuery();
            resultSet.next();
            estimatedRows = resultSet.getInt(1);
        } catch (SQLException e) {
            throw new SyncTaskExecuteException("get estimated rows error.", e);
        }
    }
    
    private void instanceSyncRunners(final SyncRunnerGroup syncRunnerGroup) {
        syncConfiguration.getReaderConfiguration().setTableNameMap(syncConfiguration.getTableNameMap());
        reader = ReaderFactory.newInstanceJdbcReader(syncConfiguration.getReaderConfiguration(), dataSourceFactory);
        Writer writer = WriterFactory.newInstance(syncConfiguration.getWriterConfiguration(), dataSourceFactory);
        MemoryChannel channel = instanceChannel();
        reader.setChannel(channel);
        writer.setChannel(channel);
        syncRunnerGroup.setChannel(channel);
        syncRunnerGroup.addSyncRunner(reader);
        syncRunnerGroup.addSyncRunner(writer);
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
