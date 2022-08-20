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

package org.apache.shardingsphere.data.pipeline.scenario.migration;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCheckResult;
import org.apache.shardingsphere.data.pipeline.api.config.ImporterConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.TableNameSchemaNameMapping;
import org.apache.shardingsphere.data.pipeline.api.config.TaskConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.ingest.DumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.job.MigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.job.PipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.job.yaml.YamlMigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.job.yaml.YamlMigrationJobConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.api.config.job.yaml.YamlPipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.context.PipelineJobItemContext;
import org.apache.shardingsphere.data.pipeline.api.datanode.JobDataNodeEntry;
import org.apache.shardingsphere.data.pipeline.api.datanode.JobDataNodeLine;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfigurationFactory;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.ShardingSpherePipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.yaml.YamlPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.yaml.YamlPipelineDataSourceConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.api.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.api.job.JobType;
import org.apache.shardingsphere.data.pipeline.api.job.PipelineJobId;
import org.apache.shardingsphere.data.pipeline.api.job.progress.InventoryIncrementalJobItemProgress;
import org.apache.shardingsphere.data.pipeline.api.metadata.ActualTableName;
import org.apache.shardingsphere.data.pipeline.api.metadata.LogicTableName;
import org.apache.shardingsphere.data.pipeline.api.pojo.CreateMigrationJobParameter;
import org.apache.shardingsphere.data.pipeline.api.pojo.DataConsistencyCheckAlgorithmInfo;
import org.apache.shardingsphere.data.pipeline.api.pojo.MigrationJobInfo;
import org.apache.shardingsphere.data.pipeline.api.pojo.PipelineJobInfo;
import org.apache.shardingsphere.data.pipeline.core.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.api.PipelineJobItemAPI;
import org.apache.shardingsphere.data.pipeline.core.api.impl.AbstractPipelineJobAPIImpl;
import org.apache.shardingsphere.data.pipeline.core.api.impl.InventoryIncrementalJobItemAPIImpl;
import org.apache.shardingsphere.data.pipeline.core.api.impl.PipelineDataSourcePersistService;
import org.apache.shardingsphere.data.pipeline.core.check.consistency.DataConsistencyCalculateAlgorithmFactory;
import org.apache.shardingsphere.data.pipeline.core.check.consistency.DataConsistencyChecker;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContext;
import org.apache.shardingsphere.data.pipeline.core.exception.AddMigrationSourceResourceException;
import org.apache.shardingsphere.data.pipeline.core.exception.DropMigrationSourceResourceException;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineVerifyFailedException;
import org.apache.shardingsphere.data.pipeline.core.job.progress.PipelineJobProgressDetector;
import org.apache.shardingsphere.data.pipeline.core.util.SchemaTableUtil;
import org.apache.shardingsphere.data.pipeline.spi.check.consistency.DataConsistencyCalculateAlgorithm;
import org.apache.shardingsphere.data.pipeline.spi.ratelimit.JobRateLimitAlgorithm;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.infra.config.rule.data.pipeline.PipelineProcessConfiguration;
import org.apache.shardingsphere.infra.config.rule.data.pipeline.PipelineReadConfiguration;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.datasource.props.DataSourcePropertiesCreator;
import org.apache.shardingsphere.infra.lock.LockContext;
import org.apache.shardingsphere.infra.lock.LockDefinition;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.data.pipeline.YamlPipelineReadConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.resource.YamlDataSourceConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.data.pipeline.YamlPipelineReadConfigurationSwapper;
import org.apache.shardingsphere.mode.lock.ExclusiveLockDefinition;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Migration job API impl.
 */
@Slf4j
public final class MigrationJobAPIImpl extends AbstractPipelineJobAPIImpl implements MigrationJobAPI {
    
    private static final YamlDataSourceConfigurationSwapper DATA_SOURCE_CONFIG_SWAPPER = new YamlDataSourceConfigurationSwapper();
    
