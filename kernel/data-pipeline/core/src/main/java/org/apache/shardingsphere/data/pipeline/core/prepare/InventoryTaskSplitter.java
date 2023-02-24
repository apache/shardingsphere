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
import org.apache.shardingsphere.data.pipeline.api.ingest.position.NoUniqueKeyPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.PlaceholderPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.StringPrimaryKeyPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.UnsupportedKeyPosition;
import org.apache.shardingsphere.data.pipeline.api.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.api.job.progress.InventoryIncrementalJobItemProgress;
import org.apache.shardingsphere.data.pipeline.api.metadata.LogicTableName;
import org.apache.shardingsphere.data.pipeline.api.metadata.model.PipelineColumnMetaData;
import org.apache.shardingsphere.data.pipeline.core.context.InventoryIncrementalJobItemContext;
import org.apache.shardingsphere.data.pipeline.core.context.InventoryIncrementalProcessContext;
import org.apache.shardingsphere.data.pipeline.core.exception.job.SplitPipelineJobByUniqueKeyException;
import org.apache.shardingsphere.data.pipeline.core.metadata.loader.PipelineTableMetaDataUtil;
import org.apache.shardingsphere.data.pipeline.core.task.InventoryTask;
import org.apache.shardingsphere.data.pipeline.core.util.PipelineJdbcUtils;
import org.apache.shardingsphere.data.pipeline.spi.ingest.channel.PipelineChannelCreator;
import org.apache.shardingsphere.data.pipeline.spi.ratelimit.JobRateLimitAlgorithm;
import org.apache.shardingsphere.data.pipeline.spi.sqlbuilder.PipelineSQLBuilder;
import org.apache.shardingsphere.data.pipeline.util.spi.PipelineTypedSPILoader;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

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
        PipelineChannelCreator pipelineChannelCreator = jobItemContext.getJobProcessContext().getPipelineChannelCreator();
        for (InventoryDumperConfiguration each : splitDumperConfig(jobItemContext, dumperConfig)) {
            result.add(new InventoryTask(each, importerConfig, pipelineChannelCreator, jobItemContext.getImporterConnector(), sourceDataSource, jobItemContext.getSourceMetaDataLoader(),
                    jobItemContext.getJobProcessContext().getInventoryDumperExecuteEngine(), jobItemContext.getJobProcessContext().getInventoryImporterExecuteEngine(), jobItemContext));
        }
        log.info("splitInventoryData cost {} ms", System.currentTimeMillis() - startTimeMillis);
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
            // use original table name, for meta data loader, since some database table name case-sensitive
            inventoryDumperConfig.setActualTableName(key.getOriginal());
            inventoryDumperConfig.setLogicTableName(value.getOriginal());
            inventoryDumperConfig.setPosition(new PlaceholderPosition());
            inventoryDumperConfig.setUniqueKeyColumns(dumperConfig.getUniqueKeyColumns());
            result.add(inventoryDumperConfig);
        });
        return result;
    }
    
    private Collection<InventoryDumperConfiguration> splitByPrimaryKey(final InventoryDumperConfiguration dumperConfig, final InventoryIncrementalJobItemContext jobItemContext,
                                                                       final DataSource dataSource) {
        if (null == dumperConfig.getUniqueKeyColumns()) {
            String schemaName = dumperConfig.getSchemaName(new LogicTableName(dumperConfig.getLogicTableName()));
            String actualTableName = dumperConfig.getActualTableName();
            List<PipelineColumnMetaData> uniqueKeyColumns = PipelineTableMetaDataUtil.getUniqueKeyColumns(schemaName, actualTableName, jobItemContext.getSourceMetaDataLoader());
            dumperConfig.setUniqueKeyColumns(uniqueKeyColumns);
        }
        Collection<InventoryDumperConfiguration> result = new LinkedList<>();
        InventoryIncrementalProcessContext jobProcessContext = jobItemContext.getJobProcessContext();
        PipelineReadConfiguration readConfig = jobProcessContext.getPipelineProcessConfig().getRead();
        int batchSize = readConfig.getBatchSize();
        JobRateLimitAlgorithm rateLimitAlgorithm = jobProcessContext.getReadRateLimitAlgorithm();
        Collection<IngestPosition<?>> inventoryPositions = getInventoryPositions(jobItemContext, dumperConfig, dataSource);
        int i = 0;
        for (IngestPosition<?> each : inventoryPositions) {
            InventoryDumperConfiguration splitDumperConfig = new InventoryDumperConfiguration(dumperConfig);
            splitDumperConfig.setPosition(each);
            splitDumperConfig.setShardingItem(i++);
            splitDumperConfig.setActualTableName(dumperConfig.getActualTableName());
            splitDumperConfig.setLogicTableName(dumperConfig.getLogicTableName());
            splitDumperConfig.setUniqueKeyColumns(dumperConfig.getUniqueKeyColumns());
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
        if (!dumperConfig.hasUniqueKey()) {
            return getPositionWithoutUniqueKey(jobItemContext, dataSource, dumperConfig);
        }
        List<PipelineColumnMetaData> uniqueKeyColumns = dumperConfig.getUniqueKeyColumns();
        if (1 == uniqueKeyColumns.size()) {
            int firstColumnDataType = uniqueKeyColumns.get(0).getDataType();
            if (PipelineJdbcUtils.isIntegerColumn(firstColumnDataType)) {
                return getPositionByIntegerUniqueKeyRange(jobItemContext, dataSource, dumperConfig);
            }
            if (PipelineJdbcUtils.isStringColumn(firstColumnDataType)) {
                return getPositionByStringUniqueKeyRange(jobItemContext, dataSource, dumperConfig);
            }
        }
        return getUnsupportedPosition(jobItemContext, dataSource, dumperConfig);
    }
    
    private Collection<IngestPosition<?>> getPositionWithoutUniqueKey(final InventoryIncrementalJobItemContext jobItemContext, final DataSource dataSource,
                                                                      final InventoryDumperConfiguration dumperConfig) {
        long tableRecordsCount = getTableRecordsCount(jobItemContext, dataSource, dumperConfig);
        jobItemContext.updateInventoryRecordsCount(tableRecordsCount);
        return Collections.singletonList(new NoUniqueKeyPosition());
    }
    
    private long getTableRecordsCount(final InventoryIncrementalJobItemContext jobItemContext, final DataSource dataSource, final InventoryDumperConfiguration dumperConfig) {
        PipelineJobConfiguration jobConfig = jobItemContext.getJobConfig();
        String schemaName = dumperConfig.getSchemaName(new LogicTableName(dumperConfig.getLogicTableName()));
        String actualTableName = dumperConfig.getActualTableName();
        PipelineSQLBuilder pipelineSQLBuilder = PipelineTypedSPILoader.getDatabaseTypedService(PipelineSQLBuilder.class, jobConfig.getSourceDatabaseType());
        Optional<String> sql = pipelineSQLBuilder.buildEstimatedCountSQL(schemaName, actualTableName);
        try {
            if (sql.isPresent()) {
                DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, jobConfig.getSourceDatabaseType());
                long result = getEstimatedCount(databaseType, dataSource, sql.get());
                return result > 0 ? result : getCount(dataSource, pipelineSQLBuilder.buildCountSQL(schemaName, actualTableName));
            }
            return getCount(dataSource, pipelineSQLBuilder.buildCountSQL(schemaName, actualTableName));
        } catch (final SQLException ex) {
            String uniqueKey = dumperConfig.hasUniqueKey() ? dumperConfig.getUniqueKeyColumns().get(0).getName() : "";
            throw new SplitPipelineJobByUniqueKeyException(dumperConfig.getActualTableName(), uniqueKey, ex);
        }
    }
    
    private long getEstimatedCount(final DatabaseType databaseType, final DataSource dataSource, final String estimatedCountSQL) throws SQLException {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(estimatedCountSQL)) {
            if (databaseType instanceof MySQLDatabaseType) {
                preparedStatement.setString(1, connection.getCatalog());
            }
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                resultSet.next();
                return resultSet.getLong(1);
            }
        }
    }
    
    private long getCount(final DataSource dataSource, final String countSQL) throws SQLException {
        long startTimeMillis = System.currentTimeMillis();
        long result;
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(countSQL)) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                resultSet.next();
                result = resultSet.getLong(1);
            }
        }
        log.info("getCountSQLResult cost {} ms", System.currentTimeMillis() - startTimeMillis);
        return result;
    }
    
    private Collection<IngestPosition<?>> getPositionByIntegerUniqueKeyRange(final InventoryIncrementalJobItemContext jobItemContext, final DataSource dataSource,
                                                                             final InventoryDumperConfiguration dumperConfig) {
        Collection<IngestPosition<?>> result = new LinkedList<>();
        PipelineJobConfiguration jobConfig = jobItemContext.getJobConfig();
        String uniqueKey = dumperConfig.getUniqueKeyColumns().get(0).getName();
        String sql = PipelineTypedSPILoader.getDatabaseTypedService(PipelineSQLBuilder.class, jobConfig.getSourceDatabaseType())
                .buildSplitByPrimaryKeyRangeSQL(dumperConfig.getSchemaName(new LogicTableName(dumperConfig.getLogicTableName())), dumperConfig.getActualTableName(), uniqueKey);
        int shardingSize = jobItemContext.getJobProcessContext().getPipelineProcessConfig().getRead().getShardingSize();
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            // TODO query minimum value less than 0
            long beginId = 0;
            long recordsCount = 0;
            for (int i = 0; i < Integer.MAX_VALUE; i++) {
                preparedStatement.setLong(1, beginId);
                preparedStatement.setLong(2, shardingSize);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (!resultSet.next()) {
                        break;
                    }
                    long endId = resultSet.getLong(1);
                    recordsCount += resultSet.getLong(2);
                    if (0 == endId) {
                        break;
                    }
                    result.add(new IntegerPrimaryKeyPosition(beginId, endId));
                    beginId = endId + 1;
                }
            }
            jobItemContext.updateInventoryRecordsCount(recordsCount);
            // fix empty table missing inventory task
            if (result.isEmpty()) {
                result.add(new IntegerPrimaryKeyPosition(0, 0));
            }
        } catch (final SQLException ex) {
            throw new SplitPipelineJobByUniqueKeyException(dumperConfig.getActualTableName(), uniqueKey, ex);
        }
        return result;
    }
    
    private Collection<IngestPosition<?>> getPositionByStringUniqueKeyRange(final InventoryIncrementalJobItemContext jobItemContext, final DataSource dataSource,
                                                                            final InventoryDumperConfiguration dumperConfig) {
        long tableRecordsCount = getTableRecordsCount(jobItemContext, dataSource, dumperConfig);
        jobItemContext.updateInventoryRecordsCount(tableRecordsCount);
        Collection<IngestPosition<?>> result = new LinkedList<>();
        result.add(new StringPrimaryKeyPosition(null, null));
        return result;
    }
    
    private Collection<IngestPosition<?>> getUnsupportedPosition(final InventoryIncrementalJobItemContext jobItemContext, final DataSource dataSource,
                                                                 final InventoryDumperConfiguration dumperConfig) {
        long tableRecordsCount = getTableRecordsCount(jobItemContext, dataSource, dumperConfig);
        jobItemContext.updateInventoryRecordsCount(tableRecordsCount);
        return Collections.singletonList(new UnsupportedKeyPosition());
    }
}
