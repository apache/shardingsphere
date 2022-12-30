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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.PipelineDataConsistencyChecker;
import org.apache.shardingsphere.data.pipeline.api.config.ImporterConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.TableNameSchemaNameMapping;
import org.apache.shardingsphere.data.pipeline.api.config.ingest.DumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.job.PipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.job.yaml.YamlPipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.process.PipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.api.context.PipelineJobItemContext;
import org.apache.shardingsphere.data.pipeline.api.datanode.JobDataNodeEntry;
import org.apache.shardingsphere.data.pipeline.api.datanode.JobDataNodeLine;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfigurationFactory;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.ShardingSpherePipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.yaml.YamlPipelineDataSourceConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.api.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.api.job.PipelineJobId;
import org.apache.shardingsphere.data.pipeline.api.job.progress.InventoryIncrementalJobItemProgress;
import org.apache.shardingsphere.data.pipeline.api.metadata.ActualTableName;
import org.apache.shardingsphere.data.pipeline.api.metadata.LogicTableName;
import org.apache.shardingsphere.data.pipeline.api.pojo.PipelineJobInfo;
import org.apache.shardingsphere.data.pipeline.cdc.api.job.type.CDCJobType;
import org.apache.shardingsphere.data.pipeline.cdc.api.pojo.CreateSubscriptionJobParameter;
import org.apache.shardingsphere.data.pipeline.cdc.config.job.CDCJobConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.config.task.CDCTaskConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.context.CDCProcessContext;
import org.apache.shardingsphere.data.pipeline.cdc.core.job.CDCJob;
import org.apache.shardingsphere.data.pipeline.cdc.core.job.CDCJobId;
import org.apache.shardingsphere.data.pipeline.cdc.yaml.job.YamlCDCJobConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.yaml.job.YamlCDCJobConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.core.api.GovernanceRepositoryAPI;
import org.apache.shardingsphere.data.pipeline.core.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.api.impl.AbstractInventoryIncrementalJobAPIImpl;
import org.apache.shardingsphere.data.pipeline.core.check.consistency.ConsistencyCheckJobItemProgressContext;
import org.apache.shardingsphere.data.pipeline.core.context.InventoryIncrementalProcessContext;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContext;
import org.apache.shardingsphere.data.pipeline.core.exception.job.PipelineJobCreationWithInvalidShardingCountException;
import org.apache.shardingsphere.data.pipeline.core.metadata.node.PipelineMetaDataNode;
import org.apache.shardingsphere.data.pipeline.core.util.JobDataNodeLineConvertUtil;
import org.apache.shardingsphere.data.pipeline.spi.job.JobType;
import org.apache.shardingsphere.data.pipeline.spi.ratelimit.JobRateLimitAlgorithm;
import org.apache.shardingsphere.data.pipeline.spi.sharding.ShardingColumnsExtractor;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.infra.datasource.props.DataSourcePropertiesCreator;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.util.spi.type.required.RequiredSPIRegistry;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.resource.YamlDataSourceConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapperEngine;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * CDC job API.
 */
@Slf4j
public final class CDCJobAPI extends AbstractInventoryIncrementalJobAPIImpl {
    
    private final YamlDataSourceConfigurationSwapper dataSourceConfigSwapper = new YamlDataSourceConfigurationSwapper();
    
    private final YamlRuleConfigurationSwapperEngine ruleConfigSwapperEngine = new YamlRuleConfigurationSwapperEngine();
    
    private final YamlPipelineDataSourceConfigurationSwapper pipelineDataSourceConfigSwapper = new YamlPipelineDataSourceConfigurationSwapper();
    
