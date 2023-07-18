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

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.shardingsphere.data.pipeline.api.config.TableNameSchemaNameMapping;
import org.apache.shardingsphere.data.pipeline.api.config.ingest.DumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.ShardingSpherePipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.yaml.YamlPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.metadata.ActualTableName;
import org.apache.shardingsphere.data.pipeline.api.metadata.LogicTableName;
import org.apache.shardingsphere.data.pipeline.api.metadata.SchemaName;
import org.apache.shardingsphere.data.pipeline.api.metadata.SchemaTableName;
import org.apache.shardingsphere.data.pipeline.api.metadata.TableName;
import org.apache.shardingsphere.data.pipeline.common.config.CreateTableConfiguration;
import org.apache.shardingsphere.data.pipeline.common.config.CreateTableConfiguration.CreateTableEntry;
import org.apache.shardingsphere.data.pipeline.common.config.ImporterConfiguration;
import org.apache.shardingsphere.data.pipeline.common.config.job.PipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.common.config.job.yaml.YamlPipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.common.config.process.PipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.common.context.InventoryIncrementalProcessContext;
import org.apache.shardingsphere.data.pipeline.common.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.common.context.PipelineContextManager;
import org.apache.shardingsphere.data.pipeline.common.datanode.DataNodeUtils;
import org.apache.shardingsphere.data.pipeline.common.datanode.JobDataNodeEntry;
import org.apache.shardingsphere.data.pipeline.common.datanode.JobDataNodeLine;
import org.apache.shardingsphere.data.pipeline.common.datanode.JobDataNodeLineConvertUtils;
import org.apache.shardingsphere.data.pipeline.common.datasource.PipelineDataSourceFactory;
import org.apache.shardingsphere.data.pipeline.common.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.common.job.PipelineJobId;
import org.apache.shardingsphere.data.pipeline.common.job.type.JobCodeRegistry;
import org.apache.shardingsphere.data.pipeline.common.job.type.JobType;
import org.apache.shardingsphere.data.pipeline.common.metadata.loader.PipelineSchemaUtils;
import org.apache.shardingsphere.data.pipeline.common.pojo.PipelineJobMetaData;
import org.apache.shardingsphere.data.pipeline.common.pojo.TableBasedPipelineJobInfo;
import org.apache.shardingsphere.data.pipeline.common.sqlbuilder.PipelineCommonSQLBuilder;
import org.apache.shardingsphere.data.pipeline.common.util.ShardingColumnsExtractor;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.ConsistencyCheckJobItemProgressContext;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.PipelineDataConsistencyChecker;
import org.apache.shardingsphere.data.pipeline.core.exception.connection.RegisterMigrationSourceStorageUnitException;
import org.apache.shardingsphere.data.pipeline.core.exception.connection.UnregisterMigrationSourceStorageUnitException;
import org.apache.shardingsphere.data.pipeline.core.exception.metadata.NoAnyRuleExistsException;
import org.apache.shardingsphere.data.pipeline.core.exception.param.PipelineInvalidParameterException;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobAPI;
import org.apache.shardingsphere.data.pipeline.core.job.service.impl.AbstractInventoryIncrementalJobAPIImpl;
import org.apache.shardingsphere.data.pipeline.core.metadata.PipelineDataSourcePersistService;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJob;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJobId;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJobType;
import org.apache.shardingsphere.data.pipeline.scenario.migration.check.consistency.MigrationDataConsistencyChecker;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.MigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.MigrationTaskConfiguration;
import org.apache.shardingsphere.data.pipeline.scenario.migration.context.MigrationProcessContext;
import org.apache.shardingsphere.data.pipeline.spi.ratelimit.JobRateLimitAlgorithm;
import org.apache.shardingsphere.data.pipeline.yaml.job.YamlMigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.yaml.job.YamlMigrationJobConfigurationSwapper;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.database.spi.DataSourceMetaData;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.resource.YamlDataSourceConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.migration.distsql.statement.MigrateTableStatement;
import org.apache.shardingsphere.migration.distsql.statement.pojo.SourceTargetEntry;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Migration job API.
 */
