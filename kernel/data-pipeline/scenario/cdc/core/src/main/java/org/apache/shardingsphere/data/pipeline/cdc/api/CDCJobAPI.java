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

package org.apache.shardingsphere.data.pipeline.cdc.api;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.type.ShardingSpherePipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.type.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.CDCJob;
import org.apache.shardingsphere.data.pipeline.cdc.CDCJobId;
import org.apache.shardingsphere.data.pipeline.cdc.CDCJobType;
import org.apache.shardingsphere.data.pipeline.cdc.config.job.CDCJobConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.config.yaml.YamlCDCJobConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.config.yaml.YamlCDCJobConfiguration.YamlSinkConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.config.yaml.YamlCDCJobConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.cdc.constant.CDCSinkType;
import org.apache.shardingsphere.data.pipeline.common.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.common.context.PipelineContextManager;
import org.apache.shardingsphere.data.pipeline.common.datanode.JobDataNodeEntry;
import org.apache.shardingsphere.data.pipeline.common.datanode.JobDataNodeLine;
import org.apache.shardingsphere.data.pipeline.common.datanode.JobDataNodeLineConvertUtils;
import org.apache.shardingsphere.data.pipeline.common.datasource.DefaultPipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.common.datasource.PipelineDataSourceConfigurationFactory;
import org.apache.shardingsphere.data.pipeline.common.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.common.datasource.yaml.YamlPipelineDataSourceConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.common.job.progress.JobItemIncrementalTasksProgress;
import org.apache.shardingsphere.data.pipeline.common.job.progress.TransmissionJobItemProgress;
import org.apache.shardingsphere.data.pipeline.common.registrycenter.repository.PipelineGovernanceFacade;
import org.apache.shardingsphere.data.pipeline.common.task.progress.IncrementalTaskProgress;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineInternalException;
import org.apache.shardingsphere.data.pipeline.core.exception.job.PipelineJobCreationWithInvalidShardingCountException;
import org.apache.shardingsphere.data.pipeline.core.exception.job.PrepareJobWithGetBinlogPositionException;
import org.apache.shardingsphere.data.pipeline.core.importer.sink.PipelineSink;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.context.DumperCommonContext;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.context.IncrementalDumperContext;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.context.mapper.TableAndSchemaNameMapper;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobCenter;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.job.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.job.api.TransmissionJobAPI;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobConfigurationManager;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobItemManager;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobManager;
import org.apache.shardingsphere.data.pipeline.core.preparer.PipelineJobPreparerUtils;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.OneOffJobBootstrap;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.util.datetime.DateTimeFormatterFactory;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.resource.YamlDataSourceConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapperEngine;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * CDC job API.
 */
@Slf4j
public final class CDCJobAPI implements TransmissionJobAPI {
    
    private final CDCJobType jobType;
    
    private final PipelineJobManager jobManager;
    
    private final PipelineJobConfigurationManager jobConfigManager;
    
    private final YamlDataSourceConfigurationSwapper dataSourceConfigSwapper;
    
    private final YamlRuleConfigurationSwapperEngine ruleConfigSwapperEngine;
    
    private final YamlPipelineDataSourceConfigurationSwapper pipelineDataSourceConfigSwapper;
    
    public CDCJobAPI() {
        jobType = new CDCJobType();
        jobManager = new PipelineJobManager(jobType);
        jobConfigManager = new PipelineJobConfigurationManager(jobType);
        dataSourceConfigSwapper = new YamlDataSourceConfigurationSwapper();
        ruleConfigSwapperEngine = new YamlRuleConfigurationSwapperEngine();
        pipelineDataSourceConfigSwapper = new YamlPipelineDataSourceConfigurationSwapper();
    }
    
