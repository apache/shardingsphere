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

package org.apache.shardingsphere.data.pipeline.api.config.rulealtered;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.config.job.PipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfigurationFactory;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.yaml.YamlPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.job.JobSubType;
import org.apache.shardingsphere.data.pipeline.api.job.JobType;
import org.apache.shardingsphere.data.pipeline.api.job.RuleAlteredJobId;
import org.apache.shardingsphere.data.pipeline.spi.rulealtered.RuleAlteredJobConfigurationPreparerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Scaling job configuration.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Slf4j
// TODO share for totally new scenario
// TODO rename to Yaml, add config class
public final class RuleAlteredJobConfiguration implements PipelineJobConfiguration {
    
    private String jobId;
    
    private String databaseName;
    
    // TODO it should not put in jobConfig since it's mutable
    private Integer jobShardingItem;
    
    /**
     * Map{altered rule yaml class name, re-shard needed table names}.
     */
    private Map<String, List<String>> alteredRuleYamlClassNameTablesMap;
    
    private Integer activeVersion;
    
    private Integer newVersion;
    
    private YamlPipelineDataSourceConfiguration source;
    
    private YamlPipelineDataSourceConfiguration target;
    
    private int concurrency = 3;
    
    private int retryTimes = 3;
    
    /**
     * Collection of each logic table's first data node.
     * <p>
     * If <pre>actualDataNodes: ds_${0..1}.t_order_${0..1}</pre> and <pre>actualDataNodes: ds_${0..1}.t_order_item_${0..1}</pre>,
     * then value may be: {@code t_order:ds_0.t_order_0|t_order_item:ds_0.t_order_item_0}.
     * </p>
     */
    private String tablesFirstDataNodes;
    
    private List<String> jobShardingDataNodes;
    
    private String logicTables;
    
    // TODO shardingSize should be configurable
    private int shardingSize = 1000 * 10000;
    
    private String sourceDatabaseType;
    
    private String targetDatabaseType;
    
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
     * Get job sharding count.
     *
     * @return job sharding count
     */
    public int getJobShardingCount() {
        return null == jobShardingDataNodes ? 0 : jobShardingDataNodes.size();
    }
    
    /**
     * Split {@linkplain #logicTables} to logic table names.
     *
     * @return logic table names
     */
    public List<String> splitLogicTableNames() {
        return Splitter.on(',').splitToList(logicTables);
    }
    
    /**
     * Build handle configuration.
     */
    public void buildHandleConfig() {
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
        if (null == jobShardingItem) {
            jobShardingItem = 0;
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
        return "RuleAlteredJobConfiguration{"
                + "jobId='" + jobId + '\'' + ", databaseName='" + databaseName + '\''
                + ", activeVersion=" + activeVersion + ", newVersion=" + newVersion + ", shardingSize=" + shardingSize
                + ", sourceDatabaseType='" + sourceDatabaseType + '\'' + ", targetDatabaseType='" + targetDatabaseType + '\''
                + '}';
    }
}
