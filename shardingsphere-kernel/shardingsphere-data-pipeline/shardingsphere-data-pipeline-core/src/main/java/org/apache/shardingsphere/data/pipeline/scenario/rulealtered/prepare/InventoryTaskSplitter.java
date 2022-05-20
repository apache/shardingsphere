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
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.RuleAlteredJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.TaskConfiguration;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.IntegerPrimaryKeyPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.PlaceholderPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.PrimaryKeyPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.StringPrimaryKeyPosition;
import org.apache.shardingsphere.data.pipeline.api.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.api.job.progress.JobProgress;
import org.apache.shardingsphere.data.pipeline.api.metadata.LogicTableName;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineJobCreationException;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineJobPrepareFailedException;
import org.apache.shardingsphere.data.pipeline.core.execute.ExecuteEngine;
import org.apache.shardingsphere.data.pipeline.core.metadata.loader.PipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.core.metadata.model.PipelineColumnMetaData;
import org.apache.shardingsphere.data.pipeline.core.metadata.model.PipelineIndexMetaData;
import org.apache.shardingsphere.data.pipeline.core.metadata.model.PipelineTableMetaData;
import org.apache.shardingsphere.data.pipeline.core.sqlbuilder.PipelineSQLBuilderFactory;
import org.apache.shardingsphere.data.pipeline.core.task.InventoryTask;
import org.apache.shardingsphere.data.pipeline.core.util.PipelineJdbcUtils;
import org.apache.shardingsphere.data.pipeline.scenario.rulealtered.RuleAlteredContext;
import org.apache.shardingsphere.data.pipeline.scenario.rulealtered.RuleAlteredJobContext;
import org.apache.shardingsphere.data.pipeline.spi.ingest.channel.PipelineChannelCreator;
import org.apache.shardingsphere.data.pipeline.spi.ratelimit.JobRateLimitAlgorithm;
import org.apache.shardingsphere.infra.config.rulealtered.OnRuleAlteredActionConfiguration.InputConfiguration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
     * @return split inventory data task
     */
    public List<InventoryTask> splitInventoryData(final RuleAlteredJobContext jobContext) {
        List<InventoryTask> result = new LinkedList<>();
        TaskConfiguration taskConfig = jobContext.getTaskConfig();
        PipelineChannelCreator pipelineChannelCreator = jobContext.getRuleAlteredContext().getPipelineChannelCreator();
        PipelineDataSourceManager dataSourceManager = jobContext.getDataSourceManager();
        DataSource dataSource = jobContext.getSourceDataSource();
        PipelineTableMetaDataLoader metaDataLoader = jobContext.getSourceMetaDataLoader();
        ExecuteEngine importerExecuteEngine = jobContext.getRuleAlteredContext().getImporterExecuteEngine();
        for (InventoryDumperConfiguration each : splitDumperConfig(jobContext, taskConfig.getDumperConfig())) {
            result.add(new InventoryTask(each, taskConfig.getImporterConfig(), pipelineChannelCreator, dataSourceManager, dataSource, metaDataLoader, importerExecuteEngine));
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
            // TODO use original table name, for metadata loader
            inventoryDumperConfig.setActualTableName(key.getLowercase());
            inventoryDumperConfig.setLogicTableName(value.getLowercase());
            inventoryDumperConfig.setPosition(new PlaceholderPosition());
            result.add(inventoryDumperConfig);
        });
        return result;
    }
    
    private Collection<InventoryDumperConfiguration> splitByPrimaryKey(final RuleAlteredJobContext jobContext, final DataSource dataSource, final PipelineTableMetaDataLoader metaDataLoader,
                                                                       final InventoryDumperConfiguration dumperConfig) {
        Collection<InventoryDumperConfiguration> result = new LinkedList<>();
        RuleAlteredContext ruleAlteredContext = jobContext.getRuleAlteredContext();
        InputConfiguration inputConfig = ruleAlteredContext.getOnRuleAlteredActionConfig().getInput();
        int batchSize = inputConfig.getBatchSize();
        JobRateLimitAlgorithm rateLimitAlgorithm = ruleAlteredContext.getInputRateLimitAlgorithm();
        Collection<IngestPosition<?>> inventoryPositions = getInventoryPositions(jobContext, dumperConfig, dataSource, metaDataLoader);
        int i = 0;
        for (IngestPosition<?> inventoryPosition : inventoryPositions) {
            InventoryDumperConfiguration splitDumperConfig = new InventoryDumperConfiguration(dumperConfig);
            splitDumperConfig.setPosition(inventoryPosition);
            splitDumperConfig.setShardingItem(i++);
            splitDumperConfig.setActualTableName(dumperConfig.getActualTableName());
            splitDumperConfig.setLogicTableName(dumperConfig.getLogicTableName());
            splitDumperConfig.setUniqueKey(dumperConfig.getUniqueKey());
            splitDumperConfig.setUniqueKeyDataType(dumperConfig.getUniqueKeyDataType());
            splitDumperConfig.setBatchSize(batchSize);
            splitDumperConfig.setRateLimitAlgorithm(rateLimitAlgorithm);
            result.add(splitDumperConfig);
        }
        return result;
    }
    
    private Collection<IngestPosition<?>> getInventoryPositions(final RuleAlteredJobContext jobContext, final InventoryDumperConfiguration dumperConfig,
                                                                final DataSource dataSource, final PipelineTableMetaDataLoader metaDataLoader) {
        JobProgress initProgress = jobContext.getInitProgress();
        String schemaName = dumperConfig.getSchemaName(new LogicTableName(dumperConfig.getLogicTableName()));
        String actualTableName = dumperConfig.getActualTableName();
        PipelineTableMetaData tableMetaData = metaDataLoader.getTableMetaData(schemaName, actualTableName);
        PipelineColumnMetaData uniqueKeyColumn = mustGetAnAppropriateUniqueKeyColumn(tableMetaData, actualTableName);
        if (null != initProgress && initProgress.getStatus() != JobStatus.PREPARING_FAILURE) {
            Collection<IngestPosition<?>> result = initProgress.getInventoryPosition(dumperConfig.getActualTableName()).values();
            for (IngestPosition<?> each : result) {
                if (each instanceof PrimaryKeyPosition) {
                    dumperConfig.setUniqueKey(uniqueKeyColumn.getName());
                    dumperConfig.setUniqueKeyDataType(uniqueKeyColumn.getDataType());
                    break;
                }
            }
            // Do NOT filter FinishedPosition here, since whole inventory tasks are required in job progress when persisting to register center.
            return result;
        }
        dumperConfig.setUniqueKey(uniqueKeyColumn.getName());
        int uniqueKeyDataType = uniqueKeyColumn.getDataType();
        dumperConfig.setUniqueKeyDataType(uniqueKeyDataType);
        if (PipelineJdbcUtils.isIntegerColumn(uniqueKeyDataType)) {
            return getPositionByIntegerPrimaryKeyRange(jobContext, dataSource, dumperConfig);
        } else if (PipelineJdbcUtils.isStringColumn(uniqueKeyDataType)) {
            return getPositionByStringPrimaryKeyRange();
        } else {
            throw new PipelineJobCreationException(String.format("Can not split range for table %s, reason: primary key is not integer or string type", actualTableName));
        }
    }
    
    private PipelineColumnMetaData mustGetAnAppropriateUniqueKeyColumn(final PipelineTableMetaData tableMetaData, final String tableName) {
        if (null == tableMetaData) {
            throw new PipelineJobCreationException(String.format("Can not split range for table %s, reason: can not get table metadata ", tableName));
        }
        List<String> primaryKeys = tableMetaData.getPrimaryKeyColumns();
        if (primaryKeys.size() > 1) {
            throw new PipelineJobCreationException(String.format("Can not split range for table %s, reason: primary key is union primary", tableName));
        }
        if (1 == primaryKeys.size()) {
            return tableMetaData.getColumnMetaData(tableMetaData.getPrimaryKeyColumns().get(0));
        }
        Collection<PipelineIndexMetaData> uniqueIndexes = tableMetaData.getUniqueIndexes();
        if (uniqueIndexes.isEmpty()) {
            throw new PipelineJobCreationException(String.format("Can not split range for table %s, reason: no primary key or unique index", tableName));
        }
        if (1 == uniqueIndexes.size() && 1 == uniqueIndexes.iterator().next().getColumns().size()) {
            PipelineColumnMetaData column = uniqueIndexes.iterator().next().getColumns().get(0);
            if (!column.isNullable()) {
                return column;
            }
        }
        throw new PipelineJobCreationException(
                String.format("Can not split range for table %s, reason: table contains multiple unique index or unique index contains nullable/multiple column(s)", tableName));
    }
    
    private Collection<IngestPosition<?>> getPositionByIntegerPrimaryKeyRange(final RuleAlteredJobContext jobContext, final DataSource dataSource, final InventoryDumperConfiguration dumperConfig) {
        Collection<IngestPosition<?>> result = new LinkedList<>();
        RuleAlteredJobConfiguration jobConfig = jobContext.getJobConfig();
        String sql = PipelineSQLBuilderFactory.getInstance(jobConfig.getSourceDatabaseType())
                .buildSplitByPrimaryKeyRangeSQL(dumperConfig.getSchemaName(new LogicTableName(dumperConfig.getLogicTableName())), dumperConfig.getActualTableName(), dumperConfig.getUniqueKey());
        int shardingSize = jobContext.getRuleAlteredContext().getOnRuleAlteredActionConfig().getInput().getShardingSize();
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)) {
            // TODO query minimum value less than 0
            long beginId = 0;
            for (int i = 0; i < Integer.MAX_VALUE; i++) {
                ps.setLong(1, beginId);
                ps.setLong(2, shardingSize);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        log.info("getPositionByPrimaryKeyRange, rs.next false, break");
                        break;
                    }
                    long endId = rs.getLong(1);
                    if (endId == 0) {
                        log.info("getPositionByPrimaryKeyRange, endId is 0, break, tableName={}, primaryKey={}, beginId={}", dumperConfig.getActualTableName(), dumperConfig.getUniqueKey(), beginId);
                        break;
                    }
                    result.add(new IntegerPrimaryKeyPosition(beginId, endId));
                    beginId = endId + 1;
                }
            }
            // fix empty table missing inventory task
            if (0 == result.size()) {
                result.add(new IntegerPrimaryKeyPosition(0, 0));
            }
        } catch (final SQLException ex) {
            throw new PipelineJobPrepareFailedException(String.format("Split task for table %s by primary key %s error", dumperConfig.getActualTableName(), dumperConfig.getUniqueKey()), ex);
        }
        return result;
    }
    
    private Collection<IngestPosition<?>> getPositionByStringPrimaryKeyRange() {
        Collection<IngestPosition<?>> result = new LinkedList<>();
        result.add(new StringPrimaryKeyPosition("!", "~"));
        return result;
    }
}
