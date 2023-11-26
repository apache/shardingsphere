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

package org.apache.shardingsphere.data.pipeline.scenario.migration.api.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.type.ShardingSpherePipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.common.config.CreateTableConfiguration;
import org.apache.shardingsphere.data.pipeline.common.config.ImporterConfiguration;
import org.apache.shardingsphere.data.pipeline.common.config.job.PipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.common.config.job.yaml.YamlPipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.common.config.process.PipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.common.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.common.context.TransmissionProcessContext;
import org.apache.shardingsphere.data.pipeline.common.datanode.DataNodeUtils;
import org.apache.shardingsphere.data.pipeline.common.datanode.JobDataNodeEntry;
import org.apache.shardingsphere.data.pipeline.common.metadata.CaseInsensitiveIdentifier;
import org.apache.shardingsphere.data.pipeline.common.metadata.CaseInsensitiveQualifiedTable;
import org.apache.shardingsphere.data.pipeline.common.pojo.PipelineJobInfo;
import org.apache.shardingsphere.data.pipeline.common.pojo.PipelineJobMetaData;
import org.apache.shardingsphere.data.pipeline.common.spi.algorithm.JobRateLimitAlgorithm;
import org.apache.shardingsphere.data.pipeline.common.util.ShardingColumnsExtractor;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.ConsistencyCheckJobItemProgressContext;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.PipelineDataConsistencyChecker;
import org.apache.shardingsphere.data.pipeline.core.exception.connection.RegisterMigrationSourceStorageUnitException;
import org.apache.shardingsphere.data.pipeline.core.exception.connection.UnregisterMigrationSourceStorageUnitException;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.context.IncrementalDumperContext;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.context.mapper.TableAndSchemaNameMapper;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.job.option.TransmissionJobOption;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobConfigurationManager;
import org.apache.shardingsphere.data.pipeline.core.job.service.TransmissionJobManager;
import org.apache.shardingsphere.data.pipeline.core.metadata.PipelineDataSourcePersistService;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJob;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJobId;
import org.apache.shardingsphere.data.pipeline.scenario.migration.check.consistency.MigrationDataConsistencyChecker;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.MigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.MigrationTaskConfiguration;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.ingest.MigrationIncrementalDumperContextCreator;
import org.apache.shardingsphere.data.pipeline.scenario.migration.context.MigrationProcessContext;
import org.apache.shardingsphere.data.pipeline.yaml.job.YamlMigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.yaml.job.YamlMigrationJobConfigurationSwapper;
import org.apache.shardingsphere.infra.database.core.connector.ConnectionProperties;
import org.apache.shardingsphere.infra.database.core.connector.ConnectionPropertiesParser;
import org.apache.shardingsphere.infra.database.core.metadata.database.DialectDatabaseMetaData;
import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.util.json.JsonUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Migration job option.
 */
@Slf4j
public final class MigrationJobOption implements TransmissionJobOption {
    
    private final PipelineDataSourcePersistService dataSourcePersistService = new PipelineDataSourcePersistService();
    
    @Override
    public PipelineJobInfo getJobInfo(final String jobId) {
        PipelineJobMetaData jobMetaData = new PipelineJobMetaData(PipelineJobIdUtils.getElasticJobConfigurationPOJO(jobId));
        List<String> sourceTables = new LinkedList<>();
        new PipelineJobConfigurationManager(this).<MigrationJobConfiguration>getJobConfiguration(jobId).getJobShardingDataNodes()
                .forEach(each -> each.getEntries().forEach(entry -> entry.getDataNodes().forEach(dataNode -> sourceTables.add(DataNodeUtils.formatWithSchema(dataNode)))));
        return new PipelineJobInfo(jobMetaData, null, String.join(",", sourceTables));
    }
    
