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

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.type.ShardingSpherePipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.type.StandardPipelineDataSourceConfiguration;
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
import org.apache.shardingsphere.data.pipeline.common.datasource.PipelineDataSourceConfigurationFactory;
import org.apache.shardingsphere.data.pipeline.common.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.common.datasource.yaml.YamlPipelineDataSourceConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.common.job.progress.InventoryIncrementalJobItemProgress;
import org.apache.shardingsphere.data.pipeline.common.job.progress.JobItemIncrementalTasksProgress;
import org.apache.shardingsphere.data.pipeline.common.metadata.CaseInsensitiveIdentifier;
import org.apache.shardingsphere.data.pipeline.common.pojo.PipelineJobInfo;
import org.apache.shardingsphere.data.pipeline.common.pojo.PipelineJobMetaData;
import org.apache.shardingsphere.data.pipeline.common.registrycenter.repository.GovernanceRepositoryAPI;
import org.apache.shardingsphere.data.pipeline.common.spi.algorithm.JobRateLimitAlgorithm;
import org.apache.shardingsphere.data.pipeline.common.task.progress.IncrementalTaskProgress;
import org.apache.shardingsphere.data.pipeline.common.util.ShardingColumnsExtractor;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.ConsistencyCheckJobItemProgressContext;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.PipelineDataConsistencyChecker;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineInternalException;
import org.apache.shardingsphere.data.pipeline.core.exception.job.PipelineJobCreationWithInvalidShardingCountException;
import org.apache.shardingsphere.data.pipeline.core.exception.job.PrepareJobWithGetBinlogPositionException;
import org.apache.shardingsphere.data.pipeline.core.importer.sink.PipelineSink;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.context.DumperCommonContext;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.context.IncrementalDumperContext;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.context.mapper.TableAndSchemaNameMapper;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobCenter;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.job.service.InventoryIncrementalJobAPI;
import org.apache.shardingsphere.data.pipeline.core.job.service.InventoryIncrementalJobManager;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobItemManager;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobManager;
import org.apache.shardingsphere.data.pipeline.core.preparer.PipelineJobPreparerUtils;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.OneOffJobBootstrap;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.resource.YamlDataSourceConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapperEngine;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
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
public final class CDCJobAPI implements InventoryIncrementalJobAPI {
    
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
        PipelineContextKey contextKey = new PipelineContextKey(param.getDatabaseName(), InstanceType.PROXY);
        YamlCDCJobConfiguration yamlJobConfig = getYamlCDCJobConfiguration(param, sinkType, sinkProps, contextKey);
        extendYamlJobConfiguration(contextKey, yamlJobConfig);
        CDCJobConfiguration jobConfig = new YamlCDCJobConfigurationSwapper().swapToObject(yamlJobConfig);
        ShardingSpherePreconditions.checkState(0 != jobConfig.getJobShardingCount(), () -> new PipelineJobCreationWithInvalidShardingCountException(jobConfig.getJobId()));
        GovernanceRepositoryAPI repositoryAPI = PipelineAPIFactory.getGovernanceRepositoryAPI(PipelineJobIdUtils.parseContextKey(jobConfig.getJobId()));
        if (repositoryAPI.getJobConfigurationGovernanceRepository().isExisted(jobConfig.getJobId())) {
            log.warn("CDC job already exists in registry center, ignore, job id is `{}`", jobConfig.getJobId());
        } else {
            repositoryAPI.getJobGovernanceRepository().create(jobConfig.getJobId(), getJobClass());
            JobConfigurationPOJO jobConfigPOJO = jobConfig.convertToJobConfigurationPOJO();
            jobConfigPOJO.setDisabled(true);
            repositoryAPI.getJobConfigurationGovernanceRepository().persist(jobConfig.getJobId(), jobConfigPOJO);
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
        Map<String, Map<String, Object>> dataSourcePoolProps = new HashMap<>();
        for (Entry<String, StorageUnit> entry : database.getResourceMetaData().getStorageUnits().entrySet()) {
            dataSourcePoolProps.put(entry.getKey(), dataSourceConfigSwapper.swapToMap(entry.getValue().getDataSourcePoolProperties()));
        }
        YamlRootConfiguration targetRootConfig = new YamlRootConfiguration();
        targetRootConfig.setDatabaseName(database.getName());
        targetRootConfig.setDataSources(dataSourcePoolProps);
        targetRootConfig.setRules(ruleConfigSwapperEngine.swapToYamlRuleConfigurations(database.getRuleMetaData().getConfigurations()));
        return new ShardingSpherePipelineDataSourceConfiguration(targetRootConfig);
    }
    
