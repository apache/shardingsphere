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
import org.apache.shardingsphere.scaling.core.config.InventoryDumperConfiguration;
import org.apache.shardingsphere.scaling.core.config.SyncConfiguration;
import org.apache.shardingsphere.scaling.core.datasource.DataSourceManager;
import org.apache.shardingsphere.scaling.core.exception.PrepareFailedException;
import org.apache.shardingsphere.scaling.core.job.position.InventoryPosition;
import org.apache.shardingsphere.scaling.core.job.position.InventoryPositionManager;
import org.apache.shardingsphere.scaling.core.job.position.PlaceholderInventoryPosition;
import org.apache.shardingsphere.scaling.core.job.position.PrimaryKeyPosition;
import org.apache.shardingsphere.scaling.core.job.task.DefaultSyncTaskFactory;
import org.apache.shardingsphere.scaling.core.job.task.ScalingTask;
import org.apache.shardingsphere.scaling.core.job.task.SyncTaskFactory;
import org.apache.shardingsphere.scaling.core.metadata.MetaDataManager;
import org.apache.shardingsphere.infra.metadata.model.physical.model.table.TableMetaData;

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
     * @param syncConfig synchronize configuration
     * @param dataSourceManager data source manager
     * @return split inventory data task
     */
    public Collection<ScalingTask<InventoryPosition>> splitInventoryData(final SyncConfiguration syncConfig, final DataSourceManager dataSourceManager) {
        Collection<ScalingTask<InventoryPosition>> result = new LinkedList<>();
        for (InventoryDumperConfiguration each : splitDumperConfiguration(syncConfig.getConcurrency(), syncConfig.getDumperConfiguration(), dataSourceManager)) {
            result.add(syncTaskFactory.createInventoryDataSyncTask(each, syncConfig.getImporterConfiguration()));
        }
        return result;
    }
    
    private Collection<InventoryDumperConfiguration> splitDumperConfiguration(final int concurrency, final DumperConfiguration dumperConfig, final DataSourceManager dataSourceManager) {
        Collection<InventoryDumperConfiguration> result = new LinkedList<>();
        DataSource dataSource = dataSourceManager.getDataSource(dumperConfig.getDataSourceConfiguration());
        MetaDataManager metaDataManager = new MetaDataManager(dataSource);
        for (InventoryDumperConfiguration each : splitByTable(dumperConfig)) {
            if (isSpiltByPrimaryKeyRange(each, metaDataManager)) {
                result.addAll(splitByPrimaryKeyRange(concurrency, each, metaDataManager, dataSource));
            } else {
                result.add(each);
            }
        }
        return result;
    }
    
    private Collection<InventoryDumperConfiguration> splitByTable(final DumperConfiguration dumperConfig) {
        Collection<InventoryDumperConfiguration> result = new LinkedList<>();
        for (String each : dumperConfig.getTableNameMap().keySet()) {
            InventoryDumperConfiguration inventoryDumperConfig = new InventoryDumperConfiguration(dumperConfig);
            inventoryDumperConfig.setTableName(each);
            inventoryDumperConfig.setPositionManager(new InventoryPositionManager<>(new PlaceholderInventoryPosition()));
            result.add(inventoryDumperConfig);
        }
        return result;
    }
    
    private boolean isSpiltByPrimaryKeyRange(final InventoryDumperConfiguration inventoryDumperConfig, final MetaDataManager metaDataManager) {
        TableMetaData tableMetaData = metaDataManager.getTableMetaData(inventoryDumperConfig.getTableName());
        if (null == tableMetaData) {
            log.warn("Can't split range for table {}, reason: can not get table metadata ", inventoryDumperConfig.getTableName());
            return false;
        }
        List<String> primaryKeys = tableMetaData.getPrimaryKeyColumns();
        if (null == primaryKeys || primaryKeys.isEmpty()) {
            log.warn("Can't split range for table {}, reason: no primary key", inventoryDumperConfig.getTableName());
            return false;
        }
        if (primaryKeys.size() > 1) {
            log.warn("Can't split range for table {}, reason: primary key is union primary", inventoryDumperConfig.getTableName());
            return false;
        }
        int index = tableMetaData.findColumnIndex(primaryKeys.get(0));
        if (isNotIntegerPrimary(tableMetaData.getColumnMetaData(index).getDataType())) {
            log.warn("Can't split range for table {}, reason: primary key is not integer number", inventoryDumperConfig.getTableName());
            return false;
        }
        return true;
    }
    
    private boolean isNotIntegerPrimary(final int columnType) {
        return Types.INTEGER != columnType && Types.BIGINT != columnType && Types.SMALLINT != columnType && Types.TINYINT != columnType;
    }
    
    private Collection<InventoryDumperConfiguration> splitByPrimaryKeyRange(final int concurrency, final InventoryDumperConfiguration inventoryDumperConfig, 
                                                                            final MetaDataManager metaDataManager, final DataSource dataSource) {
        Collection<InventoryDumperConfiguration> result = new LinkedList<>();
        String tableName = inventoryDumperConfig.getTableName();
        String primaryKey = metaDataManager.getTableMetaData(tableName).getPrimaryKeyColumns().get(0);
        inventoryDumperConfig.setPrimaryKey(primaryKey);
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement ps = connection.prepareStatement(String.format("SELECT MIN(%s),MAX(%s) FROM %s LIMIT 1", primaryKey, primaryKey, inventoryDumperConfig.getTableName()));
            ResultSet rs = ps.executeQuery();
            rs.next();
            long min = rs.getLong(1);
            long max = rs.getLong(2);
            long step = (max - min) / concurrency;
            for (int i = 0; i < concurrency && min <= max; i++) {
                InventoryDumperConfiguration splitDumperConfig = new InventoryDumperConfiguration(inventoryDumperConfig);
                if (i < concurrency - 1) {
                    splitDumperConfig.setPositionManager(new InventoryPositionManager<>(new PrimaryKeyPosition(min, min + step)));
                    min += step + 1;
                } else {
                    splitDumperConfig.setPositionManager(new InventoryPositionManager<>(new PrimaryKeyPosition(min, max)));
                }
                splitDumperConfig.setSpiltNum(i);
                splitDumperConfig.setPrimaryKey(primaryKey);
                splitDumperConfig.setTableName(tableName);
                result.add(splitDumperConfig);
            }
        } catch (final SQLException ex) {
            throw new PrepareFailedException(String.format("Split task for table %s by primary key %s error", inventoryDumperConfig.getTableName(), primaryKey), ex);
        }
        return result;
    }
}
