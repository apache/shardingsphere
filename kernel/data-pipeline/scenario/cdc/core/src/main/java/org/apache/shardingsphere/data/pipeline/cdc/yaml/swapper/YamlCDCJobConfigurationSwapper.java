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

package org.apache.shardingsphere.data.pipeline.cdc.yaml.swapper;

import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.ShardingSpherePipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.yaml.YamlPipelineDataSourceConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.cdc.config.job.CDCJobConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.config.job.CDCJobConfiguration.SinkConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.constant.CDCSinkType;
import org.apache.shardingsphere.data.pipeline.cdc.yaml.config.YamlCDCJobConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.yaml.config.YamlCDCJobConfiguration.YamlSinkConfiguration;
import org.apache.shardingsphere.data.pipeline.common.datanode.JobDataNodeLine;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.util.yaml.swapper.YamlConfigurationSwapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * YAML CDC job configuration swapper.
 */
public final class YamlCDCJobConfigurationSwapper implements YamlConfigurationSwapper<YamlCDCJobConfiguration, CDCJobConfiguration> {
    
    private final YamlPipelineDataSourceConfigurationSwapper dataSourceConfigSwapper = new YamlPipelineDataSourceConfigurationSwapper();
    
    @Override
    public YamlCDCJobConfiguration swapToYamlConfiguration(final CDCJobConfiguration data) {
        YamlCDCJobConfiguration result = new YamlCDCJobConfiguration();
        result.setJobId(data.getJobId());
        result.setDatabaseName(data.getDatabaseName());
        result.setSchemaTableNames(data.getSchemaTableNames());
        result.setFull(data.isFull());
        result.setSourceDatabaseType(data.getSourceDatabaseType().getType());
        result.setDataSourceConfiguration(dataSourceConfigSwapper.swapToYamlConfiguration(data.getDataSourceConfig()));
        result.setTablesFirstDataNodes(null == data.getTablesFirstDataNodes() ? null : data.getTablesFirstDataNodes().marshal());
        List<String> jobShardingDataNodes = null == data.getJobShardingDataNodes() ? null : data.getJobShardingDataNodes().stream().map(JobDataNodeLine::marshal).collect(Collectors.toList());
        result.setJobShardingDataNodes(jobShardingDataNodes);
        result.setDecodeWithTX(data.isDecodeWithTX());
        result.setSinkConfig(swapToYamlSinkConfiguration(data.getSinkConfig()));
        result.setConcurrency(data.getConcurrency());
        result.setRetryTimes(data.getRetryTimes());
        return result;
    }
    
    private YamlSinkConfiguration swapToYamlSinkConfiguration(final SinkConfiguration sinkConfig) {
        YamlSinkConfiguration result = new YamlSinkConfiguration();
        result.setSinkType(sinkConfig.getSinkType().name());
        result.setProps(sinkConfig.getProps());
        return result;
    }
    
    @Override
    public CDCJobConfiguration swapToObject(final YamlCDCJobConfiguration yamlConfig) {
        List<JobDataNodeLine> jobShardingDataNodes = null == yamlConfig.getJobShardingDataNodes()
                ? Collections.emptyList()
                : yamlConfig.getJobShardingDataNodes().stream().map(JobDataNodeLine::unmarshal).collect(Collectors.toList());
        YamlSinkConfiguration yamlSinkConfig = yamlConfig.getSinkConfig();
        SinkConfiguration sinkConfig = new SinkConfiguration(CDCSinkType.valueOf(yamlSinkConfig.getSinkType()), yamlSinkConfig.getProps());
        JobDataNodeLine tablesFirstDataNodes = null == yamlConfig.getTablesFirstDataNodes() ? null : JobDataNodeLine.unmarshal(yamlConfig.getTablesFirstDataNodes());
        return new CDCJobConfiguration(yamlConfig.getJobId(), yamlConfig.getDatabaseName(), yamlConfig.getSchemaTableNames(), yamlConfig.isFull(),
                TypedSPILoader.getService(DatabaseType.class, yamlConfig.getSourceDatabaseType()),
                (ShardingSpherePipelineDataSourceConfiguration) dataSourceConfigSwapper.swapToObject(yamlConfig.getDataSourceConfiguration()), tablesFirstDataNodes,
                jobShardingDataNodes, yamlConfig.isDecodeWithTX(), sinkConfig, yamlConfig.getConcurrency(), yamlConfig.getRetryTimes());
    }
    
    /**
     * Swap to job configuration from text.
     *
     * @param jobParam job parameter
     * @return job configuration
     */
    public CDCJobConfiguration swapToObject(final String jobParam) {
        return null == jobParam ? null : swapToObject(YamlEngine.unmarshal(jobParam, YamlCDCJobConfiguration.class, true));
    }
}