    private static final YamlPipelineDataSourceConfigurationSwapper PIPELINE_DATA_SOURCE_CONFIG_SWAPPER = new YamlPipelineDataSourceConfigurationSwapper();
    
    private final PipelineJobItemAPI jobItemAPI = new InventoryIncrementalJobItemAPIImpl();
    
    private final PipelineDataSourcePersistService dataSourcePersistService = new PipelineDataSourcePersistService();
    
    @Override
    protected JobType getJobType() {
        return JobType.MIGRATION;
    }
    
    @Override
    protected String marshalJobIdLeftPart(final PipelineJobId pipelineJobId) {
        MigrationJobId jobId = (MigrationJobId) pipelineJobId;
        String text = jobId.getDatabaseName() + "|" + jobId.getTableName() + "|" + jobId.getSourceDataSourceName();
        return DigestUtils.md5Hex(text.getBytes(StandardCharsets.UTF_8));
    }
    
    @Override
    public void extendYamlJobConfiguration(final YamlPipelineJobConfiguration yamlJobConfig) {
        YamlMigrationJobConfiguration config = (YamlMigrationJobConfiguration) yamlJobConfig;
        if (null == yamlJobConfig.getJobId()) {
            config.setJobId(generateJobId(config));
        }
        if (Strings.isNullOrEmpty(config.getSourceDatabaseType())) {
            PipelineDataSourceConfiguration sourceDataSourceConfig = PipelineDataSourceConfigurationFactory.newInstance(config.getSource().getType(), config.getSource().getParameter());
            config.setSourceDatabaseType(sourceDataSourceConfig.getDatabaseType().getType());
        }
        if (Strings.isNullOrEmpty(config.getTargetDatabaseType())) {
            PipelineDataSourceConfiguration targetDataSourceConfig = PipelineDataSourceConfigurationFactory.newInstance(config.getTarget().getType(), config.getTarget().getParameter());
            config.setTargetDatabaseType(targetDataSourceConfig.getDatabaseType().getType());
        }
        JobDataNodeEntry nodeEntry = new JobDataNodeEntry(config.getSourceTableName(),
                Collections.singletonList(new DataNode(config.getSourceDataSourceName(), config.getSourceTableName())));
        String dataNodeLine = new JobDataNodeLine(Collections.singletonList(nodeEntry)).marshal();
        config.setTablesFirstDataNodes(dataNodeLine);
        config.setJobShardingDataNodes(Collections.singletonList(dataNodeLine));
    }
    
    private String generateJobId(final YamlMigrationJobConfiguration config) {
        MigrationJobId jobId = new MigrationJobId();
        jobId.setTypeCode(getJobType().getTypeCode());
        jobId.setSourceDataSourceName(config.getSourceDataSourceName());
        jobId.setTableName(config.getSourceTableName());
        jobId.setDatabaseName(config.getTargetDatabaseName());
        return marshalJobId(jobId);
    }
    
    @Override
    protected YamlPipelineJobConfiguration swapToYamlJobConfiguration(final PipelineJobConfiguration jobConfig) {
        return new YamlMigrationJobConfigurationSwapper().swapToYamlConfiguration((MigrationJobConfiguration) jobConfig);
    }
    
    @Override
    public MigrationJobConfiguration getJobConfiguration(final String jobId) {
        return getJobConfiguration(getElasticJobConfigPOJO(jobId));
    }
    
    private MigrationJobConfiguration getJobConfiguration(final JobConfigurationPOJO jobConfigPOJO) {
        return YamlMigrationJobConfigurationSwapper.swapToObject(jobConfigPOJO.getJobParameter());
    }
    
