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
import org.apache.shardingsphere.data.pipeline.cdc.config.CDCJobConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.config.yaml.config.YamlCDCJobConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.config.yaml.config.YamlCDCJobConfiguration.YamlSinkConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.config.yaml.swapper.YamlCDCJobConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.cdc.constant.CDCSinkType;
import org.apache.shardingsphere.data.pipeline.cdc.core.pojo.CDCJobItemInfo;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContextManager;
import org.apache.shardingsphere.data.pipeline.core.datanode.JobDataNodeEntry;
import org.apache.shardingsphere.data.pipeline.core.datanode.JobDataNodeLine;
import org.apache.shardingsphere.data.pipeline.core.datanode.JobDataNodeLineConvertUtils;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.datasource.config.PipelineDataSourceConfigurationFactory;
import org.apache.shardingsphere.data.pipeline.core.datasource.yaml.swapper.YamlPipelineDataSourceConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineInternalException;
import org.apache.shardingsphere.data.pipeline.core.exception.job.PipelineJobCreationWithInvalidShardingCountException;
import org.apache.shardingsphere.data.pipeline.core.exception.job.PrepareJobWithGetBinlogPositionException;
import org.apache.shardingsphere.data.pipeline.core.importer.sink.PipelineSink;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.DumperCommonContext;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.incremental.IncrementalDumperContext;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.mapper.TableAndSchemaNameMapper;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobRegistry;
import org.apache.shardingsphere.data.pipeline.core.job.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.job.api.TransmissionJobAPI;
import org.apache.shardingsphere.data.pipeline.core.job.id.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.job.progress.JobItemIncrementalTasksProgress;
import org.apache.shardingsphere.data.pipeline.core.job.progress.TransmissionJobItemProgress;
import org.apache.shardingsphere.data.pipeline.core.job.progress.yaml.swapper.YamlPipelineJobItemProgressSwapper;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobConfigurationManager;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobItemManager;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobManager;
import org.apache.shardingsphere.data.pipeline.core.job.service.TransmissionJobManager;
import org.apache.shardingsphere.data.pipeline.core.pojo.TransmissionJobItemInfo;
import org.apache.shardingsphere.data.pipeline.core.preparer.incremental.IncrementalTaskPositionManager;
import org.apache.shardingsphere.data.pipeline.core.registrycenter.repository.PipelineGovernanceFacade;
import org.apache.shardingsphere.data.pipeline.core.sqlbuilder.dialect.DialectPipelineSQLBuilder;
import org.apache.shardingsphere.data.pipeline.core.task.progress.IncrementalTaskProgress;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.OneOffJobBootstrap;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.util.datetime.DateTimeFormatterFactory;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.resource.YamlDataSourceConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapperEngine;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
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
        jobConfigManager = new PipelineJobConfigurationManager(jobType.getOption());
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
            governanceFacade.getJobFacade().getJob().create(jobConfig.getJobId(), jobType.getOption().getJobClass());
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
        List<String> schemaTableNames = param.getSchemaTableNames();
        Collections.sort(schemaTableNames);
        result.setJobId(PipelineJobIdUtils.marshal(new CDCJobId(contextKey, schemaTableNames, param.isFull(), sinkType)));
        result.setDatabaseName(param.getDatabaseName());
        result.setSchemaTableNames(schemaTableNames);
        result.setFull(param.isFull());
        result.setDecodeWithTX(param.isDecodeWithTransaction());
        YamlSinkConfiguration sinkConfig = new YamlSinkConfiguration();
        sinkConfig.setSinkType(sinkType.name());
        sinkConfig.setProps(sinkProps);
        result.setSinkConfig(sinkConfig);
        ShardingSphereDatabase database = PipelineContextManager.getContext(contextKey).getMetaDataContexts().getMetaData().getDatabase(param.getDatabaseName());
        result.setDataSourceConfiguration(pipelineDataSourceConfigSwapper.swapToYamlConfiguration(getDataSourceConfiguration(database)));
        List<JobDataNodeLine> jobDataNodeLines = JobDataNodeLineConvertUtils.convertDataNodesToLines(param.getTableAndDataNodesMap());
        result.setJobShardingDataNodes(jobDataNodeLines.stream().map(JobDataNodeLine::marshal).collect(Collectors.toList()));
        JobDataNodeLine tableFirstDataNodes = new JobDataNodeLine(param.getTableAndDataNodesMap().entrySet().stream()
                .map(entry -> new JobDataNodeEntry(entry.getKey(), entry.getValue().subList(0, 1))).collect(Collectors.toList()));
        result.setTablesFirstDataNodes(tableFirstDataNodes.marshal());
        result.setSourceDatabaseType(PipelineDataSourceConfigurationFactory.newInstance(
                result.getDataSourceConfiguration().getType(), result.getDataSourceConfiguration().getParameter()).getDatabaseType().getType());
        return result;
    }
    
    private ShardingSpherePipelineDataSourceConfiguration getDataSourceConfiguration(final ShardingSphereDatabase database) {
        Map<String, Map<String, Object>> dataSourcePoolProps = new HashMap<>(database.getResourceMetaData().getStorageUnits().size(), 1F);
        for (Entry<String, StorageUnit> entry : database.getResourceMetaData().getStorageUnits().entrySet()) {
            dataSourcePoolProps.put(entry.getKey(), dataSourceConfigSwapper.swapToMap(entry.getValue().getDataSourcePoolProperties()));
        }
        YamlRootConfiguration targetRootConfig = new YamlRootConfiguration();
        targetRootConfig.setDatabaseName(database.getName());
        targetRootConfig.setDataSources(dataSourcePoolProps);
        targetRootConfig.setRules(ruleConfigSwapperEngine.swapToYamlRuleConfigurations(database.getRuleMetaData().getConfigurations()));
        return new ShardingSpherePipelineDataSourceConfiguration(targetRootConfig);
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void initIncrementalPosition(final CDCJobConfiguration jobConfig) {
        String jobId = jobConfig.getJobId();
        PipelineJobItemManager<TransmissionJobItemProgress> jobItemManager = new PipelineJobItemManager<>(jobType.getOption().getYamlJobItemProgressSwapper());
        try (PipelineDataSourceManager pipelineDataSourceManager = new PipelineDataSourceManager()) {
            for (int i = 0; i < jobConfig.getJobShardingCount(); i++) {
                if (jobItemManager.getProgress(jobId, i).isPresent()) {
                    continue;
                }
                IncrementalDumperContext dumperContext = buildDumperContext(jobConfig, i, new TableAndSchemaNameMapper(jobConfig.getSchemaTableNames()));
                TransmissionJobItemProgress jobItemProgress = getTransmissionJobItemProgress(jobConfig, pipelineDataSourceManager, dumperContext);
                YamlPipelineJobItemProgressSwapper swapper = jobType.getOption().getYamlJobItemProgressSwapper();
                PipelineAPIFactory.getPipelineGovernanceFacade(
                        PipelineJobIdUtils.parseContextKey(jobId)).getJobItemFacade().getProcess().persist(jobId, i, YamlEngine.marshal(swapper.swapToYamlConfiguration(jobItemProgress)));
            }
        } catch (final SQLException ex) {
            throw new PrepareJobWithGetBinlogPositionException(jobId, ex);
        }
    }
    
    private IncrementalDumperContext buildDumperContext(final CDCJobConfiguration jobConfig, final int jobShardingItem, final TableAndSchemaNameMapper tableAndSchemaNameMapper) {
        JobDataNodeLine dataNodeLine = jobConfig.getJobShardingDataNodes().get(jobShardingItem);
        String dataSourceName = dataNodeLine.getEntries().iterator().next().getDataNodes().iterator().next().getDataSourceName();
        StandardPipelineDataSourceConfiguration actualDataSourceConfig = jobConfig.getDataSourceConfig().getActualDataSourceConfiguration(dataSourceName);
        return new IncrementalDumperContext(new DumperCommonContext(dataSourceName, actualDataSourceConfig, JobDataNodeLineConvertUtils.buildTableNameMapper(dataNodeLine), tableAndSchemaNameMapper),
                jobConfig.getJobId(), jobConfig.isDecodeWithTX());
    }
    
    private TransmissionJobItemProgress getTransmissionJobItemProgress(final CDCJobConfiguration jobConfig, final PipelineDataSourceManager dataSourceManager,
                                                                       final IncrementalDumperContext incrementalDumperContext) throws SQLException {
        TransmissionJobItemProgress result = new TransmissionJobItemProgress();
        result.setSourceDatabaseType(jobConfig.getSourceDatabaseType());
        result.setDataSourceName(incrementalDumperContext.getCommonContext().getDataSourceName());
        IncrementalTaskPositionManager positionManager = new IncrementalTaskPositionManager(incrementalDumperContext.getCommonContext().getDataSourceConfig().getDatabaseType());
        IncrementalTaskProgress incrementalTaskProgress = new IncrementalTaskProgress(positionManager.getPosition(null, incrementalDumperContext, dataSourceManager));
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
        CDCJob job = new CDCJob(sink);
        PipelineJobRegistry.add(jobId, job);
        enable(jobId);
        JobConfigurationPOJO jobConfigPOJO = PipelineJobIdUtils.getElasticJobConfigurationPOJO(jobId);
        OneOffJobBootstrap oneOffJobBootstrap = new OneOffJobBootstrap(PipelineAPIFactory.getRegistryCenter(PipelineJobIdUtils.parseContextKey(jobId)), job, jobConfigPOJO.toJobConfiguration());
        job.getJobRunnerManager().setJobBootstrap(oneOffJobBootstrap);
        oneOffJobBootstrap.execute();
    }
    
    private void enable(final String jobId) {
        JobConfigurationPOJO jobConfigPOJO = PipelineJobIdUtils.getElasticJobConfigurationPOJO(jobId);
        jobConfigPOJO.setDisabled(false);
        jobConfigPOJO.getProps().setProperty("start_time_millis", String.valueOf(System.currentTimeMillis()));
        jobConfigPOJO.getProps().remove("stop_time");
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
        jobConfigPOJO.getProps().setProperty("stop_time", LocalDateTime.now().format(DateTimeFormatterFactory.getDatetimeFormatter()));
        PipelineAPIFactory.getJobConfigurationAPI(PipelineJobIdUtils.parseContextKey(jobConfigPOJO.getJobName())).updateJobConfiguration(jobConfigPOJO);
    }
    
    /**
     * Drop CDC job.
     *
     * @param jobId job id
     */
    public void drop(final String jobId) {
        CDCJobConfiguration jobConfig = jobConfigManager.getJobConfiguration(jobId);
        jobManager.drop(jobId);
        cleanup(jobConfig);
    }
    
    private void cleanup(final CDCJobConfiguration jobConfig) {
        for (Entry<String, Map<String, Object>> entry : jobConfig.getDataSourceConfig().getRootConfig().getDataSources().entrySet()) {
            try {
                StandardPipelineDataSourceConfiguration pipelineDataSourceConfig = new StandardPipelineDataSourceConfiguration(entry.getValue());
                new IncrementalTaskPositionManager(pipelineDataSourceConfig.getDatabaseType()).destroyPosition(jobConfig.getJobId(), pipelineDataSourceConfig);
            } catch (final SQLException ex) {
                log.warn("job destroying failed, jobId={}, dataSourceName={}", jobConfig.getJobId(), entry.getKey(), ex);
            }
        }
    }
    
    /**
     * Get job item infos.
     *
     * @param jobId job id
     * @return job item infos
     */
    public Collection<CDCJobItemInfo> getJobItemInfos(final String jobId) {
        CDCJobConfiguration jobConfig = new PipelineJobConfigurationManager(jobType.getOption()).getJobConfiguration(jobId);
        ShardingSphereDatabase database = PipelineContextManager.getProxyContext().getMetaDataContexts().getMetaData().getDatabase(jobConfig.getDatabaseName());
        Collection<CDCJobItemInfo> result = new LinkedList<>();
        for (TransmissionJobItemInfo each : new TransmissionJobManager(jobType).getJobItemInfos(jobId)) {
            TransmissionJobItemProgress jobItemProgress = each.getJobItemProgress();
            String confirmedPosition = null == jobItemProgress ? "" : jobItemProgress.getIncremental().getIncrementalPosition().map(Object::toString).orElse("");
            String currentPosition = null == jobItemProgress ? "" : getCurrentPosition(database, jobItemProgress.getDataSourceName());
            result.add(new CDCJobItemInfo(each, confirmedPosition, currentPosition));
        }
        return result;
    }
    
    private String getCurrentPosition(final ShardingSphereDatabase database, final String dataSourceName) {
        StorageUnit storageUnit = database.getResourceMetaData().getStorageUnits().get(dataSourceName);
        DialectPipelineSQLBuilder sqlBuilder = DatabaseTypedSPILoader.getService(DialectPipelineSQLBuilder.class, storageUnit.getStorageType());
        Optional<String> queryCurrentPositionSQL = sqlBuilder.buildQueryCurrentPositionSQL();
        if (!queryCurrentPositionSQL.isPresent()) {
            return "";
        }
        try (
                Connection connection = storageUnit.getDataSource().getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(queryCurrentPositionSQL.get())) {
            resultSet.next();
            return resultSet.getString(1);
        } catch (final SQLException ex) {
            throw new PipelineInternalException(ex);
        }
    }
    
    @Override
    public void commit(final String jobId) {
    }
    
    @Override
    public void rollback(final String jobId) {
    }
    
    @Override
    public String getType() {
        return "STREAMING";
    }
}
