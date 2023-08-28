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
import org.apache.shardingsphere.data.pipeline.api.metadata.SchemaTableName;
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
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.TableDataConsistencyCheckResult;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.table.TableDataConsistencyChecker;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.table.TableDataConsistencyCheckerFactory;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.table.TableInventoryCheckParameter;
import org.apache.shardingsphere.data.pipeline.core.exception.data.PipelineTableDataConsistencyCheckLoadingFailedException;
import org.apache.shardingsphere.data.pipeline.scenario.migration.api.impl.MigrationJobAPI;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.MigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.spi.ratelimit.JobRateLimitAlgorithm;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

/**
 * Data consistency checker for migration job.
 */
@Slf4j
public final class MigrationDataConsistencyChecker implements PipelineDataConsistencyChecker {
    
    private final MigrationJobConfiguration jobConfig;
    
    private final JobRateLimitAlgorithm readRateLimitAlgorithm;
    
    private final ConsistencyCheckJobItemProgressContext progressContext;
    
    private final Set<TableDataConsistencyChecker> tableCheckers = new HashSet<>();
    
    public MigrationDataConsistencyChecker(final MigrationJobConfiguration jobConfig, final InventoryIncrementalProcessContext processContext,
                                           final ConsistencyCheckJobItemProgressContext progressContext) {
        this.jobConfig = jobConfig;
        readRateLimitAlgorithm = null == processContext ? null : processContext.getReadRateLimitAlgorithm();
        this.progressContext = progressContext;
    }
    
    @Override
    public Map<String, TableDataConsistencyCheckResult> check(final String algorithmType, final Properties algorithmProps) {
        List<String> sourceTableNames = new LinkedList<>();
        jobConfig.getJobShardingDataNodes().forEach(each -> each.getEntries().forEach(entry -> entry.getDataNodes()
                .forEach(dataNode -> sourceTableNames.add(DataNodeUtils.formatWithSchema(dataNode)))));
        progressContext.setRecordsCount(getRecordsCount());
        progressContext.getTableNames().addAll(sourceTableNames);
        progressContext.onProgressUpdated(new PipelineJobProgressUpdatedParameter(0));
        Map<String, TableDataConsistencyCheckResult> result = new LinkedHashMap<>();
        try (PipelineDataSourceManager dataSourceManager = new DefaultPipelineDataSourceManager()) {
            for (JobDataNodeLine each : jobConfig.getJobShardingDataNodes()) {
                checkTableInventoryData(each, algorithmType, algorithmProps, result, dataSourceManager);
            }
        }
        return result;
    }
    
    private long getRecordsCount() {
        Map<Integer, InventoryIncrementalJobItemProgress> jobProgress = new MigrationJobAPI().getJobProgress(jobConfig);
        return jobProgress.values().stream().filter(Objects::nonNull).mapToLong(InventoryIncrementalJobItemProgress::getProcessedRecordsCount).sum();
    }
    
    private void checkTableInventoryData(final JobDataNodeLine jobDataNodeLine, final String algorithmType, final Properties algorithmProps,
                                         final Map<String, TableDataConsistencyCheckResult> checkResults, final PipelineDataSourceManager dataSourceManager) {
        for (JobDataNodeEntry entry : jobDataNodeLine.getEntries()) {
            for (DataNode each : entry.getDataNodes()) {
                TableDataConsistencyChecker tableChecker = TableDataConsistencyCheckerFactory.newInstance(algorithmType, algorithmProps);
                tableCheckers.add(tableChecker);
                TableDataConsistencyCheckResult checkResult = checkSingleTableInventoryData(entry.getLogicTableName(), each, tableChecker, dataSourceManager);
                checkResults.put(DataNodeUtils.formatWithSchema(each), checkResult);
                tableCheckers.remove(null);
                if (!checkResult.isMatched() && tableChecker.isBreakOnInventoryCheckNotMatched()) {
                    log.info("Unmatched on table '{}', ignore left tables", DataNodeUtils.formatWithSchema(each));
                    return;
                }
            }
        }
    }
    
    private TableDataConsistencyCheckResult checkSingleTableInventoryData(final String targetTableName, final DataNode dataNode,
                                                                          final TableDataConsistencyChecker tableChecker, final PipelineDataSourceManager dataSourceManager) {
        SchemaTableName sourceTable = new SchemaTableName(dataNode.getSchemaName(), dataNode.getTableName());
        SchemaTableName targetTable = new SchemaTableName(dataNode.getSchemaName(), targetTableName);
        PipelineDataSourceWrapper sourceDataSource = dataSourceManager.getDataSource(jobConfig.getSources().get(dataNode.getDataSourceName()));
        PipelineDataSourceWrapper targetDataSource = dataSourceManager.getDataSource(jobConfig.getTarget());
        PipelineTableMetaDataLoader metaDataLoader = new StandardPipelineTableMetaDataLoader(sourceDataSource);
        PipelineTableMetaData tableMetaData = metaDataLoader.getTableMetaData(dataNode.getSchemaName(), dataNode.getTableName());
        ShardingSpherePreconditions.checkNotNull(tableMetaData, () -> new PipelineTableDataConsistencyCheckLoadingFailedException(dataNode.getSchemaName(), dataNode.getTableName()));
        List<String> columnNames = tableMetaData.getColumnNames();
        List<PipelineColumnMetaData> uniqueKeys = PipelineTableMetaDataUtils.getUniqueKeyColumns(
                sourceTable.getSchemaName().getOriginal(), sourceTable.getTableName().getOriginal(), metaDataLoader);
        TableInventoryCheckParameter param = new TableInventoryCheckParameter(
                jobConfig.getJobId(), sourceDataSource, targetDataSource, sourceTable, targetTable, columnNames, uniqueKeys, readRateLimitAlgorithm, progressContext);
        return tableChecker.checkSingleTableInventoryData(param);
    }
    
    @Override
    public void cancel() {
        for (TableDataConsistencyChecker each : tableCheckers) {
            each.cancel();
        }
    }
    
    @Override
    public boolean isCanceling() {
        return tableCheckers.stream().anyMatch(TableDataConsistencyChecker::isCanceling);
    }
}
