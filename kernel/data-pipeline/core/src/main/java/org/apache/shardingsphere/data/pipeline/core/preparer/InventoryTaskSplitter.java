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

package org.apache.shardingsphere.data.pipeline.core.preparer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Range;
import org.apache.shardingsphere.data.pipeline.api.config.ingest.InventoryDumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.ingest.channel.PipelineChannel;
import org.apache.shardingsphere.data.pipeline.api.ingest.dumper.Dumper;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.api.metadata.LogicTableName;
import org.apache.shardingsphere.data.pipeline.api.metadata.model.PipelineColumnMetaData;
import org.apache.shardingsphere.data.pipeline.common.config.ImporterConfiguration;
import org.apache.shardingsphere.data.pipeline.common.config.process.PipelineReadConfiguration;
import org.apache.shardingsphere.data.pipeline.common.context.InventoryIncrementalJobItemContext;
import org.apache.shardingsphere.data.pipeline.common.context.InventoryIncrementalProcessContext;
import org.apache.shardingsphere.data.pipeline.common.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.common.ingest.position.pk.type.IntegerPrimaryKeyPosition;
import org.apache.shardingsphere.data.pipeline.common.ingest.position.PlaceholderPosition;
import org.apache.shardingsphere.data.pipeline.common.ingest.position.pk.type.StringPrimaryKeyPosition;
import org.apache.shardingsphere.data.pipeline.common.ingest.position.pk.type.UnsupportedKeyPosition;
import org.apache.shardingsphere.data.pipeline.common.job.progress.InventoryIncrementalJobItemProgress;
import org.apache.shardingsphere.data.pipeline.common.metadata.loader.PipelineTableMetaDataUtils;
import org.apache.shardingsphere.data.pipeline.common.sqlbuilder.PipelineCommonSQLBuilder;
import org.apache.shardingsphere.data.pipeline.common.util.IntervalToRangeIterator;
import org.apache.shardingsphere.data.pipeline.common.util.PipelineJdbcUtils;
import org.apache.shardingsphere.data.pipeline.core.dumper.InventoryDumper;
import org.apache.shardingsphere.data.pipeline.core.exception.job.SplitPipelineJobByUniqueKeyException;
import org.apache.shardingsphere.data.pipeline.core.importer.Importer;
import org.apache.shardingsphere.data.pipeline.core.importer.SingleChannelConsumerImporter;
import org.apache.shardingsphere.data.pipeline.core.task.InventoryTask;
import org.apache.shardingsphere.data.pipeline.core.task.PipelineTaskUtils;
import org.apache.shardingsphere.data.pipeline.spi.ratelimit.JobRateLimitAlgorithm;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Inventory data task splitter.
 */