    @Override
    public TaskConfiguration buildTaskConfiguration(final MigrationJobConfiguration jobConfig, final int jobShardingItem, final PipelineProcessConfiguration pipelineProcessConfig) {
        Map<ActualTableName, LogicTableName> tableNameMap = new LinkedHashMap<>();
        tableNameMap.put(new ActualTableName(jobConfig.getSourceTableName()), new LogicTableName(jobConfig.getSourceTableName()));
        Map<LogicTableName, String> tableNameSchemaMap = TableNameSchemaNameMapping.convert(SchemaTableUtil.getSchemaTablesMapFromActual(jobConfig.getSource(), jobConfig.getSourceTableName()));
        TableNameSchemaNameMapping tableNameSchemaNameMapping = new TableNameSchemaNameMapping(tableNameSchemaMap);
        DumperConfiguration dumperConfig = createDumperConfiguration(jobConfig.getJobId(), jobConfig.getSourceDataSourceName(), jobConfig.getSource(), tableNameMap, tableNameSchemaNameMapping);
        // TODO now shardingColumnsMap always empty, 
        ImporterConfiguration importerConfig = createImporterConfiguration(jobConfig, pipelineProcessConfig, Collections.emptyMap(), tableNameSchemaNameMapping);
        TaskConfiguration result = new TaskConfiguration(dumperConfig, importerConfig);
        log.info("createTaskConfiguration, dataSourceName={}, result={}", jobConfig.getSourceDataSourceName(), result);
        return result;
    }
    
    private static DumperConfiguration createDumperConfiguration(final String jobId, final String dataSourceName, final PipelineDataSourceConfiguration sourceDataSource,
                                                                 final Map<ActualTableName, LogicTableName> tableNameMap, final TableNameSchemaNameMapping tableNameSchemaNameMapping) {
        DumperConfiguration result = new DumperConfiguration();
        result.setJobId(jobId);
        result.setDataSourceName(dataSourceName);
        result.setDataSourceConfig(sourceDataSource);
        result.setTableNameMap(tableNameMap);
        result.setTableNameSchemaNameMapping(tableNameSchemaNameMapping);
        return result;
    }
    
    private static ImporterConfiguration createImporterConfiguration(final MigrationJobConfiguration jobConfig, final PipelineProcessConfiguration pipelineProcessConfig,
                                                                     final Map<LogicTableName, Set<String>> shardingColumnsMap, final TableNameSchemaNameMapping tableNameSchemaNameMapping) {
        PipelineDataSourceConfiguration dataSourceConfig = PipelineDataSourceConfigurationFactory.newInstance(jobConfig.getTarget().getType(), jobConfig.getTarget().getParameter());
        int batchSize = pipelineProcessConfig.getWrite().getBatchSize();
        int retryTimes = jobConfig.getRetryTimes();
        int concurrency = jobConfig.getConcurrency();
        return new ImporterConfiguration(dataSourceConfig, unmodifiable(shardingColumnsMap), tableNameSchemaNameMapping, batchSize, retryTimes, concurrency);
    }
    
    private static Map<LogicTableName, Set<String>> unmodifiable(final Map<LogicTableName, Set<String>> shardingColumnsMap) {
        Map<LogicTableName, Set<String>> result = new HashMap<>(shardingColumnsMap.size());
        for (Entry<LogicTableName, Set<String>> entry : shardingColumnsMap.entrySet()) {
            result.put(entry.getKey(), Collections.unmodifiableSet(entry.getValue()));
        }
        return Collections.unmodifiableMap(result);
    }
    
    @Override
    public MigrationProcessContext buildPipelineProcessContext(final PipelineJobConfiguration pipelineJobConfig) {
        // TODO cache process config on local
        PipelineProcessConfiguration processConfig = showProcessConfiguration();
        return new MigrationProcessContext(pipelineJobConfig.getJobId(), processConfig);
    }
    
    protected PipelineJobInfo getJobInfo(final String jobName) {
        MigrationJobInfo result = new MigrationJobInfo(jobName);
        JobConfigurationPOJO jobConfigPOJO = getElasticJobConfigPOJO(result.getJobId());
        MigrationJobConfiguration jobConfig = getJobConfiguration(jobConfigPOJO);
        result.setActive(!jobConfigPOJO.isDisabled());
        result.setShardingTotalCount(jobConfig.getJobShardingCount());
        result.setTables(jobConfig.getSourceTableName());
        result.setCreateTime(jobConfigPOJO.getProps().getProperty("create_time"));
        result.setStopTime(jobConfigPOJO.getProps().getProperty("stop_time"));
        result.setJobParameter(jobConfigPOJO.getJobParameter());
        return result;
    }
    
