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

import org.apache.shardingsphere.shardingscaling.core.config.RdbmsConfiguration;
import org.apache.shardingsphere.shardingscaling.core.config.SyncConfiguration;
import org.apache.shardingsphere.shardingscaling.core.controller.task.ReportCallback;
import org.apache.shardingsphere.shardingscaling.core.controller.SyncProgress;
import org.apache.shardingsphere.shardingscaling.core.exception.SyncTaskExecuteException;
import org.apache.shardingsphere.shardingscaling.core.execute.Event;
import org.apache.shardingsphere.shardingscaling.core.execute.EventType;
import org.apache.shardingsphere.shardingscaling.core.synctask.DefaultSyncTaskFactory;
import org.apache.shardingsphere.shardingscaling.core.synctask.SyncTask;
import org.apache.shardingsphere.shardingscaling.core.synctask.SyncTaskFactory;
import org.apache.shardingsphere.shardingscaling.core.datasource.DataSourceManager;
import org.apache.shardingsphere.shardingscaling.core.metadata.MetaDataManager;
import org.apache.shardingsphere.underlying.common.database.metadata.DataSourceMetaData;
import org.apache.shardingsphere.sql.parser.binder.metadata.table.TableMetaData;

import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * History data sync task group.
 */
@Slf4j
public final class HistoryDataSyncTaskGroup implements SyncTask {
    
    private final SyncConfiguration syncConfiguration;
    
    private final DataSourceManager dataSourceManager;
    
    private final List<SyncTask> syncTasks = new LinkedList<>();
    
    private final String syncTaskId;
    
    private final Queue<SyncTask> submitFailureTasks = new LinkedList<>();
    
    public HistoryDataSyncTaskGroup(final SyncConfiguration syncConfiguration, final DataSourceManager dataSourceManager) {
        this.syncConfiguration = syncConfiguration;
        this.dataSourceManager = dataSourceManager;
        DataSourceMetaData dataSourceMetaData = syncConfiguration.getReaderConfiguration().getDataSourceConfiguration().getDataSourceMetaData();
        syncTaskId = String.format("historyGroup-%s", null != dataSourceMetaData.getCatalog() ? dataSourceMetaData.getCatalog() : dataSourceMetaData.getSchema());
    }
    
    @Override
    public void prepare() {
        List<SyncConfiguration> tableSliceConfigurations = split(syncConfiguration);
        SyncTaskFactory syncTaskFactory = new DefaultSyncTaskFactory();
        for (SyncConfiguration each : tableSliceConfigurations) {
            SyncTask syncTask = syncTaskFactory.createHistoryDataSyncTask(each, dataSourceManager);
            syncTask.prepare();
            syncTasks.add(syncTask);
        }
    }
    
    private List<SyncConfiguration> split(final SyncConfiguration syncConfiguration) {
        List<SyncConfiguration> result = new LinkedList<>();
        DataSource dataSource = dataSourceManager.getDataSource(syncConfiguration.getReaderConfiguration().getDataSourceConfiguration());
        MetaDataManager metaDataManager = new MetaDataManager(dataSource);
        for (SyncConfiguration each : splitByTable(syncConfiguration)) {
            if (isSpiltByPrimaryKeyRange(each.getReaderConfiguration(), metaDataManager)) {
                result.addAll(splitByPrimaryKeyRange(each, metaDataManager, dataSource));
            } else {
                result.add(each);
            }
        }
        return result;
    }
    
    private Collection<SyncConfiguration> splitByTable(final SyncConfiguration syncConfiguration) {
        Collection<SyncConfiguration> result = new LinkedList<>();
        for (String each : syncConfiguration.getTableNameMap().keySet()) {
            RdbmsConfiguration readerConfig = RdbmsConfiguration.clone(syncConfiguration.getReaderConfiguration());
            readerConfig.setTableName(each);
            result.add(new SyncConfiguration(syncConfiguration.getConcurrency(), syncConfiguration.getTableNameMap(),
                    readerConfig, RdbmsConfiguration.clone(syncConfiguration.getWriterConfiguration())));
        }
        return result;
    }
    
