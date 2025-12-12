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
import org.apache.shardingsphere.data.pipeline.api.type.ShardingSpherePipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.ConsistencyCheckJobItemProgressContext;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.PipelineDataConsistencyChecker;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.position.TableCheckRangePosition;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.TableDataConsistencyCheckResult;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.table.TableDataConsistencyChecker;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.table.TableDataConsistencyCheckerFactory;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.table.TableInventoryCheckParameter;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.table.TableInventoryChecker;
import org.apache.shardingsphere.data.pipeline.core.context.TransmissionProcessContext;
import org.apache.shardingsphere.data.pipeline.core.datanode.DataNodeUtils;
import org.apache.shardingsphere.data.pipeline.core.datanode.JobDataNodeEntry;
import org.apache.shardingsphere.data.pipeline.core.datanode.JobDataNodeLine;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSource;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.exception.data.PipelineTableDataConsistencyCheckLoadingFailedException;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.pk.type.UnsupportedKeyIngestPosition;
import org.apache.shardingsphere.data.pipeline.core.job.progress.TransmissionJobItemProgress;
import org.apache.shardingsphere.data.pipeline.core.job.progress.listener.PipelineJobUpdateProgress;
import org.apache.shardingsphere.data.pipeline.core.job.service.TransmissionJobManager;
import org.apache.shardingsphere.data.pipeline.core.metadata.loader.PipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.core.metadata.loader.PipelineTableMetaDataUtils;
import org.apache.shardingsphere.data.pipeline.core.metadata.loader.StandardPipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.core.metadata.model.PipelineColumnMetaData;
import org.apache.shardingsphere.data.pipeline.core.metadata.model.PipelineTableMetaData;
import org.apache.shardingsphere.data.pipeline.core.ratelimit.JobRateLimitAlgorithm;
import org.apache.shardingsphere.data.pipeline.core.util.PipelineDataSourceConfigurationUtils;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJobType;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.MigrationJobConfiguration;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Data consistency checker for migration job.
 */
@Slf4j
public final class MigrationDataConsistencyChecker implements PipelineDataConsistencyChecker {
    
    private final MigrationJobConfiguration jobConfig;
    
    private final JobRateLimitAlgorithm readRateLimitAlgorithm;
    
    private final ConsistencyCheckJobItemProgressContext progressContext;
    
    private final AtomicReference<TableInventoryChecker> currentTableInventoryChecker = new AtomicReference<>();
    
    private final AtomicBoolean canceling = new AtomicBoolean(false);
    
    public MigrationDataConsistencyChecker(final MigrationJobConfiguration jobConfig, final TransmissionProcessContext processContext,
                                           final ConsistencyCheckJobItemProgressContext progressContext) {
        this.jobConfig = jobConfig;
        readRateLimitAlgorithm = processContext.getReadRateLimitAlgorithm();
        this.progressContext = progressContext;
    }
    
