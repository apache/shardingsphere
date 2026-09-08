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
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContextManager;
import org.apache.shardingsphere.data.pipeline.core.context.TransmissionProcessContext;
import org.apache.shardingsphere.data.pipeline.core.datanode.JobDataNodeEntry;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSource;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.importer.ImporterConfiguration;
import org.apache.shardingsphere.data.pipeline.core.importer.PipelineRequiredColumnsExtractor;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.incremental.IncrementalDumperContext;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.mapper.TableAndSchemaNameMapper;
import org.apache.shardingsphere.data.pipeline.core.job.executor.DistributedPipelineJobExecutorCallback;
import org.apache.shardingsphere.data.pipeline.core.job.id.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.job.progress.TransmissionJobItemProgress;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.PipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.core.metadata.PipelineDataSourcePersistService;
import org.apache.shardingsphere.data.pipeline.core.preparer.datasource.param.CreateTableConfiguration;
import org.apache.shardingsphere.data.pipeline.core.ratelimit.JobRateLimitAlgorithm;
import org.apache.shardingsphere.data.pipeline.core.task.runner.PipelineTasksRunner;
import org.apache.shardingsphere.data.pipeline.core.task.runner.TransmissionTasksRunner;
import org.apache.shardingsphere.data.pipeline.core.util.PipelineDataSourceConfigurationUtils;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.MigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.MigrationTaskConfiguration;
import org.apache.shardingsphere.data.pipeline.scenario.migration.context.MigrationJobItemContext;
import org.apache.shardingsphere.data.pipeline.scenario.migration.ingest.dumper.MigrationIncrementalDumperContextCreator;
import org.apache.shardingsphere.data.pipeline.scenario.migration.preparer.MigrationJobPreparer;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicy;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierNormalizeEngine;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;
import org.apache.shardingsphere.infra.metadata.identifier.ShardingSphereIdentifier;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Migration job executor callback.
 */
public final class MigrationJobExecutorCallback implements DistributedPipelineJobExecutorCallback<MigrationJobConfiguration, MigrationJobItemContext, TransmissionJobItemProgress> {
    
    private static final String JOB_TYPE = new MigrationJobType().getType();
    
    @Override
    public MigrationJobItemContext buildJobItemContext(final MigrationJobConfiguration jobConfig, final int shardingItem,
                                                       final TransmissionJobItemProgress jobItemProgress, final TransmissionProcessContext jobProcessContext,
                                                       final PipelineDataSourceManager dataSourceManager) {
        transformDataSourceConfigurations(jobConfig);
        MigrationTaskConfiguration taskConfig = buildTaskConfiguration(jobConfig, shardingItem, jobProcessContext.getProcessConfiguration(), dataSourceManager);
        return new MigrationJobItemContext(jobConfig, shardingItem, jobItemProgress, jobProcessContext, taskConfig, dataSourceManager);
    }
    
    private void transformDataSourceConfigurations(final MigrationJobConfiguration jobConfig) {
        Map<String, DataSourcePoolProperties> sourceDataSourcePoolProps = new PipelineDataSourcePersistService().load(PipelineJobIdUtils.parseContextKey(jobConfig.getJobId()), JOB_TYPE);
        Map<String, StorageUnit> targetStorageUnits = PipelineContextManager.getProxyContext().getStorageUnits(jobConfig.getTargetDatabaseName());
        for (Entry<String, PipelineDataSourceConfiguration> entry : jobConfig.getSources().entrySet()) {
            entry.setValue(transformSourceDataSourceConfiguration(jobConfig, entry.getKey(), entry.getValue(), sourceDataSourcePoolProps));
        }
        PipelineDataSourceConfigurationUtils.transformPipelineDataSourceConfiguration(jobConfig.getJobId(), jobConfig.getTarget(), targetStorageUnits);
    }
    
    private PipelineDataSourceConfiguration transformSourceDataSourceConfiguration(final MigrationJobConfiguration jobConfig, final String sourceName,
                                                                                   final PipelineDataSourceConfiguration sourceConfig,
                                                                                   final Map<String, DataSourcePoolProperties> sourceDataSourcePoolProps) {
        return sourceDataSourcePoolProps.containsKey(sourceName)
                ? PipelineDataSourceConfigurationUtils.transformPipelineDataSourceConfiguration(jobConfig.getJobId(), sourceName, sourceConfig, sourceDataSourcePoolProps.get(sourceName))
                : sourceConfig;
    }
    
