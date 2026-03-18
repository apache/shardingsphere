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
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.importer.ImporterConfiguration;
import org.apache.shardingsphere.data.pipeline.core.importer.PipelineRequiredColumnsExtractor;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.incremental.IncrementalDumperContext;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.mapper.TableAndSchemaNameMapper;
import org.apache.shardingsphere.data.pipeline.core.job.executor.DistributedPipelineJobExecutorCallback;
import org.apache.shardingsphere.data.pipeline.core.job.progress.TransmissionJobItemProgress;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.PipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.core.preparer.datasource.param.CreateTableConfiguration;
import org.apache.shardingsphere.data.pipeline.core.ratelimit.JobRateLimitAlgorithm;
import org.apache.shardingsphere.data.pipeline.core.task.runner.PipelineTasksRunner;
import org.apache.shardingsphere.data.pipeline.core.task.runner.TransmissionTasksRunner;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.MigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.MigrationTaskConfiguration;
import org.apache.shardingsphere.data.pipeline.scenario.migration.context.MigrationJobItemContext;
import org.apache.shardingsphere.data.pipeline.scenario.migration.ingest.dumper.MigrationIncrementalDumperContextCreator;
import org.apache.shardingsphere.data.pipeline.scenario.migration.preparer.MigrationJobPreparer;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;
import org.apache.shardingsphere.infra.metadata.identifier.ShardingSphereIdentifier;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Migration job executor callback.
 */
public final class MigrationJobExecutorCallback implements DistributedPipelineJobExecutorCallback<MigrationJobConfiguration, MigrationJobItemContext, TransmissionJobItemProgress> {
    
    @Override
    public MigrationJobItemContext buildJobItemContext(final MigrationJobConfiguration jobConfig, final int shardingItem,
                                                       final TransmissionJobItemProgress jobItemProgress, final TransmissionProcessContext jobProcessContext,
                                                       final PipelineDataSourceManager dataSourceManager) {
        MigrationTaskConfiguration taskConfig = buildTaskConfiguration(jobConfig, shardingItem, jobProcessContext.getProcessConfiguration());
        return new MigrationJobItemContext(jobConfig, shardingItem, jobItemProgress, jobProcessContext, taskConfig, dataSourceManager);
    }
    
    private MigrationTaskConfiguration buildTaskConfiguration(final MigrationJobConfiguration jobConfig, final int jobShardingItem, final PipelineProcessConfiguration processConfig) {
        Map<ShardingSphereIdentifier, Collection<String>> tableAndRequiredColumnsMap = getTableAndRequiredColumnsMap(jobConfig);
        IncrementalDumperContext incrementalDumperContext = new MigrationIncrementalDumperContextCreator(jobConfig).createDumperContext(jobConfig.getJobDataNodeLine(jobShardingItem));
        Collection<CreateTableConfiguration> createTableConfigs = buildCreateTableConfigurations(jobConfig, incrementalDumperContext.getCommonContext().getTableAndSchemaNameMapper());
        ImporterConfiguration importerConfig = buildImporterConfiguration(
                jobConfig, processConfig, tableAndRequiredColumnsMap, incrementalDumperContext.getCommonContext().getTableAndSchemaNameMapper());
        return new MigrationTaskConfiguration(incrementalDumperContext.getCommonContext().getDataSourceName(), createTableConfigs, incrementalDumperContext, importerConfig);
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private Map<ShardingSphereIdentifier, Collection<String>> getTableAndRequiredColumnsMap(final MigrationJobConfiguration jobConfig) {
        Map<ShardingSphereIdentifier, Collection<String>> result = new HashMap<>();
        Collection<YamlRuleConfiguration> yamlRuleConfigs = ((ShardingSpherePipelineDataSourceConfiguration) jobConfig.getTarget()).getRootConfig().getRules();
        Collection<ShardingSphereIdentifier> targetTableNames = jobConfig.getTargetTableNames().stream().map(ShardingSphereIdentifier::new).collect(Collectors.toSet());
        for (Entry<YamlRuleConfiguration, PipelineRequiredColumnsExtractor> entry : OrderedSPILoader.getServices(PipelineRequiredColumnsExtractor.class, yamlRuleConfigs).entrySet()) {
            result.putAll(entry.getValue().getTableAndRequiredColumnsMap(entry.getKey(), targetTableNames));
        }
        return result;
    }
    
    private Collection<CreateTableConfiguration> buildCreateTableConfigurations(final MigrationJobConfiguration jobConfig, final TableAndSchemaNameMapper mapper) {
        return jobConfig.getTablesFirstDataNodes().getEntries().stream().map(each -> getCreateTableConfiguration(jobConfig, mapper, each)).collect(Collectors.toList());
    }
    
    private CreateTableConfiguration getCreateTableConfiguration(final MigrationJobConfiguration jobConfig, final TableAndSchemaNameMapper mapper, final JobDataNodeEntry jobDataNodeEntry) {
        DataNode dataNode = jobDataNodeEntry.getDataNodes().get(0);
        PipelineDataSourceConfiguration sourceDataSourceConfig = jobConfig.getSources().get(dataNode.getDataSourceName());
        String sourceSchemaName = mapper.getSchemaName(jobDataNodeEntry.getLogicTableName());
        String targetSchemaName = new DatabaseTypeRegistry(jobConfig.getTargetDatabaseType()).getDialectDatabaseMetaData().getSchemaOption().isSchemaAvailable() ? sourceSchemaName : null;
        return new CreateTableConfiguration(sourceDataSourceConfig, new QualifiedTable(sourceSchemaName, dataNode.getTableName()),
                jobConfig.getTarget(), new QualifiedTable(targetSchemaName, jobDataNodeEntry.getLogicTableName()));
    }
    
    private ImporterConfiguration buildImporterConfiguration(final MigrationJobConfiguration jobConfig, final PipelineProcessConfiguration pipelineProcessConfig,
                                                             final Map<ShardingSphereIdentifier, Collection<String>> tableAndRequiredColumnsMap, final TableAndSchemaNameMapper mapper) {
        int batchSize = pipelineProcessConfig.getWrite().getBatchSize();
        JobRateLimitAlgorithm writeRateLimitAlgorithm = new TransmissionProcessContext(jobConfig.getJobId(), pipelineProcessConfig).getWriteRateLimitAlgorithm();
        int retryTimes = jobConfig.getRetryTimes();
        int concurrency = jobConfig.getConcurrency();
        return new ImporterConfiguration(jobConfig.getTarget(), tableAndRequiredColumnsMap, mapper, batchSize, writeRateLimitAlgorithm, retryTimes, concurrency);
    }
    
    @Override
    public PipelineTasksRunner buildTasksRunner(final MigrationJobItemContext jobItemContext) {
        return new TransmissionTasksRunner(jobItemContext);
    }
    
    @Override
    public void prepare(final MigrationJobItemContext jobItemContext) throws SQLException {
        new MigrationJobPreparer().prepare(jobItemContext);
    }
}
