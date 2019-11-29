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

package org.apache.shardingsphere.shardingscaling.core.synctask;

import org.apache.shardingsphere.shardingscaling.core.config.RdbmsConfiguration;
import org.apache.shardingsphere.shardingscaling.core.config.SyncConfiguration;
import org.apache.shardingsphere.shardingscaling.core.controller.ReportCallback;
import org.apache.shardingsphere.shardingscaling.core.controller.SyncProgress;
import org.apache.shardingsphere.shardingscaling.core.execute.Event;
import org.apache.shardingsphere.shardingscaling.core.execute.EventType;
import org.apache.shardingsphere.shardingscaling.core.metadata.ColumnMetaData;
import org.apache.shardingsphere.shardingscaling.core.util.DataSourceFactory;
import org.apache.shardingsphere.shardingscaling.core.util.DbMetaDataUtil;
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

/**
 * History data sync task group.
 *
 * @author avalon566
 */
@Slf4j
public class HistoryDataSyncTaskGroup implements SyncTask {

    private final SyncConfiguration syncConfiguration;

    private final List<SyncTask> syncTasks = new LinkedList<>();

    public HistoryDataSyncTaskGroup(final SyncConfiguration syncConfiguration) {
        this.syncConfiguration = syncConfiguration;
    }

    @Override
    public final void prepare() {
        List<SyncConfiguration> tableSliceConfigurations = split(syncConfiguration);
        for (SyncConfiguration each : tableSliceConfigurations) {
            SyncTask syncTask = new DefaultSyncTaskFactory().createHistoryDataSyncTask(each);
            syncTask.prepare();
            syncTasks.add(syncTask);
        }
    }

    private List<SyncConfiguration> split(final SyncConfiguration syncConfiguration) {
        List<SyncConfiguration> result = new LinkedList<>();
        DataSource dataSource = DataSourceFactory.getDataSource(syncConfiguration.getReaderConfiguration().getDataSourceConfiguration());
        DbMetaDataUtil dbMetaDataUtil = new DbMetaDataUtil(dataSource);
        for (SyncConfiguration each : splitByTable(syncConfiguration, dbMetaDataUtil)) {
            if (isSpiltByPrimaryKeyRange(each.getReaderConfiguration(), dbMetaDataUtil)) {
                result.addAll(splitByPrimaryKeyRange(each, dbMetaDataUtil, dataSource));
            } else {
                result.add(each);
            }
        }
        return result;
    }
    
    private Collection<SyncConfiguration> splitByTable(final SyncConfiguration syncConfiguration, final DbMetaDataUtil dbMetaDataUtil) {
        Collection<SyncConfiguration> result = new LinkedList<>();
        for (String each : dbMetaDataUtil.getTableNames()) {
            RdbmsConfiguration readerConfig = RdbmsConfiguration.clone(syncConfiguration.getReaderConfiguration());
            readerConfig.setTableName(each);
            result.add(new SyncConfiguration(syncConfiguration.getConcurrency(), readerConfig, RdbmsConfiguration.clone(syncConfiguration.getWriterConfiguration())));
        }
        return result;
    }
    
    private boolean isSpiltByPrimaryKeyRange(final RdbmsConfiguration rdbmsConfiguration, final DbMetaDataUtil dbMetaDataUtil) {
        List<String> primaryKeys = dbMetaDataUtil.getPrimaryKeys(rdbmsConfiguration.getTableName());
        if (null == primaryKeys || 0 == primaryKeys.size()) {
            log.warn("Can't split range for table {}, reason: no primary key", rdbmsConfiguration.getTableName());
            return false;
        }
        if (primaryKeys.size() > 1) {
            log.warn("Can't split range for table {}, reason: primary key is union primary", rdbmsConfiguration.getTableName());
            return false;
        }
        List<ColumnMetaData> metaData = dbMetaDataUtil.getColumnNames(rdbmsConfiguration.getTableName());
        int index = DbMetaDataUtil.findColumnIndex(metaData, primaryKeys.get(0));
        if (isNotIntegerPrimary(metaData.get(index).getColumnType())) {
            log.warn("Can't split range for table {}, reason: primary key is not integer number", rdbmsConfiguration.getTableName());
            return false;
        }
        return true;
    }
    
    private boolean isNotIntegerPrimary(final int columnType) {
        return Types.INTEGER != columnType && Types.BIGINT != columnType && Types.SMALLINT != columnType && Types.TINYINT != columnType;
    }
    
    private Collection<SyncConfiguration> splitByPrimaryKeyRange(final SyncConfiguration syncConfiguration, final DbMetaDataUtil dbMetaDataUtil, final DataSource dataSource) {
        int concurrency = syncConfiguration.getConcurrency();
        Collection<SyncConfiguration> result = new LinkedList<>();
        RdbmsConfiguration readerConfiguration = syncConfiguration.getReaderConfiguration();
        String primaryKey = dbMetaDataUtil.getPrimaryKeys(readerConfiguration.getTableName()).get(0);
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement ps = connection.prepareStatement(String.format("select min(%s),max(%s) from %s limit 1", primaryKey, primaryKey, readerConfiguration.getTableName()));
            ResultSet rs = ps.executeQuery();
            rs.next();
            int min = rs.getInt(1);
            int max = rs.getInt(2);
            int step = (max - min) / concurrency;
            for (int i = 0; i < concurrency; i++) {
                RdbmsConfiguration splitReaderConfig = RdbmsConfiguration.clone(readerConfiguration);
                if (i < concurrency - 1) {
                    splitReaderConfig.setWhereCondition(String.format("where id between %d and %d", min, min + step));
                    min = min + step + 1;
                } else {
                    splitReaderConfig.setWhereCondition(String.format("where id between %d and %d", min, max));
                }
                splitReaderConfig.setSpiltNum(i);
                result.add(new SyncConfiguration(concurrency, splitReaderConfig, RdbmsConfiguration.clone(syncConfiguration.getWriterConfiguration())));
            }
        } catch (SQLException e) {
            throw new RuntimeException("getTableNames error", e);
        }
        return result;
    }

    @Override
    public final void start(final ReportCallback callback) {
        final List<Event> events = new LinkedList<>();
        for (final SyncTask each : syncTasks) {
            each.start(new ReportCallback() {

                @Override
                public void onProcess(final Event event) {
                    events.add(event);
                    if (syncTasks.size() == events.size()) {
                        //TODO check error
                        callback.onProcess(new Event(syncConfiguration.getTaskId(), EventType.FINISHED));
                    }
                }
            });
        }
    }

    @Override
    public final void stop() {
        for (SyncTask each : syncTasks) {
            each.stop();
        }
    }

    @Override
    public final SyncProgress getProgress() {
        return new SyncProgress() { };
    }
}
