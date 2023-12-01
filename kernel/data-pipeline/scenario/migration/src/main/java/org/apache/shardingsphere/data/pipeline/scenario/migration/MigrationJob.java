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
import org.apache.shardingsphere.data.pipeline.api.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.type.ShardingSpherePipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.common.config.CreateTableConfiguration;
import org.apache.shardingsphere.data.pipeline.common.config.ImporterConfiguration;
import org.apache.shardingsphere.data.pipeline.common.config.process.PipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.common.context.PipelineJobItemContext;
import org.apache.shardingsphere.data.pipeline.common.context.TransmissionJobItemContext;
import org.apache.shardingsphere.data.pipeline.common.context.TransmissionProcessContext;
import org.apache.shardingsphere.data.pipeline.common.datanode.JobDataNodeEntry;
import org.apache.shardingsphere.data.pipeline.common.datasource.DefaultPipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.common.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.common.job.progress.TransmissionJobItemProgress;
import org.apache.shardingsphere.data.pipeline.common.metadata.CaseInsensitiveIdentifier;
import org.apache.shardingsphere.data.pipeline.common.metadata.CaseInsensitiveQualifiedTable;
import org.apache.shardingsphere.data.pipeline.common.spi.algorithm.JobRateLimitAlgorithm;
import org.apache.shardingsphere.data.pipeline.common.util.ShardingColumnsExtractor;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.context.IncrementalDumperContext;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.context.mapper.TableAndSchemaNameMapper;
import org.apache.shardingsphere.data.pipeline.core.job.AbstractSimplePipelineJob;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobItemManager;
import org.apache.shardingsphere.data.pipeline.core.task.runner.PipelineTasksRunner;
import org.apache.shardingsphere.data.pipeline.core.task.runner.TransmissionTasksRunner;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.MigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.MigrationTaskConfiguration;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.ingest.MigrationIncrementalDumperContextCreator;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.yaml.YamlMigrationJobConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.scenario.migration.context.MigrationJobItemContext;
import org.apache.shardingsphere.data.pipeline.scenario.migration.prepare.MigrationJobPreparer;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.apache.shardingsphere.infra.database.core.metadata.database.DialectDatabaseMetaData;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.datanode.DataNode;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Migration job.
 */
@Slf4j
public final class MigrationJob extends AbstractSimplePipelineJob {
    
    private final MigrationJobOption jobOption = new MigrationJobOption();
    
    private final PipelineJobItemManager<TransmissionJobItemProgress> jobItemManager = new PipelineJobItemManager<>(jobOption.getYamlJobItemProgressSwapper());
    
    private final PipelineDataSourceManager dataSourceManager = new DefaultPipelineDataSourceManager();
    
    // Shared by all sharding items
    private final MigrationJobPreparer jobPreparer = new MigrationJobPreparer();
    
    public MigrationJob(final String jobId) {
        super(jobId);
    }
    
    @Override
    protected TransmissionJobItemContext buildPipelineJobItemContext(final ShardingContext shardingContext) {
        int shardingItem = shardingContext.getShardingItem();
        MigrationJobConfiguration jobConfig = new YamlMigrationJobConfigurationSwapper().swapToObject(shardingContext.getJobParameter());
        Optional<TransmissionJobItemProgress> initProgress = jobItemManager.getProgress(shardingContext.getJobName(), shardingItem);
        TransmissionProcessContext jobProcessContext = jobOption.buildProcessContext(jobConfig);
        MigrationTaskConfiguration taskConfig = buildTaskConfiguration(jobConfig, shardingItem, jobProcessContext.getPipelineProcessConfig());
        return new MigrationJobItemContext(jobConfig, shardingItem, initProgress.orElse(null), jobProcessContext, taskConfig, dataSourceManager);
    }
    
