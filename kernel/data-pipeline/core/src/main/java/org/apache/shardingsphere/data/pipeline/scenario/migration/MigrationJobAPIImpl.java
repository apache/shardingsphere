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

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.PipelineDataConsistencyChecker;
import org.apache.shardingsphere.data.pipeline.api.config.CreateTableConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.CreateTableConfiguration.CreateTableEntry;
import org.apache.shardingsphere.data.pipeline.api.config.ImporterConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.TableNameSchemaNameMapping;
import org.apache.shardingsphere.data.pipeline.api.config.ingest.DumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.job.MigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.job.PipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.yaml.job.YamlMigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.yaml.job.YamlMigrationJobConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.api.config.job.yaml.YamlPipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.process.PipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datanode.JobDataNodeEntry;
import org.apache.shardingsphere.data.pipeline.api.datanode.JobDataNodeLine;
import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfigurationFactory;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.ShardingSpherePipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.yaml.YamlPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.job.JobType;
import org.apache.shardingsphere.data.pipeline.api.job.PipelineJobId;
import org.apache.shardingsphere.data.pipeline.api.metadata.ActualTableName;
import org.apache.shardingsphere.data.pipeline.api.metadata.LogicTableName;
import org.apache.shardingsphere.data.pipeline.api.metadata.SchemaName;
import org.apache.shardingsphere.data.pipeline.api.metadata.SchemaTableName;
import org.apache.shardingsphere.data.pipeline.api.metadata.TableName;
import org.apache.shardingsphere.data.pipeline.yaml.metadata.YamlPipelineColumnMetaData;
import org.apache.shardingsphere.data.pipeline.yaml.metadata.YamlPipelineColumnMetaDataSwapper;
import org.apache.shardingsphere.data.pipeline.api.pojo.CreateMigrationJobParameter;
import org.apache.shardingsphere.data.pipeline.api.pojo.MigrationJobInfo;
import org.apache.shardingsphere.data.pipeline.core.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.api.impl.AbstractInventoryIncrementalJobAPIImpl;
import org.apache.shardingsphere.data.pipeline.core.api.impl.PipelineDataSourcePersistService;
import org.apache.shardingsphere.data.pipeline.core.context.InventoryIncrementalProcessContext;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContext;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceFactory;
import org.apache.shardingsphere.data.pipeline.core.exception.connection.AddMigrationSourceResourceException;
import org.apache.shardingsphere.data.pipeline.core.exception.connection.DropMigrationSourceResourceException;
import org.apache.shardingsphere.data.pipeline.core.metadata.loader.PipelineSchemaUtil;
import org.apache.shardingsphere.data.pipeline.core.metadata.loader.PipelineTableMetaDataUtil;
import org.apache.shardingsphere.data.pipeline.core.metadata.loader.StandardPipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.core.sqlbuilder.PipelineSQLBuilderFactory;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.ConsistencyCheckJobAPIFactory;
import org.apache.shardingsphere.data.pipeline.spi.sqlbuilder.PipelineSQLBuilder;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.database.metadata.DataSourceMetaData;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.datasource.props.DataSourcePropertiesCreator;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.resource.YamlDataSourceConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapperEngine;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
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
 * Migration job API impl.
 */
@Slf4j
public final class MigrationJobAPIImpl extends AbstractInventoryIncrementalJobAPIImpl implements MigrationJobAPI {
    
    private static final YamlRuleConfigurationSwapperEngine RULE_CONFIG_SWAPPER_ENGINE = new YamlRuleConfigurationSwapperEngine();
    
    private static final YamlDataSourceConfigurationSwapper DATA_SOURCE_CONFIG_SWAPPER = new YamlDataSourceConfigurationSwapper();
    
    private final PipelineDataSourcePersistService dataSourcePersistService = new PipelineDataSourcePersistService();
    
