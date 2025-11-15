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

package org.apache.shardingsphere.data.pipeline.scenario.consistencycheck;

import org.apache.shardingsphere.data.pipeline.core.consistencycheck.position.yaml.YamlTableCheckRangePosition;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.position.yaml.YamlTableCheckRangePositionSwapper;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.pk.type.UnsupportedKeyIngestPosition;
import org.apache.shardingsphere.data.pipeline.core.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.core.job.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.job.id.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.job.progress.ConsistencyCheckJobItemProgress;
import org.apache.shardingsphere.data.pipeline.core.job.progress.yaml.config.YamlConsistencyCheckJobItemProgress;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobItemManager;
import org.apache.shardingsphere.data.pipeline.core.task.runner.PipelineTasksRunner;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.config.ConsistencyCheckJobConfiguration;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.config.yaml.config.YamlConsistencyCheckJobConfiguration;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.config.yaml.swapper.YamlConsistencyCheckJobConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.context.ConsistencyCheckJobItemContext;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.task.ConsistencyCheckTasksRunner;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.test.it.data.pipeline.core.util.JobConfigurationBuilder;
import org.apache.shardingsphere.test.it.data.pipeline.core.util.PipelineContextUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;

class ConsistencyCheckJobExecutorCallbackTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "Fixture");
    
    @BeforeAll
    static void beforeClass() {
        PipelineContextUtils.initPipelineContextManager();
    }
    
    @Test
    void assertBuildPipelineJobItemContext() {
        ConsistencyCheckJobId pipelineJobId = new ConsistencyCheckJobId(new PipelineContextKey(InstanceType.PROXY), JobConfigurationBuilder.createYamlMigrationJobConfiguration().getJobId());
        String checkJobId = PipelineJobIdUtils.marshal(pipelineJobId);
        List<YamlTableCheckRangePosition> expectedYamlTableCheckRangePositions = Collections.singletonList(createYamlTableCheckRangePosition());
        PipelineAPIFactory.getPipelineGovernanceFacade(PipelineContextUtils.getContextKey()).getJobItemFacade().getProcess().persist(checkJobId, 0,
                YamlEngine.marshal(createYamlConsistencyCheckJobItemProgress(expectedYamlTableCheckRangePositions)));
        ConsistencyCheckJobExecutorCallback callback = new ConsistencyCheckJobExecutorCallback();
        ConsistencyCheckJobConfiguration jobConfig = new YamlConsistencyCheckJobConfigurationSwapper().swapToObject(createYamlConsistencyCheckJobConfiguration(checkJobId));
        PipelineJobItemManager<ConsistencyCheckJobItemProgress> jobItemManager = new PipelineJobItemManager<>(new ConsistencyCheckJobType().getOption().getYamlJobItemProgressSwapper());
        Optional<ConsistencyCheckJobItemProgress> jobItemProgress = jobItemManager.getProgress(jobConfig.getJobId(), 0);
        ConsistencyCheckJobItemContext actual = callback.buildJobItemContext(jobConfig, 0, jobItemProgress.orElse(null), null, null);
        YamlTableCheckRangePositionSwapper tableCheckPositionSwapper = new YamlTableCheckRangePositionSwapper();
        List<YamlTableCheckRangePosition> actualYamlTableCheckPositions = actual.getProgressContext().getTableCheckRangePositions().stream()
                .map(tableCheckPositionSwapper::swapToYamlConfiguration).collect(Collectors.toList());
        assertThat(actualYamlTableCheckPositions.size(), is(expectedYamlTableCheckRangePositions.size()));
        for (int i = 0; i < actualYamlTableCheckPositions.size(); i++) {
            assertThat(actualYamlTableCheckPositions.get(i), is(expectedYamlTableCheckRangePositions.get(i)));
        }
    }
    
    @Test
    void assertBuildTasksRunner() {
        ConsistencyCheckJobConfiguration jobConfig = new ConsistencyCheckJobConfiguration("check_job", "parent_job", "DATA_MATCH", new Properties(), databaseType);
        PipelineTasksRunner actual = new ConsistencyCheckJobExecutorCallback().buildTasksRunner(new ConsistencyCheckJobItemContext(jobConfig, 0, JobStatus.RUNNING, null));
        assertThat(actual, instanceOf(ConsistencyCheckTasksRunner.class));
    }
    
    @Test
    void assertPrepare() {
        assertDoesNotThrow(() -> new ConsistencyCheckJobExecutorCallback().prepare(mock()));
    }
    
    private YamlTableCheckRangePosition createYamlTableCheckRangePosition() {
        YamlTableCheckRangePosition result = new YamlTableCheckRangePosition();
        result.setSplittingItem(0);
        result.setSourceDataNode("ds_0.t_order");
        result.setLogicTableName("t_order");
        result.setSourceRange(new UnsupportedKeyIngestPosition().toString());
        result.setTargetRange(new UnsupportedKeyIngestPosition().toString());
        result.setSourcePosition(100);
        result.setTargetPosition(100);
        return result;
    }
    
    private YamlConsistencyCheckJobItemProgress createYamlConsistencyCheckJobItemProgress(final List<YamlTableCheckRangePosition> yamlTableCheckRangePositions) {
        YamlConsistencyCheckJobItemProgress result = new YamlConsistencyCheckJobItemProgress();
        result.setStatus(JobStatus.RUNNING.name());
        result.setTableCheckRangePositions(yamlTableCheckRangePositions);
        return result;
    }
    
    private YamlConsistencyCheckJobConfiguration createYamlConsistencyCheckJobConfiguration(final String checkJobId) {
        YamlConsistencyCheckJobConfiguration result = new YamlConsistencyCheckJobConfiguration();
        result.setJobId(checkJobId);
        result.setParentJobId("");
        result.setAlgorithmTypeName("DATA_MATCH");
        result.setSourceDatabaseType("H2");
        return result;
    }
}
