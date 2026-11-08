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

package org.apache.shardingsphere.data.pipeline.scenario.migration.config.yaml.config;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.data.pipeline.core.datasource.yaml.config.YamlPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.config.yaml.config.YamlPipelineJobConfiguration;

import java.util.List;
import java.util.Map;

/**
 * Migration job configuration for YAML.
 */
@Getter
@Setter
public final class YamlMigrationJobConfiguration implements YamlPipelineJobConfiguration {
    
    private String jobId;
    
    private String targetDatabaseName;
    
    private String sourceDatabaseType;
    
    private String targetDatabaseType;
    
    private Map<String, YamlPipelineDataSourceConfiguration> sources;
    
    private YamlPipelineDataSourceConfiguration target;
    
    private List<String> targetTableNames;
    
    /**
     * Map{logic table names, schema name}.
     */
    private Map<String, String> targetTableSchemaMap;
    
    private String tablesFirstDataNodes;
    
    private List<String> jobShardingDataNodes;
    
    private int concurrency = 3;
    
    private int retryTimes = 3;
    
    @Override
    public String getDatabaseName() {
        return targetDatabaseName;
    }
    
    /**
     * Set sources.
     *
     * @param sources source configurations
     */
    public void setSources(final Map<String, YamlPipelineDataSourceConfiguration> sources) {
        sources.values().forEach(this::checkParameters);
        this.sources = sources;
    }
    
    /**
     * Set target.
     *
     * @param target target configuration
     */
    public void setTarget(final YamlPipelineDataSourceConfiguration target) {
        checkParameters(target);
        this.target = target;
    }
    
    private void checkParameters(final YamlPipelineDataSourceConfiguration yamlConfig) {
        Preconditions.checkNotNull(yamlConfig);
        Preconditions.checkNotNull(yamlConfig.getType());
        Preconditions.checkNotNull(yamlConfig.getParameter());
    }
}