    /**
     * Create CDC job config.
     *
     * @param event create CDC job event
     * @return job id
     */
    public boolean createJob(final CreateSubscriptionJobParameter event) {
        YamlCDCJobConfiguration yamlJobConfig = new YamlCDCJobConfiguration();
        yamlJobConfig.setDatabase(event.getDatabase());
        yamlJobConfig.setTableNames(event.getSubscribeTableNames());
        yamlJobConfig.setSubscriptionName(event.getSubscriptionName());
        yamlJobConfig.setSubscriptionMode(event.getSubscriptionMode());
        ShardingSphereDatabase database = PipelineContext.getContextManager().getMetaDataContexts().getMetaData().getDatabase(event.getDatabase());
        yamlJobConfig.setDataSourceConfiguration(pipelineDataSourceConfigSwapper.swapToYamlConfiguration(getDataSourceConfiguration(database)));
        List<JobDataNodeLine> jobDataNodeLines = JobDataNodeLineConvertUtil.convertDataNodesToLines(event.getDataNodesMap());
        yamlJobConfig.setJobShardingDataNodes(jobDataNodeLines.stream().map(JobDataNodeLine::marshal).collect(Collectors.toList()));
        JobDataNodeLine tableFirstDataNodes = new JobDataNodeLine(event.getDataNodesMap().entrySet().stream().map(each -> new JobDataNodeEntry(each.getKey(), each.getValue().subList(0, 1)))
                .collect(Collectors.toList()));
        yamlJobConfig.setTablesFirstDataNodes(tableFirstDataNodes.marshal());
        extendYamlJobConfiguration(yamlJobConfig);
        CDCJobConfiguration jobConfig = new YamlCDCJobConfigurationSwapper().swapToObject(yamlJobConfig);
        ShardingSpherePreconditions.checkState(0 != jobConfig.getJobShardingCount(), () -> new PipelineJobCreationWithInvalidShardingCountException(jobConfig.getJobId()));
        GovernanceRepositoryAPI repositoryAPI = PipelineAPIFactory.getGovernanceRepositoryAPI();
        String jobConfigKey = PipelineMetaDataNode.getJobConfigPath(jobConfig.getJobId());
        if (repositoryAPI.isExisted(jobConfigKey)) {
            log.warn("cdc job already exists in registry center, ignore, jobConfigKey={}", jobConfigKey);
            return false;
        }
        repositoryAPI.persist(PipelineMetaDataNode.getJobRootPath(jobConfig.getJobId()), getJobClassName());
        JobConfigurationPOJO jobConfigPOJO = convertJobConfiguration(jobConfig);
        jobConfigPOJO.setDisabled(true);
        repositoryAPI.persist(jobConfigKey, YamlEngine.marshal(jobConfigPOJO));
        return true;
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
    public CDCTaskConfiguration buildTaskConfiguration(final PipelineJobConfiguration pipelineJobConfig, final int jobShardingItem, final PipelineProcessConfiguration pipelineProcessConfig) {
        CDCJobConfiguration jobConfig = (CDCJobConfiguration) pipelineJobConfig;
        JobDataNodeLine dataNodeLine = jobConfig.getJobShardingDataNodes().get(jobShardingItem);
        Map<ActualTableName, LogicTableName> tableNameMap = new LinkedHashMap<>();
        dataNodeLine.getEntries().forEach(each -> each.getDataNodes().forEach(node -> tableNameMap.put(new ActualTableName(node.getTableName()), new LogicTableName(each.getLogicTableName()))));
        TableNameSchemaNameMapping tableNameSchemaNameMapping = new TableNameSchemaNameMapping(Collections.emptyMap());
        String dataSourceName = dataNodeLine.getEntries().iterator().next().getDataNodes().iterator().next().getDataSourceName();
        StandardPipelineDataSourceConfiguration actualDataSourceConfiguration = jobConfig.getDataSourceConfig().getActualDataSourceConfiguration(dataSourceName);
        DumperConfiguration dumperConfig = buildDumperConfiguration(jobConfig.getJobId(), dataSourceName, actualDataSourceConfiguration, tableNameMap, tableNameSchemaNameMapping);
        ImporterConfiguration importerConfig = buildImporterConfiguration(jobConfig, pipelineProcessConfig, jobConfig.getTableNames(), tableNameSchemaNameMapping);
        CDCTaskConfiguration result = new CDCTaskConfiguration(dumperConfig, importerConfig);
        log.debug("buildTaskConfiguration, result={}", result);
        return result;
    }
    
    private static DumperConfiguration buildDumperConfiguration(final String jobId, final String dataSourceName, final PipelineDataSourceConfiguration sourceDataSourceConfig,
                                                                final Map<ActualTableName, LogicTableName> tableNameMap, final TableNameSchemaNameMapping tableNameSchemaNameMapping) {
        DumperConfiguration result = new DumperConfiguration();
        result.setJobId(jobId);
        result.setDataSourceName(dataSourceName);
        result.setDataSourceConfig(sourceDataSourceConfig);
        result.setTableNameMap(tableNameMap);
        result.setTableNameSchemaNameMapping(tableNameSchemaNameMapping);
        return result;
    }
    
    private ImporterConfiguration buildImporterConfiguration(final CDCJobConfiguration jobConfig, final PipelineProcessConfiguration pipelineProcessConfig, final List<String> logicalTableNames,
                                                             final TableNameSchemaNameMapping tableNameSchemaNameMapping) {
        PipelineDataSourceConfiguration dataSourceConfig = PipelineDataSourceConfigurationFactory.newInstance(jobConfig.getDataSourceConfig().getType(),
                jobConfig.getDataSourceConfig().getParameter());
        CDCProcessContext processContext = new CDCProcessContext(jobConfig.getJobId(), pipelineProcessConfig);
        JobRateLimitAlgorithm writeRateLimitAlgorithm = processContext.getWriteRateLimitAlgorithm();
        int batchSize = pipelineProcessConfig.getWrite().getBatchSize();
        Map<LogicTableName, Set<String>> shardingColumnsMap = RequiredSPIRegistry.getRegisteredService(ShardingColumnsExtractor.class)
                .getShardingColumnsMap(jobConfig.getDataSourceConfig().getRootConfig().getRules(), logicalTableNames.stream().map(LogicTableName::new).collect(Collectors.toSet()));
        return new ImporterConfiguration(dataSourceConfig, shardingColumnsMap, tableNameSchemaNameMapping, batchSize, writeRateLimitAlgorithm, 0, 1);
    }
    
    @Override
    public CDCProcessContext buildPipelineProcessContext(final PipelineJobConfiguration pipelineJobConfig) {
        return new CDCProcessContext(pipelineJobConfig.getJobId(), showProcessConfiguration());
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
        // TODO to be implemented
    }
    
    @Override
    public Optional<InventoryIncrementalJobItemProgress> getJobItemProgress(final String jobId, final int shardingItem) {
        // TODO to be implemented
        return Optional.empty();
    }
    
    @Override
    public void updateJobItemStatus(final String jobId, final int shardingItem, final JobStatus status) {
        // TODO to be implemented
    }
    
    @Override
    protected PipelineJobInfo getJobInfo(final String jobId) {
        // TODO to be implemented
        return null;
    }
    
    @Override
    public void rollback(final String jobId) throws SQLException {
        // TODO to be implemented
    }
    
    @Override
    public void commit(final String jobId) {
        // TODO to be implemented
    }
    
    @Override
    protected PipelineDataConsistencyChecker buildPipelineDataConsistencyChecker(final PipelineJobConfiguration pipelineJobConfig, final InventoryIncrementalProcessContext processContext,
                                                                                 final ConsistencyCheckJobItemProgressContext progressContext) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    protected String getTargetDatabaseType(final PipelineJobConfiguration pipelineJobConfig) {
        throw new UnsupportedOperationException();
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
