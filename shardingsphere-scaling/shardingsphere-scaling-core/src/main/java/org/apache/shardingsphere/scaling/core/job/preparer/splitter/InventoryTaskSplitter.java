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

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.scaling.core.common.datasource.DataSourceManager;
import org.apache.shardingsphere.scaling.core.common.datasource.MetaDataManager;
import org.apache.shardingsphere.scaling.core.common.exception.PrepareFailedException;
import org.apache.shardingsphere.scaling.core.common.sqlbuilder.ScalingSQLBuilderFactory;
import org.apache.shardingsphere.scaling.core.config.DumperConfiguration;
import org.apache.shardingsphere.scaling.core.config.InventoryDumperConfiguration;
import org.apache.shardingsphere.scaling.core.config.TaskConfiguration;
import org.apache.shardingsphere.scaling.core.job.JobContext;
import org.apache.shardingsphere.scaling.core.job.position.PlaceholderPosition;
import org.apache.shardingsphere.scaling.core.job.position.PrimaryKeyPosition;
import org.apache.shardingsphere.scaling.core.job.position.ScalingPosition;
import org.apache.shardingsphere.scaling.core.job.task.ScalingTaskFactory;
import org.apache.shardingsphere.scaling.core.job.task.inventory.InventoryTask;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Inventory data task splitter.
 */
@Slf4j
public final class InventoryTaskSplitter {
    
    /**
     * Split inventory data to multi-tasks.
     *
     * @param jobContext job context
     * @param taskConfig task configuration
     * @param dataSourceManager data source manager
     * @return split inventory data task
     */
    public List<InventoryTask> splitInventoryData(final JobContext jobContext, final TaskConfiguration taskConfig, final DataSourceManager dataSourceManager) {
        List<InventoryTask> result = new LinkedList<>();
        for (InventoryDumperConfiguration each : splitDumperConfig(jobContext, taskConfig.getDumperConfig(), dataSourceManager)) {
            result.add(ScalingTaskFactory.createInventoryTask(each, taskConfig.getImporterConfig()));
        }
        return result;
    }
    
    private Collection<InventoryDumperConfiguration> splitDumperConfig(
            final JobContext jobContext, final DumperConfiguration dumperConfig, final DataSourceManager dataSourceManager) {
        Collection<InventoryDumperConfiguration> result = new LinkedList<>();
        DataSource dataSource = dataSourceManager.getDataSource(dumperConfig.getDataSourceConfig());
        MetaDataManager metaDataManager = new MetaDataManager(dataSource);
        for (InventoryDumperConfiguration each : splitByTable(dumperConfig)) {
            result.addAll(splitByPrimaryKey(jobContext, dataSource, metaDataManager, each));
        }
        return result;
    }
    
    private Collection<InventoryDumperConfiguration> splitByTable(final DumperConfiguration dumperConfig) {
        Collection<InventoryDumperConfiguration> result = new LinkedList<>();
        dumperConfig.getTableNameMap().forEach((key, value) -> {
            InventoryDumperConfiguration inventoryDumperConfig = new InventoryDumperConfiguration(dumperConfig);
            inventoryDumperConfig.setTableName(key);
            inventoryDumperConfig.setPosition(new PlaceholderPosition());
            result.add(inventoryDumperConfig);
        });
        return result;
    }
    
    private Collection<InventoryDumperConfiguration> splitByPrimaryKey(
            final JobContext jobContext, final DataSource dataSource, final MetaDataManager metaDataManager, final InventoryDumperConfiguration dumperConfig) {
        Collection<InventoryDumperConfiguration> result = new LinkedList<>();
        Collection<ScalingPosition<?>> inventoryPositions = getInventoryPositions(jobContext, dumperConfig, dataSource, metaDataManager);
        int i = 0;
        for (ScalingPosition<?> inventoryPosition : inventoryPositions) {
            InventoryDumperConfiguration splitDumperConfig = new InventoryDumperConfiguration(dumperConfig);
            splitDumperConfig.setPosition(inventoryPosition);
            splitDumperConfig.setShardingItem(i++);
            splitDumperConfig.setTableName(dumperConfig.getTableName());
            splitDumperConfig.setPrimaryKey(dumperConfig.getPrimaryKey());
            result.add(splitDumperConfig);
        }
        return result;
    }
    
    private Collection<ScalingPosition<?>> getInventoryPositions(
            final JobContext jobContext, final InventoryDumperConfiguration dumperConfig, final DataSource dataSource, final MetaDataManager metaDataManager) {
        if (null != jobContext.getInitProgress()) {
            return jobContext.getInitProgress().getInventoryPosition(dumperConfig.getTableName()).values();
        }
        if (isSpiltByPrimaryKeyRange(metaDataManager, dumperConfig.getTableName())) {
            String primaryKey = metaDataManager.getTableMetaData(dumperConfig.getTableName()).getPrimaryKeyColumns().get(0);
            dumperConfig.setPrimaryKey(primaryKey);
            return getPositionByPrimaryKeyRange(jobContext, dataSource, dumperConfig);
        }
        return Lists.newArrayList(new PlaceholderPosition());
    }
    
    private boolean isSpiltByPrimaryKeyRange(final MetaDataManager metaDataManager, final String tableName) {
        TableMetaData tableMetaData = metaDataManager.getTableMetaData(tableName);
        if (null == tableMetaData) {
            log.warn("Can't split range for table {}, reason: can not get table metadata ", tableName);
            return false;
        }
        List<String> primaryKeys = tableMetaData.getPrimaryKeyColumns();
        if (null == primaryKeys || primaryKeys.isEmpty()) {
            log.warn("Can't split range for table {}, reason: no primary key", tableName);
            return false;
        }
        if (primaryKeys.size() > 1) {
            log.warn("Can't split range for table {}, reason: primary key is union primary", tableName);
            return false;
        }
        int index = tableMetaData.findColumnIndex(primaryKeys.get(0));
        if (isNotIntegerPrimary(tableMetaData.getColumnMetaData(index).getDataType())) {
            log.warn("Can't split range for table {}, reason: primary key is not integer number", tableName);
            return false;
        }
        return true;
    }
    
    private boolean isNotIntegerPrimary(final int columnType) {
        return Types.INTEGER != columnType && Types.BIGINT != columnType && Types.SMALLINT != columnType && Types.TINYINT != columnType;
    }
    
    private Collection<ScalingPosition<?>> getPositionByPrimaryKeyRange(final JobContext jobContext, final DataSource dataSource, final InventoryDumperConfiguration dumperConfig) {
        Collection<ScalingPosition<?>> result = new ArrayList<>();
        String sql = ScalingSQLBuilderFactory.newInstance(jobContext.getJobConfig().getHandleConfig().getDatabaseType())
                .buildSplitByPrimaryKeyRangeSQL(dumperConfig.getTableName(), dumperConfig.getPrimaryKey());
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            long beginId = 0;
            for (int i = 0; i < Integer.MAX_VALUE; i++) {
                ps.setLong(1, beginId);
                ps.setLong(2, jobContext.getJobConfig().getHandleConfig().getShardingSize());
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    long endId = rs.getLong(1);
                    if (endId == 0) {
                        break;
                    }
                    result.add(new PrimaryKeyPosition(beginId, endId));
                    beginId = endId + 1;
                }
            }
        } catch (final SQLException ex) {
            throw new PrepareFailedException(String.format("Split task for table %s by primary key %s error", dumperConfig.getTableName(), dumperConfig.getPrimaryKey()), ex);
        }
        return result;
    }
}
