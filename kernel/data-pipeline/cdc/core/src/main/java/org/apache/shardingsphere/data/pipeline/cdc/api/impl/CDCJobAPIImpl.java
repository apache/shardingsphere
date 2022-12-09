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

package org.apache.shardingsphere.data.pipeline.cdc.api.impl;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.shardingsphere.data.pipeline.api.config.PipelineTaskConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.job.PipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.job.yaml.YamlPipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.process.PipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.api.context.PipelineJobItemContext;
import org.apache.shardingsphere.data.pipeline.api.context.PipelineProcessContext;
import org.apache.shardingsphere.data.pipeline.api.datanode.JobDataNodeEntry;
import org.apache.shardingsphere.data.pipeline.api.datanode.JobDataNodeLine;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfigurationFactory;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.ShardingSpherePipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.yaml.YamlPipelineDataSourceConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.api.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.api.job.PipelineJobId;
import org.apache.shardingsphere.data.pipeline.api.job.progress.PipelineJobItemProgress;
import org.apache.shardingsphere.data.pipeline.api.pojo.PipelineJobInfo;
import org.apache.shardingsphere.data.pipeline.cdc.api.CDCJobAPI;
import org.apache.shardingsphere.data.pipeline.cdc.api.job.type.CDCJobType;
import org.apache.shardingsphere.data.pipeline.cdc.api.pojo.CreateSubscriptionJobParameter;
import org.apache.shardingsphere.data.pipeline.cdc.config.job.CDCJobConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.core.job.CDCJob;
import org.apache.shardingsphere.data.pipeline.cdc.core.job.CDCJobId;
import org.apache.shardingsphere.data.pipeline.cdc.yaml.job.YamlCDCJobConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.yaml.job.YamlCDCJobConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.core.api.impl.AbstractPipelineJobAPIImpl;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContext;
import org.apache.shardingsphere.data.pipeline.spi.job.JobType;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.datasource.props.DataSourcePropertiesCreator;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.resource.YamlDataSourceConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapperEngine;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * CDC job API impl.
 */
public final class CDCJobAPIImpl extends AbstractPipelineJobAPIImpl implements CDCJobAPI {
    
    private final YamlDataSourceConfigurationSwapper dataSourceConfigSwapper = new YamlDataSourceConfigurationSwapper();
    
    private final YamlRuleConfigurationSwapperEngine ruleConfigSwapperEngine = new YamlRuleConfigurationSwapperEngine();
    
    private final YamlPipelineDataSourceConfigurationSwapper pipelineDataSourceConfigSwapper = new YamlPipelineDataSourceConfigurationSwapper();
    
    @Override
    public String createJobAndStart(final CreateSubscriptionJobParameter event) {
        YamlCDCJobConfiguration yamlJobConfig = new YamlCDCJobConfiguration();
        Map<String, String> tablesFirstDataNodesMap = new HashMap<>(event.getDataNodesMap().size(), 1);
        for (Entry<String, List<DataNode>> entry : event.getDataNodesMap().entrySet()) {
            tablesFirstDataNodesMap.put(entry.getKey(), new JobDataNodeLine(Collections.singleton(new JobDataNodeEntry(entry.getKey(), entry.getValue().subList(0, 1)))).marshal());
        }
        yamlJobConfig.setDatabase(event.getDatabase());
        yamlJobConfig.setTableNames(event.getSubscribeTableNames());
        yamlJobConfig.setSubscriptionName(event.getSubscriptionName());
        yamlJobConfig.setSubscriptionMode(event.getSubscriptionMode());
        ShardingSphereDatabase database = PipelineContext.getContextManager().getMetaDataContexts().getMetaData().getDatabase(event.getDatabase());
        yamlJobConfig.setDataSourceConfiguration(pipelineDataSourceConfigSwapper.swapToYamlConfiguration(getDataSourceConfiguration(database)));
        yamlJobConfig.setTablesFirstDataNodesMap(tablesFirstDataNodesMap);
        extendYamlJobConfiguration(yamlJobConfig);
        CDCJobConfiguration jobConfig = new YamlCDCJobConfigurationSwapper().swapToObject(yamlJobConfig);
        start(jobConfig);
        return jobConfig.getJobId();
    }
    