@RequiredArgsConstructor
@Slf4j
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
        long startTimeMillis = System.currentTimeMillis();
        InventoryIncrementalProcessContext processContext = jobItemContext.getJobProcessContext();
        for (InventoryDumperConfiguration each : splitInventoryDumperConfig(jobItemContext)) {
            AtomicReference<IngestPosition> position = new AtomicReference<>(each.getPosition());
            PipelineChannel channel = PipelineTaskUtils.createInventoryChannel(processContext.getPipelineChannelCreator(), importerConfig.getBatchSize(), position);
            Dumper dumper = new InventoryDumper(each, channel, sourceDataSource, jobItemContext.getSourceMetaDataLoader());
            Importer importer = new SingleChannelConsumerImporter(channel, importerConfig.getBatchSize(), 3, TimeUnit.SECONDS, jobItemContext.getSink(), jobItemContext);
            result.add(new InventoryTask(PipelineTaskUtils.generateInventoryTaskId(each), processContext.getInventoryDumperExecuteEngine(),
                    processContext.getInventoryImporterExecuteEngine(), dumper, importer, position));
        }
        log.info("splitInventoryData cost {} ms", System.currentTimeMillis() - startTimeMillis);
        return result;
    }
    
    /**
     * Split inventory dumper configuration.
     *
     * @param jobItemContext job item context
     * @return inventory dumper configurations
     */
    public Collection<InventoryDumperConfiguration> splitInventoryDumperConfig(final InventoryIncrementalJobItemContext jobItemContext) {
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
            inventoryDumperConfig.setInsertColumnNames(dumperConfig.getInsertColumnNames());
            inventoryDumperConfig.setUniqueKeyColumns(dumperConfig.getUniqueKeyColumns());
            result.add(inventoryDumperConfig);
        });
        return result;
    }
    
    private Collection<InventoryDumperConfiguration> splitByPrimaryKey(final InventoryDumperConfiguration dumperConfig, final InventoryIncrementalJobItemContext jobItemContext,
                                                                       final PipelineDataSourceWrapper dataSource) {
        if (null == dumperConfig.getUniqueKeyColumns()) {
            String schemaName = dumperConfig.getSchemaName(new LogicTableName(dumperConfig.getLogicTableName()));
            String actualTableName = dumperConfig.getActualTableName();
            List<PipelineColumnMetaData> uniqueKeyColumns = PipelineTableMetaDataUtils.getUniqueKeyColumns(schemaName, actualTableName, jobItemContext.getSourceMetaDataLoader());
            dumperConfig.setUniqueKeyColumns(uniqueKeyColumns);
        }
        Collection<InventoryDumperConfiguration> result = new LinkedList<>();
        InventoryIncrementalProcessContext jobProcessContext = jobItemContext.getJobProcessContext();
        PipelineReadConfiguration readConfig = jobProcessContext.getPipelineProcessConfig().getRead();
        int batchSize = readConfig.getBatchSize();
        JobRateLimitAlgorithm rateLimitAlgorithm = jobProcessContext.getReadRateLimitAlgorithm();
        Collection<IngestPosition> inventoryPositions = getInventoryPositions(dumperConfig, jobItemContext, dataSource);
        int i = 0;
        for (IngestPosition each : inventoryPositions) {
            InventoryDumperConfiguration splitDumperConfig = new InventoryDumperConfiguration(dumperConfig);
            splitDumperConfig.setPosition(each);
            splitDumperConfig.setShardingItem(i++);
            splitDumperConfig.setActualTableName(dumperConfig.getActualTableName());
            splitDumperConfig.setLogicTableName(dumperConfig.getLogicTableName());
            splitDumperConfig.setUniqueKeyColumns(dumperConfig.getUniqueKeyColumns());
            splitDumperConfig.setInsertColumnNames(dumperConfig.getInsertColumnNames());
            splitDumperConfig.setBatchSize(batchSize);
            splitDumperConfig.setRateLimitAlgorithm(rateLimitAlgorithm);
            result.add(splitDumperConfig);
        }
        return result;
    }
    
    private Collection<IngestPosition> getInventoryPositions(final InventoryDumperConfiguration dumperConfig, final InventoryIncrementalJobItemContext jobItemContext,
                                                             final PipelineDataSourceWrapper dataSource) {
        InventoryIncrementalJobItemProgress initProgress = jobItemContext.getInitProgress();
        if (null != initProgress) {
            // Do NOT filter FinishedPosition here, since whole inventory tasks are required in job progress when persisting to register center.
            Collection<IngestPosition> result = initProgress.getInventory().getInventoryPosition(dumperConfig.getActualTableName()).values();
            if (!result.isEmpty()) {
                return result;
            }
        }
        long tableRecordsCount = InventoryRecordsCountCalculator.getTableRecordsCount(dumperConfig, dataSource);
        jobItemContext.updateInventoryRecordsCount(tableRecordsCount);
        if (!dumperConfig.hasUniqueKey()) {
            return Collections.singleton(new UnsupportedKeyPosition());
        }
        List<PipelineColumnMetaData> uniqueKeyColumns = dumperConfig.getUniqueKeyColumns();
        if (1 == uniqueKeyColumns.size()) {
            int firstColumnDataType = uniqueKeyColumns.get(0).getDataType();
            if (PipelineJdbcUtils.isIntegerColumn(firstColumnDataType)) {
                return getPositionByIntegerUniqueKeyRange(dumperConfig, tableRecordsCount, jobItemContext, dataSource);
            }
            if (PipelineJdbcUtils.isStringColumn(firstColumnDataType)) {
                // TODO Support string unique key table splitting. Ascii characters ordering are different in different versions of databases.
                return Collections.singleton(new StringPrimaryKeyPosition(null, null));
            }
        }
        return Collections.singleton(new UnsupportedKeyPosition());
    }
    
    private Collection<IngestPosition> getPositionByIntegerUniqueKeyRange(final InventoryDumperConfiguration dumperConfig, final long tableRecordsCount,
                                                                          final InventoryIncrementalJobItemContext jobItemContext, final PipelineDataSourceWrapper dataSource) {
        if (0 == tableRecordsCount) {
            return Collections.singletonList(new IntegerPrimaryKeyPosition(0, 0));
        }
        Collection<IngestPosition> result = new LinkedList<>();
        Range<Long> uniqueKeyValuesRange = getUniqueKeyValuesRange(jobItemContext, dataSource, dumperConfig);
        int shardingSize = jobItemContext.getJobProcessContext().getPipelineProcessConfig().getRead().getShardingSize();
        long splitCount = tableRecordsCount / shardingSize + (tableRecordsCount % shardingSize > 0 ? 1 : 0);
        long interval = (uniqueKeyValuesRange.getMaximum() - uniqueKeyValuesRange.getMinimum()) / splitCount;
        IntervalToRangeIterator rangeIterator = new IntervalToRangeIterator(uniqueKeyValuesRange.getMinimum(), uniqueKeyValuesRange.getMaximum(), interval);
        while (rangeIterator.hasNext()) {
            Range<Long> range = rangeIterator.next();
            result.add(new IntegerPrimaryKeyPosition(range.getMinimum(), range.getMaximum()));
        }
        return result;
    }
    
    private Range<Long> getUniqueKeyValuesRange(final InventoryIncrementalJobItemContext jobItemContext, final DataSource dataSource, final InventoryDumperConfiguration dumperConfig) {
        String uniqueKey = dumperConfig.getUniqueKeyColumns().get(0).getName();
        PipelineCommonSQLBuilder pipelineSQLBuilder = new PipelineCommonSQLBuilder(jobItemContext.getJobConfig().getSourceDatabaseType());
        String sql = pipelineSQLBuilder.buildUniqueKeyMinMaxValuesSQL(dumperConfig.getSchemaName(new LogicTableName(dumperConfig.getLogicTableName())), dumperConfig.getActualTableName(), uniqueKey);
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql)) {
            resultSet.next();
            return Range.between(resultSet.getLong(1), resultSet.getLong(2));
        } catch (final SQLException ex) {
            throw new SplitPipelineJobByUniqueKeyException(dumperConfig.getActualTableName(), uniqueKey, ex);
        }
    }
}