    private boolean isSpiltByPrimaryKeyRange(final RdbmsConfiguration rdbmsConfiguration, final MetaDataManager metaDataManager) {
        List<String> primaryKeys = metaDataManager.getTableMetaData(rdbmsConfiguration.getTableName()).getPrimaryKeyColumns();
        if (null == primaryKeys || 0 == primaryKeys.size()) {
            log.warn("Can't split range for table {}, reason: no primary key", rdbmsConfiguration.getTableName());
            return false;
        }
        if (primaryKeys.size() > 1) {
            log.warn("Can't split range for table {}, reason: primary key is union primary", rdbmsConfiguration.getTableName());
            return false;
        }
        TableMetaData tableMetaData = metaDataManager.getTableMetaData(rdbmsConfiguration.getTableName());
        int index = tableMetaData.findColumnIndex(primaryKeys.get(0));
        if (isNotIntegerPrimary(tableMetaData.getColumnMetaData(index).getDataType())) {
            log.warn("Can't split range for table {}, reason: primary key is not integer number", rdbmsConfiguration.getTableName());
            return false;
        }
        return true;
    }
    
    private boolean isNotIntegerPrimary(final int columnType) {
        return Types.INTEGER != columnType && Types.BIGINT != columnType && Types.SMALLINT != columnType && Types.TINYINT != columnType;
    }
    
    private Collection<SyncConfiguration> splitByPrimaryKeyRange(final SyncConfiguration syncConfiguration, final MetaDataManager metaDataManager, final DataSource dataSource) {
        int concurrency = syncConfiguration.getConcurrency();
        Collection<SyncConfiguration> result = new LinkedList<>();
        RdbmsConfiguration readerConfiguration = syncConfiguration.getReaderConfiguration();
        String primaryKey = metaDataManager.getTableMetaData(readerConfiguration.getTableName()).getPrimaryKeyColumns().get(0);
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement ps = connection.prepareStatement(String.format("SELECT MIN(%s),MAX(%s) FROM %s LIMIT 1", primaryKey, primaryKey, readerConfiguration.getTableName()));
            ResultSet rs = ps.executeQuery();
            rs.next();
            int min = rs.getInt(1);
            int max = rs.getInt(2);
            int step = (max - min) / concurrency;
            for (int i = 0; i < concurrency; i++) {
                RdbmsConfiguration splitReaderConfig = RdbmsConfiguration.clone(readerConfiguration);
                if (i < concurrency - 1) {
                    splitReaderConfig.setWhereCondition(String.format("WHERE %s BETWEEN %d AND %d", primaryKey, min, min + step));
                    min = min + step + 1;
                } else {
                    splitReaderConfig.setWhereCondition(String.format("WHERE %s BETWEEN %d AND %d", primaryKey, min, max));
                }
                splitReaderConfig.setSpiltNum(i);
                result.add(new SyncConfiguration(concurrency, syncConfiguration.getTableNameMap(),
                        splitReaderConfig, RdbmsConfiguration.clone(syncConfiguration.getWriterConfiguration())));
            }
        } catch (SQLException e) {
            throw new RuntimeException("getTableNames error", e);
        }
        return result;
    }
    
    @Override
    public void start(final ReportCallback callback) {
        final AtomicInteger finishedTask = new AtomicInteger();
        for (final SyncTask each : syncTasks) {
            try {
                each.start(new ReportCallback() {
        
                    @Override
                    public void report(final Event event) {
                        if (EventType.FINISHED == event.getEventType()) {
                            finishedTask.incrementAndGet();
                        } else {
                            callback.report(new Event(syncTaskId, EventType.EXCEPTION_EXIT));
                        }
                        if (syncTasks.size() == finishedTask.get() && submitFailureTasks.isEmpty()) {
                            callback.report(new Event(syncTaskId, EventType.FINISHED));
                        } else if (!submitFailureTasks.isEmpty()) {
                            submitFailureTasks.peek().start(this);
                            submitFailureTasks.poll();
                        }
                    }
                });
            } catch (RejectedExecutionException ex) {
                submitFailureTasks.offer(each);
            } catch (SyncTaskExecuteException syncTaskEx) {
                stop();
                callback.report(new Event(syncTaskId, EventType.EXCEPTION_EXIT));
                break;
            }
        }
    }
    
    @Override
    public void stop() {
        for (SyncTask each : syncTasks) {
            each.stop();
        }
    }
    
    @Override
    public SyncProgress getProgress() {
        HistoryDataSyncTaskProgressGroup result = new HistoryDataSyncTaskProgressGroup();
        for (SyncTask each : syncTasks) {
            result.addSyncProgress(each.getProgress());
        }
        return result;
    }
}
