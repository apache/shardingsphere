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

package org.apache.shardingsphere.data.pipeline.api.config.job.yaml;

import org.apache.shardingsphere.data.pipeline.api.config.job.MigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.yaml.YamlPipelineDataSourceConfigurationSwapper;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.util.yaml.swapper.YamlConfigurationSwapper;

/**
 * YAML migration job configuration swapper.
 */
// TODO add test
public final class YamlMigrationJobConfigurationSwapper implements YamlConfigurationSwapper<YamlMigrationJobConfiguration, MigrationJobConfiguration> {
    
    private static final YamlMigrationJobConfigurationSwapper JOB_CONFIG_SWAPPER = new YamlMigrationJobConfigurationSwapper();
    
    private final YamlPipelineDataSourceConfigurationSwapper dataSourceConfigSwapper = new YamlPipelineDataSourceConfigurationSwapper();
    
    @Override
    public YamlMigrationJobConfiguration swapToYamlConfiguration(final MigrationJobConfiguration data) {
        YamlMigrationJobConfiguration result = new YamlMigrationJobConfiguration();
        result.setJobId(data.getJobId());
        result.setSourceResourceName(data.getSourceResourceName());
        result.setTargetDatabaseName(data.getTargetDatabaseName());
        result.setSourceDatabaseType(data.getSourceDatabaseType());
        result.setTargetDatabaseType(data.getTargetDatabaseType());
        result.setSourceSchemaName(data.getSourceSchemaName());
        result.setSourceTableName(data.getSourceTableName());
        result.setTargetTableName(data.getTargetTableName());
        result.setSource(dataSourceConfigSwapper.swapToYamlConfiguration(data.getSource()));
        result.setTarget(dataSourceConfigSwapper.swapToYamlConfiguration(data.getTarget()));
        result.setTablesFirstDataNodes(data.getTablesFirstDataNodes());
        result.setJobShardingDataNodes(data.getJobShardingDataNodes());
        result.setConcurrency(data.getConcurrency());
        result.setRetryTimes(data.getRetryTimes());
        return result;
    }
    
    @Override
    public MigrationJobConfiguration swapToObject(final YamlMigrationJobConfiguration yamlConfig) {
        return new MigrationJobConfiguration(yamlConfig.getJobId(), yamlConfig.getSourceResourceName(), yamlConfig.getTargetDatabaseName(),
                yamlConfig.getSourceSchemaName(),
                yamlConfig.getSourceDatabaseType(), yamlConfig.getTargetDatabaseType(),
                yamlConfig.getSourceTableName(), yamlConfig.getTargetTableName(),
                dataSourceConfigSwapper.swapToObject(yamlConfig.getSource()), dataSourceConfigSwapper.swapToObject(yamlConfig.getTarget()),
                yamlConfig.getTablesFirstDataNodes(), yamlConfig.getJobShardingDataNodes(),
                yamlConfig.getConcurrency(), yamlConfig.getRetryTimes());
    }
    
    /**
     * Swap to job configuration from text.
     *
     * @param jobParameter job parameter
     * @return job configuration
     */
    public static MigrationJobConfiguration swapToObject(final String jobParameter) {
        YamlMigrationJobConfiguration yamlJobConfig = YamlEngine.unmarshal(jobParameter, YamlMigrationJobConfiguration.class, true);
        return JOB_CONFIG_SWAPPER.swapToObject(yamlJobConfig);
    }
}
