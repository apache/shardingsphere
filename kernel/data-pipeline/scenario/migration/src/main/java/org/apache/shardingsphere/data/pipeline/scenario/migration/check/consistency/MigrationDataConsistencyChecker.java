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

package org.apache.shardingsphere.data.pipeline.scenario.migration.check.consistency;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.metadata.SchemaName;
import org.apache.shardingsphere.data.pipeline.api.metadata.SchemaTableName;
import org.apache.shardingsphere.data.pipeline.api.metadata.TableName;
import org.apache.shardingsphere.data.pipeline.api.metadata.loader.PipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.api.metadata.model.PipelineColumnMetaData;
import org.apache.shardingsphere.data.pipeline.api.metadata.model.PipelineTableMetaData;
import org.apache.shardingsphere.data.pipeline.common.context.InventoryIncrementalProcessContext;
import org.apache.shardingsphere.data.pipeline.common.datanode.DataNodeUtils;
import org.apache.shardingsphere.data.pipeline.common.datanode.JobDataNodeEntry;
import org.apache.shardingsphere.data.pipeline.common.datanode.JobDataNodeLine;
import org.apache.shardingsphere.data.pipeline.common.datasource.DefaultPipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.common.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.common.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.common.job.progress.InventoryIncrementalJobItemProgress;
import org.apache.shardingsphere.data.pipeline.common.job.progress.listener.PipelineJobProgressUpdatedParameter;
import org.apache.shardingsphere.data.pipeline.common.metadata.loader.PipelineTableMetaDataUtils;
import org.apache.shardingsphere.data.pipeline.common.metadata.loader.StandardPipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.ConsistencyCheckJobItemProgressContext;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.PipelineDataConsistencyChecker;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.SingleTableInventoryDataConsistencyChecker;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.algorithm.DataConsistencyCalculateAlgorithm;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.DataConsistencyCheckResult;
import org.apache.shardingsphere.data.pipeline.core.exception.data.PipelineTableDataConsistencyCheckLoadingFailedException;
import org.apache.shardingsphere.data.pipeline.core.exception.data.UnsupportedPipelineDatabaseTypeException;
import org.apache.shardingsphere.data.pipeline.scenario.migration.api.impl.MigrationJobAPI;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.MigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.spi.ratelimit.JobRateLimitAlgorithm;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Data consistency checker for migration job.
 */
@Slf4j
public final class MigrationDataConsistencyChecker implements PipelineDataConsistencyChecker {
    
    private final MigrationJobConfiguration jobConfig;
    
    private final JobRateLimitAlgorithm readRateLimitAlgorithm;
    
    private final ConsistencyCheckJobItemProgressContext progressContext;
    
    public MigrationDataConsistencyChecker(final MigrationJobConfiguration jobConfig, final InventoryIncrementalProcessContext processContext,
                                           final ConsistencyCheckJobItemProgressContext progressContext) {
        this.jobConfig = jobConfig;
        readRateLimitAlgorithm = null == processContext ? null : processContext.getReadRateLimitAlgorithm();
        this.progressContext = progressContext;
    }
    
    @Override
    public Map<String, DataConsistencyCheckResult> check(final DataConsistencyCalculateAlgorithm calculateAlgorithm) {
        verifyPipelineDatabaseType(calculateAlgorithm, jobConfig.getSources().values().iterator().next());
        verifyPipelineDatabaseType(calculateAlgorithm, jobConfig.getTarget());
        List<String> sourceTableNames = new LinkedList<>();
        jobConfig.getJobShardingDataNodes().forEach(each -> each.getEntries().forEach(entry -> entry.getDataNodes()
                .forEach(dataNode -> sourceTableNames.add(DataNodeUtils.formatWithSchema(dataNode)))));
        progressContext.setRecordsCount(getRecordsCount());
        progressContext.getTableNames().addAll(sourceTableNames);
        progressContext.onProgressUpdated(new PipelineJobProgressUpdatedParameter(0));
        Map<String, DataConsistencyCheckResult> result = new LinkedHashMap<>();
        PipelineDataSourceManager dataSourceManager = new DefaultPipelineDataSourceManager();
        try {
            AtomicBoolean checkFailed = new AtomicBoolean(false);
            for (JobDataNodeLine each : jobConfig.getJobShardingDataNodes()) {
                each.getEntries().forEach(entry -> entry.getDataNodes().forEach(dataNode -> check(calculateAlgorithm, result, dataSourceManager, checkFailed, each, entry, dataNode)));
            }
        } finally {
            dataSourceManager.close();
        }
        return result;
    }
    