    @Override
    public void extendYamlJobConfiguration(final PipelineContextKey contextKey, final YamlPipelineJobConfiguration yamlJobConfig) {
        YamlMigrationJobConfiguration config = (YamlMigrationJobConfiguration) yamlJobConfig;
        if (null == yamlJobConfig.getJobId()) {
            config.setJobId(new MigrationJobId(contextKey, config.getJobShardingDataNodes()).marshal());
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public YamlMigrationJobConfigurationSwapper getYamlJobConfigurationSwapper() {
        return new YamlMigrationJobConfigurationSwapper();
    }
    
    @Override
    public MigrationTaskConfiguration buildTaskConfiguration(final PipelineJobConfiguration pipelineJobConfig, final int jobShardingItem, final PipelineProcessConfiguration processConfig) {
        MigrationJobConfiguration jobConfig = (MigrationJobConfiguration) pipelineJobConfig;
        IncrementalDumperContext incrementalDumperContext = new MigrationIncrementalDumperContextCreator(
                jobConfig).createDumperContext(jobConfig.getJobShardingDataNodes().get(jobShardingItem));
        Collection<CreateTableConfiguration> createTableConfigs = buildCreateTableConfigurations(jobConfig, incrementalDumperContext.getCommonContext().getTableAndSchemaNameMapper());
        Set<CaseInsensitiveIdentifier> targetTableNames = jobConfig.getTargetTableNames().stream().map(CaseInsensitiveIdentifier::new).collect(Collectors.toSet());
        Map<CaseInsensitiveIdentifier, Set<String>> shardingColumnsMap = new ShardingColumnsExtractor().getShardingColumnsMap(
                ((ShardingSpherePipelineDataSourceConfiguration) jobConfig.getTarget()).getRootConfig().getRules(), targetTableNames);
        ImporterConfiguration importerConfig = buildImporterConfiguration(
                jobConfig, processConfig, shardingColumnsMap, incrementalDumperContext.getCommonContext().getTableAndSchemaNameMapper());
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
            CreateTableConfiguration createTableConfig = new CreateTableConfiguration(
                    sourceDataSourceConfig, new CaseInsensitiveQualifiedTable(sourceSchemaName, dataNode.getTableName()),
                    jobConfig.getTarget(), new CaseInsensitiveQualifiedTable(targetSchemaName, each.getLogicTableName()));
            result.add(createTableConfig);
        }
        log.info("buildCreateTableConfigurations, result={}", result);
        return result;
    }
    
    private ImporterConfiguration buildImporterConfiguration(final MigrationJobConfiguration jobConfig, final PipelineProcessConfiguration pipelineProcessConfig,
                                                             final Map<CaseInsensitiveIdentifier, Set<String>> shardingColumnsMap, final TableAndSchemaNameMapper tableAndSchemaNameMapper) {
        MigrationProcessContext processContext = new MigrationProcessContext(jobConfig.getJobId(), pipelineProcessConfig);
        JobRateLimitAlgorithm writeRateLimitAlgorithm = processContext.getWriteRateLimitAlgorithm();
        int batchSize = pipelineProcessConfig.getWrite().getBatchSize();
        int retryTimes = jobConfig.getRetryTimes();
        int concurrency = jobConfig.getConcurrency();
        return new ImporterConfiguration(jobConfig.getTarget(), shardingColumnsMap, tableAndSchemaNameMapper, batchSize, writeRateLimitAlgorithm, retryTimes, concurrency);
    }
    
    @Override
    public MigrationProcessContext buildProcessContext(final PipelineJobConfiguration jobConfig) {
        PipelineProcessConfiguration processConfig = new TransmissionJobManager(this).showProcessConfiguration(PipelineJobIdUtils.parseContextKey(jobConfig.getJobId()));
        return new MigrationProcessContext(jobConfig.getJobId(), processConfig);
    }
    
    @Override
    public PipelineDataConsistencyChecker buildDataConsistencyChecker(final PipelineJobConfiguration jobConfig, final TransmissionProcessContext processContext,
                                                                      final ConsistencyCheckJobItemProgressContext progressContext) {
        return new MigrationDataConsistencyChecker((MigrationJobConfiguration) jobConfig, processContext, progressContext);
    }
    
    @Override
    public Optional<String> getToBeStartDisabledNextJobType() {
        return Optional.of("CONSISTENCY_CHECK");
    }
    
    @Override
    public Optional<String> getToBeStoppedPreviousJobType() {
        return Optional.of("CONSISTENCY_CHECK");
    }
    
    /**
     * Add migration source resources.
     *
     * @param contextKey context key
     * @param propsMap data source pool properties map
     */
    public void addMigrationSourceResources(final PipelineContextKey contextKey, final Map<String, DataSourcePoolProperties> propsMap) {
        Map<String, DataSourcePoolProperties> existDataSources = dataSourcePersistService.load(contextKey, getType());
        Collection<String> duplicateDataSourceNames = new HashSet<>(propsMap.size(), 1F);
        for (Entry<String, DataSourcePoolProperties> entry : propsMap.entrySet()) {
            if (existDataSources.containsKey(entry.getKey())) {
                duplicateDataSourceNames.add(entry.getKey());
            }
        }
        ShardingSpherePreconditions.checkState(duplicateDataSourceNames.isEmpty(), () -> new RegisterMigrationSourceStorageUnitException(duplicateDataSourceNames));
        Map<String, DataSourcePoolProperties> result = new LinkedHashMap<>(existDataSources);
        result.putAll(propsMap);
        dataSourcePersistService.persist(contextKey, getType(), result);
    }
    
    /**
     * Drop migration source resources.
     *
     * @param contextKey context key
     * @param resourceNames resource names
     */
    public void dropMigrationSourceResources(final PipelineContextKey contextKey, final Collection<String> resourceNames) {
        Map<String, DataSourcePoolProperties> metaDataDataSource = dataSourcePersistService.load(contextKey, getType());
        List<String> noExistResources = resourceNames.stream().filter(each -> !metaDataDataSource.containsKey(each)).collect(Collectors.toList());
        ShardingSpherePreconditions.checkState(noExistResources.isEmpty(), () -> new UnregisterMigrationSourceStorageUnitException(noExistResources));
        for (String each : resourceNames) {
            metaDataDataSource.remove(each);
        }
        dataSourcePersistService.persist(contextKey, getType(), metaDataDataSource);
    }
    
    /**
     * Query migration source resources list.
     *
     * @param contextKey context key
     * @return migration source resources
     */
    public Collection<Collection<Object>> listMigrationSourceResources(final PipelineContextKey contextKey) {
        Map<String, DataSourcePoolProperties> propsMap = dataSourcePersistService.load(contextKey, getType());
        Collection<Collection<Object>> result = new ArrayList<>(propsMap.size());
        for (Entry<String, DataSourcePoolProperties> entry : propsMap.entrySet()) {
            String dataSourceName = entry.getKey();
            DataSourcePoolProperties value = entry.getValue();
            Collection<Object> props = new LinkedList<>();
            props.add(dataSourceName);
            String url = String.valueOf(value.getConnectionPropertySynonyms().getStandardProperties().get("url"));
            DatabaseType databaseType = DatabaseTypeFactory.get(url);
            props.add(databaseType.getType());
            ConnectionProperties connectionProps = DatabaseTypedSPILoader.getService(ConnectionPropertiesParser.class, databaseType).parse(url, "", null);
            props.add(connectionProps.getHostname());
            props.add(connectionProps.getPort());
            props.add(connectionProps.getCatalog());
            Map<String, Object> standardProps = value.getPoolPropertySynonyms().getStandardProperties();
            props.add(getStandardProperty(standardProps, "connectionTimeoutMilliseconds"));
            props.add(getStandardProperty(standardProps, "idleTimeoutMilliseconds"));
            props.add(getStandardProperty(standardProps, "maxLifetimeMilliseconds"));
            props.add(getStandardProperty(standardProps, "maxPoolSize"));
            props.add(getStandardProperty(standardProps, "minPoolSize"));
            props.add(getStandardProperty(standardProps, "readOnly"));
            Map<String, Object> otherProps = value.getCustomProperties().getProperties();
            props.add(otherProps.isEmpty() ? "" : JsonUtils.toJsonString(otherProps));
            result.add(props);
        }
        return result;
    }
    
    private String getStandardProperty(final Map<String, Object> standardProps, final String key) {
        if (standardProps.containsKey(key) && null != standardProps.get(key)) {
            return standardProps.get(key).toString();
        }
        return "";
    }
    
    @Override
    public Class<MigrationJob> getJobClass() {
        return MigrationJob.class;
    }
    
    @Override
    public String getType() {
        return "MIGRATION";
    }
}