@Slf4j
public final class MigrationJobAPI extends AbstractInventoryIncrementalJobAPIImpl {
    
    private final PipelineDataSourcePersistService dataSourcePersistService = new PipelineDataSourcePersistService();
    
    /**
     * Create job migration config and start.
     *
     * @param contextKey context key
     * @param param create migration job parameter
     * @return job id
     */
    public String createJobAndStart(final PipelineContextKey contextKey, final MigrateTableStatement param) {
        MigrationJobConfiguration jobConfig = new YamlMigrationJobConfigurationSwapper().swapToObject(buildYamlJobConfiguration(contextKey, param));
        start(jobConfig);
        return jobConfig.getJobId();
    }
    
    private YamlMigrationJobConfiguration buildYamlJobConfiguration(final PipelineContextKey contextKey, final MigrateTableStatement param) {
        YamlMigrationJobConfiguration result = new YamlMigrationJobConfiguration();
        result.setTargetDatabaseName(param.getTargetDatabaseName());
        Map<String, DataSourceProperties> metaDataDataSource = dataSourcePersistService.load(contextKey, new MigrationJobType());
        Map<String, List<DataNode>> sourceDataNodes = new LinkedHashMap<>();
        Map<String, YamlPipelineDataSourceConfiguration> configSources = new LinkedHashMap<>();
        List<SourceTargetEntry> sourceTargetEntries = new ArrayList<>(new HashSet<>(param.getSourceTargetEntries())).stream().sorted(Comparator.comparing(SourceTargetEntry::getTargetTableName)
                .thenComparing(each -> DataNodeUtils.formatWithSchema(each.getSource()))).collect(Collectors.toList());
        YamlDataSourceConfigurationSwapper dataSourceConfigSwapper = new YamlDataSourceConfigurationSwapper();
        for (SourceTargetEntry each : sourceTargetEntries) {
            sourceDataNodes.computeIfAbsent(each.getTargetTableName(), key -> new LinkedList<>()).add(each.getSource());
            ShardingSpherePreconditions.checkState(1 == sourceDataNodes.get(each.getTargetTableName()).size(),
                    () -> new PipelineInvalidParameterException("more than one source table for " + each.getTargetTableName()));
            String dataSourceName = each.getSource().getDataSourceName();
            if (configSources.containsKey(dataSourceName)) {
                continue;
            }
            ShardingSpherePreconditions.checkState(metaDataDataSource.containsKey(dataSourceName),
                    () -> new PipelineInvalidParameterException(dataSourceName + " doesn't exist. Run `SHOW MIGRATION SOURCE STORAGE UNITS;` to verify it."));
            Map<String, Object> sourceDataSourceProps = dataSourceConfigSwapper.swapToMap(metaDataDataSource.get(dataSourceName));
            StandardPipelineDataSourceConfiguration sourceDataSourceConfig = new StandardPipelineDataSourceConfiguration(sourceDataSourceProps);
            configSources.put(dataSourceName, buildYamlPipelineDataSourceConfiguration(sourceDataSourceConfig.getType(), sourceDataSourceConfig.getParameter()));
            if (null == each.getSource().getSchemaName() && sourceDataSourceConfig.getDatabaseType().isSchemaAvailable()) {
                each.getSource().setSchemaName(PipelineSchemaUtils.getDefaultSchema(sourceDataSourceConfig));
            }
            DatabaseType sourceDatabaseType = sourceDataSourceConfig.getDatabaseType();
            if (null == result.getSourceDatabaseType()) {
                result.setSourceDatabaseType(sourceDatabaseType.getType());
            } else if (!result.getSourceDatabaseType().equals(sourceDatabaseType.getType())) {
                throw new PipelineInvalidParameterException("Source storage units have different database types");
            }
        }
        result.setSources(configSources);
        ShardingSphereDatabase targetDatabase = PipelineContextManager.getProxyContext().getContextManager().getMetaDataContexts().getMetaData().getDatabase(param.getTargetDatabaseName());
        PipelineDataSourceConfiguration targetPipelineDataSourceConfig = buildTargetPipelineDataSourceConfiguration(targetDatabase);
        result.setTarget(buildYamlPipelineDataSourceConfiguration(targetPipelineDataSourceConfig.getType(), targetPipelineDataSourceConfig.getParameter()));
        result.setTargetDatabaseType(targetPipelineDataSourceConfig.getDatabaseType().getType());
        List<JobDataNodeEntry> tablesFirstDataNodes = sourceDataNodes.entrySet().stream()
                .map(entry -> new JobDataNodeEntry(entry.getKey(), entry.getValue().subList(0, 1))).collect(Collectors.toList());
        result.setTargetTableNames(new ArrayList<>(sourceDataNodes.keySet()).stream().sorted().collect(Collectors.toList()));
        result.setTargetTableSchemaMap(buildTargetTableSchemaMap(sourceDataNodes));
        result.setTablesFirstDataNodes(new JobDataNodeLine(tablesFirstDataNodes).marshal());
        result.setJobShardingDataNodes(JobDataNodeLineConvertUtils.convertDataNodesToLines(sourceDataNodes).stream().map(JobDataNodeLine::marshal).collect(Collectors.toList()));
        extendYamlJobConfiguration(contextKey, result);
        return result;
    }
    