    @Override
    public Map<Integer, InventoryIncrementalJobItemProgress> getJobProgress(final String jobId) {
        checkModeConfig();
        return getJobProgress(getJobConfiguration(jobId));
    }
    
    @Override
    public Map<Integer, InventoryIncrementalJobItemProgress> getJobProgress(final MigrationJobConfiguration jobConfig) {
        String jobId = jobConfig.getJobId();
        JobConfigurationPOJO jobConfigPOJO = getElasticJobConfigPOJO(jobId);
        return IntStream.range(0, jobConfig.getJobShardingCount()).boxed().collect(LinkedHashMap::new, (map, each) -> {
            InventoryIncrementalJobItemProgress jobItemProgress = getJobItemProgress(jobId, each);
            if (null != jobItemProgress) {
                jobItemProgress.setActive(!jobConfigPOJO.isDisabled());
            }
            map.put(each, jobItemProgress);
        }, LinkedHashMap::putAll);
    }
    
    @Override
    public InventoryIncrementalJobItemProgress getJobItemProgress(final String jobId, final int shardingItem) {
        return (InventoryIncrementalJobItemProgress) jobItemAPI.getJobItemProgress(jobId, shardingItem);
    }
    
    @Override
    public void persistJobItemProgress(final PipelineJobItemContext jobItemContext) {
        jobItemAPI.persistJobItemProgress(jobItemContext);
    }
    
    @Override
    public void updateJobItemStatus(final String jobId, final int shardingItem, final JobStatus status) {
        jobItemAPI.updateJobItemStatus(jobId, shardingItem, status);
    }
    
    private void verifyManualMode(final MigrationJobConfiguration jobConfig) {
        MigrationProcessContext processContext = buildPipelineProcessContext(jobConfig);
        if (null != processContext.getCompletionDetectAlgorithm()) {
            throw new PipelineVerifyFailedException("It's not necessary to do it in auto mode.");
        }
    }
    
    private void verifyJobNotCompleted(final MigrationJobConfiguration jobConfig) {
        if (PipelineJobProgressDetector.isJobCompleted(jobConfig.getJobShardingCount(), getJobProgress(jobConfig).values())) {
            throw new PipelineVerifyFailedException("Job is completed, it's not necessary to do it.");
        }
    }
    
    @Override
    public void stopClusterWriteDB(final String jobId) {
        checkModeConfig();
        log.info("stopClusterWriteDB for job {}", jobId);
        JobConfigurationPOJO jobConfigPOJO = getElasticJobConfigPOJO(jobId);
        MigrationJobConfiguration jobConfig = getJobConfiguration(jobConfigPOJO);
        verifyManualMode(jobConfig);
        verifyJobNotStopped(jobConfigPOJO);
        verifyJobNotCompleted(jobConfig);
        stopClusterWriteDB(jobConfig);
    }
    
    @Override
    public void stopClusterWriteDB(final MigrationJobConfiguration jobConfig) {
        String databaseName = jobConfig.getTargetDatabaseName();
        LockContext lockContext = PipelineContext.getContextManager().getInstanceContext().getLockContext();
        LockDefinition lockDefinition = new ExclusiveLockDefinition(databaseName);
        if (lockContext.isLocked(lockDefinition)) {
            log.info("stopClusterWriteDB, already stopped");
            return;
        }
        if (lockContext.tryLock(lockDefinition)) {
            log.info("stopClusterWriteDB, tryLockSuccess=true");
            return;
        }
        throw new RuntimeException("Stop source writing failed");
    }
    
    @Override
    public void restoreClusterWriteDB(final String jobId) {
        checkModeConfig();
        log.info("restoreClusterWriteDB for job {}", jobId);
        JobConfigurationPOJO jobConfigPOJO = getElasticJobConfigPOJO(jobId);
        MigrationJobConfiguration jobConfig = getJobConfiguration(jobConfigPOJO);
        verifyManualMode(jobConfig);
        restoreClusterWriteDB(jobConfig);
    }
    
