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

package org.apache.shardingsphere.data.pipeline.cdc;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.type.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.config.job.CDCJobConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.config.task.CDCTaskConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.context.CDCProcessContext;
import org.apache.shardingsphere.data.pipeline.cdc.config.yaml.YamlCDCJobConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.config.yaml.YamlCDCJobConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.common.config.ImporterConfiguration;
import org.apache.shardingsphere.data.pipeline.common.config.job.PipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.common.config.job.yaml.YamlPipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.common.config.process.PipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.common.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.common.context.TransmissionProcessContext;
import org.apache.shardingsphere.data.pipeline.common.datanode.JobDataNodeLine;
import org.apache.shardingsphere.data.pipeline.common.datanode.JobDataNodeLineConvertUtils;
import org.apache.shardingsphere.data.pipeline.common.datasource.PipelineDataSourceConfigurationFactory;
import org.apache.shardingsphere.data.pipeline.common.metadata.CaseInsensitiveIdentifier;
import org.apache.shardingsphere.data.pipeline.common.pojo.PipelineJobInfo;
import org.apache.shardingsphere.data.pipeline.common.pojo.PipelineJobMetaData;
import org.apache.shardingsphere.data.pipeline.common.spi.algorithm.JobRateLimitAlgorithm;
import org.apache.shardingsphere.data.pipeline.common.util.ShardingColumnsExtractor;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.ConsistencyCheckJobItemProgressContext;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.PipelineDataConsistencyChecker;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.context.DumperCommonContext;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.context.IncrementalDumperContext;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.context.mapper.TableAndSchemaNameMapper;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.job.option.TransmissionJobOption;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobConfigurationManager;
import org.apache.shardingsphere.data.pipeline.core.job.service.TransmissionJobManager;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * CDC job option.
 */
@Slf4j
public final class CDCJobOption implements TransmissionJobOption {
    
    @SuppressWarnings("unchecked")
    @Override
    public YamlCDCJobConfigurationSwapper getYamlJobConfigurationSwapper() {
        return new YamlCDCJobConfigurationSwapper();
    }
    
    @Override
    public Class<CDCJob> getJobClass() {
        return CDCJob.class;
    }
    
    @Override
    public boolean isForceNoShardingWhenConvertToJobConfigurationPOJO() {
        return true;
    }
    
    @Override
    public PipelineJobInfo getJobInfo(final String jobId) {
        PipelineJobMetaData jobMetaData = new PipelineJobMetaData(PipelineJobIdUtils.getElasticJobConfigurationPOJO(jobId));
        CDCJobConfiguration jobConfig = new PipelineJobConfigurationManager(this).getJobConfiguration(jobId);
        return new PipelineJobInfo(jobMetaData, jobConfig.getDatabaseName(), String.join(", ", jobConfig.getSchemaTableNames()));
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
    public CDCTaskConfiguration buildTaskConfiguration(final PipelineJobConfiguration pipelineJobConfig, final int jobShardingItem, final PipelineProcessConfiguration processConfig) {
        CDCJobConfiguration jobConfig = (CDCJobConfiguration) pipelineJobConfig;
        TableAndSchemaNameMapper tableAndSchemaNameMapper = new TableAndSchemaNameMapper(jobConfig.getSchemaTableNames());
        IncrementalDumperContext dumperContext = buildDumperContext(jobConfig, jobShardingItem, tableAndSchemaNameMapper);
        ImporterConfiguration importerConfig = buildImporterConfiguration(jobConfig, processConfig, jobConfig.getSchemaTableNames(), tableAndSchemaNameMapper);
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
    public CDCProcessContext buildProcessContext(final PipelineJobConfiguration jobConfig) {
        TransmissionJobManager jobManager = new TransmissionJobManager(this);
        return new CDCProcessContext(jobConfig.getJobId(), jobManager.showProcessConfiguration(PipelineJobIdUtils.parseContextKey(jobConfig.getJobId())));
    }
    
    @Override
    public PipelineDataConsistencyChecker buildDataConsistencyChecker(final PipelineJobConfiguration jobConfig, final TransmissionProcessContext processContext,
                                                                      final ConsistencyCheckJobItemProgressContext progressContext) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public String getType() {
        return "STREAMING";
    }
}
