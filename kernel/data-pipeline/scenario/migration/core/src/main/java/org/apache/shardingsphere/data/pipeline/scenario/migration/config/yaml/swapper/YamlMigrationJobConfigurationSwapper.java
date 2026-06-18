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

package org.apache.shardingsphere.data.pipeline.scenario.migration.config.yaml.swapper;

import org.apache.shardingsphere.data.pipeline.core.datanode.JobDataNodeLine;
import org.apache.shardingsphere.data.pipeline.core.datasource.yaml.swapper.YamlPipelineDataSourceConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.core.job.config.yaml.swapper.YamlPipelineJobConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.MigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.yaml.config.YamlMigrationJobConfiguration;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;

import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * YAML migration job configuration swapper.
 */
public final class YamlMigrationJobConfigurationSwapper implements YamlPipelineJobConfigurationSwapper<YamlMigrationJobConfiguration, MigrationJobConfiguration> {
    
    private final YamlPipelineDataSourceConfigurationSwapper dataSourceConfigSwapper = new YamlPipelineDataSourceConfigurationSwapper();
    
    @Override
    public YamlMigrationJobConfiguration swapToYamlConfiguration(final MigrationJobConfiguration data) {
        YamlMigrationJobConfiguration result = new YamlMigrationJobConfiguration();
        result.setJobId(data.getJobId());
        result.setTargetDatabaseName(data.getTargetDatabaseName());
        result.setSourceDatabaseType(data.getSourceDatabaseType().getType());
        result.setTargetDatabaseType(data.getTargetDatabaseType().getType());
        result.setSources(data.getSources().entrySet().stream().collect(Collectors.toMap(Entry::getKey,
                entry -> dataSourceConfigSwapper.swapToYamlConfiguration(entry.getValue()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new)));
        result.setTarget(dataSourceConfigSwapper.swapToYamlConfiguration(data.getTarget()));
        result.setTargetTableNames(data.getTargetTableNames());
        result.setTargetTableSchemaMap(data.getTargetTableSchemaMap());
        result.setTablesFirstDataNodes(data.getTablesFirstDataNodes().marshal());
        result.setJobShardingDataNodes(data.getJobShardingDataNodes().stream().map(JobDataNodeLine::marshal).collect(Collectors.toList()));
        result.setConcurrency(data.getConcurrency());
        result.setRetryTimes(data.getRetryTimes());
        return result;
    }
    
    @Override
    public MigrationJobConfiguration swapToObject(final YamlMigrationJobConfiguration yamlConfig) {
        return new MigrationJobConfiguration(yamlConfig.getJobId(), yamlConfig.getDatabaseName(),
                TypedSPILoader.getService(DatabaseType.class, yamlConfig.getSourceDatabaseType()), TypedSPILoader.getService(DatabaseType.class, yamlConfig.getTargetDatabaseType()),
                yamlConfig.getSources().entrySet().stream().collect(Collectors.toMap(Entry::getKey,
                        entry -> dataSourceConfigSwapper.swapToObject(entry.getValue()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new)),
                dataSourceConfigSwapper.swapToObject(yamlConfig.getTarget()),
                yamlConfig.getTargetTableNames(), yamlConfig.getTargetTableSchemaMap(),
                JobDataNodeLine.unmarshal(yamlConfig.getTablesFirstDataNodes()), yamlConfig.getJobShardingDataNodes().stream().map(JobDataNodeLine::unmarshal).collect(Collectors.toList()),
                yamlConfig.getConcurrency(), yamlConfig.getRetryTimes());
    }
    
    @Override
    public MigrationJobConfiguration swapToObject(final String jobParam) {
        return swapToObject(YamlEngine.unmarshal(jobParam, YamlMigrationJobConfiguration.class, true));
    }
}