    private void check(final DataConsistencyCalculateAlgorithm calculateAlgorithm, final Map<String, DataConsistencyCheckResult> checkResults, final PipelineDataSourceManager dataSourceManager,
                       final AtomicBoolean checkFailed, final JobDataNodeLine jobDataNodeLine, final JobDataNodeEntry entry, final DataNode dataNode) {
        if (checkFailed.get()) {
            return;
        }
        DataConsistencyCheckResult checkResult = checkSingleTable(entry.getLogicTableName(), dataNode, calculateAlgorithm, dataSourceManager);
        checkResults.put(DataNodeUtils.formatWithSchema(dataNode), checkResult);
        if (!checkResult.isMatched()) {
            log.info("unmatched on table '{}', ignore left tables", jobDataNodeLine);
            checkFailed.set(true);
        }
    }
    
    private DataConsistencyCheckResult checkSingleTable(final String targetTableName, final DataNode dataNode,
                                                        final DataConsistencyCalculateAlgorithm calculateAlgorithm, final PipelineDataSourceManager dataSourceManager) {
        SchemaTableName sourceTable = new SchemaTableName(new SchemaName(dataNode.getSchemaName()), new TableName(dataNode.getTableName()));
        SchemaTableName targetTable = new SchemaTableName(new SchemaName(dataNode.getSchemaName()), new TableName(targetTableName));
        PipelineDataSourceWrapper sourceDataSource = dataSourceManager.getDataSource(jobConfig.getSources().get(dataNode.getDataSourceName()));
        PipelineDataSourceWrapper targetDataSource = dataSourceManager.getDataSource(jobConfig.getTarget());
        PipelineTableMetaDataLoader metaDataLoader = new StandardPipelineTableMetaDataLoader(sourceDataSource);
        PipelineTableMetaData tableMetaData = metaDataLoader.getTableMetaData(dataNode.getSchemaName(), dataNode.getTableName());
        ShardingSpherePreconditions.checkNotNull(tableMetaData, () -> new PipelineTableDataConsistencyCheckLoadingFailedException(dataNode.getSchemaName(), dataNode.getTableName()));
        List<String> columnNames = tableMetaData.getColumnNames();
        List<PipelineColumnMetaData> uniqueKeyColumns = PipelineTableMetaDataUtils.getUniqueKeyColumns(
                sourceTable.getSchemaName().getOriginal(), sourceTable.getTableName().getOriginal(), metaDataLoader);
        PipelineColumnMetaData uniqueKey = uniqueKeyColumns.isEmpty() ? null : uniqueKeyColumns.get(0);
        SingleTableInventoryDataConsistencyChecker singleTableInventoryChecker = new SingleTableInventoryDataConsistencyChecker(
                jobConfig.getJobId(), sourceDataSource, targetDataSource, sourceTable, targetTable, columnNames, uniqueKey, readRateLimitAlgorithm, progressContext);
        return singleTableInventoryChecker.check(calculateAlgorithm);
    }
    
    private void verifyPipelineDatabaseType(final DataConsistencyCalculateAlgorithm calculateAlgorithm, final PipelineDataSourceConfiguration dataSourceConfig) {
        ShardingSpherePreconditions.checkState(calculateAlgorithm.getSupportedDatabaseTypes().contains(dataSourceConfig.getDatabaseType()),
                () -> new UnsupportedPipelineDatabaseTypeException(dataSourceConfig.getDatabaseType()));
    }
    
    private long getRecordsCount() {
        Map<Integer, InventoryIncrementalJobItemProgress> jobProgress = new MigrationJobAPI().getJobProgress(jobConfig);
        return jobProgress.values().stream().filter(Objects::nonNull).mapToLong(InventoryIncrementalJobItemProgress::getProcessedRecordsCount).sum();
    }
}