    @Override
    public Map<String, TableDataConsistencyCheckResult> check(final String algorithmType, final Properties algorithmProps) {
        List<String> sourceTableNames = new LinkedList<>();
        jobConfig.getJobShardingDataNodes().forEach(each -> each.getEntries().forEach(entry -> entry.getDataNodes()
                .forEach(dataNode -> sourceTableNames.add(new QualifiedTable(dataNode.getSchemaName(), dataNode.getTableName()).format()))));
        progressContext.setRecordsCount(getRecordsCount());
        progressContext.getTableNames().addAll(sourceTableNames);
        progressContext.onProgressUpdated(new PipelineJobUpdateProgress(0));
        Map<QualifiedTable, TableDataConsistencyCheckResult> checkResultMap = new LinkedHashMap<>();
        try (
                PipelineDataSourceManager dataSourceManager = new PipelineDataSourceManager();
                TableDataConsistencyChecker tableChecker = TableDataConsistencyCheckerFactory.newInstance(algorithmType, algorithmProps)) {
            PipelineDataSourceConfigurationUtils.transformPipelineDataSourceConfiguration(jobConfig.getJobId(), (ShardingSpherePipelineDataSourceConfiguration) jobConfig.getTarget());
            if (progressContext.getTableCheckRangePositions().isEmpty()) {
                progressContext.getTableCheckRangePositions().addAll(splitCrossTables());
            }
            for (TableCheckRangePosition each : progressContext.getTableCheckRangePositions()) {
                TableDataConsistencyCheckResult checkResult = checkSingleTableInventoryData(each, tableChecker, dataSourceManager);
                log.info("checkResult: {}, table: {}, checkRangePosition: {}", checkResult, each.getSourceDataNode(), each);
                DataNode dataNode = DataNodeUtils.parseWithSchema(each.getSourceDataNode());
                QualifiedTable sourceTable = new QualifiedTable(dataNode.getSchemaName(), dataNode.getTableName());
                checkResultMap.put(sourceTable, checkResult);
                if (checkResult.isIgnored()) {
                    progressContext.getIgnoredTableNames().add(sourceTable.format());
                    log.info("Table '{}' is ignored, ignore type: {}", each.getSourceDataNode(), checkResult.getIgnoredType());
                    continue;
                }
                if (!checkResult.isMatched() && tableChecker.isBreakOnInventoryCheckNotMatched()) {
                    log.info("Unmatched on table '{}', ignore left tables", each.getSourceDataNode());
                    cancel();
                    return checkResultMap.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey().toString(), Entry::getValue));
                }
            }
        }
        log.info("check done, jobId={}", jobConfig.getJobId());
        return checkResultMap.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey().format(), Entry::getValue));
    }
    
    private long getRecordsCount() {
        Map<Integer, TransmissionJobItemProgress> jobProgress = new TransmissionJobManager(new MigrationJobType()).getJobProgress(jobConfig);
        return jobProgress.values().stream().filter(Objects::nonNull).mapToLong(TransmissionJobItemProgress::getInventoryRecordsCount).sum();
    }
    
    private List<TableCheckRangePosition> splitCrossTables() {
        List<TableCheckRangePosition> result = new LinkedList<>();
        int splittingItem = 0;
        for (JobDataNodeLine each : jobConfig.getJobShardingDataNodes()) {
            for (JobDataNodeEntry entry : each.getEntries()) {
                for (DataNode dataNode : entry.getDataNodes()) {
                    result.add(new TableCheckRangePosition(splittingItem++, dataNode.format(), entry.getLogicTableName(),
                            new UnsupportedKeyIngestPosition(), new UnsupportedKeyIngestPosition(), null));
                }
            }
        }
        return result;
    }
    
    private TableDataConsistencyCheckResult checkSingleTableInventoryData(final TableCheckRangePosition checkRangePosition,
                                                                          final TableDataConsistencyChecker tableChecker, final PipelineDataSourceManager dataSourceManager) {
        log.info("checkSingleTableInventoryData, jobId: {}, checkRangePosition: {}", jobConfig.getJobId(), checkRangePosition);
        DataNode dataNode = DataNodeUtils.parseWithSchema(checkRangePosition.getSourceDataNode());
        QualifiedTable sourceTable = new QualifiedTable(dataNode.getSchemaName(), dataNode.getTableName());
        PipelineDataSource sourceDataSource = dataSourceManager.getDataSource(jobConfig.getSources().get(dataNode.getDataSourceName()));
        PipelineTableMetaDataLoader metaDataLoader = new StandardPipelineTableMetaDataLoader(sourceDataSource);
        PipelineTableMetaData tableMetaData = metaDataLoader.getTableMetaData(dataNode.getSchemaName(), dataNode.getTableName());
        ShardingSpherePreconditions.checkNotNull(tableMetaData,
                () -> new PipelineTableDataConsistencyCheckLoadingFailedException(new QualifiedTable(dataNode.getSchemaName(), dataNode.getTableName())));
        String targetTableName = checkRangePosition.getLogicTableName();
        List<String> columnNames = tableMetaData.getColumnNames();
        List<PipelineColumnMetaData> uniqueKeys = PipelineTableMetaDataUtils.getUniqueKeyColumns(sourceTable.getSchemaName(), sourceTable.getTableName(), metaDataLoader);
        QualifiedTable targetTable = new QualifiedTable(dataNode.getSchemaName(), targetTableName);
        PipelineDataSource targetDataSource = dataSourceManager.getDataSource(jobConfig.getTarget());
        TableInventoryCheckParameter param = new TableInventoryCheckParameter(
                jobConfig.getJobId(), checkRangePosition.getSplittingItem(), sourceDataSource, targetDataSource, sourceTable, targetTable, columnNames, uniqueKeys,
                readRateLimitAlgorithm, progressContext, checkRangePosition.getQueryCondition());
        TableInventoryChecker tableInventoryChecker = tableChecker.buildTableInventoryChecker(param);
        currentTableInventoryChecker.set(tableInventoryChecker);
        Optional<TableDataConsistencyCheckResult> preCheckResult = tableInventoryChecker.preCheck();
        TableDataConsistencyCheckResult result = preCheckResult.orElseGet(tableInventoryChecker::checkSingleTableInventoryData);
        tableInventoryChecker.cancel();
        currentTableInventoryChecker.set(null);
        return result;
    }
    
    @Override
    public void cancel() {
        canceling.set(true);
        Optional.ofNullable(currentTableInventoryChecker.get()).ifPresent(TableInventoryChecker::cancel);
        currentTableInventoryChecker.set(null);
    }
    
    @Override
    public boolean isCanceling() {
        return canceling.get();
    }
}
