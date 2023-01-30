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
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCheckIgnoredType;
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
import org.apache.shardingsphere.data.pipeline.core.check.consistency.ConsistencyCheckJobItemProgressContext;
import org.apache.shardingsphere.data.pipeline.core.check.consistency.SingleTableInventoryDataConsistencyChecker;
import org.apache.shardingsphere.data.pipeline.core.context.InventoryIncrementalProcessContext;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceFactory;
import org.apache.shardingsphere.data.pipeline.core.exception.data.UnsupportedPipelineDatabaseTypeException;
import org.apache.shardingsphere.data.pipeline.core.metadata.loader.StandardPipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.scenario.migration.api.impl.MigrationJobAPI;
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
    
    private final ConsistencyCheckJobItemProgressContext progressContext;
    
    public MigrationDataConsistencyChecker(final MigrationJobConfiguration jobConfig, final InventoryIncrementalProcessContext processContext,
                                           final ConsistencyCheckJobItemProgressContext progressContext) {
        this.jobConfig = jobConfig;
        readRateLimitAlgorithm = null == processContext ? null : processContext.getReadRateLimitAlgorithm();
        tableNameSchemaNameMapping = new TableNameSchemaNameMapping(
                TableNameSchemaNameMapping.convert(jobConfig.getSourceSchemaName(), new HashSet<>(Arrays.asList(jobConfig.getSourceTableName(), jobConfig.getTargetTableName()))));
        this.progressContext = progressContext;
    }
    
    @Override
    public Map<String, DataConsistencyCheckResult> check(final DataConsistencyCalculateAlgorithm calculateAlgorithm) {
        verifyPipelineDatabaseType(calculateAlgorithm, jobConfig.getSource());
        verifyPipelineDatabaseType(calculateAlgorithm, jobConfig.getTarget());
        SchemaTableName sourceTable = new SchemaTableName(new SchemaName(tableNameSchemaNameMapping.getSchemaName(jobConfig.getSourceTableName())), new TableName(jobConfig.getSourceTableName()));
        SchemaTableName targetTable = new SchemaTableName(new SchemaName(tableNameSchemaNameMapping.getSchemaName(jobConfig.getTargetTableName())), new TableName(jobConfig.getTargetTableName()));
        progressContext.getTableNames().add(jobConfig.getSourceTableName());
        Map<String, DataConsistencyCheckResult> result = new LinkedHashMap<>();
        if (null == jobConfig.getUniqueKeyColumn()) {
            progressContext.getIgnoredTableNames().add(sourceTable.getTableName().getOriginal());
            result.put(sourceTable.getTableName().getOriginal(), new DataConsistencyCheckResult(DataConsistencyCheckIgnoredType.NO_UNIQUE_KEY));
            return result;
        }
        try (
                PipelineDataSourceWrapper sourceDataSource = PipelineDataSourceFactory.newInstance(jobConfig.getSource());
                PipelineDataSourceWrapper targetDataSource = PipelineDataSourceFactory.newInstance(jobConfig.getTarget())) {
            progressContext.setRecordsCount(getRecordsCount());
            PipelineTableMetaDataLoader metaDataLoader = new StandardPipelineTableMetaDataLoader(sourceDataSource);
            SingleTableInventoryDataConsistencyChecker singleTableInventoryChecker = new SingleTableInventoryDataConsistencyChecker(
                    jobConfig.getJobId(), sourceDataSource, targetDataSource, sourceTable, targetTable, jobConfig.getUniqueKeyColumn(), metaDataLoader, readRateLimitAlgorithm, progressContext);
            result.put(sourceTable.getTableName().getOriginal(), singleTableInventoryChecker.check(calculateAlgorithm));
        } catch (final SQLException ex) {
            throw new SQLWrapperException(ex);
        }
        return result;
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