    private YamlPipelineDataSourceConfiguration buildYamlPipelineDataSourceConfiguration(final String type, final String param) {
        YamlPipelineDataSourceConfiguration result = new YamlPipelineDataSourceConfiguration();
        result.setType(type);
        result.setParameter(param);
        return result;
    }
    
    private PipelineDataSourceConfiguration buildTargetPipelineDataSourceConfiguration(final ShardingSphereDatabase targetDatabase) {
        Map<String, Map<String, Object>> targetDataSourceProps = new HashMap<>();
        YamlDataSourceConfigurationSwapper dataSourceConfigSwapper = new YamlDataSourceConfigurationSwapper();
        for (Entry<String, DataSourceProperties> entry : targetDatabase.getResourceMetaData().getDataSourcePropsMap().entrySet()) {
            Map<String, Object> dataSourceProps = dataSourceConfigSwapper.swapToMap(entry.getValue());
            targetDataSourceProps.put(entry.getKey(), dataSourceProps);
        }
        YamlRootConfiguration targetRootConfig = buildYamlRootConfiguration(targetDatabase.getName(), targetDataSourceProps, targetDatabase.getRuleMetaData().getConfigurations());
        return new ShardingSpherePipelineDataSourceConfiguration(targetRootConfig);
    }
    
    private YamlRootConfiguration buildYamlRootConfiguration(final String databaseName, final Map<String, Map<String, Object>> yamlDataSources, final Collection<RuleConfiguration> rules) {
        if (rules.isEmpty()) {
            throw new NoAnyRuleExistsException(databaseName);
        }
        YamlRootConfiguration result = new YamlRootConfiguration();
        result.setDatabaseName(databaseName);
        result.setDataSources(yamlDataSources);
        Collection<YamlRuleConfiguration> yamlRuleConfigurations = new YamlRuleConfigurationSwapperEngine().swapToYamlRuleConfigurations(rules);
        result.setRules(yamlRuleConfigurations);
        return result;
    }
    
    private Map<String, String> buildTargetTableSchemaMap(final Map<String, List<DataNode>> sourceDataNodes) {
        Map<String, String> result = new LinkedHashMap<>();
        sourceDataNodes.forEach((tableName, dataNodes) -> result.put(tableName, dataNodes.get(0).getSchemaName()));
        return result;
    }
    
    @Override
    protected String marshalJobIdLeftPart(final PipelineJobId pipelineJobId) {
        String text = JsonUtils.toJsonString(pipelineJobId);
        return DigestUtils.md5Hex(text.getBytes(StandardCharsets.UTF_8));
    }
    
    @Override
    protected TableBasedPipelineJobInfo getJobInfo(final String jobId) {
        JobConfigurationPOJO jobConfigPOJO = getElasticJobConfigPOJO(jobId);
        PipelineJobMetaData jobMetaData = buildPipelineJobMetaData(jobConfigPOJO);
        List<String> sourceTables = new LinkedList<>();
        getJobConfiguration(jobConfigPOJO).getJobShardingDataNodes().forEach(each -> each.getEntries().forEach(entry -> entry.getDataNodes()
                .forEach(dataNode -> sourceTables.add(DataNodeUtils.formatWithSchema(dataNode)))));
        return new TableBasedPipelineJobInfo(jobMetaData, String.join(",", sourceTables));
    }
    
