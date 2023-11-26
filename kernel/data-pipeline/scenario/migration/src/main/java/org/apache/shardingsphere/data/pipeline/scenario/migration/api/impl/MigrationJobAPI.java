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
import org.apache.shardingsphere.data.pipeline.api.type.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.common.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.common.context.PipelineContextManager;
import org.apache.shardingsphere.data.pipeline.common.datanode.DataNodeUtils;
import org.apache.shardingsphere.data.pipeline.common.datanode.JobDataNodeEntry;
import org.apache.shardingsphere.data.pipeline.common.datanode.JobDataNodeLine;
import org.apache.shardingsphere.data.pipeline.common.datanode.JobDataNodeLineConvertUtils;
import org.apache.shardingsphere.data.pipeline.common.datasource.PipelineDataSourceFactory;
import org.apache.shardingsphere.data.pipeline.common.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.common.datasource.yaml.YamlPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.common.job.type.PipelineJobType;
import org.apache.shardingsphere.data.pipeline.common.metadata.loader.PipelineSchemaUtils;
import org.apache.shardingsphere.data.pipeline.common.sqlbuilder.PipelineCommonSQLBuilder;
import org.apache.shardingsphere.data.pipeline.core.exception.metadata.NoAnyRuleExistsException;
import org.apache.shardingsphere.data.pipeline.core.exception.param.PipelineInvalidParameterException;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.context.mapper.TableAndSchemaNameMapper;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.job.option.PipelineJobOption;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobConfigurationManager;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobManager;
import org.apache.shardingsphere.data.pipeline.core.job.service.TransmissionJobAPI;
import org.apache.shardingsphere.data.pipeline.core.metadata.PipelineDataSourcePersistService;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.MigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.yaml.job.YamlMigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.yaml.job.YamlMigrationJobConfigurationSwapper;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.database.core.metadata.database.DialectDatabaseMetaData;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.resource.YamlDataSourceConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.migration.distsql.statement.MigrateTableStatement;
import org.apache.shardingsphere.migration.distsql.statement.pojo.SourceTargetEntry;
import org.apache.shardingsphere.mode.manager.ContextManager;

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
import java.util.stream.Collectors;

/**
 * Migration job API.
 */
@Slf4j
public final class MigrationJobAPI implements TransmissionJobAPI {
    
    private final MigrationJobOption jobOption;
    
    private final PipelineJobManager jobManager;
    
    private final PipelineDataSourcePersistService dataSourcePersistService;
    
    public MigrationJobAPI(final MigrationJobOption jobOption) {
        this.jobOption = jobOption;
        jobManager = new PipelineJobManager(jobOption);
        dataSourcePersistService = new PipelineDataSourcePersistService();
    }
    
    /**
     * Start migration job.
     *
     * @param contextKey context key
     * @param param create migration job parameter
     * @return job id
     */
    public String start(final PipelineContextKey contextKey, final MigrateTableStatement param) {
        MigrationJobConfiguration jobConfig = new YamlMigrationJobConfigurationSwapper().swapToObject(buildYamlJobConfiguration(contextKey, param));
        jobManager.start(jobConfig);
        return jobConfig.getJobId();
    }
    
