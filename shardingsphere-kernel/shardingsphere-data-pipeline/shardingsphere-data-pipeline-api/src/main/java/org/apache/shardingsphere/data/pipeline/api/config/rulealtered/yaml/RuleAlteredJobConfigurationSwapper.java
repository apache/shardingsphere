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

package org.apache.shardingsphere.data.pipeline.api.config.rulealtered.yaml;

import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.RuleAlteredJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.yaml.PipelineDataSourceConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.YamlConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;

/**
 * Rule altered job configuration swapper.
 */
// TODO add RuleAlteredJobConfigurationSwapper test
public final class RuleAlteredJobConfigurationSwapper implements YamlConfigurationSwapper<YamlRuleAlteredJobConfiguration, RuleAlteredJobConfiguration> {
    
    private static final RuleAlteredJobConfigurationSwapper JOB_CONFIG_SWAPPER = new RuleAlteredJobConfigurationSwapper();
    
    private final PipelineDataSourceConfigurationSwapper dataSourceConfigSwapper = new PipelineDataSourceConfigurationSwapper();
    
    @Override
    public YamlRuleAlteredJobConfiguration swapToYamlConfiguration(final RuleAlteredJobConfiguration data) {
        YamlRuleAlteredJobConfiguration result = new YamlRuleAlteredJobConfiguration();
        result.setJobId(data.getJobId());
        result.setDatabaseName(data.getDatabaseName());
        result.setActiveVersion(data.getActiveVersion());
        result.setNewVersion(data.getNewVersion());
        result.setSourceDatabaseType(data.getSourceDatabaseType());
        result.setTargetDatabaseType(data.getTargetDatabaseType());
        result.setSource(dataSourceConfigSwapper.swapToYamlConfiguration(data.getSource()));
        result.setTarget(dataSourceConfigSwapper.swapToYamlConfiguration(data.getTarget()));
        result.setAlteredRuleYamlClassNameTablesMap(data.getAlteredRuleYamlClassNameTablesMap());
        result.setSchemaTablesMap(data.getSchemaTablesMap());
        result.setLogicTables(data.getLogicTables());
        result.setTablesFirstDataNodes(data.getTablesFirstDataNodes());
        result.setJobShardingDataNodes(data.getJobShardingDataNodes());
        result.setConcurrency(data.getConcurrency());
        result.setRetryTimes(data.getRetryTimes());
        return result;
    }
    
    @Override
    public RuleAlteredJobConfiguration swapToObject(final YamlRuleAlteredJobConfiguration yamlConfig) {
        return new RuleAlteredJobConfiguration(yamlConfig.getJobId(), yamlConfig.getDatabaseName(),
                yamlConfig.getActiveVersion(), yamlConfig.getNewVersion(),
                yamlConfig.getSourceDatabaseType(), yamlConfig.getTargetDatabaseType(),
                dataSourceConfigSwapper.swapToObject(yamlConfig.getSource()), dataSourceConfigSwapper.swapToObject(yamlConfig.getTarget()),
                yamlConfig.getAlteredRuleYamlClassNameTablesMap(), yamlConfig.getSchemaTablesMap(), yamlConfig.getLogicTables(),
                yamlConfig.getTablesFirstDataNodes(), yamlConfig.getJobShardingDataNodes(),
                yamlConfig.getConcurrency(), yamlConfig.getRetryTimes());
    }
    
    /**
     * Swap to job configuration from text.
     *
     * @param jobParameter job parameter
     * @return job configuration
     */
    public static RuleAlteredJobConfiguration swapToObject(final String jobParameter) {
        YamlRuleAlteredJobConfiguration yamlJobConfig = YamlEngine.unmarshal(jobParameter, YamlRuleAlteredJobConfiguration.class, true);
        return JOB_CONFIG_SWAPPER.swapToObject(yamlJobConfig);
    }
}
