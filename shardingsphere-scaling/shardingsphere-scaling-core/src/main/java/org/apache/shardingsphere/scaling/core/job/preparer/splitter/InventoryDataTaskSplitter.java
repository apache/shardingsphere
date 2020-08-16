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

package org.apache.shardingsphere.scaling.core.job.preparer.splitter;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.scaling.core.config.DumperConfiguration;
import org.apache.shardingsphere.scaling.core.config.SyncConfiguration;
import org.apache.shardingsphere.scaling.core.datasource.DataSourceManager;
import org.apache.shardingsphere.scaling.core.exception.PrepareFailedException;
import org.apache.shardingsphere.scaling.core.job.position.InventoryPosition;
import org.apache.shardingsphere.scaling.core.job.position.InventoryPositionManager;
import org.apache.shardingsphere.scaling.core.job.position.PlaceholderPosition;
import org.apache.shardingsphere.scaling.core.job.position.PrimaryKeyPosition;
import org.apache.shardingsphere.scaling.core.job.task.DefaultSyncTaskFactory;
import org.apache.shardingsphere.scaling.core.job.task.ScalingTask;
import org.apache.shardingsphere.scaling.core.job.task.SyncTaskFactory;
import org.apache.shardingsphere.scaling.core.metadata.MetaDataManager;
import org.apache.shardingsphere.sql.parser.binder.metadata.table.TableMetaData;

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
 * Inventory data task splitter.
 */
@Slf4j
public final class InventoryDataTaskSplitter {
    
    private final SyncTaskFactory syncTaskFactory = new DefaultSyncTaskFactory();
    
    /**
     * Split inventory data to multi-tasks.
     *
     * @param syncConfiguration synchronize configuration
     * @param dataSourceManager data source manager
     * @return split inventory data task
     */
    public Collection<ScalingTask<InventoryPosition>> splitInventoryData(final SyncConfiguration syncConfiguration, final DataSourceManager dataSourceManager) {
        Collection<ScalingTask<InventoryPosition>> result = new LinkedList<>();
        for (SyncConfiguration each : splitConfiguration(syncConfiguration, dataSourceManager)) {
            result.add(syncTaskFactory.createInventoryDataSyncTask(each));
        }
        return result;
    }
    
    private Collection<SyncConfiguration> splitConfiguration(final SyncConfiguration syncConfiguration, final DataSourceManager dataSourceManager) {
        Collection<SyncConfiguration> result = new LinkedList<>();
        DataSource dataSource = dataSourceManager.getDataSource(syncConfiguration.getDumperConfiguration().getDataSourceConfiguration());
        MetaDataManager metaDataManager = new MetaDataManager(dataSource);
        for (SyncConfiguration each : splitByTable(syncConfiguration)) {
            if (isSpiltByPrimaryKeyRange(each.getDumperConfiguration(), metaDataManager)) {
                result.addAll(splitByPrimaryKeyRange(each, metaDataManager, dataSource));
            } else {
                result.add(each);
            }
        }
        return result;
    }
    
    private Collection<SyncConfiguration> splitByTable(final SyncConfiguration syncConfiguration) {
        Collection<SyncConfiguration> result = new LinkedList<>();
        for (String each : syncConfiguration.getDumperConfiguration().getTableNameMap().keySet()) {
            DumperConfiguration dumperConfig = DumperConfiguration.clone(syncConfiguration.getDumperConfiguration());
            dumperConfig.setTableName(each);
            dumperConfig.setPositionManager(new InventoryPositionManager<>(new PlaceholderPosition()));
            result.add(new SyncConfiguration(syncConfiguration.getConcurrency(), dumperConfig, syncConfiguration.getImporterConfiguration()));
        }
        return result;
    }
    
    private boolean isSpiltByPrimaryKeyRange(final DumperConfiguration rdbmsConfiguration, final MetaDataManager metaDataManager) {
        TableMetaData tableMetaData = metaDataManager.getTableMetaData(rdbmsConfiguration.getTableName());
        if (null == tableMetaData) {
            log.warn("Can't split range for table {}, reason: can not get table metadata ", rdbmsConfiguration.getTableName());
            return false;
        }
        List<String> primaryKeys = tableMetaData.getPrimaryKeyColumns();
        if (null == primaryKeys || primaryKeys.isEmpty()) {
            log.warn("Can't split range for table {}, reason: no primary key", rdbmsConfiguration.getTableName());
            return false;
        }
        if (primaryKeys.size() > 1) {
            log.warn("Can't split range for table {}, reason: primary key is union primary", rdbmsConfiguration.getTableName());
            return false;
        }
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
        DumperConfiguration dumperConfiguration = syncConfiguration.getDumperConfiguration();
        String primaryKey = metaDataManager.getTableMetaData(dumperConfiguration.getTableName()).getPrimaryKeyColumns().get(0);
        dumperConfiguration.setPrimaryKey(primaryKey);
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement ps = connection.prepareStatement(String.format("SELECT MIN(%s),MAX(%s) FROM %s LIMIT 1", primaryKey, primaryKey, dumperConfiguration.getTableName()));
            ResultSet rs = ps.executeQuery();
            rs.next();
            long min = rs.getLong(1);
            long max = rs.getLong(2);
            long step = (max - min) / concurrency;
            for (int i = 0; i < concurrency && min <= max; i++) {
                DumperConfiguration splitDumperConfig = DumperConfiguration.clone(dumperConfiguration);
                if (i < concurrency - 1) {
                    splitDumperConfig.setPositionManager(new InventoryPositionManager<>(new PrimaryKeyPosition(min, min + step)));
                    min += step + 1;
                } else {
                    splitDumperConfig.setPositionManager(new InventoryPositionManager<>(new PrimaryKeyPosition(min, max)));
                }
                splitDumperConfig.setSpiltNum(i);
                result.add(new SyncConfiguration(concurrency, splitDumperConfig, syncConfiguration.getImporterConfiguration()));
            }
        } catch (final SQLException ex) {
            throw new PrepareFailedException(String.format("Split task for table %s by primary key %s error", dumperConfiguration.getTableName(), primaryKey), ex);
        }
        return result;
    }
}
