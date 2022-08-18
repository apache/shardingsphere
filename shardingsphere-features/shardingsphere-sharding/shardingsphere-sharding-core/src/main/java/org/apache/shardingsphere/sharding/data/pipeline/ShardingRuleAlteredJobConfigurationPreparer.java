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

package org.apache.shardingsphere.sharding.data.pipeline;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.config.ImporterConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.TableNameSchemaNameMapping;
import org.apache.shardingsphere.data.pipeline.api.config.TaskConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.ingest.DumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.job.MigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.job.yaml.YamlMigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.job.yaml.YamlMigrationJobConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.api.datanode.JobDataNodeEntry;
import org.apache.shardingsphere.data.pipeline.api.datanode.JobDataNodeLine;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfigurationFactory;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.ShardingSpherePipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.metadata.ActualTableName;
import org.apache.shardingsphere.data.pipeline.api.metadata.LogicTableName;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContext;
import org.apache.shardingsphere.data.pipeline.spi.rulealtered.RuleAlteredJobConfigurationPreparer;
import org.apache.shardingsphere.infra.config.rule.data.pipeline.PipelineProcessConfiguration;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.swapper.resource.YamlDataSourceConfigurationSwapper;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ComplexShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;
import org.apache.shardingsphere.sharding.yaml.config.YamlShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.swapper.ShardingRuleConfigurationConverter;

import java.util.ArrayList;
import java.util.Arrays;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Sharding rule altered job configuration preparer.
 */
@Slf4j
public final class ShardingRuleAlteredJobConfigurationPreparer implements RuleAlteredJobConfigurationPreparer {
    
    @Override
    public void extendJobConfiguration(final YamlMigrationJobConfiguration yamlJobConfig) {
        Map<String, List<DataNode>> actualDataNodes = getActualDataNodes(new YamlMigrationJobConfigurationSwapper().swapToObject(yamlJobConfig));
        yamlJobConfig.setJobShardingDataNodes(getJobShardingDataNodes(actualDataNodes));
        yamlJobConfig.setLogicTables(getLogicTables(actualDataNodes.keySet()));
        yamlJobConfig.setTablesFirstDataNodes(getTablesFirstDataNodes(actualDataNodes));
        yamlJobConfig.setSchemaTablesMap(getSchemaTablesMap(yamlJobConfig.getDatabaseName(), actualDataNodes.keySet()));
    }
    
    private static Map<String, List<DataNode>> getActualDataNodes(final MigrationJobConfiguration jobConfig) {
        PipelineDataSourceConfiguration sourceDataSourceConfig = PipelineDataSourceConfigurationFactory.newInstance(jobConfig.getSource().getType(), jobConfig.getSource().getParameter());
        ShardingSpherePipelineDataSourceConfiguration source = (ShardingSpherePipelineDataSourceConfiguration) sourceDataSourceConfig;
        ShardingRuleConfiguration sourceRuleConfig = ShardingRuleConfigurationConverter.findAndConvertShardingRuleConfiguration(source.getRootConfig().getRules());
        // TODO InstanceContext should not null
        ShardingRule shardingRule = new ShardingRule(sourceRuleConfig, source.getRootConfig().getDataSources().keySet(), null);
        Map<String, TableRule> tableRules = shardingRule.getTableRules();
        Map<String, List<DataNode>> result = new LinkedHashMap<>();
        Set<String> reShardNeededTables = new HashSet<>(jobConfig.getAlteredRuleYamlClassNameTablesMap().get(YamlShardingRuleConfiguration.class.getName()));
        for (Entry<String, TableRule> entry : tableRules.entrySet()) {
            if (reShardNeededTables.contains(entry.getKey())) {
                result.put(entry.getKey(), entry.getValue().getActualDataNodes());
            }
        }
        return result;
    }
    
    private List<String> getJobShardingDataNodes(final Map<String, List<DataNode>> actualDataNodes) {
        List<String> result = new LinkedList<>();
        Map<String, Map<String, List<DataNode>>> groupedDataSourceDataNodesMap = groupDataSourceDataNodesMapByDataSourceName(actualDataNodes);
        for (Map<String, List<DataNode>> each : groupedDataSourceDataNodesMap.values()) {
            List<JobDataNodeEntry> dataNodeEntries = new ArrayList<>(each.size());
            for (Entry<String, List<DataNode>> entry : each.entrySet()) {
                dataNodeEntries.add(new JobDataNodeEntry(entry.getKey(), entry.getValue()));
            }
            result.add(new JobDataNodeLine(dataNodeEntries).marshal());
        }
        return result;
    }
    
    private Map<String, Map<String, List<DataNode>>> groupDataSourceDataNodesMapByDataSourceName(final Map<String, List<DataNode>> actualDataNodes) {
        Map<String, Map<String, List<DataNode>>> result = new LinkedHashMap<>();
        for (Entry<String, List<DataNode>> entry : actualDataNodes.entrySet()) {
            for (DataNode each : entry.getValue()) {
                Map<String, List<DataNode>> groupedDataNodesMap = result.computeIfAbsent(each.getDataSourceName(), key -> new LinkedHashMap<>());
                groupedDataNodesMap.computeIfAbsent(entry.getKey(), key -> new LinkedList<>()).add(each);
            }
        }
        return result;
    }
    
