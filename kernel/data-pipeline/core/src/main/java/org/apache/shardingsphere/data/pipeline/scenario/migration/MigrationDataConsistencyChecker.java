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

package org.apache.shardingsphere.data.pipeline.scenario.migration;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.InventoryIncrementalJobPublicAPI;
import org.apache.shardingsphere.data.pipeline.api.PipelineJobPublicAPIFactory;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCheckResult;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.PipelineDataConsistencyChecker;
import org.apache.shardingsphere.data.pipeline.api.config.TableNameSchemaNameMapping;
import org.apache.shardingsphere.data.pipeline.api.config.job.MigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.job.progress.InventoryIncrementalJobItemProgress;
import org.apache.shardingsphere.data.pipeline.api.metadata.SchemaName;
import org.apache.shardingsphere.data.pipeline.api.metadata.SchemaTableName;
import org.apache.shardingsphere.data.pipeline.api.metadata.TableName;
import org.apache.shardingsphere.data.pipeline.api.metadata.loader.PipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.core.check.consistency.SingleTableInventoryDataConsistencyChecker;
import org.apache.shardingsphere.data.pipeline.core.context.InventoryIncrementalProcessContext;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceFactory;
import org.apache.shardingsphere.data.pipeline.core.exception.data.UnsupportedPipelineDatabaseTypeException;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.metadata.loader.StandardPipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.ConsistencyCheckJobItemContext;
import org.apache.shardingsphere.data.pipeline.spi.check.consistency.DataConsistencyCalculateAlgorithm;
import org.apache.shardingsphere.data.pipeline.spi.ratelimit.JobRateLimitAlgorithm;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.util.exception.external.sql.type.wrapper.SQLWrapperException;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Data consistency checker for migration job.
 */
@Slf4j
public final class MigrationDataConsistencyChecker implements PipelineDataConsistencyChecker {
    
    private final MigrationJobConfiguration jobConfig;
    
    private final JobRateLimitAlgorithm readRateLimitAlgorithm;
    
    private final TableNameSchemaNameMapping tableNameSchemaNameMapping;
    
    private final ConsistencyCheckJobItemContext checkJobItemContext;
    
    public MigrationDataConsistencyChecker(final MigrationJobConfiguration jobConfig, final InventoryIncrementalProcessContext processContext,
                                           final ConsistencyCheckJobItemContext checkJobItemContext) {
        this.jobConfig = jobConfig;
        readRateLimitAlgorithm = null != processContext ? processContext.getReadRateLimitAlgorithm() : null;
        tableNameSchemaNameMapping = new TableNameSchemaNameMapping(
                TableNameSchemaNameMapping.convert(jobConfig.getSourceSchemaName(), new HashSet<>(Arrays.asList(jobConfig.getSourceTableName(), jobConfig.getTargetTableName()))));
        this.checkJobItemContext = checkJobItemContext;
    }
    
    @Override
    public Map<String, DataConsistencyCheckResult> check(final DataConsistencyCalculateAlgorithm calculateAlgorithm) {
        verifyPipelineDatabaseType(calculateAlgorithm, jobConfig.getSource());
        verifyPipelineDatabaseType(calculateAlgorithm, jobConfig.getTarget());
        SchemaTableName sourceTable = new SchemaTableName(new SchemaName(tableNameSchemaNameMapping.getSchemaName(jobConfig.getSourceTableName())), new TableName(jobConfig.getSourceTableName()));
        SchemaTableName targetTable = new SchemaTableName(new SchemaName(tableNameSchemaNameMapping.getSchemaName(jobConfig.getTargetTableName())), new TableName(jobConfig.getTargetTableName()));
        Map<String, DataConsistencyCheckResult> result = new LinkedHashMap<>();
        try (
                PipelineDataSourceWrapper sourceDataSource = PipelineDataSourceFactory.newInstance(jobConfig.getSource());
                PipelineDataSourceWrapper targetDataSource = PipelineDataSourceFactory.newInstance(jobConfig.getTarget())) {
            String jobId = jobConfig.getJobId();
            // TODO simplify code
            InventoryIncrementalJobPublicAPI inventoryIncrementalJobPublicAPI = PipelineJobPublicAPIFactory.getInventoryIncrementalJobPublicAPI(PipelineJobIdUtils.parseJobType(jobId).getTypeName());
            Map<Integer, InventoryIncrementalJobItemProgress> jobProgress = inventoryIncrementalJobPublicAPI.getJobProgress(jobId);
            long recordsCount = jobProgress.values().stream().filter(Objects::nonNull).mapToLong(InventoryIncrementalJobItemProgress::getProcessedRecordsCount).sum();
            checkJobItemContext.setRecordsCount(recordsCount);
            checkJobItemContext.getTableNames().add(jobConfig.getSourceTableName());
            log.info("consistency check, get records count: {}", recordsCount);
            PipelineTableMetaDataLoader metaDataLoader = new StandardPipelineTableMetaDataLoader(sourceDataSource);
            SingleTableInventoryDataConsistencyChecker singleTableInventoryChecker = new SingleTableInventoryDataConsistencyChecker(jobId, sourceDataSource, targetDataSource,
                    sourceTable, targetTable, jobConfig.getUniqueKeyColumn(), metaDataLoader, readRateLimitAlgorithm, checkJobItemContext);
            result.put(sourceTable.getTableName().getOriginal(), singleTableInventoryChecker.check(calculateAlgorithm));
            // TODO make sure checkEndTimeMillis will be set
            checkJobItemContext.setCheckEndTimeMillis(System.currentTimeMillis());
        } catch (final SQLException ex) {
            throw new SQLWrapperException(ex);
        }
        return result;
    }
    
    private void verifyPipelineDatabaseType(final DataConsistencyCalculateAlgorithm calculateAlgorithm, final PipelineDataSourceConfiguration dataSourceConfig) {
        ShardingSpherePreconditions.checkState(calculateAlgorithm.getSupportedDatabaseTypes().contains(dataSourceConfig.getDatabaseType().getType()),
                () -> new UnsupportedPipelineDatabaseTypeException(dataSourceConfig.getDatabaseType()));
    }
}
