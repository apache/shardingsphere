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

package org.apache.shardingsphere.test.it.data.pipeline.core.util;

import lombok.SneakyThrows;
import org.apache.shardingsphere.data.pipeline.api.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.type.ShardingSpherePipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContextManager;
import org.apache.shardingsphere.data.pipeline.core.context.TransmissionProcessContext;
import org.apache.shardingsphere.data.pipeline.core.datanode.JobDataNodeEntry;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.execute.PipelineExecuteEngine;
import org.apache.shardingsphere.data.pipeline.core.importer.ImporterConfiguration;
import org.apache.shardingsphere.data.pipeline.core.importer.PipelineRequiredColumnsExtractor;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.incremental.IncrementalDumperContext;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.mapper.TableAndSchemaNameMapper;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.PipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.PipelineProcessConfigurationUtils;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.yaml.config.YamlPipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.yaml.config.YamlPipelineReadConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.yaml.swapper.YamlPipelineProcessConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.core.metadata.model.PipelineColumnMetaData;
import org.apache.shardingsphere.data.pipeline.core.preparer.datasource.param.CreateTableConfiguration;
import org.apache.shardingsphere.data.pipeline.core.ratelimit.JobRateLimitAlgorithm;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.MigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.MigrationTaskConfiguration;
import org.apache.shardingsphere.data.pipeline.scenario.migration.context.MigrationJobItemContext;
import org.apache.shardingsphere.data.pipeline.scenario.migration.ingest.dumper.MigrationIncrementalDumperContextCreator;
import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.identifier.ShardingSphereIdentifier;
import org.apache.shardingsphere.infra.metadata.statistics.builder.ShardingSphereStatisticsFactory;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.file.SystemResourceFileUtils;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.mode.YamlModeConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.resource.YamlDataSourceConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistFacade;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;
import org.apache.shardingsphere.test.it.data.pipeline.core.fixture.EmbedTestingServer;
import org.mockito.internal.configuration.plugins.Plugins;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import static org.mockito.Mockito.mock;

/**
 * Pipeline context utility class.
 */
public final class PipelineContextUtils {
    
    private static final PipelineContextKey CONTEXT_KEY = new PipelineContextKey(InstanceType.PROXY);
    
    private static final PipelineExecuteEngine EXECUTE_ENGINE = PipelineExecuteEngine.newCachedThreadInstance(PipelineContextUtils.class.getSimpleName());
    
    /**
     * Init pipeline context manager.
     */
    public static void initPipelineContextManager() {
        EmbedTestingServer.start();
        PipelineContextKey contextKey = getContextKey();
        if (null != PipelineContextManager.getContext(contextKey)) {
            return;
        }
        ShardingSpherePipelineDataSourceConfiguration pipelineDataSourceConfig = new ShardingSpherePipelineDataSourceConfiguration(
                SystemResourceFileUtils.readFile("config_sharding_sphere_jdbc_source.yaml"));
        YamlRootConfiguration rootConfig = (YamlRootConfiguration) pipelineDataSourceConfig.getDataSourceConfiguration();
        ContextManager contextManager = getContextManager(rootConfig);
        ClusterPersistRepository persistRepository = getClusterPersistRepository(
                (ClusterPersistRepositoryConfiguration) contextManager.getComputeNodeInstanceContext().getModeConfiguration().getRepository());
        MetaDataContexts metaDataContexts = renewMetaDataContexts(contextManager.getMetaDataContexts(), new MetaDataPersistFacade(persistRepository, true));
        PipelineContextManager.putContext(contextKey, new ContextManager(
                metaDataContexts, contextManager.getComputeNodeInstanceContext(), contextManager.getExclusiveOperatorEngine(), contextManager.getPersistServiceFacade().getRepository()));
    }
    
    @SneakyThrows({ReflectiveOperationException.class, SQLException.class})
    private static ContextManager getContextManager(final YamlRootConfiguration rootConfig) {
        return (ContextManager) Plugins.getMemberAccessor().get(ShardingSphereDataSource.class.getDeclaredField("contextManager"), createDataSource(rootConfig));
    }
    