    private MigrationTaskConfiguration buildTaskConfiguration(final MigrationJobConfiguration jobConfig, final int jobShardingItem, final PipelineProcessConfiguration processConfig) {
        IncrementalDumperContext incrementalDumperContext = new MigrationIncrementalDumperContextCreator(jobConfig).createDumperContext(jobConfig.getJobShardingDataNodes().get(jobShardingItem));
        Collection<CreateTableConfiguration> createTableConfigs = buildCreateTableConfigurations(jobConfig, incrementalDumperContext.getCommonContext().getTableAndSchemaNameMapper());
        Set<CaseInsensitiveIdentifier> targetTableNames = jobConfig.getTargetTableNames().stream().map(CaseInsensitiveIdentifier::new).collect(Collectors.toSet());
        Map<CaseInsensitiveIdentifier, Set<String>> shardingColumnsMap = new ShardingColumnsExtractor().getShardingColumnsMap(
                ((ShardingSpherePipelineDataSourceConfiguration) jobConfig.getTarget()).getRootConfig().getRules(), targetTableNames);
        ImporterConfiguration importerConfig = buildImporterConfiguration(jobConfig, processConfig, shardingColumnsMap, incrementalDumperContext.getCommonContext().getTableAndSchemaNameMapper());
        MigrationTaskConfiguration result = new MigrationTaskConfiguration(
                incrementalDumperContext.getCommonContext().getDataSourceName(), createTableConfigs, incrementalDumperContext, importerConfig);
        log.info("buildTaskConfiguration, result={}", result);
        return result;
    }
    
    private Collection<CreateTableConfiguration> buildCreateTableConfigurations(final MigrationJobConfiguration jobConfig, final TableAndSchemaNameMapper tableAndSchemaNameMapper) {
        Collection<CreateTableConfiguration> result = new LinkedList<>();
        for (JobDataNodeEntry each : jobConfig.getTablesFirstDataNodes().getEntries()) {
            String sourceSchemaName = tableAndSchemaNameMapper.getSchemaName(each.getLogicTableName());
            DialectDatabaseMetaData dialectDatabaseMetaData = new DatabaseTypeRegistry(jobConfig.getTargetDatabaseType()).getDialectDatabaseMetaData();
            String targetSchemaName = dialectDatabaseMetaData.isSchemaAvailable() ? sourceSchemaName : null;
            DataNode dataNode = each.getDataNodes().get(0);
            PipelineDataSourceConfiguration sourceDataSourceConfig = jobConfig.getSources().get(dataNode.getDataSourceName());
            CreateTableConfiguration createTableConfig = new CreateTableConfiguration(sourceDataSourceConfig, new CaseInsensitiveQualifiedTable(sourceSchemaName, dataNode.getTableName()),
                    jobConfig.getTarget(), new CaseInsensitiveQualifiedTable(targetSchemaName, each.getLogicTableName()));
            result.add(createTableConfig);
        }
        log.info("buildCreateTableConfigurations, result={}", result);
        return result;
    }
    
    private ImporterConfiguration buildImporterConfiguration(final MigrationJobConfiguration jobConfig, final PipelineProcessConfiguration pipelineProcessConfig,
                                                             final Map<CaseInsensitiveIdentifier, Set<String>> shardingColumnsMap, final TableAndSchemaNameMapper tableAndSchemaNameMapper) {
        int batchSize = pipelineProcessConfig.getWrite().getBatchSize();
        JobRateLimitAlgorithm writeRateLimitAlgorithm = new TransmissionProcessContext(jobConfig.getJobId(), pipelineProcessConfig).getWriteRateLimitAlgorithm();
        int retryTimes = jobConfig.getRetryTimes();
        int concurrency = jobConfig.getConcurrency();
        return new ImporterConfiguration(jobConfig.getTarget(), shardingColumnsMap, tableAndSchemaNameMapper, batchSize, writeRateLimitAlgorithm, retryTimes, concurrency);
    }
    
    @Override
    protected PipelineTasksRunner buildPipelineTasksRunner(final PipelineJobItemContext pipelineJobItemContext) {
        return new TransmissionTasksRunner((TransmissionJobItemContext) pipelineJobItemContext);
    }
    
    @Override
    protected void doPrepare(final PipelineJobItemContext jobItemContext) throws SQLException {
        jobPreparer.prepare((MigrationJobItemContext) jobItemContext);
    }
    
    @Override
    public void doClean() {
        dataSourceManager.close();
    }
}