    private YamlMigrationJobConfiguration buildYamlJobConfiguration(final PipelineContextKey contextKey, final MigrateTableStatement param) {
        YamlMigrationJobConfiguration result = new YamlMigrationJobConfiguration();
        result.setTargetDatabaseName(param.getTargetDatabaseName());
        Map<String, DataSourcePoolProperties> metaDataDataSource = dataSourcePersistService.load(contextKey, "MIGRATION");
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
            Map<String, Object> sourceDataSourcePoolProps = dataSourceConfigSwapper.swapToMap(metaDataDataSource.get(dataSourceName));
            StandardPipelineDataSourceConfiguration sourceDataSourceConfig = new StandardPipelineDataSourceConfiguration(sourceDataSourcePoolProps);
            configSources.put(dataSourceName, buildYamlPipelineDataSourceConfiguration(sourceDataSourceConfig.getType(), sourceDataSourceConfig.getParameter()));
            DialectDatabaseMetaData dialectDatabaseMetaData = new DatabaseTypeRegistry(sourceDataSourceConfig.getDatabaseType()).getDialectDatabaseMetaData();
            if (null == each.getSource().getSchemaName() && dialectDatabaseMetaData.isSchemaAvailable()) {
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
        jobOption.extendYamlJobConfiguration(contextKey, result);
        return result;
    }
    
    private YamlPipelineDataSourceConfiguration buildYamlPipelineDataSourceConfiguration(final String type, final String param) {
        YamlPipelineDataSourceConfiguration result = new YamlPipelineDataSourceConfiguration();
        result.setType(type);
        result.setParameter(param);
        return result;
    }
    
    private PipelineDataSourceConfiguration buildTargetPipelineDataSourceConfiguration(final ShardingSphereDatabase targetDatabase) {
        Map<String, Map<String, Object>> targetPoolProps = new HashMap<>();
        YamlDataSourceConfigurationSwapper dataSourceConfigSwapper = new YamlDataSourceConfigurationSwapper();
        for (Entry<String, StorageUnit> entry : targetDatabase.getResourceMetaData().getStorageUnits().entrySet()) {
            targetPoolProps.put(entry.getKey(), dataSourceConfigSwapper.swapToMap(entry.getValue().getDataSourcePoolProperties()));
        }
        YamlRootConfiguration targetRootConfig = buildYamlRootConfiguration(targetDatabase.getName(), targetPoolProps, targetDatabase.getRuleMetaData().getConfigurations());
        return new ShardingSpherePipelineDataSourceConfiguration(targetRootConfig);
    }
    
    private YamlRootConfiguration buildYamlRootConfiguration(final String databaseName, final Map<String, Map<String, Object>> yamlDataSources, final Collection<RuleConfiguration> rules) {
        ShardingSpherePreconditions.checkState(!rules.isEmpty(), () -> new NoAnyRuleExistsException(databaseName));
        YamlRootConfiguration result = new YamlRootConfiguration();
        result.setDatabaseName(databaseName);
        result.setDataSources(yamlDataSources);
        result.setRules(new YamlRuleConfigurationSwapperEngine().swapToYamlRuleConfigurations(rules));
        return result;
    }
    
    private Map<String, String> buildTargetTableSchemaMap(final Map<String, List<DataNode>> sourceDataNodes) {
        Map<String, String> result = new LinkedHashMap<>();
        sourceDataNodes.forEach((tableName, dataNodes) -> result.put(tableName, dataNodes.get(0).getSchemaName()));
        return result;
    }
    
    @Override
    public void commit(final String jobId) {
        log.info("Commit job {}", jobId);
        final long startTimeMillis = System.currentTimeMillis();
        PipelineJobOption jobOption = new MigrationJobOption();
        PipelineJobManager jobManager = new PipelineJobManager(jobOption);
        jobManager.stop(jobId);
        dropCheckJobs(jobId);
        MigrationJobConfiguration jobConfig = new PipelineJobConfigurationManager(jobOption).getJobConfiguration(jobId);
        refreshTableMetadata(jobId, jobConfig.getTargetDatabaseName());
        jobManager.drop(jobId);
        log.info("Commit cost {} ms", System.currentTimeMillis() - startTimeMillis);
    }
    
    private void refreshTableMetadata(final String jobId, final String databaseName) {
        // TODO use origin database name now, wait reloadDatabaseMetaData fix case-sensitive probelm
        ContextManager contextManager = PipelineContextManager.getContext(PipelineJobIdUtils.parseContextKey(jobId)).getContextManager();
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabase(databaseName);
        contextManager.reloadDatabaseMetaData(database.getName());
    }
    
    @Override
    public void rollback(final String jobId) throws SQLException {
        final long startTimeMillis = System.currentTimeMillis();
        dropCheckJobs(jobId);
        cleanTempTableOnRollback(jobId);
        new PipelineJobManager(TypedSPILoader.getService(PipelineJobType.class, getType()).getOption()).drop(jobId);
        log.info("Rollback job {} cost {} ms", jobId, System.currentTimeMillis() - startTimeMillis);
    }
    
    private void dropCheckJobs(final String jobId) {
        Collection<String> checkJobIds = PipelineAPIFactory.getPipelineGovernanceFacade(PipelineJobIdUtils.parseContextKey(jobId)).getJobFacade().getCheck().listCheckJobIds(jobId);
        if (checkJobIds.isEmpty()) {
            return;
        }
        for (String each : checkJobIds) {
            try {
                new PipelineJobManager(TypedSPILoader.getService(PipelineJobType.class, getType()).getOption()).drop(each);
                // CHECKSTYLE:OFF
            } catch (final RuntimeException ex) {
                // CHECKSTYLE:ON
                log.info("drop check job failed, check job id: {}, error: {}", each, ex.getMessage());
            }
        }
    }
    
    private void cleanTempTableOnRollback(final String jobId) throws SQLException {
        MigrationJobConfiguration jobConfig = new PipelineJobConfigurationManager(TypedSPILoader.getService(PipelineJobType.class, getType()).getOption()).getJobConfiguration(jobId);
        PipelineCommonSQLBuilder pipelineSQLBuilder = new PipelineCommonSQLBuilder(jobConfig.getTargetDatabaseType());
        TableAndSchemaNameMapper mapping = new TableAndSchemaNameMapper(jobConfig.getTargetTableSchemaMap());
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
    public String getType() {
        return "MIGRATION";
    }
}