    private static String getLogicTables(final Set<String> logicTables) {
        return String.join(",", logicTables);
    }
    
    private static String getTablesFirstDataNodes(final Map<String, List<DataNode>> actualDataNodes) {
        List<JobDataNodeEntry> dataNodeEntries = new ArrayList<>(actualDataNodes.size());
        for (Entry<String, List<DataNode>> entry : actualDataNodes.entrySet()) {
            dataNodeEntries.add(new JobDataNodeEntry(entry.getKey(), entry.getValue().subList(0, 1)));
        }
        return new JobDataNodeLine(dataNodeEntries).marshal();
    }
    
    private static Map<String, List<String>> getSchemaTablesMap(final String databaseName, final Set<String> logicTables) {
        // TODO get by search_path
        ShardingSphereDatabase database = PipelineContext.getContextManager().getMetaDataContexts().getMetaData().getDatabase(databaseName);
        Map<String, List<String>> result = new LinkedHashMap<>();
        database.getSchemas().forEach((schemaName, schema) -> {
            for (String each : schema.getAllTableNames()) {
                if (!logicTables.contains(each)) {
                    continue;
                }
                result.computeIfAbsent(schemaName, unused -> new LinkedList<>()).add(each);
            }
        });
        log.info("getSchemaTablesMap, result={}", result);
        return result;
    }
    
    @Override
    public TaskConfiguration createTaskConfiguration(final MigrationJobConfiguration jobConfig, final int jobShardingItem, final PipelineProcessConfiguration pipelineProcessConfig) {
        ShardingSpherePipelineDataSourceConfiguration sourceConfig = getSourceConfiguration(jobConfig);
        ShardingRuleConfiguration sourceRuleConfig = ShardingRuleConfigurationConverter.findAndConvertShardingRuleConfiguration(sourceConfig.getRootConfig().getRules());
        Map<String, DataSourceProperties> dataSourcePropsMap = new YamlDataSourceConfigurationSwapper().getDataSourcePropertiesMap(sourceConfig.getRootConfig());
        JobDataNodeLine dataNodeLine = JobDataNodeLine.unmarshal(jobConfig.getJobShardingDataNodes().get(jobShardingItem));
        String dataSourceName = dataNodeLine.getEntries().get(0).getDataNodes().get(0).getDataSourceName();
        Map<ActualTableName, LogicTableName> tableNameMap = new LinkedHashMap<>();
        for (JobDataNodeEntry each : dataNodeLine.getEntries()) {
            for (DataNode dataNode : each.getDataNodes()) {
                tableNameMap.put(new ActualTableName(dataNode.getTableName()), new LogicTableName(each.getLogicTableName()));
            }
        }
        TableNameSchemaNameMapping tableNameSchemaNameMapping = new TableNameSchemaNameMapping(TableNameSchemaNameMapping.convert(jobConfig.getSchemaTablesMap()));
        DumperConfiguration dumperConfig = createDumperConfiguration(jobConfig.getDatabaseName(), dataSourceName,
                dataSourcePropsMap.get(dataSourceName).getAllLocalProperties(), tableNameMap, tableNameSchemaNameMapping);
        Optional<ShardingRuleConfiguration> targetRuleConfig = getTargetRuleConfiguration(jobConfig);
        Set<LogicTableName> reShardNeededTables = jobConfig.splitLogicTableNames().stream().map(LogicTableName::new).collect(Collectors.toSet());
        Map<LogicTableName, Set<String>> shardingColumnsMap = getShardingColumnsMap(targetRuleConfig.orElse(sourceRuleConfig), reShardNeededTables);
        ImporterConfiguration importerConfig = createImporterConfiguration(jobConfig, pipelineProcessConfig, shardingColumnsMap, tableNameSchemaNameMapping);
        TaskConfiguration result = new TaskConfiguration(dumperConfig, importerConfig);
        log.info("createTaskConfiguration, dataSourceName={}, result={}", dataSourceName, result);
        return result;
    }
    
    private static ShardingSpherePipelineDataSourceConfiguration getSourceConfiguration(final MigrationJobConfiguration jobConfig) {
        return (ShardingSpherePipelineDataSourceConfiguration) PipelineDataSourceConfigurationFactory.newInstance(jobConfig.getSource().getType(), jobConfig.getSource().getParameter());
    }
    
