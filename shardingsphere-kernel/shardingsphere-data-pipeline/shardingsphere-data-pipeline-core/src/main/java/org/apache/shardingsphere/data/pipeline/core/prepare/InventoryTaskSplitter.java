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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.config.ImporterConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.ingest.InventoryDumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.job.PipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.process.PipelineReadConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.IntegerPrimaryKeyPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.PlaceholderPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.StringPrimaryKeyPosition;
import org.apache.shardingsphere.data.pipeline.api.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.api.job.progress.InventoryIncrementalJobItemProgress;
import org.apache.shardingsphere.data.pipeline.api.metadata.LogicTableName;
import org.apache.shardingsphere.data.pipeline.api.metadata.model.PipelineColumnMetaData;
import org.apache.shardingsphere.data.pipeline.core.context.InventoryIncrementalJobItemContext;
import org.apache.shardingsphere.data.pipeline.core.context.InventoryIncrementalProcessContext;
import org.apache.shardingsphere.data.pipeline.core.exception.job.SplitPipelineJobByRangeException;
import org.apache.shardingsphere.data.pipeline.core.exception.job.SplitPipelineJobByUniqueKeyException;
import org.apache.shardingsphere.data.pipeline.core.metadata.loader.PipelineTableMetaDataUtil;
import org.apache.shardingsphere.data.pipeline.core.sqlbuilder.PipelineSQLBuilderFactory;
import org.apache.shardingsphere.data.pipeline.core.task.InventoryTask;
import org.apache.shardingsphere.data.pipeline.core.util.PipelineJdbcUtils;
import org.apache.shardingsphere.data.pipeline.spi.ingest.channel.PipelineChannelCreator;
import org.apache.shardingsphere.data.pipeline.spi.ratelimit.JobRateLimitAlgorithm;

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
@RequiredArgsConstructor
public final class InventoryTaskSplitter {
    
    private final PipelineDataSourceWrapper sourceDataSource;
    
    private final InventoryDumperConfiguration dumperConfig;
    
    private final ImporterConfiguration importerConfig;
    
    /**
     * Split inventory data to multi-tasks.
     *
     * @param jobItemContext job item context
     * @return split inventory data task
     */
    public List<InventoryTask> splitInventoryData(final InventoryIncrementalJobItemContext jobItemContext) {
        List<InventoryTask> result = new LinkedList<>();
        PipelineChannelCreator pipelineChannelCreator = jobItemContext.getJobProcessContext().getPipelineChannelCreator();
        for (InventoryDumperConfiguration each : splitDumperConfig(jobItemContext, dumperConfig)) {
            result.add(new InventoryTask(each, importerConfig, pipelineChannelCreator, jobItemContext.getDataSourceManager(), sourceDataSource, jobItemContext.getSourceMetaDataLoader(),
                    jobItemContext.getJobProcessContext().getInventoryDumperExecuteEngine(), jobItemContext.getJobProcessContext().getImporterExecuteEngine(), jobItemContext));
        }
        return result;
    }
    
    private Collection<InventoryDumperConfiguration> splitDumperConfig(final InventoryIncrementalJobItemContext jobItemContext, final InventoryDumperConfiguration dumperConfig) {
        Collection<InventoryDumperConfiguration> result = new LinkedList<>();
        for (InventoryDumperConfiguration each : splitByTable(dumperConfig)) {
            result.addAll(splitByPrimaryKey(each, jobItemContext, sourceDataSource));
        }
        return result;
    }
    
    private Collection<InventoryDumperConfiguration> splitByTable(final InventoryDumperConfiguration dumperConfig) {
        Collection<InventoryDumperConfiguration> result = new LinkedList<>();
        dumperConfig.getTableNameMap().forEach((key, value) -> {
            InventoryDumperConfiguration inventoryDumperConfig = new InventoryDumperConfiguration(dumperConfig);
            // use original table name, for metadata loader, since some database table name case-sensitive
            inventoryDumperConfig.setActualTableName(key.getOriginal());
            inventoryDumperConfig.setLogicTableName(value.getOriginal());
            inventoryDumperConfig.setPosition(new PlaceholderPosition());
            inventoryDumperConfig.setUniqueKey(dumperConfig.getUniqueKey());
            inventoryDumperConfig.setUniqueKeyDataType(dumperConfig.getUniqueKeyDataType());
            result.add(inventoryDumperConfig);
        });
        return result;
    }
    