    /**
     * Create CDC job.
     *
     * @param param CDC job parameter
     * @param sinkType sink type
     * @param sinkProps sink properties
     * @return job id
     */
    public String create(final StreamDataParameter param, final CDCSinkType sinkType, final Properties sinkProps) {
        PipelineContextKey contextKey = new PipelineContextKey(param.getDatabaseName(), InstanceType.PROXY);
        YamlCDCJobConfiguration yamlJobConfig = getYamlCDCJobConfiguration(param, sinkType, sinkProps, contextKey);
        CDCJobConfiguration jobConfig = new YamlCDCJobConfigurationSwapper().swapToObject(yamlJobConfig);
        ShardingSpherePreconditions.checkState(0 != jobConfig.getJobShardingCount(), () -> new PipelineJobCreationWithInvalidShardingCountException(jobConfig.getJobId()));
        PipelineGovernanceFacade governanceFacade = PipelineAPIFactory.getPipelineGovernanceFacade(PipelineJobIdUtils.parseContextKey(jobConfig.getJobId()));
        if (governanceFacade.getJobFacade().getConfiguration().isExisted(jobConfig.getJobId())) {
            log.warn("CDC job already exists in registry center, ignore, job id is `{}`", jobConfig.getJobId());
        } else {
            governanceFacade.getJobFacade().getJob().create(jobConfig.getJobId(), jobType.getJobClass());
            JobConfigurationPOJO jobConfigPOJO = jobConfigManager.convertToJobConfigurationPOJO(jobConfig);
            jobConfigPOJO.setDisabled(true);
            governanceFacade.getJobFacade().getConfiguration().persist(jobConfig.getJobId(), jobConfigPOJO);
            if (!param.isFull()) {
                initIncrementalPosition(jobConfig);
            }
        }
        return jobConfig.getJobId();
    }
    
    private YamlCDCJobConfiguration getYamlCDCJobConfiguration(final StreamDataParameter param, final CDCSinkType sinkType, final Properties sinkProps, final PipelineContextKey contextKey) {
        YamlCDCJobConfiguration result = new YamlCDCJobConfiguration();
        result.setJobId(new CDCJobId(contextKey, param.getSchemaTableNames(), param.isFull()).marshal());
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
        result.setSourceDatabaseType(PipelineDataSourceConfigurationFactory.newInstance(
                result.getDataSourceConfiguration().getType(), result.getDataSourceConfiguration().getParameter()).getDatabaseType().getType());
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
        PipelineJobItemManager<TransmissionJobItemProgress> jobItemManager = new PipelineJobItemManager<>(jobType.getYamlJobItemProgressSwapper());
        try (PipelineDataSourceManager pipelineDataSourceManager = new DefaultPipelineDataSourceManager()) {
            for (int i = 0; i < jobConfig.getJobShardingCount(); i++) {
                if (jobItemManager.getProgress(jobId, i).isPresent()) {
                    continue;
                }
                IncrementalDumperContext dumperContext = buildDumperContext(jobConfig, i, new TableAndSchemaNameMapper(jobConfig.getSchemaTableNames()));
                TransmissionJobItemProgress jobItemProgress = getTransmissionJobItemProgress(jobConfig, pipelineDataSourceManager, dumperContext);
                PipelineAPIFactory.getPipelineGovernanceFacade(PipelineJobIdUtils.parseContextKey(jobId)).getJobItemFacade().getProcess().persist(
                        jobId, i, YamlEngine.marshal(jobType.getYamlJobItemProgressSwapper().swapToYamlConfiguration(jobItemProgress)));
            }
        } catch (final SQLException ex) {
            throw new PrepareJobWithGetBinlogPositionException(jobId, ex);
        }
    }
    
    private IncrementalDumperContext buildDumperContext(final CDCJobConfiguration jobConfig, final int jobShardingItem, final TableAndSchemaNameMapper tableAndSchemaNameMapper) {
        JobDataNodeLine dataNodeLine = jobConfig.getJobShardingDataNodes().get(jobShardingItem);
        String dataSourceName = dataNodeLine.getEntries().iterator().next().getDataNodes().iterator().next().getDataSourceName();
        StandardPipelineDataSourceConfiguration actualDataSourceConfig = jobConfig.getDataSourceConfig().getActualDataSourceConfiguration(dataSourceName);
        return new IncrementalDumperContext(
                new DumperCommonContext(dataSourceName, actualDataSourceConfig, JobDataNodeLineConvertUtils.buildTableNameMapper(dataNodeLine), tableAndSchemaNameMapper),
                jobConfig.getJobId(), jobConfig.isDecodeWithTX());
    }
    
