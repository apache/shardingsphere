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

package org.apache.shardingsphere.data.pipeline.scenario.rulealtered.prepare;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.config.ingest.DumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.ingest.InventoryDumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.JobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.TaskConfiguration;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.PlaceholderPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.PrimaryKeyPosition;
import org.apache.shardingsphere.data.pipeline.api.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.api.job.progress.JobProgress;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineJobPrepareFailedException;
import org.apache.shardingsphere.data.pipeline.core.execute.ExecuteEngine;
import org.apache.shardingsphere.data.pipeline.core.metadata.loader.PipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.core.metadata.model.PipelineTableMetaData;
import org.apache.shardingsphere.data.pipeline.core.sqlbuilder.PipelineSQLBuilderFactory;
import org.apache.shardingsphere.data.pipeline.core.task.InventoryTask;
import org.apache.shardingsphere.data.pipeline.scenario.rulealtered.RuleAlteredContext;
import org.apache.shardingsphere.data.pipeline.scenario.rulealtered.RuleAlteredJobContext;
import org.apache.shardingsphere.data.pipeline.spi.ingest.channel.PipelineChannelFactory;
import org.apache.shardingsphere.data.pipeline.spi.ratelimit.JobRateLimitAlgorithm;
import org.apache.shardingsphere.infra.config.rulealtered.OnRuleAlteredActionConfiguration.InputConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.rulealtered.YamlOnRuleAlteredActionConfiguration.YamlInputConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.rulealtered.OnRuleAlteredActionConfigurationYamlSwapper.InputConfigurationSwapper;

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
     * @return split inventory data task
     */
    public List<InventoryTask> splitInventoryData(final RuleAlteredJobContext jobContext) {
        List<InventoryTask> result = new LinkedList<>();
        TaskConfiguration taskConfig = jobContext.getTaskConfig();
        PipelineChannelFactory pipelineChannelFactory = jobContext.getRuleAlteredContext().getPipelineChannelFactory();
        PipelineDataSourceManager dataSourceManager = jobContext.getDataSourceManager();
        DataSource dataSource = jobContext.getSourceDataSource();
        PipelineTableMetaDataLoader metaDataLoader = jobContext.getSourceMetaDataLoader();
        ExecuteEngine importerExecuteEngine = jobContext.getRuleAlteredContext().getImporterExecuteEngine();
        for (InventoryDumperConfiguration each : splitDumperConfig(jobContext, taskConfig.getDumperConfig())) {
            result.add(new InventoryTask(each, taskConfig.getImporterConfig(), pipelineChannelFactory, dataSourceManager, dataSource, metaDataLoader, importerExecuteEngine));
        }
        return result;
    }
    
    private Collection<InventoryDumperConfiguration> splitDumperConfig(final RuleAlteredJobContext jobContext, final DumperConfiguration dumperConfig) {
        Collection<InventoryDumperConfiguration> result = new LinkedList<>();
        DataSource dataSource = jobContext.getSourceDataSource();
        PipelineTableMetaDataLoader metaDataLoader = jobContext.getSourceMetaDataLoader();
        for (InventoryDumperConfiguration each : splitByTable(dumperConfig)) {
            result.addAll(splitByPrimaryKey(jobContext, dataSource, metaDataLoader, each));
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
            final RuleAlteredJobContext jobContext, final DataSource dataSource, final PipelineTableMetaDataLoader metaDataLoader, final InventoryDumperConfiguration dumperConfig) {
        Collection<InventoryDumperConfiguration> result = new LinkedList<>();
        RuleAlteredContext ruleAlteredContext = jobContext.getRuleAlteredContext();
        InputConfiguration inputConfig = ruleAlteredContext.getOnRuleAlteredActionConfig().getInput();
        if (null == inputConfig) {
            inputConfig = new InputConfigurationSwapper().swapToObject(new YamlInputConfiguration());
        }
        int batchSize = inputConfig.getBatchSize();
        JobRateLimitAlgorithm rateLimitAlgorithm = ruleAlteredContext.getInputRateLimitAlgorithm();
        Collection<IngestPosition<?>> inventoryPositions = getInventoryPositions(jobContext, dumperConfig, dataSource, metaDataLoader);
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
    
    private Collection<IngestPosition<?>> getInventoryPositions(final RuleAlteredJobContext jobContext, final InventoryDumperConfiguration dumperConfig,
                                                                final DataSource dataSource, final PipelineTableMetaDataLoader metaDataLoader) {
        JobProgress initProgress = jobContext.getInitProgress();
        if (null != initProgress && initProgress.getStatus() != JobStatus.PREPARING_FAILURE) {
            Collection<IngestPosition<?>> result = initProgress.getInventoryPosition(dumperConfig.getTableName()).values();
            for (IngestPosition<?> each : result) {
                if (each instanceof PrimaryKeyPosition) {
                    String primaryKey = metaDataLoader.getTableMetaData(dumperConfig.getTableName()).getPrimaryKeyColumns().get(0);
                    dumperConfig.setPrimaryKey(primaryKey);
                    break;
                }
            }
            // Do NOT filter FinishedPosition here, since whole inventory tasks are required in job progress when persisting to register center.
            return result;
        }
        PipelineTableMetaData tableMetaData = metaDataLoader.getTableMetaData(dumperConfig.getTableName());
        if (isSpiltByPrimaryKeyRange(tableMetaData, dumperConfig.getTableName())) {
            String primaryKey = tableMetaData.getPrimaryKeyColumns().get(0);
            dumperConfig.setPrimaryKey(primaryKey);
            return getPositionByPrimaryKeyRange(jobContext, dataSource, dumperConfig);
        }
        return Collections.singletonList(new PlaceholderPosition());
    }
    
    private boolean isSpiltByPrimaryKeyRange(final PipelineTableMetaData tableMetaData, final String tableName) {
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
        if (isNotIntegerPrimary(tableMetaData.getColumnMetaData(primaryKeys.get(0)).getDataType())) {
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
        JobConfiguration jobConfig = jobContext.getJobConfig();
        String sql = PipelineSQLBuilderFactory.getSQLBuilder(jobConfig.getHandleConfig().getSourceDatabaseType())
                .buildSplitByPrimaryKeyRangeSQL(dumperConfig.getTableName(), dumperConfig.getPrimaryKey());
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            long beginId = 0;
            for (int i = 0; i < Integer.MAX_VALUE; i++) {
                ps.setLong(1, beginId);
                ps.setLong(2, jobConfig.getHandleConfig().getShardingSize());
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