    @Override
    protected String marshalJobIdLeftPart(final PipelineJobId pipelineJobId) {
        MigrationJobId jobId = (MigrationJobId) pipelineJobId;
        String sourceSchemaName = null != jobId.getSourceSchemaName() ? jobId.getSourceSchemaName() : "";
        String text = Joiner.on('|').join(jobId.getSourceResourceName(), sourceSchemaName, jobId.getSourceTableName(), jobId.getTargetDatabaseName(), jobId.getTargetTableName());
        return DigestUtils.md5Hex(text.getBytes(StandardCharsets.UTF_8));
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public List<MigrationJobInfo> list() {
        return (List<MigrationJobInfo>) super.list();
    }
    
    @Override
    protected MigrationJobInfo getJobInfo(final String jobId) {
        MigrationJobInfo result = new MigrationJobInfo(jobId);
        JobConfigurationPOJO jobConfigPOJO = getElasticJobConfigPOJO(jobId);
        fillJobInfo(result, jobConfigPOJO);
        result.setTable(getJobConfiguration(jobConfigPOJO).getSourceTableName());
        return result;
    }
    
    @Override
    public void extendYamlJobConfiguration(final YamlPipelineJobConfiguration yamlJobConfig) {
        YamlMigrationJobConfiguration config = (YamlMigrationJobConfiguration) yamlJobConfig;
        if (null == yamlJobConfig.getJobId()) {
            config.setJobId(generateJobId(config));
        }
        if (Strings.isNullOrEmpty(config.getSourceDatabaseType())) {
            PipelineDataSourceConfiguration sourceDataSourceConfig = PipelineDataSourceConfigurationFactory.newInstance(config.getSource().getType(), config.getSource().getParameter());
            config.setSourceDatabaseType(sourceDataSourceConfig.getDatabaseType().getType());
        }
        if (Strings.isNullOrEmpty(config.getTargetDatabaseType())) {
            PipelineDataSourceConfiguration targetDataSourceConfig = PipelineDataSourceConfigurationFactory.newInstance(config.getTarget().getType(), config.getTarget().getParameter());
            config.setTargetDatabaseType(targetDataSourceConfig.getDatabaseType().getType());
        }
        // target table name is logic table name, source table name is actual table name.
        JobDataNodeEntry nodeEntry = new JobDataNodeEntry(config.getTargetTableName(),
                Collections.singletonList(new DataNode(config.getSourceResourceName(), config.getSourceTableName())));
        String dataNodeLine = new JobDataNodeLine(Collections.singletonList(nodeEntry)).marshal();
        config.setTablesFirstDataNodes(dataNodeLine);
        config.setJobShardingDataNodes(Collections.singletonList(dataNodeLine));
    }
    
    private String generateJobId(final YamlMigrationJobConfiguration config) {
        MigrationJobId jobId = new MigrationJobId(config.getSourceResourceName(), config.getSourceSchemaName(), config.getSourceTableName(),
                config.getTargetDatabaseName(), config.getTargetTableName());
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
    protected String getTargetDatabaseType(final PipelineJobConfiguration pipelineJobConfig) {
        return ((MigrationJobConfiguration) pipelineJobConfig).getTargetDatabaseType();
    }
    
    @Override
    public MigrationTaskConfiguration buildTaskConfiguration(final PipelineJobConfiguration pipelineJobConfig, final int jobShardingItem, final PipelineProcessConfiguration pipelineProcessConfig) {
        MigrationJobConfiguration jobConfig = (MigrationJobConfiguration) pipelineJobConfig;
        Map<ActualTableName, LogicTableName> tableNameMap = new LinkedHashMap<>();
        tableNameMap.put(new ActualTableName(jobConfig.getSourceTableName()), new LogicTableName(jobConfig.getTargetTableName()));
        Map<LogicTableName, String> tableNameSchemaMap = TableNameSchemaNameMapping.convert(jobConfig.getSourceSchemaName(), Collections.singletonList(jobConfig.getTargetTableName()));
        TableNameSchemaNameMapping tableNameSchemaNameMapping = new TableNameSchemaNameMapping(tableNameSchemaMap);
        CreateTableConfiguration createTableConfig = buildCreateTableConfiguration(jobConfig);
        DumperConfiguration dumperConfig = buildDumperConfiguration(jobConfig.getJobId(), jobConfig.getSourceResourceName(), jobConfig.getSource(), tableNameMap, tableNameSchemaNameMapping);
        // TODO now shardingColumnsMap always empty,
        ImporterConfiguration importerConfig = buildImporterConfiguration(jobConfig, pipelineProcessConfig, Collections.emptyMap(), tableNameSchemaNameMapping);
        MigrationTaskConfiguration result = new MigrationTaskConfiguration(jobConfig.getSourceResourceName(), createTableConfig, dumperConfig, importerConfig);
        log.info("buildTaskConfiguration, sourceResourceName={}, result={}", jobConfig.getSourceResourceName(), result);
        return result;
    }
    
    private CreateTableConfiguration buildCreateTableConfiguration(final MigrationJobConfiguration jobConfig) {
        String sourceSchemaName = jobConfig.getSourceSchemaName();
        String targetSchemaName = DatabaseTypeFactory.getInstance(jobConfig.getTargetDatabaseType()).isSchemaAvailable() ? sourceSchemaName : null;
        CreateTableEntry createTableEntry = new CreateTableEntry(
                jobConfig.getSource(), new SchemaTableName(new SchemaName(sourceSchemaName), new TableName(jobConfig.getSourceTableName())),
                jobConfig.getTarget(), new SchemaTableName(new SchemaName(targetSchemaName), new TableName(jobConfig.getTargetTableName())));
        return new CreateTableConfiguration(Collections.singletonList(createTableEntry));
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
        int batchSize = pipelineProcessConfig.getWrite().getBatchSize();
        int retryTimes = jobConfig.getRetryTimes();
        int concurrency = jobConfig.getConcurrency();
        MigrationProcessContext migrationProcessContext = new MigrationProcessContext(jobConfig.getJobId(), pipelineProcessConfig);
        return new ImporterConfiguration(jobConfig.getTarget(), unmodifiable(shardingColumnsMap), tableNameSchemaNameMapping, batchSize, migrationProcessContext.getWriteRateLimitAlgorithm(),
                retryTimes, concurrency);
    }
    
    private Map<LogicTableName, Set<String>> unmodifiable(final Map<LogicTableName, Set<String>> shardingColumnsMap) {
        Map<LogicTableName, Set<String>> result = new HashMap<>(shardingColumnsMap.size());
        for (Entry<LogicTableName, Set<String>> entry : shardingColumnsMap.entrySet()) {
            result.put(entry.getKey(), Collections.unmodifiableSet(entry.getValue()));
        }
        return Collections.unmodifiableMap(result);
    }
    
    @Override
    public MigrationProcessContext buildPipelineProcessContext(final PipelineJobConfiguration pipelineJobConfig) {
        // TODO cache process config on local
        PipelineProcessConfiguration processConfig = showProcessConfiguration();
        return new MigrationProcessContext(pipelineJobConfig.getJobId(), processConfig);
    }
    
    @Override
    protected PipelineDataConsistencyChecker buildPipelineDataConsistencyChecker(final PipelineJobConfiguration pipelineJobConfig, final InventoryIncrementalProcessContext processContext) {
        return new MigrationDataConsistencyChecker((MigrationJobConfiguration) pipelineJobConfig, processContext);
    }
    
    @Override
    public void rollback(final String jobId) throws SQLException {
        log.info("Rollback job {}", jobId);
        final long startTimeMillis = System.currentTimeMillis();
        dropCheckJobs(jobId);
        stop(jobId);
        cleanTempTableOnRollback(jobId);
        dropJob(jobId);
        log.info("Rollback cost {} ms", System.currentTimeMillis() - startTimeMillis);
    }
    
    private void dropCheckJobs(final String jobId) {
        Collection<String> checkJobIds = PipelineAPIFactory.getGovernanceRepositoryAPI().listCheckJobIds(jobId);
        if (checkJobIds.isEmpty()) {
            return;
        }
        log.info("dropCheckJobs start...");
        long startTimeMillis = System.currentTimeMillis();
        for (String each : checkJobIds) {
            dropJob(each);
        }
        log.info("dropCheckJobs cost {} ms", System.currentTimeMillis() - startTimeMillis);
    }
    
    private void cleanTempTableOnRollback(final String jobId) throws SQLException {
        MigrationJobConfiguration jobConfig = getJobConfiguration(jobId);
        String targetTableName = jobConfig.getTargetTableName();
        // TODO use jobConfig.targetSchemaName
        String targetSchemaName = jobConfig.getSourceSchemaName();
        PipelineSQLBuilder pipelineSQLBuilder = PipelineSQLBuilderFactory.getInstance(jobConfig.getTargetDatabaseType());
        try (
                PipelineDataSourceWrapper dataSource = PipelineDataSourceFactory.newInstance(jobConfig.getTarget());
                Connection connection = dataSource.getConnection()) {
            String sql = pipelineSQLBuilder.buildDropSQL(targetSchemaName, targetTableName);
            log.info("cleanTempTableOnRollback, targetSchemaName={}, targetTableName={}, sql={}", targetSchemaName, targetTableName, sql);
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.execute();
            }
        }
    }
    
    @Override
    public void commit(final String jobId) {
        checkModeConfig();
        log.info("Commit job {}", jobId);
        final long startTimeMillis = System.currentTimeMillis();
        dropCheckJobs(jobId);
        stop(jobId);
        dropJob(jobId);
        log.info("Commit cost {} ms", System.currentTimeMillis() - startTimeMillis);
    }
    
    @Override
    public void addMigrationSourceResources(final Map<String, DataSourceProperties> dataSourcePropsMap) {
        log.info("Add migration source resources {}", dataSourcePropsMap.keySet());
        Map<String, DataSourceProperties> existDataSources = dataSourcePersistService.load(getJobType());
        Collection<String> duplicateDataSourceNames = new HashSet<>(dataSourcePropsMap.size(), 1);
        for (Entry<String, DataSourceProperties> entry : dataSourcePropsMap.entrySet()) {
            if (existDataSources.containsKey(entry.getKey())) {
                duplicateDataSourceNames.add(entry.getKey());
            }
        }
        ShardingSpherePreconditions.checkState(duplicateDataSourceNames.isEmpty(), () -> new AddMigrationSourceResourceException(duplicateDataSourceNames));
        Map<String, DataSourceProperties> result = new LinkedHashMap<>(existDataSources);
        result.putAll(dataSourcePropsMap);
        dataSourcePersistService.persist(getJobType(), result);
    }
    
    @Override
    public void dropMigrationSourceResources(final Collection<String> resourceNames) {
        Map<String, DataSourceProperties> metaDataDataSource = dataSourcePersistService.load(getJobType());
        List<String> noExistResources = resourceNames.stream().filter(each -> !metaDataDataSource.containsKey(each)).collect(Collectors.toList());
        ShardingSpherePreconditions.checkState(noExistResources.isEmpty(), () -> new DropMigrationSourceResourceException(resourceNames));
        for (String each : resourceNames) {
            metaDataDataSource.remove(each);
        }
        dataSourcePersistService.persist(getJobType(), metaDataDataSource);
    }
    
    @Override
    public Collection<Collection<Object>> listMigrationSourceResources() {
        Map<String, DataSourceProperties> dataSourcePropertiesMap = dataSourcePersistService.load(getJobType());
        Collection<Collection<Object>> result = new ArrayList<>(dataSourcePropertiesMap.size());
        for (Entry<String, DataSourceProperties> entry : dataSourcePropertiesMap.entrySet()) {
            String dataSourceName = entry.getKey();
            DataSourceProperties value = entry.getValue();
            Collection<Object> props = new LinkedList<>();
            props.add(dataSourceName);
            String url = String.valueOf(value.getConnectionPropertySynonyms().getStandardProperties().get("url"));
            DatabaseType databaseType = DatabaseTypeEngine.getDatabaseType(url);
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
    
    @Override
    public String createJobAndStart(final CreateMigrationJobParameter parameter) {
        YamlMigrationJobConfiguration result = new YamlMigrationJobConfiguration();
        Map<String, DataSourceProperties> metaDataDataSource = dataSourcePersistService.load(JobType.MIGRATION);
        Map<String, Object> sourceDataSourceProps = DATA_SOURCE_CONFIG_SWAPPER.swapToMap(metaDataDataSource.get(parameter.getSourceResourceName()));
        YamlPipelineDataSourceConfiguration sourcePipelineDataSourceConfiguration = createYamlPipelineDataSourceConfiguration(StandardPipelineDataSourceConfiguration.TYPE,
                YamlEngine.marshal(sourceDataSourceProps));
        result.setSource(sourcePipelineDataSourceConfiguration);
        result.setSourceResourceName(parameter.getSourceResourceName());
        StandardPipelineDataSourceConfiguration sourceDataSourceConfig = new StandardPipelineDataSourceConfiguration(sourceDataSourceProps);
        DatabaseType sourceDatabaseType = sourceDataSourceConfig.getDatabaseType();
        result.setSourceDatabaseType(sourceDatabaseType.getType());
        String sourceSchemaName = null == parameter.getSourceSchemaName() && sourceDatabaseType.isSchemaAvailable()
                ? PipelineSchemaUtil.getDefaultSchema(sourceDataSourceConfig)
                : parameter.getSourceSchemaName();
        result.setSourceSchemaName(sourceSchemaName);
        result.setSourceTableName(parameter.getSourceTableName());
        Map<String, Map<String, Object>> targetDataSourceProperties = new HashMap<>();
        ShardingSphereDatabase targetDatabase = PipelineContext.getContextManager().getMetaDataContexts().getMetaData().getDatabase(parameter.getTargetDatabaseName());
        for (Entry<String, DataSource> entry : targetDatabase.getResource().getDataSources().entrySet()) {
            Map<String, Object> dataSourceProps = DATA_SOURCE_CONFIG_SWAPPER.swapToMap(DataSourcePropertiesCreator.create(entry.getValue()));
            targetDataSourceProperties.put(entry.getKey(), dataSourceProps);
        }
        String targetDatabaseName = parameter.getTargetDatabaseName();
        YamlRootConfiguration targetRootConfig = getYamlRootConfiguration(targetDatabaseName, targetDataSourceProperties, targetDatabase.getRuleMetaData().getConfigurations());
        PipelineDataSourceConfiguration targetPipelineDataSource = new ShardingSpherePipelineDataSourceConfiguration(targetRootConfig);
        result.setTarget(createYamlPipelineDataSourceConfiguration(targetPipelineDataSource.getType(), YamlEngine.marshal(targetPipelineDataSource.getDataSourceConfiguration())));
        result.setTargetDatabaseType(targetPipelineDataSource.getDatabaseType().getType());
        result.setTargetDatabaseName(targetDatabaseName);
        result.setTargetTableName(parameter.getTargetTableName());
        try (PipelineDataSourceWrapper dataSource = PipelineDataSourceFactory.newInstance(sourceDataSourceConfig)) {
            StandardPipelineTableMetaDataLoader metaDataLoader = new StandardPipelineTableMetaDataLoader(dataSource);
            YamlPipelineColumnMetaData uniqueKeyColumn = new YamlPipelineColumnMetaDataSwapper().swapToYamlConfiguration(
                    PipelineTableMetaDataUtil.getUniqueKeyColumn(sourceSchemaName, parameter.getSourceTableName(), metaDataLoader));
            result.setUniqueKeyColumn(uniqueKeyColumn);
        } catch (final SQLException ex) {
            throw new RuntimeException(ex);
        }
        extendYamlJobConfiguration(result);
        MigrationJobConfiguration jobConfiguration = new YamlMigrationJobConfigurationSwapper().swapToObject(result);
        start(jobConfiguration);
        return jobConfiguration.getJobId();
    }
    
    private YamlRootConfiguration getYamlRootConfiguration(final String databaseName, final Map<String, Map<String, Object>> yamlDataSources, final Collection<RuleConfiguration> rules) {
        YamlRootConfiguration result = new YamlRootConfiguration();
        result.setDatabaseName(databaseName);
        result.setDataSources(yamlDataSources);
        Collection<YamlRuleConfiguration> yamlRuleConfigurations = RULE_CONFIG_SWAPPER_ENGINE.swapToYamlRuleConfigurations(rules);
        result.setRules(yamlRuleConfigurations);
        return result;
    }
    
    private YamlPipelineDataSourceConfiguration createYamlPipelineDataSourceConfiguration(final String type, final String parameter) {
        YamlPipelineDataSourceConfiguration result = new YamlPipelineDataSourceConfiguration();
        result.setType(type);
        result.setParameter(parameter);
        return result;
    }
    
    @Override
    public void startDisabledJob(final String jobId) {
        super.startDisabledJob(jobId);
        Optional<String> optional = PipelineAPIFactory.getGovernanceRepositoryAPI().getCheckLatestJobId(jobId);
        optional.ifPresent(s -> ConsistencyCheckJobAPIFactory.getInstance().startDisabledJob(s));
    }
    
    @Override
    public JobType getJobType() {
        return JobType.MIGRATION;
    }
    
    @Override
    protected String getJobClassName() {
        return MigrationJob.class.getName();
    }
}