    @Override
    public void extendYamlJobConfiguration(final PipelineContextKey contextKey, final YamlPipelineJobConfiguration yamlJobConfig) {
        YamlMigrationJobConfiguration config = (YamlMigrationJobConfiguration) yamlJobConfig;
        if (null == yamlJobConfig.getJobId()) {
            config.setJobId(generateJobId(contextKey, config));
        }
    }
    
    private String generateJobId(final PipelineContextKey contextKey, final YamlMigrationJobConfiguration config) {
        MigrationJobId jobId = new MigrationJobId(contextKey, config.getJobShardingDataNodes());
        return marshalJobId(jobId);
    }
    
    @Override
    protected YamlPipelineJobConfiguration swapToYamlJobConfiguration(final PipelineJobConfiguration jobConfig) {
        return new YamlMigrationJobConfigurationSwapper().swapToYamlConfiguration((MigrationJobConfiguration) jobConfig);
    }
    
    @Override
    public MigrationJobConfiguration getJobConfiguration(final String jobId) {
        return getJobConfiguration(getElasticJobConfigPOJO(jobId));
    }
    
    @Override
    protected MigrationJobConfiguration getJobConfiguration(final JobConfigurationPOJO jobConfigPOJO) {
        return new YamlMigrationJobConfigurationSwapper().swapToObject(jobConfigPOJO.getJobParameter());
    }
    
    @Override
    public MigrationTaskConfiguration buildTaskConfiguration(final PipelineJobConfiguration pipelineJobConfig, final int jobShardingItem, final PipelineProcessConfiguration pipelineProcessConfig) {
        MigrationJobConfiguration jobConfig = (MigrationJobConfiguration) pipelineJobConfig;
        JobDataNodeLine dataNodeLine = jobConfig.getJobShardingDataNodes().get(jobShardingItem);
        Map<ActualTableName, LogicTableName> tableNameMap = buildTableNameMap(dataNodeLine);
        TableNameSchemaNameMapping tableNameSchemaNameMapping = new TableNameSchemaNameMapping(jobConfig.getTargetTableSchemaMap());
        CreateTableConfiguration createTableConfig = buildCreateTableConfiguration(jobConfig, tableNameSchemaNameMapping);
        String dataSourceName = dataNodeLine.getEntries().get(0).getDataNodes().get(0).getDataSourceName();
        DumperConfiguration dumperConfig = buildDumperConfiguration(jobConfig.getJobId(), dataSourceName, jobConfig.getSources().get(dataSourceName), tableNameMap, tableNameSchemaNameMapping);
        Set<LogicTableName> targetTableNames = jobConfig.getTargetTableNames().stream().map(LogicTableName::new).collect(Collectors.toSet());
        Map<LogicTableName, Set<String>> shardingColumnsMap = new ShardingColumnsExtractor().getShardingColumnsMap(
                ((ShardingSpherePipelineDataSourceConfiguration) jobConfig.getTarget()).getRootConfig().getRules(), targetTableNames);
        ImporterConfiguration importerConfig = buildImporterConfiguration(jobConfig, pipelineProcessConfig, shardingColumnsMap, tableNameSchemaNameMapping);
        MigrationTaskConfiguration result = new MigrationTaskConfiguration(dataSourceName, createTableConfig, dumperConfig, importerConfig);
        log.info("buildTaskConfiguration, result={}", result);
        return result;
    }
    
    private Map<ActualTableName, LogicTableName> buildTableNameMap(final JobDataNodeLine dataNodeLine) {
        Map<ActualTableName, LogicTableName> result = new LinkedHashMap<>();
        for (JobDataNodeEntry each : dataNodeLine.getEntries()) {
            for (DataNode dataNode : each.getDataNodes()) {
                result.put(new ActualTableName(dataNode.getTableName()), new LogicTableName(each.getLogicTableName()));
            }
        }
        return result;
    }
    
