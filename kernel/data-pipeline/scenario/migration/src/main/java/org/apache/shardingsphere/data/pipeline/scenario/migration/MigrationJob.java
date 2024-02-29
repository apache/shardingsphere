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

import org.apache.shardingsphere.data.pipeline.api.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.type.ShardingSpherePipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.core.context.TransmissionProcessContext;
import org.apache.shardingsphere.data.pipeline.core.datanode.JobDataNodeEntry;
import org.apache.shardingsphere.data.pipeline.core.importer.ImporterConfiguration;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.incremental.IncrementalDumperContext;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.mapper.TableAndSchemaNameMapper;
import org.apache.shardingsphere.data.pipeline.core.job.AbstractSeparablePipelineJob;
import org.apache.shardingsphere.data.pipeline.core.job.progress.TransmissionJobItemProgress;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.PipelineProcessConfiguration;
import org.apache.shardingsphere.infra.metadata.caseinsensitive.CaseInsensitiveIdentifier;
import org.apache.shardingsphere.infra.metadata.caseinsensitive.CaseInsensitiveQualifiedTable;
import org.apache.shardingsphere.data.pipeline.core.preparer.datasource.param.CreateTableConfiguration;
import org.apache.shardingsphere.data.pipeline.core.ratelimit.JobRateLimitAlgorithm;
import org.apache.shardingsphere.data.pipeline.core.task.runner.PipelineTasksRunner;
import org.apache.shardingsphere.data.pipeline.core.task.runner.TransmissionTasksRunner;
import org.apache.shardingsphere.data.pipeline.core.util.ShardingColumnsExtractor;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.MigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.MigrationTaskConfiguration;
import org.apache.shardingsphere.data.pipeline.scenario.migration.context.MigrationJobItemContext;
import org.apache.shardingsphere.data.pipeline.scenario.migration.ingest.dumper.MigrationIncrementalDumperContextCreator;
import org.apache.shardingsphere.data.pipeline.scenario.migration.preparer.MigrationJobPreparer;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.datanode.DataNode;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Migration job.
 */
public final class MigrationJob extends AbstractSeparablePipelineJob<MigrationJobConfiguration, MigrationJobItemContext, TransmissionJobItemProgress> {
    
    private final MigrationJobPreparer jobPreparer = new MigrationJobPreparer();
    
    public MigrationJob(final String jobId) {
        super(jobId);
    }
    
    @Override
    protected MigrationJobItemContext buildJobItemContext(final MigrationJobConfiguration jobConfig,
                                                          final int shardingItem, final TransmissionJobItemProgress jobItemProgress, final TransmissionProcessContext jobProcessContext) {
        MigrationTaskConfiguration taskConfig = buildTaskConfiguration(jobConfig, shardingItem, jobProcessContext.getProcessConfig());
        return new MigrationJobItemContext(jobConfig, shardingItem, jobItemProgress, jobProcessContext, taskConfig, getJobRunnerManager().getDataSourceManager());
    }
    
    private MigrationTaskConfiguration buildTaskConfiguration(final MigrationJobConfiguration jobConfig, final int jobShardingItem, final PipelineProcessConfiguration processConfig) {
        IncrementalDumperContext incrementalDumperContext = new MigrationIncrementalDumperContextCreator(jobConfig).createDumperContext(jobConfig.getJobShardingDataNodes().get(jobShardingItem));
        Collection<CreateTableConfiguration> createTableConfigs = buildCreateTableConfigurations(jobConfig, incrementalDumperContext.getCommonContext().getTableAndSchemaNameMapper());
        Set<CaseInsensitiveIdentifier> targetTableNames = jobConfig.getTargetTableNames().stream().map(CaseInsensitiveIdentifier::new).collect(Collectors.toSet());
        Map<CaseInsensitiveIdentifier, Set<String>> shardingColumnsMap = new ShardingColumnsExtractor().getShardingColumnsMap(
                ((ShardingSpherePipelineDataSourceConfiguration) jobConfig.getTarget()).getRootConfig().getRules(), targetTableNames);
        ImporterConfiguration importerConfig = buildImporterConfiguration(jobConfig, processConfig, shardingColumnsMap, incrementalDumperContext.getCommonContext().getTableAndSchemaNameMapper());
        return new MigrationTaskConfiguration(incrementalDumperContext.getCommonContext().getDataSourceName(), createTableConfigs, incrementalDumperContext, importerConfig);
    }
    
    private Collection<CreateTableConfiguration> buildCreateTableConfigurations(final MigrationJobConfiguration jobConfig, final TableAndSchemaNameMapper mapper) {
        return jobConfig.getTablesFirstDataNodes().getEntries().stream().map(each -> getCreateTableConfiguration(jobConfig, mapper, each)).collect(Collectors.toList());
    }
    
    private CreateTableConfiguration getCreateTableConfiguration(final MigrationJobConfiguration jobConfig, final TableAndSchemaNameMapper mapper, final JobDataNodeEntry jobDataNodeEntry) {
        DataNode dataNode = jobDataNodeEntry.getDataNodes().get(0);
        PipelineDataSourceConfiguration sourceDataSourceConfig = jobConfig.getSources().get(dataNode.getDataSourceName());
        String sourceSchemaName = mapper.getSchemaName(jobDataNodeEntry.getLogicTableName());
        String targetSchemaName = new DatabaseTypeRegistry(jobConfig.getTargetDatabaseType()).getDialectDatabaseMetaData().isSchemaAvailable() ? sourceSchemaName : null;
        return new CreateTableConfiguration(sourceDataSourceConfig, new CaseInsensitiveQualifiedTable(sourceSchemaName, dataNode.getTableName()),
                jobConfig.getTarget(), new CaseInsensitiveQualifiedTable(targetSchemaName, jobDataNodeEntry.getLogicTableName()));
    }
    
    private ImporterConfiguration buildImporterConfiguration(final MigrationJobConfiguration jobConfig, final PipelineProcessConfiguration pipelineProcessConfig,
                                                             final Map<CaseInsensitiveIdentifier, Set<String>> shardingColumnsMap, final TableAndSchemaNameMapper mapper) {
        int batchSize = pipelineProcessConfig.getWrite().getBatchSize();
        JobRateLimitAlgorithm writeRateLimitAlgorithm = new TransmissionProcessContext(jobConfig.getJobId(), pipelineProcessConfig).getWriteRateLimitAlgorithm();
        int retryTimes = jobConfig.getRetryTimes();
        int concurrency = jobConfig.getConcurrency();
        return new ImporterConfiguration(jobConfig.getTarget(), shardingColumnsMap, mapper, batchSize, writeRateLimitAlgorithm, retryTimes, concurrency);
    }
    
    @Override
    protected PipelineTasksRunner buildTasksRunner(final MigrationJobItemContext jobItemContext) {
        return new TransmissionTasksRunner(jobItemContext);
    }
    
    @Override
    protected void doPrepare(final MigrationJobItemContext jobItemContext) throws SQLException {
        jobPreparer.prepare(jobItemContext);
    }
}