    private MigrationTaskConfiguration buildTaskConfiguration(final MigrationJobConfiguration jobConfig, final int jobShardingItem, final PipelineProcessConfiguration processConfig,
                                                              final PipelineDataSourceManager dataSourceManager) {
        Map<ShardingSphereIdentifier, Collection<String>> tableAndRequiredColumnsMap = getTableAndRequiredColumnsMap(jobConfig);
        IncrementalDumperContext incrementalDumperContext = new MigrationIncrementalDumperContextCreator(jobConfig).createDumperContext(jobConfig.getJobDataNodeLine(jobShardingItem));
        PipelineDataSource targetDataSource = dataSourceManager.getDataSource(jobConfig.getTarget());
        IdentifierCasePolicy targetSchemaIdentifierPolicy = IdentifierNormalizeEngine.resolvePolicy(jobConfig.getTargetDatabaseType(), targetDataSource, IdentifierScope.SCHEMA);
        IdentifierCasePolicy targetTableIdentifierPolicy = IdentifierNormalizeEngine.resolvePolicy(jobConfig.getTargetDatabaseType(), targetDataSource, IdentifierScope.TABLE);
        Collection<CreateTableConfiguration> createTableConfigs = buildCreateTableConfigurations(
                jobConfig, incrementalDumperContext.getCommonContext().getTableAndSchemaNameMapper(), targetSchemaIdentifierPolicy, targetTableIdentifierPolicy);
        ImporterConfiguration importerConfig = buildImporterConfiguration(
                jobConfig, processConfig, tableAndRequiredColumnsMap, getTargetTableAndSchemaNameMapper(jobConfig, targetSchemaIdentifierPolicy, targetTableIdentifierPolicy));
        return new MigrationTaskConfiguration(incrementalDumperContext.getCommonContext().getDataSourceName(), createTableConfigs, incrementalDumperContext, importerConfig);
    }
    
    private TableAndSchemaNameMapper getTargetTableAndSchemaNameMapper(final MigrationJobConfiguration jobConfig, final IdentifierCasePolicy targetSchemaIdentifierPolicy,
                                                                       final IdentifierCasePolicy targetTableIdentifierPolicy) {
        if (null == jobConfig.getTargetTableSchemaMap()) {
            return new TableAndSchemaNameMapper(Collections.emptyMap());
        }
        Map<String, String> targetTableSchemaMap = new LinkedHashMap<>(jobConfig.getTargetTableSchemaMap().size(), 1F);
        for (Entry<String, String> entry : jobConfig.getTargetTableSchemaMap().entrySet()) {
            String schemaName = null == entry.getValue() ? null : IdentifierNormalizeEngine.normalize(targetSchemaIdentifierPolicy, entry.getValue());
            targetTableSchemaMap.put(IdentifierNormalizeEngine.normalize(targetTableIdentifierPolicy, entry.getKey()), schemaName);
        }
        return new TableAndSchemaNameMapper(targetTableSchemaMap);
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
    
    private Collection<CreateTableConfiguration> buildCreateTableConfigurations(final MigrationJobConfiguration jobConfig, final TableAndSchemaNameMapper mapper,
                                                                                final IdentifierCasePolicy targetSchemaIdentifierPolicy,
                                                                                final IdentifierCasePolicy targetTableIdentifierPolicy) {
        return jobConfig.getTablesFirstDataNodes().getEntries().stream()
                .map(each -> getCreateTableConfiguration(jobConfig, mapper, each, targetSchemaIdentifierPolicy, targetTableIdentifierPolicy)).collect(Collectors.toList());
    }
    
    private CreateTableConfiguration getCreateTableConfiguration(final MigrationJobConfiguration jobConfig, final TableAndSchemaNameMapper mapper,
                                                                 final JobDataNodeEntry jobDataNodeEntry, final IdentifierCasePolicy targetSchemaIdentifierPolicy,
                                                                 final IdentifierCasePolicy targetTableIdentifierPolicy) {
        DataNode dataNode = jobDataNodeEntry.getDataNodes().get(0);
        PipelineDataSourceConfiguration sourceDataSourceConfig = jobConfig.getSources().get(dataNode.getDataSourceName());
        String sourceSchemaName = mapper.getSchemaName(jobDataNodeEntry.getLogicTableName());
        String targetSchemaName = null != sourceSchemaName && new DatabaseTypeRegistry(jobConfig.getTargetDatabaseType()).getDialectDatabaseMetaData().getSchemaOption().isSchemaAvailable()
                ? IdentifierNormalizeEngine.normalize(targetSchemaIdentifierPolicy, sourceSchemaName)
                : null;
        String targetTableName = IdentifierNormalizeEngine.normalize(targetTableIdentifierPolicy, jobDataNodeEntry.getLogicTableName());
        return new CreateTableConfiguration(sourceDataSourceConfig, new QualifiedTable(sourceSchemaName, dataNode.getTableName()),
                jobConfig.getTarget(), new QualifiedTable(targetSchemaName, targetTableName));
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