    private Collection<InventoryDumperConfiguration> splitByPrimaryKey(final InventoryDumperConfiguration dumperConfig, final InventoryIncrementalJobItemContext jobItemContext,
                                                                       final DataSource dataSource) {
        if (null == dumperConfig.getUniqueKey()) {
            String schemaName = dumperConfig.getSchemaName(new LogicTableName(dumperConfig.getLogicTableName()));
            String actualTableName = dumperConfig.getActualTableName();
            PipelineColumnMetaData uniqueKeyColumn = PipelineTableMetaDataUtil.getUniqueKeyColumn(schemaName, actualTableName, jobItemContext.getSourceMetaDataLoader());
            dumperConfig.setUniqueKey(uniqueKeyColumn.getName());
            dumperConfig.setUniqueKeyDataType(uniqueKeyColumn.getDataType());
        }
        Collection<InventoryDumperConfiguration> result = new LinkedList<>();
        InventoryIncrementalProcessContext jobProcessContext = jobItemContext.getJobProcessContext();
        PipelineReadConfiguration readConfig = jobProcessContext.getPipelineProcessConfig().getRead();
        int batchSize = readConfig.getBatchSize();
        JobRateLimitAlgorithm rateLimitAlgorithm = jobProcessContext.getReadRateLimitAlgorithm();
        Collection<IngestPosition<?>> inventoryPositions = getInventoryPositions(jobItemContext, dumperConfig, dataSource);
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
    
    private Collection<IngestPosition<?>> getInventoryPositions(final InventoryIncrementalJobItemContext jobItemContext, final InventoryDumperConfiguration dumperConfig,
                                                                final DataSource dataSource) {
        InventoryIncrementalJobItemProgress initProgress = jobItemContext.getInitProgress();
        if (null != initProgress && initProgress.getStatus() != JobStatus.PREPARING_FAILURE) {
            // Do NOT filter FinishedPosition here, since whole inventory tasks are required in job progress when persisting to register center.
            return initProgress.getInventory().getInventoryPosition(dumperConfig.getActualTableName()).values();
        }
        int uniqueKeyDataType = dumperConfig.getUniqueKeyDataType();
        if (PipelineJdbcUtils.isIntegerColumn(uniqueKeyDataType)) {
            return getPositionByIntegerPrimaryKeyRange(jobItemContext, dataSource, dumperConfig);
        }
        if (PipelineJdbcUtils.isStringColumn(uniqueKeyDataType)) {
            return getPositionByStringPrimaryKeyRange();
        }
        throw new SplitPipelineJobByRangeException(dumperConfig.getActualTableName(), "primary key is not integer or string type");
    }
    
    private Collection<IngestPosition<?>> getPositionByIntegerPrimaryKeyRange(final InventoryIncrementalJobItemContext jobItemContext, final DataSource dataSource,
                                                                              final InventoryDumperConfiguration dumperConfig) {
        Collection<IngestPosition<?>> result = new LinkedList<>();
        PipelineJobConfiguration jobConfig = jobItemContext.getJobConfig();
        String sql = PipelineSQLBuilderFactory.getInstance(jobConfig.getSourceDatabaseType())
                .buildSplitByPrimaryKeyRangeSQL(dumperConfig.getSchemaName(new LogicTableName(dumperConfig.getLogicTableName())), dumperConfig.getActualTableName(), dumperConfig.getUniqueKey());
        int shardingSize = jobItemContext.getJobProcessContext().getPipelineProcessConfig().getRead().getShardingSize();
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
                    if (0 == endId) {
                        log.info("getPositionByPrimaryKeyRange, endId is 0, break, tableName={}, primaryKey={}, beginId={}", dumperConfig.getActualTableName(), dumperConfig.getUniqueKey(), beginId);
                        break;
                    }
                    result.add(new IntegerPrimaryKeyPosition(beginId, endId));
                    beginId = endId + 1;
                }
            }
            // fix empty table missing inventory task
            if (result.isEmpty()) {
                result.add(new IntegerPrimaryKeyPosition(0, 0));
            }
        } catch (final SQLException ex) {
            throw new SplitPipelineJobByUniqueKeyException(dumperConfig.getActualTableName(), dumperConfig.getUniqueKey(), ex);
        }
        return result;
    }
    
    private Collection<IngestPosition<?>> getPositionByStringPrimaryKeyRange() {
        Collection<IngestPosition<?>> result = new LinkedList<>();
        result.add(new StringPrimaryKeyPosition("!", "~"));
        return result;
    }
}