    private static DataSource createDataSource(final YamlRootConfiguration rootConfig) throws SQLException {
        Map<String, DataSource> dataSourceMap = new YamlDataSourceConfigurationSwapper().swapToDataSources(rootConfig.getDataSources(), false);
        ModeConfiguration modeConfig = new YamlModeConfigurationSwapper().swapToObject(rootConfig.getMode());
        Collection<RuleConfiguration> ruleConfigs = new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(rootConfig.getRules());
        return ShardingSphereDataSourceFactory.createDataSource(rootConfig.getDatabaseName(), modeConfig, dataSourceMap, ruleConfigs, rootConfig.getProps());
    }
    
    private static ClusterPersistRepository getClusterPersistRepository(final ClusterPersistRepositoryConfiguration repositoryConfig) {
        ClusterPersistRepository result = TypedSPILoader.getService(ClusterPersistRepository.class, repositoryConfig.getType(), repositoryConfig.getProps());
        result.init(repositoryConfig, mock(ComputeNodeInstanceContext.class));
        return result;
    }
    
    private static MetaDataContexts renewMetaDataContexts(final MetaDataContexts old, final MetaDataPersistFacade persistFacade) {
        Collection<ShardingSphereTable> tables = new LinkedList<>();
        tables.add(new ShardingSphereTable("t_order", Arrays.asList(
                new ShardingSphereColumn("order_id", Types.INTEGER, true, false, false, true, false, false),
                new ShardingSphereColumn("user_id", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("status", Types.VARCHAR, false, false, false, true, false, false)), Collections.emptyList(), Collections.emptyList()));
        tables.add(new ShardingSphereTable("t_order_item", Arrays.asList(
                new ShardingSphereColumn("item_id", Types.INTEGER, true, false, false, true, false, false),
                new ShardingSphereColumn("order_id", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("user_id", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("status", Types.VARCHAR, false, false, false, true, false, false)),
                Collections.emptyList(), Collections.emptyList()));
        tables.forEach(each -> old.getMetaData().getDatabase("logic_db").getSchema("logic_db").putTable(each));
        return new MetaDataContexts(old.getMetaData(), ShardingSphereStatisticsFactory.create(old.getMetaData(), persistFacade.getStatisticsService().load(old.getMetaData())));
    }
    
    /**
     * Get create order table schema.
     *
     * @return order table schema
     */
    public static String getCreateOrderTableSchema() {
        return "CREATE TABLE IF NOT EXISTS t_order (order_id INT PRIMARY KEY, user_id INT, status VARCHAR(32))";
    }
    
    /**
     * Mock order_id column meta data.
     *
     * @return mocked column meta data
     */
    public static PipelineColumnMetaData mockOrderIdColumnMetaData() {
        return new PipelineColumnMetaData(1, "order_id", Types.INTEGER, "int", false, true, true);
    }
    
    /**
     * Get context key.
     *
     * @return context key
     */
    public static PipelineContextKey getContextKey() {
        return CONTEXT_KEY;
    }
    
    /**
     * Get execute engine.
     *
     * @return execute engine
     */
    public static PipelineExecuteEngine getExecuteEngine() {
        return EXECUTE_ENGINE;
    }
    
    /**
     * Mock migration job item context.
     *
     * @param jobConfig job configuration
     * @return job item context
     */
    public static MigrationJobItemContext mockMigrationJobItemContext(final MigrationJobConfiguration jobConfig) {
        PipelineProcessConfiguration processConfig = mockPipelineProcessConfiguration();
        TransmissionProcessContext processContext = new TransmissionProcessContext(jobConfig.getJobId(), processConfig);
        int jobShardingItem = 0;
        MigrationTaskConfiguration taskConfig = buildTaskConfiguration(jobConfig, jobShardingItem, processConfig);
        return new MigrationJobItemContext(jobConfig, jobShardingItem, null, processContext, taskConfig, new PipelineDataSourceManager());
    }
    
    private static PipelineProcessConfiguration mockPipelineProcessConfiguration() {
        YamlPipelineReadConfiguration yamlReadConfig = new YamlPipelineReadConfiguration();
        yamlReadConfig.setShardingSize(10);
        YamlPipelineProcessConfiguration yamlProcessConfig = new YamlPipelineProcessConfiguration();
        yamlProcessConfig.setRead(yamlReadConfig);
        PipelineProcessConfigurationUtils.fillInDefaultValue(new YamlPipelineProcessConfigurationSwapper().swapToObject(yamlProcessConfig));
        return new YamlPipelineProcessConfigurationSwapper().swapToObject(yamlProcessConfig);
    }
    
    private static MigrationTaskConfiguration buildTaskConfiguration(final MigrationJobConfiguration jobConfig, final int jobShardingItem, final PipelineProcessConfiguration processConfig) {
        Map<ShardingSphereIdentifier, Collection<String>> tableAndRequiredColumnsMap = getTableAndRequiredColumnsMap(jobConfig);
        IncrementalDumperContext incrementalDumperContext = new MigrationIncrementalDumperContextCreator(jobConfig).createDumperContext(jobConfig.getJobShardingDataNodes().get(jobShardingItem));
        Collection<CreateTableConfiguration> createTableConfigs = buildCreateTableConfigurations(jobConfig, incrementalDumperContext.getCommonContext().getTableAndSchemaNameMapper());
        ImporterConfiguration importerConfig = buildImporterConfiguration(
                jobConfig, processConfig, tableAndRequiredColumnsMap, incrementalDumperContext.getCommonContext().getTableAndSchemaNameMapper());
        return new MigrationTaskConfiguration(incrementalDumperContext.getCommonContext().getDataSourceName(), createTableConfigs, incrementalDumperContext, importerConfig);
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Map<ShardingSphereIdentifier, Collection<String>> getTableAndRequiredColumnsMap(final MigrationJobConfiguration jobConfig) {
        Map<ShardingSphereIdentifier, Collection<String>> result = new HashMap<>();
        Collection<YamlRuleConfiguration> yamlRuleConfigs = ((ShardingSpherePipelineDataSourceConfiguration) jobConfig.getTarget()).getRootConfig().getRules();
        Set<ShardingSphereIdentifier> targetTableNames = jobConfig.getTargetTableNames().stream().map(ShardingSphereIdentifier::new).collect(Collectors.toSet());
        for (Entry<YamlRuleConfiguration, PipelineRequiredColumnsExtractor> entry : OrderedSPILoader.getServices(PipelineRequiredColumnsExtractor.class, yamlRuleConfigs).entrySet()) {
            result.putAll(entry.getValue().getTableAndRequiredColumnsMap(entry.getKey(), targetTableNames));
        }
        return result;
    }
    
    private static Collection<CreateTableConfiguration> buildCreateTableConfigurations(final MigrationJobConfiguration jobConfig, final TableAndSchemaNameMapper tableAndSchemaNameMapper) {
        Collection<CreateTableConfiguration> result = new LinkedList<>();
        for (JobDataNodeEntry each : jobConfig.getTablesFirstDataNodes().getEntries()) {
            String sourceSchemaName = tableAndSchemaNameMapper.getSchemaName(each.getLogicTableName());
            DialectDatabaseMetaData dialectDatabaseMetaData = new DatabaseTypeRegistry(jobConfig.getTargetDatabaseType()).getDialectDatabaseMetaData();
            String targetSchemaName = dialectDatabaseMetaData.getSchemaOption().isSchemaAvailable() ? sourceSchemaName : null;
            DataNode dataNode = each.getDataNodes().get(0);
            PipelineDataSourceConfiguration sourceDataSourceConfig = jobConfig.getSources().get(dataNode.getDataSourceName());
            CreateTableConfiguration createTableConfig = new CreateTableConfiguration(sourceDataSourceConfig, new QualifiedTable(sourceSchemaName, dataNode.getTableName()),
                    jobConfig.getTarget(), new QualifiedTable(targetSchemaName, each.getLogicTableName()));
            result.add(createTableConfig);
        }
        return result;
    }
    
    private static ImporterConfiguration buildImporterConfiguration(final MigrationJobConfiguration jobConfig, final PipelineProcessConfiguration pipelineProcessConfig,
                                                                    final Map<ShardingSphereIdentifier, Collection<String>> tableAndRequiredColumnsMap,
                                                                    final TableAndSchemaNameMapper tableAndSchemaNameMapper) {
        int batchSize = pipelineProcessConfig.getWrite().getBatchSize();
        JobRateLimitAlgorithm writeRateLimitAlgorithm = new TransmissionProcessContext(jobConfig.getJobId(), pipelineProcessConfig).getWriteRateLimitAlgorithm();
        int retryTimes = jobConfig.getRetryTimes();
        int concurrency = jobConfig.getConcurrency();
        return new ImporterConfiguration(jobConfig.getTarget(), tableAndRequiredColumnsMap, tableAndSchemaNameMapper, batchSize, writeRateLimitAlgorithm, retryTimes, concurrency);
    }
}
