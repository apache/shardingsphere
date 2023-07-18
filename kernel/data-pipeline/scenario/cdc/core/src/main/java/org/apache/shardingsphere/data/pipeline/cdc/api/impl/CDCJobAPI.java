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
import org.apache.shardingsphere.data.pipeline.api.config.TableNameSchemaNameMapping;
import org.apache.shardingsphere.data.pipeline.api.config.ingest.DumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfigurationFactory;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.ShardingSpherePipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.yaml.YamlPipelineDataSourceConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.api.metadata.ActualTableName;
import org.apache.shardingsphere.data.pipeline.api.metadata.LogicTableName;
import org.apache.shardingsphere.data.pipeline.cdc.api.job.type.CDCJobType;
import org.apache.shardingsphere.data.pipeline.cdc.api.pojo.StreamDataParameter;
import org.apache.shardingsphere.data.pipeline.cdc.config.job.CDCJobConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.config.task.CDCTaskConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.constant.CDCSinkType;
import org.apache.shardingsphere.data.pipeline.cdc.context.CDCProcessContext;
import org.apache.shardingsphere.data.pipeline.cdc.core.job.CDCJob;
import org.apache.shardingsphere.data.pipeline.cdc.core.job.CDCJobId;
import org.apache.shardingsphere.data.pipeline.cdc.yaml.config.YamlCDCJobConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.yaml.config.YamlCDCJobConfiguration.YamlSinkConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.yaml.swapper.YamlCDCJobConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.common.config.ImporterConfiguration;
import org.apache.shardingsphere.data.pipeline.common.config.job.PipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.common.config.job.yaml.YamlPipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.common.config.process.PipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.common.context.InventoryIncrementalProcessContext;
import org.apache.shardingsphere.data.pipeline.common.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.common.context.PipelineContextManager;
import org.apache.shardingsphere.data.pipeline.common.datanode.JobDataNodeEntry;
import org.apache.shardingsphere.data.pipeline.common.datanode.JobDataNodeLine;
import org.apache.shardingsphere.data.pipeline.common.datanode.JobDataNodeLineConvertUtils;
import org.apache.shardingsphere.data.pipeline.common.datasource.DefaultPipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.common.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.common.job.PipelineJobId;
import org.apache.shardingsphere.data.pipeline.common.job.progress.InventoryIncrementalJobItemProgress;
import org.apache.shardingsphere.data.pipeline.common.job.progress.JobItemIncrementalTasksProgress;
import org.apache.shardingsphere.data.pipeline.common.job.type.JobType;
import org.apache.shardingsphere.data.pipeline.common.metadata.node.PipelineMetaDataNode;
import org.apache.shardingsphere.data.pipeline.common.pojo.PipelineJobMetaData;
import org.apache.shardingsphere.data.pipeline.common.pojo.TableBasedPipelineJobInfo;
import org.apache.shardingsphere.data.pipeline.common.registrycenter.repository.GovernanceRepositoryAPI;
import org.apache.shardingsphere.data.pipeline.common.task.progress.IncrementalTaskProgress;
import org.apache.shardingsphere.data.pipeline.common.util.ShardingColumnsExtractor;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.ConsistencyCheckJobItemProgressContext;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.PipelineDataConsistencyChecker;
import org.apache.shardingsphere.data.pipeline.core.exception.job.PipelineJobCreationWithInvalidShardingCountException;
import org.apache.shardingsphere.data.pipeline.core.exception.job.PrepareJobWithGetBinlogPositionException;
import org.apache.shardingsphere.data.pipeline.core.importer.sink.PipelineSink;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobCenter;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.job.service.impl.AbstractInventoryIncrementalJobAPIImpl;
import org.apache.shardingsphere.data.pipeline.core.preparer.PipelineJobPreparerUtils;
import org.apache.shardingsphere.data.pipeline.spi.ratelimit.JobRateLimitAlgorithm;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.OneOffJobBootstrap;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.resource.YamlDataSourceConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapperEngine;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
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
     * @param param create CDC job param
     * @param sinkType sink type
     * @param sinkProps sink properties
     * @return job id
     */
    public String createJob(final StreamDataParameter param, final CDCSinkType sinkType, final Properties sinkProps) {
        PipelineContextKey contextKey = PipelineContextKey.buildForProxy(param.getDatabaseName());
        YamlCDCJobConfiguration yamlJobConfig = getYamlCDCJobConfiguration(param, sinkType, sinkProps, contextKey);
        extendYamlJobConfiguration(contextKey, yamlJobConfig);
        CDCJobConfiguration jobConfig = new YamlCDCJobConfigurationSwapper().swapToObject(yamlJobConfig);
        ShardingSpherePreconditions.checkState(0 != jobConfig.getJobShardingCount(), () -> new PipelineJobCreationWithInvalidShardingCountException(jobConfig.getJobId()));
        GovernanceRepositoryAPI repositoryAPI = PipelineAPIFactory.getGovernanceRepositoryAPI(PipelineJobIdUtils.parseContextKey(jobConfig.getJobId()));
        String jobConfigKey = PipelineMetaDataNode.getJobConfigPath(jobConfig.getJobId());
        if (repositoryAPI.isExisted(jobConfigKey)) {
            log.warn("CDC job already exists in registry center, ignore, jobConfigKey={}", jobConfigKey);
        } else {
            repositoryAPI.persist(PipelineMetaDataNode.getJobRootPath(jobConfig.getJobId()), getJobClassName());
            JobConfigurationPOJO jobConfigPOJO = convertJobConfiguration(jobConfig);
            jobConfigPOJO.setDisabled(true);
            repositoryAPI.persist(jobConfigKey, YamlEngine.marshal(jobConfigPOJO));
            if (!param.isFull()) {
                initIncrementalPosition(jobConfig);
            }
        }
        return jobConfig.getJobId();
    }
    
    private YamlCDCJobConfiguration getYamlCDCJobConfiguration(final StreamDataParameter param, final CDCSinkType sinkType, final Properties sinkProps, final PipelineContextKey contextKey) {
        YamlCDCJobConfiguration result = new YamlCDCJobConfiguration();
        result.setDatabaseName(param.getDatabaseName());
        result.setSchemaTableNames(param.getSchemaTableNames());
        result.setFull(param.isFull());
        result.setDecodeWithTX(param.isDecodeWithTX());
        YamlSinkConfiguration sinkConfig = new YamlSinkConfiguration();
        sinkConfig.setSinkType(sinkType.name());
        sinkConfig.setProps(sinkProps);
        result.setSinkConfig(sinkConfig);
        ShardingSphereDatabase database = PipelineContextManager.getContext(contextKey).getContextManager().getMetaDataContexts().getMetaData().getDatabase(param.getDatabaseName());
        result.setDataSourceConfiguration(pipelineDataSourceConfigSwapper.swapToYamlConfiguration(getDataSourceConfiguration(database)));
        List<JobDataNodeLine> jobDataNodeLines = JobDataNodeLineConvertUtils.convertDataNodesToLines(param.getDataNodesMap());
        result.setJobShardingDataNodes(jobDataNodeLines.stream().map(JobDataNodeLine::marshal).collect(Collectors.toList()));
        JobDataNodeLine tableFirstDataNodes = new JobDataNodeLine(param.getDataNodesMap().entrySet().stream()
                .map(each -> new JobDataNodeEntry(each.getKey(), each.getValue().subList(0, 1))).collect(Collectors.toList()));
        result.setTablesFirstDataNodes(tableFirstDataNodes.marshal());
        return result;
    }
    
    private ShardingSpherePipelineDataSourceConfiguration getDataSourceConfiguration(final ShardingSphereDatabase database) {
        Map<String, Map<String, Object>> dataSourceProps = new HashMap<>();
        for (Entry<String, DataSourceProperties> entry : database.getResourceMetaData().getDataSourcePropsMap().entrySet()) {
            dataSourceProps.put(entry.getKey(), dataSourceConfigSwapper.swapToMap(entry.getValue()));
        }
        YamlRootConfiguration targetRootConfig = new YamlRootConfiguration();
        targetRootConfig.setDatabaseName(database.getName());
        targetRootConfig.setDataSources(dataSourceProps);
        Collection<YamlRuleConfiguration> yamlRuleConfigurations = ruleConfigSwapperEngine.swapToYamlRuleConfigurations(database.getRuleMetaData().getConfigurations());
        targetRootConfig.setRules(yamlRuleConfigurations);
        return new ShardingSpherePipelineDataSourceConfiguration(targetRootConfig);
    }
    
    private void initIncrementalPosition(final CDCJobConfiguration jobConfig) {
        String jobId = jobConfig.getJobId();
        try (PipelineDataSourceManager pipelineDataSourceManager = new DefaultPipelineDataSourceManager()) {
            for (int i = 0; i < jobConfig.getJobShardingCount(); i++) {
                if (getJobItemProgress(jobId, i).isPresent()) {
                    continue;
                }
                DumperConfiguration dumperConfig = buildDumperConfiguration(jobConfig, i, getTableNameSchemaNameMapping(jobConfig.getSchemaTableNames()));
                InventoryIncrementalJobItemProgress jobItemProgress = getInventoryIncrementalJobItemProgress(jobConfig, pipelineDataSourceManager, dumperConfig);
                PipelineAPIFactory.getGovernanceRepositoryAPI(PipelineJobIdUtils.parseContextKey(jobId)).persistJobItemProgress(
                        jobId, i, YamlEngine.marshal(getJobItemProgressSwapper().swapToYamlConfiguration(jobItemProgress)));
            }
        } catch (final SQLException ex) {
            throw new PrepareJobWithGetBinlogPositionException(jobId, ex);
        }
    }
    
    private static InventoryIncrementalJobItemProgress getInventoryIncrementalJobItemProgress(final CDCJobConfiguration jobConfig,
                                                                                              final PipelineDataSourceManager dataSourceManager,
                                                                                              final DumperConfiguration dumperConfig) throws SQLException {
        InventoryIncrementalJobItemProgress result = new InventoryIncrementalJobItemProgress();
        result.setSourceDatabaseType(jobConfig.getSourceDatabaseType());
        result.setDataSourceName(dumperConfig.getDataSourceName());
        IncrementalTaskProgress incrementalTaskProgress = new IncrementalTaskProgress(PipelineJobPreparerUtils.getIncrementalPosition(null, dumperConfig, dataSourceManager));
        result.setIncremental(new JobItemIncrementalTasksProgress(incrementalTaskProgress));
        return result;
    }
    
    @Override
    protected JobConfigurationPOJO convertJobConfiguration(final PipelineJobConfiguration jobConfig) {
        JobConfigurationPOJO result = super.convertJobConfiguration(jobConfig);
        result.setShardingTotalCount(1);
        return result;
    }
    
    /**
     * Start job.
     *
     * @param jobId job id
     * @param sink sink
     */
    public void startJob(final String jobId, final PipelineSink sink) {
        CDCJob job = new CDCJob(jobId, sink);
        PipelineJobCenter.addJob(jobId, job);
        updateJobConfigurationDisabled(jobId, false);
        JobConfigurationPOJO jobConfigPOJO = getElasticJobConfigPOJO(jobId);
        OneOffJobBootstrap oneOffJobBootstrap = new OneOffJobBootstrap(PipelineAPIFactory.getRegistryCenter(PipelineJobIdUtils.parseContextKey(jobId)), job, jobConfigPOJO.toJobConfiguration());
        job.setJobBootstrap(oneOffJobBootstrap);
        oneOffJobBootstrap.execute();
    }
    
    /**
     * Update job configuration disabled.
     *
     * @param jobId job id
     * @param disabled disabled
     */
    public void updateJobConfigurationDisabled(final String jobId, final boolean disabled) {
        JobConfigurationPOJO jobConfigPOJO = getElasticJobConfigPOJO(jobId);
        jobConfigPOJO.setDisabled(disabled);
        if (disabled) {
            jobConfigPOJO.getProps().setProperty("stop_time_millis", String.valueOf(System.currentTimeMillis()));
        } else {
            jobConfigPOJO.getProps().setProperty("start_time_millis", String.valueOf(System.currentTimeMillis()));
            jobConfigPOJO.getProps().remove("stop_time_millis");
        }
        PipelineAPIFactory.getJobConfigurationAPI(PipelineJobIdUtils.parseContextKey(jobConfigPOJO.getJobName())).updateJobConfiguration(jobConfigPOJO);
    }
    
    @Override
    public void extendYamlJobConfiguration(final PipelineContextKey contextKey, final YamlPipelineJobConfiguration yamlJobConfig) {
        YamlCDCJobConfiguration config = (YamlCDCJobConfiguration) yamlJobConfig;
        if (null == yamlJobConfig.getJobId()) {
            config.setJobId(generateJobId(contextKey, config));
        }
        if (Strings.isNullOrEmpty(config.getSourceDatabaseType())) {
            PipelineDataSourceConfiguration sourceDataSourceConfig = PipelineDataSourceConfigurationFactory.newInstance(config.getDataSourceConfiguration().getType(),
                    config.getDataSourceConfiguration().getParameter());
            config.setSourceDatabaseType(sourceDataSourceConfig.getDatabaseType().getType());
        }
    }
    
    private String generateJobId(final PipelineContextKey contextKey, final YamlCDCJobConfiguration config) {
        CDCJobId jobId = new CDCJobId(contextKey, config.getSchemaTableNames(), config.isFull(), config.getSinkConfig().getSinkType());
        return marshalJobId(jobId);
    }
    
    @Override
    protected String marshalJobIdLeftPart(final PipelineJobId pipelineJobId) {
        CDCJobId jobId = (CDCJobId) pipelineJobId;
        String text = Joiner.on('|').join(jobId.getContextKey().getDatabaseName(), jobId.getSchemaTableNames(), jobId.isFull());
        return DigestUtils.md5Hex(text.getBytes(StandardCharsets.UTF_8));
    }
    
    @Override
    public CDCTaskConfiguration buildTaskConfiguration(final PipelineJobConfiguration pipelineJobConfig, final int jobShardingItem, final PipelineProcessConfiguration pipelineProcessConfig) {
        CDCJobConfiguration jobConfig = (CDCJobConfiguration) pipelineJobConfig;
        TableNameSchemaNameMapping tableNameSchemaNameMapping = getTableNameSchemaNameMapping(jobConfig.getSchemaTableNames());
        DumperConfiguration dumperConfig = buildDumperConfiguration(jobConfig, jobShardingItem, tableNameSchemaNameMapping);
        ImporterConfiguration importerConfig = buildImporterConfiguration(jobConfig, pipelineProcessConfig, jobConfig.getSchemaTableNames(), tableNameSchemaNameMapping);
        CDCTaskConfiguration result = new CDCTaskConfiguration(dumperConfig, importerConfig);
        log.debug("buildTaskConfiguration, result={}", result);
        return result;
    }
    
    private TableNameSchemaNameMapping getTableNameSchemaNameMapping(final Collection<String> tableNames) {
        Map<String, String> tableNameSchemaMap = new LinkedHashMap<>();
        for (String each : tableNames) {
            String[] split = each.split("\\.");
            if (split.length > 1) {
                tableNameSchemaMap.put(split[1], split[0]);
            }
        }
        return new TableNameSchemaNameMapping(tableNameSchemaMap);
    }
    
    private DumperConfiguration buildDumperConfiguration(final CDCJobConfiguration jobConfig, final int jobShardingItem, final TableNameSchemaNameMapping tableNameSchemaNameMapping) {
        JobDataNodeLine dataNodeLine = jobConfig.getJobShardingDataNodes().get(jobShardingItem);
        Map<ActualTableName, LogicTableName> tableNameMap = new LinkedHashMap<>();
        dataNodeLine.getEntries().forEach(each -> each.getDataNodes().forEach(node -> tableNameMap.put(new ActualTableName(node.getTableName()), new LogicTableName(each.getLogicTableName()))));
        String dataSourceName = dataNodeLine.getEntries().iterator().next().getDataNodes().iterator().next().getDataSourceName();
        StandardPipelineDataSourceConfiguration actualDataSourceConfig = jobConfig.getDataSourceConfig().getActualDataSourceConfiguration(dataSourceName);
        DumperConfiguration result = new DumperConfiguration();
        result.setJobId(jobConfig.getJobId());
        result.setDataSourceName(dataSourceName);
        result.setDataSourceConfig(actualDataSourceConfig);
        result.setTableNameMap(tableNameMap);
        result.setTableNameSchemaNameMapping(tableNameSchemaNameMapping);
        result.setDecodeWithTX(jobConfig.isDecodeWithTX());
        return result;
    }
    
    private ImporterConfiguration buildImporterConfiguration(final CDCJobConfiguration jobConfig, final PipelineProcessConfiguration pipelineProcessConfig, final Collection<String> schemaTableNames,
                                                             final TableNameSchemaNameMapping tableNameSchemaNameMapping) {
        PipelineDataSourceConfiguration dataSourceConfig = PipelineDataSourceConfigurationFactory.newInstance(jobConfig.getDataSourceConfig().getType(),
                jobConfig.getDataSourceConfig().getParameter());
        CDCProcessContext processContext = new CDCProcessContext(jobConfig.getJobId(), pipelineProcessConfig);
        JobRateLimitAlgorithm writeRateLimitAlgorithm = processContext.getWriteRateLimitAlgorithm();
        int batchSize = pipelineProcessConfig.getWrite().getBatchSize();
        Map<LogicTableName, Set<String>> shardingColumnsMap = new ShardingColumnsExtractor()
                .getShardingColumnsMap(jobConfig.getDataSourceConfig().getRootConfig().getRules(), schemaTableNames.stream().map(LogicTableName::new).collect(Collectors.toSet()));
        return new ImporterConfiguration(dataSourceConfig, shardingColumnsMap, tableNameSchemaNameMapping, batchSize, writeRateLimitAlgorithm, 0, 1);
    }
    
    @Override
    public CDCProcessContext buildPipelineProcessContext(final PipelineJobConfiguration pipelineJobConfig) {
        return new CDCProcessContext(pipelineJobConfig.getJobId(), showProcessConfiguration(PipelineJobIdUtils.parseContextKey(pipelineJobConfig.getJobId())));
    }
    
    @Override
    public CDCJobConfiguration getJobConfiguration(final String jobId) {
        return getJobConfiguration(getElasticJobConfigPOJO(jobId));
    }
    
    @Override
    protected CDCJobConfiguration getJobConfiguration(final JobConfigurationPOJO jobConfigPOJO) {
        return new YamlCDCJobConfigurationSwapper().swapToObject(jobConfigPOJO.getJobParameter());
    }
    
    @Override
    protected YamlPipelineJobConfiguration swapToYamlJobConfiguration(final PipelineJobConfiguration jobConfig) {
        return new YamlCDCJobConfigurationSwapper().swapToYamlConfiguration((CDCJobConfiguration) jobConfig);
    }
    
    @Override
    protected TableBasedPipelineJobInfo getJobInfo(final String jobId) {
        JobConfigurationPOJO jobConfigPOJO = getElasticJobConfigPOJO(jobId);
        PipelineJobMetaData jobMetaData = buildPipelineJobMetaData(jobConfigPOJO);
        CDCJobConfiguration jobConfig = getJobConfiguration(jobConfigPOJO);
        return new TableBasedPipelineJobInfo(jobMetaData, jobConfig.getDatabaseName(), String.join(", ", jobConfig.getSchemaTableNames()));
    }
    
    @Override
    public void commit(final String jobId) {
    }
    
    /**
     * Stop and drop job.
     *
     * @param jobId job id
     */
    public void stopAndDrop(final String jobId) {
        CDCJobConfiguration jobConfig = getJobConfiguration(jobId);
        if (CDCSinkType.SOCKET == jobConfig.getSinkConfig().getSinkType()) {
            PipelineJobCenter.stop(jobId);
        } else {
            stop(jobId);
        }
        dropJob(jobId);
    }
    
    @Override
    public void rollback(final String jobId) throws SQLException {
    }
    
    @Override
    protected PipelineDataConsistencyChecker buildPipelineDataConsistencyChecker(final PipelineJobConfiguration pipelineJobConfig, final InventoryIncrementalProcessContext processContext,
                                                                                 final ConsistencyCheckJobItemProgressContext progressContext) {
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