    private CreateTableConfiguration buildCreateTableConfiguration(final MigrationJobConfiguration jobConfig, final TableNameSchemaNameMapping tableNameSchemaNameMapping) {
        Collection<CreateTableEntry> createTableEntries = new LinkedList<>();
        for (JobDataNodeEntry each : jobConfig.getTablesFirstDataNodes().getEntries()) {
            String sourceSchemaName = tableNameSchemaNameMapping.getSchemaName(each.getLogicTableName());
            String targetSchemaName = jobConfig.getTargetDatabaseType().isSchemaAvailable() ? sourceSchemaName : null;
            DataNode dataNode = each.getDataNodes().get(0);
            PipelineDataSourceConfiguration sourceDataSourceConfig = jobConfig.getSources().get(dataNode.getDataSourceName());
            CreateTableEntry createTableEntry = new CreateTableEntry(
                    sourceDataSourceConfig, new SchemaTableName(new SchemaName(sourceSchemaName), new TableName(dataNode.getTableName())),
                    jobConfig.getTarget(), new SchemaTableName(new SchemaName(targetSchemaName), new TableName(each.getLogicTableName())));
            createTableEntries.add(createTableEntry);
        }
        CreateTableConfiguration result = new CreateTableConfiguration(createTableEntries);
        log.info("getCreateTableConfiguration, result={}", result);
        return result;
    }
    
    private DumperConfiguration buildDumperConfiguration(final String jobId, final String dataSourceName, final PipelineDataSourceConfiguration sourceDataSource,
                                                         final Map<ActualTableName, LogicTableName> tableNameMap, final TableNameSchemaNameMapping tableNameSchemaNameMapping) {
        DumperConfiguration result = new DumperConfiguration();
        result.setJobId(jobId);
        result.setDataSourceName(dataSourceName);
        result.setDataSourceConfig(sourceDataSource);
        result.setTableNameMap(tableNameMap);
        result.setTableNameSchemaNameMapping(tableNameSchemaNameMapping);
        return result;
    }
    
    private ImporterConfiguration buildImporterConfiguration(final MigrationJobConfiguration jobConfig, final PipelineProcessConfiguration pipelineProcessConfig,
                                                             final Map<LogicTableName, Set<String>> shardingColumnsMap, final TableNameSchemaNameMapping tableNameSchemaNameMapping) {
        MigrationProcessContext processContext = new MigrationProcessContext(jobConfig.getJobId(), pipelineProcessConfig);
        JobRateLimitAlgorithm writeRateLimitAlgorithm = processContext.getWriteRateLimitAlgorithm();
        int batchSize = pipelineProcessConfig.getWrite().getBatchSize();
        int retryTimes = jobConfig.getRetryTimes();
        int concurrency = jobConfig.getConcurrency();
        return new ImporterConfiguration(jobConfig.getTarget(), shardingColumnsMap, tableNameSchemaNameMapping, batchSize, writeRateLimitAlgorithm, retryTimes, concurrency);
    }
    
    @Override
    public MigrationProcessContext buildPipelineProcessContext(final PipelineJobConfiguration pipelineJobConfig) {
        PipelineProcessConfiguration processConfig = showProcessConfiguration(PipelineJobIdUtils.parseContextKey(pipelineJobConfig.getJobId()));
        return new MigrationProcessContext(pipelineJobConfig.getJobId(), processConfig);
    }
    
    @Override
    protected PipelineDataConsistencyChecker buildPipelineDataConsistencyChecker(final PipelineJobConfiguration pipelineJobConfig, final InventoryIncrementalProcessContext processContext,
                                                                                 final ConsistencyCheckJobItemProgressContext progressContext) {
        return new MigrationDataConsistencyChecker((MigrationJobConfiguration) pipelineJobConfig, processContext, progressContext);
    }
    