    private ShardingSpherePipelineDataSourceConfiguration getDataSourceConfiguration(final ShardingSphereDatabase database) {
        Map<String, Map<String, Object>> dataSourceProps = new HashMap<>();
        for (Entry<String, DataSource> entry : database.getResourceMetaData().getDataSources().entrySet()) {
            dataSourceProps.put(entry.getKey(), dataSourceConfigSwapper.swapToMap(DataSourcePropertiesCreator.create(entry.getValue())));
        }
        YamlRootConfiguration targetRootConfig = new YamlRootConfiguration();
        targetRootConfig.setDatabaseName(database.getName());
        targetRootConfig.setDataSources(dataSourceProps);
        Collection<YamlRuleConfiguration> yamlRuleConfigurations = ruleConfigSwapperEngine.swapToYamlRuleConfigurations(database.getRuleMetaData().getConfigurations());
        targetRootConfig.setRules(yamlRuleConfigurations);
        return new ShardingSpherePipelineDataSourceConfiguration(targetRootConfig);
    }
    
    @Override
    public void extendYamlJobConfiguration(final YamlPipelineJobConfiguration yamlJobConfig) {
        YamlCDCJobConfiguration config = (YamlCDCJobConfiguration) yamlJobConfig;
        if (null == yamlJobConfig.getJobId()) {
            config.setJobId(generateJobId(config));
        }
        if (Strings.isNullOrEmpty(config.getSourceDatabaseType())) {
            PipelineDataSourceConfiguration sourceDataSourceConfig = PipelineDataSourceConfigurationFactory.newInstance(config.getDataSourceConfiguration().getType(),
                    config.getDataSourceConfiguration().getParameter());
            config.setSourceDatabaseType(sourceDataSourceConfig.getDatabaseType().getType());
        }
    }
    
    private String generateJobId(final YamlCDCJobConfiguration config) {
        CDCJobId jobId = new CDCJobId(config.getDatabase(), config.getSubscriptionName());
        return marshalJobId(jobId);
    }
    
    @Override
    protected String marshalJobIdLeftPart(final PipelineJobId pipelineJobId) {
        CDCJobId jobId = (CDCJobId) pipelineJobId;
        String text = Joiner.on('|').join(jobId.getDatabaseName(), jobId.getSubscriptionName());
        return DigestUtils.md5Hex(text.getBytes(StandardCharsets.UTF_8));
    }
    
    @Override
    public PipelineTaskConfiguration buildTaskConfiguration(final PipelineJobConfiguration pipelineJobConfig, final int jobShardingItem, final PipelineProcessConfiguration pipelineProcessConfig) {
        // TODO to be implement
        return null;
    }
    
    @Override
    public PipelineProcessContext buildPipelineProcessContext(final PipelineJobConfiguration pipelineJobConfig) {
        // TODO to be implement
        return null;
    }
    
    @Override
    public PipelineJobConfiguration getJobConfiguration(final String jobId) {
        return getJobConfiguration(getElasticJobConfigPOJO(jobId));
    }
    
    @Override
    protected PipelineJobConfiguration getJobConfiguration(final JobConfigurationPOJO jobConfigPOJO) {
        return new YamlCDCJobConfigurationSwapper().swapToObject(jobConfigPOJO.getJobParameter());
    }
    
    @Override
    protected YamlPipelineJobConfiguration swapToYamlJobConfiguration(final PipelineJobConfiguration jobConfig) {
        return new YamlCDCJobConfigurationSwapper().swapToYamlConfiguration((CDCJobConfiguration) jobConfig);
    }
    
    @Override
    public void persistJobItemProgress(final PipelineJobItemContext jobItemContext) {
        // TODO to be implement
    }
    
    @Override
    public PipelineJobItemProgress getJobItemProgress(final String jobId, final int shardingItem) {
        // TODO to be implement
        return null;
    }
    
    @Override
    public void updateJobItemStatus(final String jobId, final int shardingItem, final JobStatus status) {
        // TODO to be implement
    }
    
    @Override
    protected PipelineJobInfo getJobInfo(final String jobId) {
        // TODO to be implement
        return null;
    }
    
    @Override
    protected String getJobClassName() {
        return CDCJob.class.getName();
    }
    
    @Override
    public JobType getJobType() {
        return new CDCJobType();
    }
}