    private void initIncrementalPosition(final CDCJobConfiguration jobConfig) {
        String jobId = jobConfig.getJobId();
        PipelineJobItemManager<InventoryIncrementalJobItemProgress> jobItemManager = new PipelineJobItemManager<>(getYamlJobItemProgressSwapper());
        try (PipelineDataSourceManager pipelineDataSourceManager = new DefaultPipelineDataSourceManager()) {
            for (int i = 0; i < jobConfig.getJobShardingCount(); i++) {
                if (jobItemManager.getProgress(jobId, i).isPresent()) {
                    continue;
                }
                IncrementalDumperContext dumperContext = buildDumperContext(jobConfig, i, new TableAndSchemaNameMapper(jobConfig.getSchemaTableNames()));
                InventoryIncrementalJobItemProgress jobItemProgress = getInventoryIncrementalJobItemProgress(jobConfig, pipelineDataSourceManager, dumperContext);
                PipelineAPIFactory.getGovernanceRepositoryAPI(PipelineJobIdUtils.parseContextKey(jobId)).getJobItemProcessGovernanceRepository().persist(
                        jobId, i, YamlEngine.marshal(getYamlJobItemProgressSwapper().swapToYamlConfiguration(jobItemProgress)));
            }
        } catch (final SQLException ex) {
            throw new PrepareJobWithGetBinlogPositionException(jobId, ex);
        }
    }
    
    private static InventoryIncrementalJobItemProgress getInventoryIncrementalJobItemProgress(final CDCJobConfiguration jobConfig,
                                                                                              final PipelineDataSourceManager dataSourceManager,
                                                                                              final IncrementalDumperContext incrementalDumperContext) throws SQLException {
        InventoryIncrementalJobItemProgress result = new InventoryIncrementalJobItemProgress();
        result.setSourceDatabaseType(jobConfig.getSourceDatabaseType());
        result.setDataSourceName(incrementalDumperContext.getCommonContext().getDataSourceName());
        IncrementalTaskProgress incrementalTaskProgress = new IncrementalTaskProgress(PipelineJobPreparerUtils.getIncrementalPosition(null, incrementalDumperContext, dataSourceManager));
        result.setIncremental(new JobItemIncrementalTasksProgress(incrementalTaskProgress));
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
        JobConfigurationPOJO jobConfigPOJO = PipelineJobIdUtils.getElasticJobConfigurationPOJO(jobId);
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
        JobConfigurationPOJO jobConfigPOJO = PipelineJobIdUtils.getElasticJobConfigurationPOJO(jobId);
        jobConfigPOJO.setDisabled(disabled);
        if (disabled) {
            jobConfigPOJO.getProps().setProperty("stop_time", LocalDateTime.now().format(PipelineJobConfiguration.DATE_TIME_FORMATTER));
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
            config.setJobId(new CDCJobId(contextKey, config.getSchemaTableNames(), config.isFull(), config.getSinkConfig().getSinkType()).marshal());
        }
        if (Strings.isNullOrEmpty(config.getSourceDatabaseType())) {
            PipelineDataSourceConfiguration sourceDataSourceConfig = PipelineDataSourceConfigurationFactory.newInstance(config.getDataSourceConfiguration().getType(),
                    config.getDataSourceConfiguration().getParameter());
            config.setSourceDatabaseType(sourceDataSourceConfig.getDatabaseType().getType());
        }
    }
    
    @Override
    public CDCTaskConfiguration buildTaskConfiguration(final PipelineJobConfiguration pipelineJobConfig, final int jobShardingItem, final PipelineProcessConfiguration pipelineProcessConfig) {
        CDCJobConfiguration jobConfig = (CDCJobConfiguration) pipelineJobConfig;
        TableAndSchemaNameMapper tableAndSchemaNameMapper = new TableAndSchemaNameMapper(jobConfig.getSchemaTableNames());
        IncrementalDumperContext dumperContext = buildDumperContext(jobConfig, jobShardingItem, tableAndSchemaNameMapper);
        ImporterConfiguration importerConfig = buildImporterConfiguration(jobConfig, pipelineProcessConfig, jobConfig.getSchemaTableNames(), tableAndSchemaNameMapper);
        CDCTaskConfiguration result = new CDCTaskConfiguration(dumperContext, importerConfig);
        log.debug("buildTaskConfiguration, result={}", result);
        return result;
    }
    