    @Override
    public void startDisabledJob(final String jobId) {
        super.startDisabledJob(jobId);
        PipelineAPIFactory.getGovernanceRepositoryAPI(PipelineJobIdUtils.parseContextKey(jobId)).getLatestCheckJobId(jobId).ifPresent(optional -> {
            try {
                TypedSPILoader.getService(PipelineJobAPI.class, "CONSISTENCY_CHECK").startDisabledJob(optional);
                // CHECKSTYLE:OFF
            } catch (final RuntimeException ex) {
                // CHECKSTYLE:ON
                log.warn("start related check job failed, check job id: {}, error: {}", optional, ex.getMessage());
            }
        });
    }
    
    @Override
    public void stop(final String jobId) {
        PipelineAPIFactory.getGovernanceRepositoryAPI(PipelineJobIdUtils.parseContextKey(jobId)).getLatestCheckJobId(jobId).ifPresent(optional -> {
            try {
                TypedSPILoader.getService(PipelineJobAPI.class, "CONSISTENCY_CHECK").stop(optional);
                // CHECKSTYLE:OFF
            } catch (final RuntimeException ex) {
                // CHECKSTYLE:ON
                log.warn("stop related check job failed, check job id: {}, error: {}", optional, ex.getMessage());
            }
        });
        super.stop(jobId);
    }
    
    @Override
    public void rollback(final String jobId) throws SQLException {
        final long startTimeMillis = System.currentTimeMillis();
        stop(jobId);
        dropCheckJobs(jobId);
        cleanTempTableOnRollback(jobId);
        dropJob(jobId);
        log.info("Rollback job {} cost {} ms", jobId, System.currentTimeMillis() - startTimeMillis);
    }
    
    private void dropCheckJobs(final String jobId) {
        Collection<String> checkJobIds = PipelineAPIFactory.getGovernanceRepositoryAPI(PipelineJobIdUtils.parseContextKey(jobId)).listCheckJobIds(jobId);
        if (checkJobIds.isEmpty()) {
            return;
        }
        for (String each : checkJobIds) {
            try {
                dropJob(each);
                // CHECKSTYLE:OFF
            } catch (final RuntimeException ex) {
                // CHECKSTYLE:ON
                log.info("drop check job failed, check job id: {}, error: {}", each, ex.getMessage());
            }
        }
    }
    
    private void cleanTempTableOnRollback(final String jobId) throws SQLException {
        MigrationJobConfiguration jobConfig = getJobConfiguration(jobId);
        PipelineCommonSQLBuilder pipelineSQLBuilder = new PipelineCommonSQLBuilder(jobConfig.getTargetDatabaseType());
        TableNameSchemaNameMapping mapping = new TableNameSchemaNameMapping(jobConfig.getTargetTableSchemaMap());
        try (
                PipelineDataSourceWrapper dataSource = PipelineDataSourceFactory.newInstance(jobConfig.getTarget());
                Connection connection = dataSource.getConnection()) {
            for (String each : jobConfig.getTargetTableNames()) {
                String targetSchemaName = mapping.getSchemaName(each);
                String sql = pipelineSQLBuilder.buildDropSQL(targetSchemaName, each);
                log.info("cleanTempTableOnRollback, targetSchemaName={}, targetTableName={}, sql={}", targetSchemaName, each, sql);
                try (Statement statement = connection.createStatement()) {
                    statement.execute(sql);
                }
            }
        }
    }
    
    @Override
    public void commit(final String jobId) {
        log.info("Commit job {}", jobId);
        final long startTimeMillis = System.currentTimeMillis();
        stop(jobId);
        dropCheckJobs(jobId);
        MigrationJobConfiguration jobConfig = getJobConfiguration(jobId);
        refreshTableMetadata(jobId, jobConfig.getTargetDatabaseName());
        dropJob(jobId);
        log.info("Commit cost {} ms", System.currentTimeMillis() - startTimeMillis);
    }
    
