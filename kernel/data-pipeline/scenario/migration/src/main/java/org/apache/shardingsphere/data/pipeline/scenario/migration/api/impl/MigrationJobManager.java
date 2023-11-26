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
import org.apache.shardingsphere.data.pipeline.common.datasource.yaml.YamlPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.common.metadata.loader.PipelineSchemaUtils;
import org.apache.shardingsphere.data.pipeline.core.exception.connection.RegisterMigrationSourceStorageUnitException;
import org.apache.shardingsphere.data.pipeline.core.exception.connection.UnregisterMigrationSourceStorageUnitException;
import org.apache.shardingsphere.data.pipeline.core.exception.metadata.NoAnyRuleExistsException;
import org.apache.shardingsphere.data.pipeline.core.exception.param.PipelineInvalidParameterException;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobManager;
import org.apache.shardingsphere.data.pipeline.core.metadata.PipelineDataSourcePersistService;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.MigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.yaml.job.YamlMigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.yaml.job.YamlMigrationJobConfigurationSwapper;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
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
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.resource.YamlDataSourceConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.migration.distsql.statement.MigrateTableStatement;
import org.apache.shardingsphere.migration.distsql.statement.pojo.SourceTargetEntry;

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
 * Migration job manager.
 */
@Slf4j
public final class MigrationJobManager {
    
    private final MigrationJobOption jobOption;
    
    private final PipelineJobManager jobManager;
    
    private final PipelineDataSourcePersistService dataSourcePersistService;
    
    public MigrationJobManager(final MigrationJobOption jobOption) {
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
    
    /**
     * Add migration source resources.
     *
     * @param contextKey context key
     * @param propsMap data source pool properties map
     */
    public void addMigrationSourceResources(final PipelineContextKey contextKey, final Map<String, DataSourcePoolProperties> propsMap) {
        Map<String, DataSourcePoolProperties> existDataSources = dataSourcePersistService.load(contextKey, jobOption.getType());
        Collection<String> duplicateDataSourceNames = new HashSet<>(propsMap.size(), 1F);
        for (Entry<String, DataSourcePoolProperties> entry : propsMap.entrySet()) {
            if (existDataSources.containsKey(entry.getKey())) {
                duplicateDataSourceNames.add(entry.getKey());
            }
        }
        ShardingSpherePreconditions.checkState(duplicateDataSourceNames.isEmpty(), () -> new RegisterMigrationSourceStorageUnitException(duplicateDataSourceNames));
        Map<String, DataSourcePoolProperties> result = new LinkedHashMap<>(existDataSources);
        result.putAll(propsMap);
        dataSourcePersistService.persist(contextKey, jobOption.getType(), result);
    }
    
    /**
     * Drop migration source resources.
     *
     * @param contextKey context key
     * @param resourceNames resource names
     */
    public void dropMigrationSourceResources(final PipelineContextKey contextKey, final Collection<String> resourceNames) {
        Map<String, DataSourcePoolProperties> metaDataDataSource = dataSourcePersistService.load(contextKey, jobOption.getType());
        List<String> noExistResources = resourceNames.stream().filter(each -> !metaDataDataSource.containsKey(each)).collect(Collectors.toList());
        ShardingSpherePreconditions.checkState(noExistResources.isEmpty(), () -> new UnregisterMigrationSourceStorageUnitException(noExistResources));
        for (String each : resourceNames) {
            metaDataDataSource.remove(each);
        }
        dataSourcePersistService.persist(contextKey, jobOption.getType(), metaDataDataSource);
    }
    
    /**
     * Query migration source resources list.
     *
     * @param contextKey context key
     * @return migration source resources
     */
    public Collection<Collection<Object>> listMigrationSourceResources(final PipelineContextKey contextKey) {
        Map<String, DataSourcePoolProperties> propsMap = dataSourcePersistService.load(contextKey, jobOption.getType());
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
}