    @Override
    public void restoreClusterWriteDB(final MigrationJobConfiguration jobConfig) {
        String databaseName = jobConfig.getTargetDatabaseName();
        LockContext lockContext = PipelineContext.getContextManager().getInstanceContext().getLockContext();
        LockDefinition lockDefinition = new ExclusiveLockDefinition(databaseName);
        if (lockContext.isLocked(lockDefinition)) {
            log.info("restoreClusterWriteDB, before unlock, databaseName={}, jobId={}", databaseName, jobConfig.getJobId());
            lockContext.unlock(lockDefinition);
            return;
        }
        log.info("restoreClusterWriteDB, isLocked false, databaseName={}", databaseName);
    }
    
    @Override
    public Collection<DataConsistencyCheckAlgorithmInfo> listDataConsistencyCheckAlgorithms() {
        checkModeConfig();
        return DataConsistencyCalculateAlgorithmFactory.getAllInstances().stream().map(each -> {
            DataConsistencyCheckAlgorithmInfo result = new DataConsistencyCheckAlgorithmInfo();
            result.setType(each.getType());
            result.setDescription(each.getDescription());
            result.setSupportedDatabaseTypes(each.getSupportedDatabaseTypes());
            return result;
        }).collect(Collectors.toList());
    }
    
    @Override
    public boolean isDataConsistencyCheckNeeded(final String jobId) {
        log.info("isDataConsistencyCheckNeeded for job {}", jobId);
        MigrationJobConfiguration jobConfig = getJobConfiguration(jobId);
        return isDataConsistencyCheckNeeded(jobConfig);
    }
    
    @Override
    public boolean isDataConsistencyCheckNeeded(final MigrationJobConfiguration jobConfig) {
        MigrationProcessContext processContext = buildPipelineProcessContext(jobConfig);
        return isDataConsistencyCheckNeeded(processContext);
    }
    
    private boolean isDataConsistencyCheckNeeded(final MigrationProcessContext processContext) {
        return null != processContext.getDataConsistencyCalculateAlgorithm();
    }
    
    @Override
    public Map<String, DataConsistencyCheckResult> dataConsistencyCheck(final String jobId) {
        checkModeConfig();
        log.info("Data consistency check for job {}", jobId);
        MigrationJobConfiguration jobConfig = getJobConfiguration(getElasticJobConfigPOJO(jobId));
        verifyDataConsistencyCheck(jobConfig);
        return dataConsistencyCheck(jobConfig);
    }
    
    @Override
    public Map<String, DataConsistencyCheckResult> dataConsistencyCheck(final MigrationJobConfiguration jobConfig) {
        MigrationProcessContext processContext = buildPipelineProcessContext(jobConfig);
        if (!isDataConsistencyCheckNeeded(processContext)) {
            log.info("DataConsistencyCalculatorAlgorithm is not configured, data consistency check is ignored.");
            return Collections.emptyMap();
        }
        return dataConsistencyCheck(jobConfig, processContext.getDataConsistencyCalculateAlgorithm());
    }
    
    @Override
    public Map<String, DataConsistencyCheckResult> dataConsistencyCheck(final String jobId, final String algorithmType, final Properties algorithmProps) {
        checkModeConfig();
        log.info("Data consistency check for job {}, algorithmType: {}", jobId, algorithmType);
        MigrationJobConfiguration jobConfig = getJobConfiguration(getElasticJobConfigPOJO(jobId));
        verifyDataConsistencyCheck(jobConfig);
        return dataConsistencyCheck(jobConfig, DataConsistencyCalculateAlgorithmFactory.newInstance(algorithmType, algorithmProps));
    }
    
