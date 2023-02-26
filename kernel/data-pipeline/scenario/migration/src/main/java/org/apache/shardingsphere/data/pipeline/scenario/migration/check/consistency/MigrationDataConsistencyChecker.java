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
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCheckResult;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.PipelineDataConsistencyChecker;
import org.apache.shardingsphere.data.pipeline.api.config.job.MigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datanode.DataNodeUtil;
import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.job.progress.InventoryIncrementalJobItemProgress;
import org.apache.shardingsphere.data.pipeline.api.metadata.SchemaName;
import org.apache.shardingsphere.data.pipeline.api.metadata.SchemaTableName;
import org.apache.shardingsphere.data.pipeline.api.metadata.TableName;
import org.apache.shardingsphere.data.pipeline.api.metadata.loader.PipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.api.metadata.model.PipelineColumnMetaData;
import org.apache.shardingsphere.data.pipeline.core.check.consistency.ConsistencyCheckJobItemProgressContext;
import org.apache.shardingsphere.data.pipeline.core.check.consistency.SingleTableInventoryDataConsistencyChecker;
import org.apache.shardingsphere.data.pipeline.core.context.InventoryIncrementalProcessContext;
import org.apache.shardingsphere.data.pipeline.core.datasource.DefaultPipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.exception.data.UnsupportedPipelineDatabaseTypeException;
import org.apache.shardingsphere.data.pipeline.core.metadata.loader.PipelineTableMetaDataUtil;
import org.apache.shardingsphere.data.pipeline.core.metadata.loader.StandardPipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.scenario.migration.api.impl.MigrationJobAPI;
import org.apache.shardingsphere.data.pipeline.spi.check.consistency.DataConsistencyCalculateAlgorithm;
import org.apache.shardingsphere.data.pipeline.spi.ratelimit.JobRateLimitAlgorithm;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;

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
                .forEach(dataNode -> sourceTableNames.add(DataNodeUtil.formatWithSchema(dataNode)))));
        progressContext.setRecordsCount(getRecordsCount());
        progressContext.getTableNames().addAll(sourceTableNames);
        Map<String, DataConsistencyCheckResult> result = new LinkedHashMap<>();
        PipelineDataSourceManager dataSourceManager = new DefaultPipelineDataSourceManager();
        try {
            AtomicBoolean checkFailed = new AtomicBoolean(false);
            jobConfig.getJobShardingDataNodes().forEach(each -> each.getEntries().forEach(entry -> entry.getDataNodes().forEach(dataNode -> {
                if (checkFailed.get()) {
                    return;
                }
                DataConsistencyCheckResult checkResult = checkSingleTable(entry.getLogicTableName(), dataNode, calculateAlgorithm, dataSourceManager);
                result.put(DataNodeUtil.formatWithSchema(dataNode), checkResult);
                if (!checkResult.isMatched()) {
                    log.info("unmatched on table '{}', ignore left tables", each);
                    checkFailed.set(true);
                }
            })));
        } finally {
            dataSourceManager.close();
        }
        return result;
    }
    
    private DataConsistencyCheckResult checkSingleTable(final String targetTableName, final DataNode dataNode,
                                                        final DataConsistencyCalculateAlgorithm calculateAlgorithm, final PipelineDataSourceManager dataSourceManager) {
        SchemaTableName sourceTable = new SchemaTableName(new SchemaName(dataNode.getSchemaName()), new TableName(dataNode.getTableName()));
        SchemaTableName targetTable = new SchemaTableName(new SchemaName(dataNode.getSchemaName()), new TableName(targetTableName));
        PipelineDataSourceWrapper sourceDataSource = dataSourceManager.getDataSource(jobConfig.getSources().get(dataNode.getDataSourceName()));
        PipelineDataSourceWrapper targetDataSource = dataSourceManager.getDataSource(jobConfig.getTarget());
        PipelineTableMetaDataLoader metaDataLoader = new StandardPipelineTableMetaDataLoader(sourceDataSource);
        List<PipelineColumnMetaData> uniqueKeyColumns = PipelineTableMetaDataUtil.getUniqueKeyColumns(
                sourceTable.getSchemaName().getOriginal(), sourceTable.getTableName().getOriginal(), metaDataLoader);
        PipelineColumnMetaData uniqueKey = uniqueKeyColumns.isEmpty() ? null : uniqueKeyColumns.get(0);
        SingleTableInventoryDataConsistencyChecker singleTableInventoryChecker = new SingleTableInventoryDataConsistencyChecker(
                jobConfig.getJobId(), sourceDataSource, targetDataSource, sourceTable, targetTable, uniqueKey, metaDataLoader, readRateLimitAlgorithm, progressContext);
        return singleTableInventoryChecker.check(calculateAlgorithm);
    }
    
    private void verifyPipelineDatabaseType(final DataConsistencyCalculateAlgorithm calculateAlgorithm, final PipelineDataSourceConfiguration dataSourceConfig) {
        ShardingSpherePreconditions.checkState(calculateAlgorithm.getSupportedDatabaseTypes().contains(dataSourceConfig.getDatabaseType().getType()),
                () -> new UnsupportedPipelineDatabaseTypeException(dataSourceConfig.getDatabaseType()));
    }
    
    private long getRecordsCount() {
        Map<Integer, InventoryIncrementalJobItemProgress> jobProgress = new MigrationJobAPI().getJobProgress(jobConfig);
        return jobProgress.values().stream().filter(Objects::nonNull).mapToLong(InventoryIncrementalJobItemProgress::getProcessedRecordsCount).sum();
    }
}
