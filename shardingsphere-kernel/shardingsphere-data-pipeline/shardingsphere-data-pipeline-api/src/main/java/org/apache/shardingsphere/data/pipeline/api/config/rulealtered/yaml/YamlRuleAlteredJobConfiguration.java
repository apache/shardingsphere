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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfigurationFactory;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.yaml.YamlPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.job.JobSubType;
import org.apache.shardingsphere.data.pipeline.api.job.JobType;
import org.apache.shardingsphere.data.pipeline.api.job.RuleAlteredJobId;
import org.apache.shardingsphere.data.pipeline.spi.rulealtered.RuleAlteredJobConfigurationPreparerFactory;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlConfiguration;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Rule altered job configuration for YAML.
 */
@Getter
@Setter
@Slf4j
public final class YamlRuleAlteredJobConfiguration implements YamlConfiguration {
    
    private String jobId;
    
    private String databaseName;
    
    private Integer activeVersion;
    
    private Integer newVersion;
    
    private String sourceDatabaseType;
    
    private String targetDatabaseType;
    
    private YamlPipelineDataSourceConfiguration source;
    
    private YamlPipelineDataSourceConfiguration target;
    
    /**
     * Map{altered rule yaml class name, re-shard needed table names}.
     */
    private Map<String, List<String>> alteredRuleYamlClassNameTablesMap;
    
    /**
     * Map{schema name, logic table names}.
     */
    private Map<String, List<String>> schemaTablesMap;
    
    private String logicTables;
    
    /**
     * Collection of each logic table's first data node.
     * <p>
     * If <pre>actualDataNodes: ds_${0..1}.t_order_${0..1}</pre> and <pre>actualDataNodes: ds_${0..1}.t_order_item_${0..1}</pre>,
     * then value may be: {@code t_order:ds_0.t_order_0|t_order_item:ds_0.t_order_item_0}.
     * </p>
     */
    private String tablesFirstDataNodes;
    
    private List<String> jobShardingDataNodes;
    
    private int concurrency = 3;
    
    private int retryTimes = 3;
    
    /**
     * Set source.
     *
     * @param source source configuration
     */
    public void setSource(final YamlPipelineDataSourceConfiguration source) {
        checkParameters(source);
        this.source = source;
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
    
    /**
     * Extend configuration.
     */
    public void extendConfiguration() {
        if (null == getJobShardingDataNodes()) {
            RuleAlteredJobConfigurationPreparerFactory.getInstance().extendJobConfiguration(this);
        }
        if (null == jobId) {
            jobId = generateJobId();
        }
        if (Strings.isNullOrEmpty(getSourceDatabaseType())) {
            PipelineDataSourceConfiguration sourceDataSourceConfig = PipelineDataSourceConfigurationFactory.newInstance(source.getType(), source.getParameter());
            setSourceDatabaseType(sourceDataSourceConfig.getDatabaseType().getType());
        }
        if (Strings.isNullOrEmpty(getTargetDatabaseType())) {
            PipelineDataSourceConfiguration targetDataSourceConfig = PipelineDataSourceConfigurationFactory.newInstance(target.getType(), target.getParameter());
            setTargetDatabaseType(targetDataSourceConfig.getDatabaseType().getType());
        }
    }
    
    private String generateJobId() {
        RuleAlteredJobId jobId = new RuleAlteredJobId();
        // TODO type, subTypes
        jobId.setType(JobType.RULE_ALTERED.getValue());
        jobId.setFormatVersion(RuleAlteredJobId.CURRENT_VERSION);
        jobId.setSubTypes(Collections.singletonList(JobSubType.SCALING.getValue()));
        jobId.setCurrentMetadataVersion(activeVersion);
        jobId.setNewMetadataVersion(newVersion);
        jobId.setDatabaseName(databaseName);
        return jobId.marshal();
    }
    
    @Override
    public String toString() {
        return "YamlRuleAlteredJobConfiguration{"
                + "jobId='" + jobId + '\'' + ", databaseName='" + databaseName + '\''
                + ", activeVersion=" + activeVersion + ", newVersion=" + newVersion
                + ", sourceDatabaseType='" + sourceDatabaseType + '\'' + ", targetDatabaseType='" + targetDatabaseType + '\''
                + '}';
    }
}