    private static TransmissionJobItemProgress getTransmissionJobItemProgress(final CDCJobConfiguration jobConfig,
                                                                              final PipelineDataSourceManager dataSourceManager,
                                                                              final IncrementalDumperContext incrementalDumperContext) throws SQLException {
        TransmissionJobItemProgress result = new TransmissionJobItemProgress();
        result.setSourceDatabaseType(jobConfig.getSourceDatabaseType());
        result.setDataSourceName(incrementalDumperContext.getCommonContext().getDataSourceName());
        IncrementalTaskProgress incrementalTaskProgress = new IncrementalTaskProgress(PipelineJobPreparerUtils.getIncrementalPosition(null, incrementalDumperContext, dataSourceManager));
        result.setIncremental(new JobItemIncrementalTasksProgress(incrementalTaskProgress));
        return result;
    }
    
    /**
     * Start CDC job.
     *
     * @param jobId job id
     * @param sink sink
     */
    public void start(final String jobId, final PipelineSink sink) {
        CDCJob job = new CDCJob(jobId, sink);
        PipelineJobCenter.addJob(jobId, job);
        enable(jobId);
        JobConfigurationPOJO jobConfigPOJO = PipelineJobIdUtils.getElasticJobConfigurationPOJO(jobId);
        OneOffJobBootstrap oneOffJobBootstrap = new OneOffJobBootstrap(PipelineAPIFactory.getRegistryCenter(PipelineJobIdUtils.parseContextKey(jobId)), job, jobConfigPOJO.toJobConfiguration());
        job.setJobBootstrap(oneOffJobBootstrap);
        oneOffJobBootstrap.execute();
    }
    
    private void enable(final String jobId) {
        JobConfigurationPOJO jobConfigPOJO = PipelineJobIdUtils.getElasticJobConfigurationPOJO(jobId);
        jobConfigPOJO.setDisabled(false);
        jobConfigPOJO.getProps().setProperty("start_time_millis", String.valueOf(System.currentTimeMillis()));
        jobConfigPOJO.getProps().remove("stop_time_millis");
        PipelineAPIFactory.getJobConfigurationAPI(PipelineJobIdUtils.parseContextKey(jobConfigPOJO.getJobName())).updateJobConfiguration(jobConfigPOJO);
    }
    
    /**
     * Disable CDC job.
     *
     * @param jobId job id
     */
    public void disable(final String jobId) {
        JobConfigurationPOJO jobConfigPOJO = PipelineJobIdUtils.getElasticJobConfigurationPOJO(jobId);
        jobConfigPOJO.setDisabled(true);
        jobConfigPOJO.getProps().setProperty("stop_time", LocalDateTime.now().format(DateTimeFormatterFactory.getStandardFormatter()));
        jobConfigPOJO.getProps().setProperty("stop_time_millis", String.valueOf(System.currentTimeMillis()));
        PipelineAPIFactory.getJobConfigurationAPI(PipelineJobIdUtils.parseContextKey(jobConfigPOJO.getJobName())).updateJobConfiguration(jobConfigPOJO);
    }
    
    /**
     * Drop CDC job.
     *
     * @param jobId job id
     */
    public void drop(final String jobId) {
        CDCJobConfiguration jobConfig = jobConfigManager.getJobConfiguration(jobId);
        ShardingSpherePreconditions.checkState(PipelineJobIdUtils.getElasticJobConfigurationPOJO(jobId).isDisabled(), () -> new PipelineInternalException("Can't drop streaming job which is active"));
        jobManager.drop(jobId);
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
    public void commit(final String jobId) throws SQLException {
    }
    
    @Override
    public void rollback(final String jobId) throws SQLException {
    }
    
    @Override
    public String getType() {
        return "STREAMING";
    }
}
