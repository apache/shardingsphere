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
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.scaling.core.config.DumperConfiguration;
import org.apache.shardingsphere.scaling.core.config.InventoryDumperConfiguration;
import org.apache.shardingsphere.scaling.core.config.TaskConfiguration;
import org.apache.shardingsphere.scaling.core.datasource.DataSourceManager;
import org.apache.shardingsphere.scaling.core.exception.PrepareFailedException;
import org.apache.shardingsphere.scaling.core.execute.executor.sqlbuilder.ScalingSQLBuilder;
import org.apache.shardingsphere.scaling.core.execute.executor.sqlbuilder.ScalingSQLBuilderFactory;
import org.apache.shardingsphere.scaling.core.job.position.PlaceholderPosition;
import org.apache.shardingsphere.scaling.core.job.position.PositionManager;
import org.apache.shardingsphere.scaling.core.job.position.PrimaryKeyPosition;
import org.apache.shardingsphere.scaling.core.job.task.DefaultScalingTaskFactory;
import org.apache.shardingsphere.scaling.core.job.task.ScalingTask;
import org.apache.shardingsphere.scaling.core.job.task.ScalingTaskFactory;
import org.apache.shardingsphere.scaling.core.metadata.MetaDataManager;

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
public final class InventoryTaskSplitter {
    
    private final ScalingTaskFactory scalingTaskFactory = new DefaultScalingTaskFactory();
    
    /**
     * Split inventory data to multi-tasks.
     *
     * @param databaseType database type
     * @param taskConfig task configuration
     * @param dataSourceManager data source manager
     * @return split inventory data task
     */
    public Collection<ScalingTask> splitInventoryData(final String databaseType, final TaskConfiguration taskConfig, final DataSourceManager dataSourceManager) {
        Collection<ScalingTask> result = new LinkedList<>();
        for (InventoryDumperConfiguration each : splitDumperConfig(databaseType, taskConfig.getJobConfig().getShardingSize(), taskConfig.getDumperConfig(), dataSourceManager)) {
            result.add(scalingTaskFactory.createInventoryTask(each, taskConfig.getImporterConfig()));
        }
        return result;
    }
    
    private Collection<InventoryDumperConfiguration> splitDumperConfig(
            final String databaseType, final int shardingSize, final DumperConfiguration dumperConfig, final DataSourceManager dataSourceManager) {
        Collection<InventoryDumperConfiguration> result = new LinkedList<>();
        DataSource dataSource = dataSourceManager.getDataSource(dumperConfig.getDataSourceConfig());
        MetaDataManager metaDataManager = new MetaDataManager(dataSource);
        for (InventoryDumperConfiguration each : splitByTable(dumperConfig)) {
            if (isSpiltByPrimaryKeyRange(each, metaDataManager)) {
                result.addAll(splitByPrimaryKeyRange(databaseType, shardingSize, each, metaDataManager, dataSource));
            } else {
                result.add(each);
            }
        }
        return result;
    }
    
    private Collection<InventoryDumperConfiguration> splitByTable(final DumperConfiguration dumperConfig) {
        Collection<InventoryDumperConfiguration> result = new LinkedList<>();
        dumperConfig.getTableNameMap().forEach((key, value) -> {
            InventoryDumperConfiguration inventoryDumperConfig = new InventoryDumperConfiguration(dumperConfig);
            inventoryDumperConfig.setTableName(key);
            inventoryDumperConfig.setPositionManager(new PositionManager(new PlaceholderPosition()));
            result.add(inventoryDumperConfig);
        });
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
    
    private Collection<InventoryDumperConfiguration> splitByPrimaryKeyRange(
            final String databaseType, final int shardingSize, final InventoryDumperConfiguration inventoryDumperConfig, final MetaDataManager metaDataManager, final DataSource dataSource) {
        Collection<InventoryDumperConfiguration> result = new LinkedList<>();
        String tableName = inventoryDumperConfig.getTableName();
        String primaryKey = metaDataManager.getTableMetaData(tableName).getPrimaryKeyColumns().get(0);
        ScalingSQLBuilder scalingSqlBuilder = ScalingSQLBuilderFactory.newInstance(databaseType);
        inventoryDumperConfig.setPrimaryKey(primaryKey);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(scalingSqlBuilder.buildSplitByPrimaryKeyRangeSQL(tableName, primaryKey))) {
            long beginId = 0;
            for (int i = 0; i < Integer.MAX_VALUE; i++) {
                ps.setLong(1, beginId);
                ps.setLong(2, shardingSize);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    long endId = rs.getLong(1);
                    if (endId == 0) {
                        break;
                    }
                    InventoryDumperConfiguration splitDumperConfig = new InventoryDumperConfiguration(inventoryDumperConfig);
                    splitDumperConfig.setPositionManager(new PositionManager(new PrimaryKeyPosition(beginId, endId)));
                    splitDumperConfig.setShardingItem(i);
                    splitDumperConfig.setPrimaryKey(primaryKey);
                    splitDumperConfig.setTableName(tableName);
                    result.add(splitDumperConfig);
                    beginId = endId + 1;
                }
            }
        } catch (final SQLException ex) {
            throw new PrepareFailedException(String.format("Split task for table %s by primary key %s error", inventoryDumperConfig.getTableName(), primaryKey), ex);
        }
        return result;
    }
}