    private IncrementalDumperContext buildDumperContext(final CDCJobConfiguration jobConfig, final int jobShardingItem, final TableAndSchemaNameMapper tableAndSchemaNameMapper) {
        JobDataNodeLine dataNodeLine = jobConfig.getJobShardingDataNodes().get(jobShardingItem);
        String dataSourceName = dataNodeLine.getEntries().iterator().next().getDataNodes().iterator().next().getDataSourceName();
        StandardPipelineDataSourceConfiguration actualDataSourceConfig = jobConfig.getDataSourceConfig().getActualDataSourceConfiguration(dataSourceName);
        return new IncrementalDumperContext(
                new DumperCommonContext(dataSourceName, actualDataSourceConfig, JobDataNodeLineConvertUtils.buildTableNameMapper(dataNodeLine), tableAndSchemaNameMapper),
                jobConfig.getJobId(), jobConfig.isDecodeWithTX());
    }
    
    private ImporterConfiguration buildImporterConfiguration(final CDCJobConfiguration jobConfig, final PipelineProcessConfiguration pipelineProcessConfig, final Collection<String> schemaTableNames,
                                                             final TableAndSchemaNameMapper tableAndSchemaNameMapper) {
        PipelineDataSourceConfiguration dataSourceConfig = PipelineDataSourceConfigurationFactory.newInstance(jobConfig.getDataSourceConfig().getType(),
                jobConfig.getDataSourceConfig().getParameter());
        CDCProcessContext processContext = new CDCProcessContext(jobConfig.getJobId(), pipelineProcessConfig);
        JobRateLimitAlgorithm writeRateLimitAlgorithm = processContext.getWriteRateLimitAlgorithm();
        int batchSize = pipelineProcessConfig.getWrite().getBatchSize();
        Map<CaseInsensitiveIdentifier, Set<String>> shardingColumnsMap = new ShardingColumnsExtractor()
                .getShardingColumnsMap(jobConfig.getDataSourceConfig().getRootConfig().getRules(), schemaTableNames.stream().map(CaseInsensitiveIdentifier::new).collect(Collectors.toSet()));
        return new ImporterConfiguration(dataSourceConfig, shardingColumnsMap, tableAndSchemaNameMapper, batchSize, writeRateLimitAlgorithm, 0, 1);
    }
    
    @Override
    public CDCProcessContext buildPipelineProcessContext(final PipelineJobConfiguration pipelineJobConfig) {
        InventoryIncrementalJobManager jobManager = new InventoryIncrementalJobManager(this);
        return new CDCProcessContext(pipelineJobConfig.getJobId(), jobManager.showProcessConfiguration(PipelineJobIdUtils.parseContextKey(pipelineJobConfig.getJobId())));
    }
    
    @Override
    public YamlCDCJobConfigurationSwapper getYamlJobConfigurationSwapper() {
        return new YamlCDCJobConfigurationSwapper();
    }
    
    @Override
    public PipelineJobInfo getJobInfo(final String jobId) {
        PipelineJobMetaData jobMetaData = new PipelineJobMetaData(PipelineJobIdUtils.getElasticJobConfigurationPOJO(jobId));
        CDCJobConfiguration jobConfig = new PipelineJobManager(this).getJobConfiguration(jobId);
        return new PipelineJobInfo(jobMetaData, jobConfig.getDatabaseName(), String.join(", ", jobConfig.getSchemaTableNames()));
    }
    
    @Override
    public void commit(final String jobId) {
    }
    
    /**
     * Drop streaming job.
     *
     * @param jobId job id
     */
    public void dropStreaming(final String jobId) {
        CDCJobConfiguration jobConfig = new PipelineJobManager(this).getJobConfiguration(jobId);
        ShardingSpherePreconditions.checkState(PipelineJobIdUtils.getElasticJobConfigurationPOJO(jobId).isDisabled(), () -> new PipelineInternalException("Can't drop streaming job which is active"));
        new PipelineJobManager(this).drop(jobId);
        cleanup(jobConfig);
    }
    
    private void cleanup(final CDCJobConfiguration jobConfig) {
        for (Entry<String, Map<String, Object>> entry : jobConfig.getDataSourceConfig().getRootConfig().getDataSources().entrySet()) {
            try {
                PipelineJobPreparerUtils.destroyPosition(jobConfig.getJobId(), new StandardPipelineDataSourceConfiguration(entry.getValue()));
            } catch (final SQLException ex) {
                log.warn("job destroying failed, jobId={}, dataSourceName={}", jobConfig.getJobId(), entry.getKey(), ex);
            }
        }
    }
    
    @Override
    public void rollback(final String jobId) throws SQLException {
    }
    
    @Override
    public PipelineDataConsistencyChecker buildPipelineDataConsistencyChecker(final PipelineJobConfiguration pipelineJobConfig, final InventoryIncrementalProcessContext processContext,
                                                                              final ConsistencyCheckJobItemProgressContext progressContext) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public Class<CDCJob> getJobClass() {
        return CDCJob.class;
    }
    
    @Override
    public String getType() {
        return "STREAMING";
    }
}
