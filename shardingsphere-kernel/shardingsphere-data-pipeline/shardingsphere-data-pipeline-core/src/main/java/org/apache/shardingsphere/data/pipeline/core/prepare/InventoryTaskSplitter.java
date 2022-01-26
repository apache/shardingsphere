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

package org.apache.shardingsphere.data.pipeline.core.prepare;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.config.ingest.DumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.ingest.InventoryDumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.TaskConfiguration;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.PlaceholderPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.PrimaryKeyPosition;
import org.apache.shardingsphere.data.pipeline.api.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.api.job.progress.JobProgress;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineMetaDataManager;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineJobPrepareFailedException;
import org.apache.shardingsphere.data.pipeline.core.execute.ExecuteEngine;
import org.apache.shardingsphere.data.pipeline.core.task.InventoryTask;
import org.apache.shardingsphere.data.pipeline.scenario.rulealtered.RuleAlteredJobContext;
import org.apache.shardingsphere.data.pipeline.spi.ingest.channel.PipelineChannelFactory;
import org.apache.shardingsphere.data.pipeline.spi.ratelimit.JobRateLimitAlgorithm;
import org.apache.shardingsphere.infra.config.rulealtered.OnRuleAlteredActionConfiguration.InputConfiguration;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.yaml.config.pojo.rulealtered.YamlOnRuleAlteredActionConfiguration.YamlInputConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.rulealtered.OnRuleAlteredActionConfigurationYamlSwapper.InputConfigurationSwapper;
import org.apache.shardingsphere.scaling.core.job.sqlbuilder.ScalingSQLBuilderFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
     * @param pipelineChannelFactory channel factory
     * @param importerExecuteEngine execute engine
     * @return split inventory data task
     */
    // TODO remove jobContext, use init JobProgress -  sourceDatabaseType - readBatchSize - rateLimitAlgorithm
    public List<InventoryTask> splitInventoryData(final RuleAlteredJobContext jobContext, final TaskConfiguration taskConfig, final PipelineDataSourceManager dataSourceManager,
                                                  final PipelineChannelFactory pipelineChannelFactory, final ExecuteEngine importerExecuteEngine) {
        List<InventoryTask> result = new LinkedList<>();
        for (InventoryDumperConfiguration each : splitDumperConfig(jobContext, taskConfig.getDumperConfig(), dataSourceManager)) {
            result.add(new InventoryTask(each, taskConfig.getImporterConfig(), pipelineChannelFactory, dataSourceManager, importerExecuteEngine));
        }
        return result;
    }
    
    private Collection<InventoryDumperConfiguration> splitDumperConfig(
            final RuleAlteredJobContext jobContext, final DumperConfiguration dumperConfig, final PipelineDataSourceManager dataSourceManager) {
        Collection<InventoryDumperConfiguration> result = new LinkedList<>();
        DataSource dataSource = dataSourceManager.getDataSource(dumperConfig.getDataSourceConfig());
        PipelineMetaDataManager metaDataManager = new PipelineMetaDataManager(dataSource);
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
            final RuleAlteredJobContext jobContext, final DataSource dataSource, final PipelineMetaDataManager metaDataManager, final InventoryDumperConfiguration dumperConfig) {
        Collection<InventoryDumperConfiguration> result = new LinkedList<>();
        InputConfiguration inputConfig = jobContext.getRuleAlteredContext().getOnRuleAlteredActionConfig().getInput();
        if (null == inputConfig) {
            inputConfig = new InputConfigurationSwapper().swapToObject(new YamlInputConfiguration());
        }
        int batchSize = inputConfig.getBatchSize();
        JobRateLimitAlgorithm rateLimitAlgorithm = jobContext.getRuleAlteredContext().getInputRateLimitAlgorithm();
        Collection<IngestPosition<?>> inventoryPositions = getInventoryPositions(jobContext, dumperConfig, dataSource, metaDataManager);
        int i = 0;
        for (IngestPosition<?> inventoryPosition : inventoryPositions) {
            InventoryDumperConfiguration splitDumperConfig = new InventoryDumperConfiguration(dumperConfig);
            splitDumperConfig.setPosition(inventoryPosition);
            splitDumperConfig.setShardingItem(i++);
            splitDumperConfig.setTableName(dumperConfig.getTableName());
            splitDumperConfig.setPrimaryKey(dumperConfig.getPrimaryKey());
            splitDumperConfig.setBatchSize(batchSize);
            splitDumperConfig.setRateLimitAlgorithm(rateLimitAlgorithm);
            result.add(splitDumperConfig);
        }
        return result;
    }
    
    private Collection<IngestPosition<?>> getInventoryPositions(
            final RuleAlteredJobContext jobContext, final InventoryDumperConfiguration dumperConfig, final DataSource dataSource, final PipelineMetaDataManager metaDataManager) {
        DatabaseType databaseType = dumperConfig.getDataSourceConfig().getDatabaseType();
        JobProgress initProgress = jobContext.getInitProgress();
        if (null != initProgress && initProgress.getStatus() != JobStatus.PREPARING_FAILURE) {
            Collection<IngestPosition<?>> result = jobContext.getInitProgress().getInventoryPosition(dumperConfig.getTableName()).values();
            for (IngestPosition<?> each : result) {
                if (each instanceof PrimaryKeyPosition) {
                    String primaryKey = metaDataManager.getTableMetaData(dumperConfig.getTableName(), databaseType).getPrimaryKeyColumns().get(0);
                    dumperConfig.setPrimaryKey(primaryKey);
                    break;
                }
            }
            // Do NOT filter FinishedPosition here, since whole inventory tasks are required in job progress when persisting to register center.
            return result;
        }
        TableMetaData tableMetaData = metaDataManager.getTableMetaData(dumperConfig.getTableName(), databaseType);
        if (isSpiltByPrimaryKeyRange(tableMetaData, dumperConfig.getTableName())) {
            String primaryKey = tableMetaData.getPrimaryKeyColumns().get(0);
            dumperConfig.setPrimaryKey(primaryKey);
            return getPositionByPrimaryKeyRange(jobContext, dataSource, dumperConfig);
        }
        return Collections.singletonList(new PlaceholderPosition());
    }
    
    private boolean isSpiltByPrimaryKeyRange(final TableMetaData tableMetaData, final String tableName) {
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
    
    private Collection<IngestPosition<?>> getPositionByPrimaryKeyRange(final RuleAlteredJobContext jobContext, final DataSource dataSource, final InventoryDumperConfiguration dumperConfig) {
        Collection<IngestPosition<?>> result = new ArrayList<>();
        String sql = ScalingSQLBuilderFactory.newInstance(jobContext.getJobConfig().getHandleConfig().getSourceDatabaseType())
                .buildSplitByPrimaryKeyRangeSQL(dumperConfig.getTableName(), dumperConfig.getPrimaryKey());
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            long beginId = 0;
            for (int i = 0; i < Integer.MAX_VALUE; i++) {
                ps.setLong(1, beginId);
                ps.setLong(2, jobContext.getJobConfig().getHandleConfig().getShardingSize());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        log.info("getPositionByPrimaryKeyRange, rs.next false, break");
                        break;
                    }
                    long endId = rs.getLong(1);
                    if (endId == 0) {
                        log.info("getPositionByPrimaryKeyRange, endId is 0, break, tableName={}, primaryKey={}, beginId={}", dumperConfig.getTableName(), dumperConfig.getPrimaryKey(), beginId);
                        break;
                    }
                    result.add(new PrimaryKeyPosition(beginId, endId));
                    beginId = endId + 1;
                }
            }
            // fix empty table missing inventory task
            if (0 == result.size()) {
                result.add(new PrimaryKeyPosition(0, 0));
            }
        } catch (final SQLException ex) {
            throw new PipelineJobPrepareFailedException(String.format("Split task for table %s by primary key %s error", dumperConfig.getTableName(), dumperConfig.getPrimaryKey()), ex);
        }
        return result;
    }
}