    private Map<String, DataConsistencyCheckResult> dataConsistencyCheck(final MigrationJobConfiguration jobConfig, final DataConsistencyCalculateAlgorithm calculator) {
        String jobId = jobConfig.getJobId();
        JobRateLimitAlgorithm readRateLimitAlgorithm = buildPipelineProcessContext(jobConfig).getReadRateLimitAlgorithm();
        Map<String, DataConsistencyCheckResult> result = new DataConsistencyChecker(jobConfig, readRateLimitAlgorithm).check(calculator);
        log.info("Scaling job {} with check algorithm '{}' data consistency checker result {}", jobId, calculator.getType(), result);
        PipelineAPIFactory.getGovernanceRepositoryAPI().persistJobCheckResult(jobId, aggregateDataConsistencyCheckResults(jobId, result));
        return result;
    }
    
    private void verifyDataConsistencyCheck(final MigrationJobConfiguration jobConfig) {
        verifyManualMode(jobConfig);
    }
    
    @Override
    public boolean aggregateDataConsistencyCheckResults(final String jobId, final Map<String, DataConsistencyCheckResult> checkResults) {
        if (checkResults.isEmpty()) {
            return false;
        }
        for (Entry<String, DataConsistencyCheckResult> entry : checkResults.entrySet()) {
            DataConsistencyCheckResult checkResult = entry.getValue();
            boolean isCountMatched = checkResult.getCountCheckResult().isMatched();
            boolean isContentMatched = checkResult.getContentCheckResult().isMatched();
            if (!isCountMatched || !isContentMatched) {
                log.error("Scaling job: {}, table: {} data consistency check failed, count matched: {}, content matched: {}", jobId, entry.getKey(), isCountMatched, isContentMatched);
                return false;
            }
        }
        return true;
    }
    
    @Override
    public void switchClusterConfiguration(final String jobId) {
        checkModeConfig();
        log.info("Switch cluster configuration for job {}", jobId);
        JobConfigurationPOJO jobConfigPOJO = getElasticJobConfigPOJO(jobId);
        MigrationJobConfiguration jobConfig = getJobConfiguration(jobConfigPOJO);
        verifyManualMode(jobConfig);
        verifyJobNotStopped(jobConfigPOJO);
        verifyJobNotCompleted(jobConfig);
        switchClusterConfiguration(jobConfig);
    }
    
    @Override
    public void switchClusterConfiguration(final MigrationJobConfiguration jobConfig) {
    }
    
    @Override
    public void reset(final String jobId) {
        checkModeConfig();
        log.info("Scaling job {} reset target table", jobId);
        JobConfigurationPOJO jobConfigPOJO = getElasticJobConfigPOJO(jobId);
        verifyJobStopped(jobConfigPOJO);
    }
    
    @Override
    public void addMigrationSourceResources(final Map<String, DataSourceProperties> dataSourcePropsMap) {
        log.info("Add migration source resources {}", dataSourcePropsMap.keySet());
        Map<String, DataSourceProperties> existDataSources = dataSourcePersistService.load(getJobType());
        Collection<String> duplicateDataSourceNames = new HashSet<>(dataSourcePropsMap.size(), 1);
        for (Entry<String, DataSourceProperties> entry : dataSourcePropsMap.entrySet()) {
            if (existDataSources.containsKey(entry.getKey())) {
                duplicateDataSourceNames.add(entry.getKey());
            }
        }
        if (!duplicateDataSourceNames.isEmpty()) {
            throw new AddMigrationSourceResourceException(String.format("Duplicate resource names %s.", duplicateDataSourceNames));
        }
        Map<String, DataSourceProperties> result = new LinkedHashMap<>(existDataSources);
        result.putAll(dataSourcePropsMap);
        dataSourcePersistService.persist(getJobType(), result);
    }
    
    @Override
    public void dropMigrationSourceResources(final Collection<String> resourceNames) {
        Map<String, DataSourceProperties> metaDataDataSource = dataSourcePersistService.load(getJobType());
        List<String> noExistResources = resourceNames.stream().filter(each -> !metaDataDataSource.containsKey(each)).collect(Collectors.toList());
        if (!noExistResources.isEmpty()) {
            throw new DropMigrationSourceResourceException(String.format("Resource names %s not exist.", resourceNames));
        }
        for (String each : resourceNames) {
            metaDataDataSource.remove(each);
        }
        dataSourcePersistService.persist(getJobType(), metaDataDataSource);
    }
    