    private static Optional<ShardingRuleConfiguration> getTargetRuleConfiguration(final MigrationJobConfiguration jobConfig) {
        PipelineDataSourceConfiguration targetDataSourceConfig = PipelineDataSourceConfigurationFactory.newInstance(jobConfig.getTarget().getType(), jobConfig.getTarget().getParameter());
        if (!(targetDataSourceConfig instanceof ShardingSpherePipelineDataSourceConfiguration)) {
            return Optional.empty();
        }
        ShardingSpherePipelineDataSourceConfiguration target = (ShardingSpherePipelineDataSourceConfiguration) targetDataSourceConfig;
        return Optional.of(ShardingRuleConfigurationConverter.findAndConvertShardingRuleConfiguration(target.getRootConfig().getRules()));
    }
    
    private static Map<LogicTableName, Set<String>> getShardingColumnsMap(final ShardingRuleConfiguration shardingRuleConfig, final Set<LogicTableName> reShardNeededTables) {
        Set<String> defaultDatabaseShardingColumns = extractShardingColumns(shardingRuleConfig.getDefaultDatabaseShardingStrategy());
        Set<String> defaultTableShardingColumns = extractShardingColumns(shardingRuleConfig.getDefaultTableShardingStrategy());
        Map<LogicTableName, Set<String>> result = new ConcurrentHashMap<>();
        for (ShardingTableRuleConfiguration each : shardingRuleConfig.getTables()) {
            LogicTableName logicTableName = new LogicTableName(each.getLogicTable());
            if (!reShardNeededTables.contains(logicTableName)) {
                continue;
            }
            Set<String> shardingColumns = new HashSet<>();
            shardingColumns.addAll(null == each.getDatabaseShardingStrategy() ? defaultDatabaseShardingColumns : extractShardingColumns(each.getDatabaseShardingStrategy()));
            shardingColumns.addAll(null == each.getTableShardingStrategy() ? defaultTableShardingColumns : extractShardingColumns(each.getTableShardingStrategy()));
            result.put(logicTableName, shardingColumns);
        }
        for (ShardingAutoTableRuleConfiguration each : shardingRuleConfig.getAutoTables()) {
            LogicTableName logicTableName = new LogicTableName(each.getLogicTable());
            if (!reShardNeededTables.contains(logicTableName)) {
                continue;
            }
            ShardingStrategyConfiguration shardingStrategy = each.getShardingStrategy();
            Set<String> shardingColumns = new HashSet<>(extractShardingColumns(shardingStrategy));
            result.put(logicTableName, shardingColumns);
        }
        return result;
    }
    
    private static Set<String> extractShardingColumns(final ShardingStrategyConfiguration shardingStrategy) {
        if (shardingStrategy instanceof StandardShardingStrategyConfiguration) {
            return new HashSet<>(Collections.singleton(((StandardShardingStrategyConfiguration) shardingStrategy).getShardingColumn()));
        }
        if (shardingStrategy instanceof ComplexShardingStrategyConfiguration) {
            return new HashSet<>(Arrays.asList(((ComplexShardingStrategyConfiguration) shardingStrategy).getShardingColumns().split(",")));
        }
        return Collections.emptySet();
    }
    
    private static DumperConfiguration createDumperConfiguration(final String databaseName, final String dataSourceName, final Map<String, Object> props,
                                                                 final Map<ActualTableName, LogicTableName> tableNameMap, final TableNameSchemaNameMapping tableNameSchemaNameMapping) {
        DumperConfiguration result = new DumperConfiguration();
        result.setDatabaseName(databaseName);
        result.setDataSourceName(dataSourceName);
        result.setDataSourceConfig(new StandardPipelineDataSourceConfiguration(YamlEngine.marshal(props)));
        result.setTableNameMap(tableNameMap);
        result.setTableNameSchemaNameMapping(tableNameSchemaNameMapping);
        return result;
    }
    
    private static ImporterConfiguration createImporterConfiguration(final MigrationJobConfiguration jobConfig, final PipelineProcessConfiguration pipelineProcessConfig,
                                                                     final Map<LogicTableName, Set<String>> shardingColumnsMap, final TableNameSchemaNameMapping tableNameSchemaNameMapping) {
        PipelineDataSourceConfiguration dataSourceConfig = PipelineDataSourceConfigurationFactory.newInstance(jobConfig.getTarget().getType(), jobConfig.getTarget().getParameter());
        int batchSize = pipelineProcessConfig.getWrite().getBatchSize();
        int retryTimes = jobConfig.getRetryTimes();
        int concurrency = jobConfig.getConcurrency();
        return new ImporterConfiguration(dataSourceConfig, unmodifiable(shardingColumnsMap), tableNameSchemaNameMapping, batchSize, retryTimes, concurrency);
    }
    
    private static Map<LogicTableName, Set<String>> unmodifiable(final Map<LogicTableName, Set<String>> shardingColumnsMap) {
        Map<LogicTableName, Set<String>> result = new HashMap<>(shardingColumnsMap.size());
        for (Entry<LogicTableName, Set<String>> entry : shardingColumnsMap.entrySet()) {
            result.put(entry.getKey(), Collections.unmodifiableSet(entry.getValue()));
        }
        return Collections.unmodifiableMap(result);
    }
}