    /**
     * Add migration source resources.
     *
     * @param contextKey context key
     * @param dataSourcePropsMap data source properties map
     */
    public void addMigrationSourceResources(final PipelineContextKey contextKey, final Map<String, DataSourceProperties> dataSourcePropsMap) {
        Map<String, DataSourceProperties> existDataSources = dataSourcePersistService.load(contextKey, getJobType());
        Collection<String> duplicateDataSourceNames = new HashSet<>(dataSourcePropsMap.size(), 1F);
        for (Entry<String, DataSourceProperties> entry : dataSourcePropsMap.entrySet()) {
            if (existDataSources.containsKey(entry.getKey())) {
                duplicateDataSourceNames.add(entry.getKey());
            }
        }
        ShardingSpherePreconditions.checkState(duplicateDataSourceNames.isEmpty(), () -> new RegisterMigrationSourceStorageUnitException(duplicateDataSourceNames));
        Map<String, DataSourceProperties> result = new LinkedHashMap<>(existDataSources);
        result.putAll(dataSourcePropsMap);
        dataSourcePersistService.persist(contextKey, getJobType(), result);
    }
    
    /**
     * Drop migration source resources.
     *
     * @param contextKey context key
     * @param resourceNames resource names
     */
    public void dropMigrationSourceResources(final PipelineContextKey contextKey, final Collection<String> resourceNames) {
        Map<String, DataSourceProperties> metaDataDataSource = dataSourcePersistService.load(contextKey, getJobType());
        List<String> noExistResources = resourceNames.stream().filter(each -> !metaDataDataSource.containsKey(each)).collect(Collectors.toList());
        ShardingSpherePreconditions.checkState(noExistResources.isEmpty(), () -> new UnregisterMigrationSourceStorageUnitException(noExistResources));
        for (String each : resourceNames) {
            metaDataDataSource.remove(each);
        }
        dataSourcePersistService.persist(contextKey, getJobType(), metaDataDataSource);
    }
    
    /**
     * Query migration source resources list.
     *
     * @param contextKey context key
     * @return migration source resources
     */
    public Collection<Collection<Object>> listMigrationSourceResources(final PipelineContextKey contextKey) {
        Map<String, DataSourceProperties> dataSourcePropertiesMap = dataSourcePersistService.load(contextKey, getJobType());
        Collection<Collection<Object>> result = new ArrayList<>(dataSourcePropertiesMap.size());
        for (Entry<String, DataSourceProperties> entry : dataSourcePropertiesMap.entrySet()) {
            String dataSourceName = entry.getKey();
            DataSourceProperties value = entry.getValue();
            Collection<Object> props = new LinkedList<>();
            props.add(dataSourceName);
            String url = String.valueOf(value.getConnectionPropertySynonyms().getStandardProperties().get("url"));
            DatabaseType databaseType = DatabaseTypeFactory.get(url);
            props.add(databaseType.getType());
            DataSourceMetaData metaData = databaseType.getDataSourceMetaData(url, "");
            props.add(metaData.getHostname());
            props.add(metaData.getPort());
            props.add(metaData.getCatalog());
            Map<String, Object> standardProps = value.getPoolPropertySynonyms().getStandardProperties();
            props.add(getStandardProperty(standardProps, "connectionTimeoutMilliseconds"));
            props.add(getStandardProperty(standardProps, "idleTimeoutMilliseconds"));
            props.add(getStandardProperty(standardProps, "maxLifetimeMilliseconds"));
            props.add(getStandardProperty(standardProps, "maxPoolSize"));
            props.add(getStandardProperty(standardProps, "minPoolSize"));
            props.add(getStandardProperty(standardProps, "readOnly"));
            Map<String, Object> otherProps = value.getCustomDataSourceProperties().getProperties();
            props.add(otherProps.isEmpty() ? "" : new Gson().toJson(otherProps));
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
    
    /**
     * Refresh table metadata.
     *
     * @param jobId job id
     * @param databaseName database name
     */
    public void refreshTableMetadata(final String jobId, final String databaseName) {
        // TODO use origin database name now, wait reloadDatabaseMetaData fix case-sensitive probelm
        ContextManager contextManager = PipelineContextManager.getContext(PipelineJobIdUtils.parseContextKey(jobId)).getContextManager();
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabase(databaseName);
        contextManager.reloadDatabaseMetaData(database.getName());
    }
    
    @Override
    public JobType getJobType() {
        return TypedSPILoader.getService(JobType.class, JobCodeRegistry.getJobType(MigrationJobType.TYPE_CODE));
    }
    
    @Override
    protected String getJobClassName() {
        return MigrationJob.class.getName();
    }
}