    @Override
    public void createJobAndStart(final CreateMigrationJobParameter parameter) {
        YamlMigrationJobConfiguration result = new YamlMigrationJobConfiguration();
        Map<String, DataSourceProperties> metaDataDataSource = dataSourcePersistService.load(JobType.MIGRATION);
        Map<String, Object> sourceDataSourceProps = DATA_SOURCE_CONFIG_SWAPPER.swapToMap(metaDataDataSource.get(parameter.getSourceDataSourceName()));
        YamlPipelineDataSourceConfiguration sourcePipelineDataSourceConfiguration = createYamlPipelineDataSourceConfiguration(StandardPipelineDataSourceConfiguration.TYPE,
                YamlEngine.marshal(sourceDataSourceProps));
        result.setSource(sourcePipelineDataSourceConfiguration);
        result.setSourceDatabaseType(new StandardPipelineDataSourceConfiguration(sourceDataSourceProps).getDatabaseType().getType());
        result.setSourceDataSourceName(parameter.getSourceDataSourceName());
        result.setSourceTableName(parameter.getSourceTableName());
        Map<String, Map<String, Object>> targetDataSourceProperties = new HashMap<>();
        for (Entry<String, DataSource> entry : parameter.getTargetDataSources().entrySet()) {
            Map<String, Object> dataSourceProps = DATA_SOURCE_CONFIG_SWAPPER.swapToMap(DataSourcePropertiesCreator.create(entry.getValue()));
            targetDataSourceProperties.put(entry.getKey(), dataSourceProps);
        }
        String targetDatabaseName = parameter.getTargetDatabaseName();
        YamlRootConfiguration targetRootConfig = getYamlRootConfiguration(targetDatabaseName, targetDataSourceProperties, parameter.getTargetShardingRuleConfig());
        PipelineDataSourceConfiguration targetPipelineDataSource = new ShardingSpherePipelineDataSourceConfiguration(targetRootConfig);
        result.setTarget(createYamlPipelineDataSourceConfiguration(targetPipelineDataSource.getType(), YamlEngine.marshal(targetPipelineDataSource.getDataSourceConfiguration())));
        result.setTargetDatabaseType(targetPipelineDataSource.getDatabaseType().getType());
        result.setTargetDatabaseName(targetDatabaseName);
        result.setTargetTableName(parameter.getTargetTableName());
        result.setSchemaTablesMap(SchemaTableUtil.getSchemaTablesMapFromActual(PIPELINE_DATA_SOURCE_CONFIG_SWAPPER.swapToObject(sourcePipelineDataSourceConfiguration),
                parameter.getSourceTableName()));
        extendYamlJobConfiguration(result);
        MigrationJobConfiguration jobConfiguration = new YamlMigrationJobConfigurationSwapper().swapToObject(result);
        start(jobConfiguration);
    }
    
    private YamlRootConfiguration getYamlRootConfiguration(final String databaseName, final Map<String, Map<String, Object>> yamlDataSources, final YamlRuleConfiguration shardingRule) {
        YamlRootConfiguration result = new YamlRootConfiguration();
        result.setDatabaseName(databaseName);
        result.setDataSources(yamlDataSources);
        Collection<YamlRuleConfiguration> yamlRuleConfigs = Collections.singletonList(shardingRule);
        result.setRules(yamlRuleConfigs);
        return result;
    }
    
    private YamlPipelineDataSourceConfiguration createYamlPipelineDataSourceConfiguration(final String type, final String parameter) {
        YamlPipelineDataSourceConfiguration result = new YamlPipelineDataSourceConfiguration();
        result.setType(type);
        result.setParameter(parameter);
        return result;
    }
    
    @Override
    public String getType() {
        return getJobType().getTypeName();
    }
}
