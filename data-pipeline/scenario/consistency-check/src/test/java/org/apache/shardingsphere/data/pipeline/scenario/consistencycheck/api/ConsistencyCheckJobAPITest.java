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

package org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.api;

import org.apache.shardingsphere.data.pipeline.core.consistencycheck.table.TableDataConsistencyChecker;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.table.TableDataConsistencyCheckerFactory;
import org.apache.shardingsphere.data.pipeline.core.exception.data.UnsupportedPipelineDatabaseTypeException;
import org.apache.shardingsphere.data.pipeline.core.exception.job.UncompletedConsistencyCheckJobExistsException;
import org.apache.shardingsphere.data.pipeline.core.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.core.job.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.job.progress.ConsistencyCheckJobItemProgress;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobItemManager;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobManager;
import org.apache.shardingsphere.data.pipeline.core.registrycenter.repository.PipelineGovernanceFacade;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.ConsistencyCheckJobType;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.config.ConsistencyCheckJobConfiguration;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.config.yaml.swapper.YamlConsistencyCheckJobConfigurationSwapper;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.MockedConstruction;
import org.mockito.internal.configuration.plugins.Plugins;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({PipelineAPIFactory.class, TableDataConsistencyCheckerFactory.class})
class ConsistencyCheckJobAPITest {
    
    private static final String PARENT_JOB_ID = "j0102p0000123456789abcdef0123456789abcdef";
    
    private final DatabaseType sourceDatabaseType = TypedSPILoader.getService(DatabaseType.class, "H2");
    
    private final DatabaseType targetDatabaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
    
    private final PipelineGovernanceFacade governanceFacade = mock(PipelineGovernanceFacade.class, RETURNS_DEEP_STUBS);
    
    private final PipelineJobManager jobManager = mock(PipelineJobManager.class);
    
    private final TableDataConsistencyChecker checker = mock(TableDataConsistencyChecker.class);
    
    private ConsistencyCheckJobAPI jobAPI;
    
    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        jobAPI = new ConsistencyCheckJobAPI(new ConsistencyCheckJobType());
        Plugins.getMemberAccessor().set(ConsistencyCheckJobAPI.class.getDeclaredField("jobManager"), jobAPI, jobManager);
        when(PipelineAPIFactory.getPipelineGovernanceFacade(any())).thenReturn(governanceFacade);
        when(governanceFacade.getJobFacade().getCheck().findLatestCheckJobId(PARENT_JOB_ID)).thenReturn(Optional.empty());
    }
    
    @Test
    void assertStartFirstJob() {
        when(TableDataConsistencyCheckerFactory.newInstance(any(), any())).thenReturn(checker);
        when(checker.getSupportedDatabaseTypes()).thenReturn(Arrays.asList(sourceDatabaseType, targetDatabaseType));
        String expectedJobId = "j0202p0000" + PARENT_JOB_ID + "1";
        assertThat(jobAPI.start(new CreateConsistencyCheckJobParameter(PARENT_JOB_ID, null, null, sourceDatabaseType, targetDatabaseType)), is(expectedJobId));
        ArgumentCaptor<ConsistencyCheckJobConfiguration> jobConfigCaptor = ArgumentCaptor.forClass(ConsistencyCheckJobConfiguration.class);
        InOrder order = inOrder(checker, governanceFacade.getJobFacade().getCheck(), jobManager);
        order.verify(checker).close();
        order.verify(governanceFacade.getJobFacade().getCheck()).persistLatestCheckJobId(PARENT_JOB_ID, expectedJobId);
        order.verify(governanceFacade.getJobFacade().getCheck()).initCheckJobResult(PARENT_JOB_ID, expectedJobId);
        order.verify(jobManager).drop(expectedJobId);
        order.verify(jobManager).start(jobConfigCaptor.capture());
        ConsistencyCheckJobConfiguration actual = jobConfigCaptor.getValue();
        assertThat(actual.getJobId(), is(expectedJobId));
        assertThat(actual.getParentJobId(), is(PARENT_JOB_ID));
        assertThat(actual.getSourceDatabaseType().getType(), is("H2"));
        assertNull(actual.getAlgorithmTypeName());
        assertNull(actual.getAlgorithmProps());
    }
    
    @SuppressWarnings("rawtypes")
    @ParameterizedTest(name = "{0}: sequence {1} to {2}")
    @CsvSource({"FINISHED, 1, 2", "FINISHED, 2, 3", "CONSISTENCY_CHECK_FAILURE, 3, 1"})
    void assertStartSubsequentJob(final JobStatus status, final int latestSequence, final int expectedSequence) {
        when(TableDataConsistencyCheckerFactory.newInstance(any(), any())).thenReturn(checker);
        when(checker.getSupportedDatabaseTypes()).thenReturn(Arrays.asList(sourceDatabaseType, targetDatabaseType));
        String latestJobId = "j0202p0000" + PARENT_JOB_ID + latestSequence;
        when(governanceFacade.getJobFacade().getCheck().findLatestCheckJobId(PARENT_JOB_ID)).thenReturn(Optional.of(latestJobId));
        ConsistencyCheckJobItemProgress progress = mock(ConsistencyCheckJobItemProgress.class);
        when(progress.getStatus()).thenReturn(status);
        Properties algorithmProps = PropertiesBuilder.build(new Property("chunk-size", "1000"));
        try (
                MockedConstruction<PipelineJobItemManager> ignored = mockConstruction(PipelineJobItemManager.class,
                        (mock, context) -> when(mock.getProgress(latestJobId, 0)).thenReturn(Optional.of(progress)))) {
            String expectedJobId = "j0202p0000" + PARENT_JOB_ID + expectedSequence;
            assertThat(jobAPI.start(new CreateConsistencyCheckJobParameter(PARENT_JOB_ID, "FIXTURE", algorithmProps, sourceDatabaseType, targetDatabaseType)), is(expectedJobId));
            ArgumentCaptor<ConsistencyCheckJobConfiguration> jobConfigCaptor = ArgumentCaptor.forClass(ConsistencyCheckJobConfiguration.class);
            verify(jobManager).start(jobConfigCaptor.capture());
            YamlConsistencyCheckJobConfigurationSwapper swapper = new YamlConsistencyCheckJobConfigurationSwapper();
            ConsistencyCheckJobConfiguration actual = swapper.swapToObject(swapper.swapToYamlConfiguration(jobConfigCaptor.getValue()));
            assertThat(actual.getJobId(), is(expectedJobId));
            assertThat(actual.getParentJobId(), is(PARENT_JOB_ID));
            assertThat(actual.getSourceDatabaseType().getType(), is("H2"));
            assertThat(actual.getAlgorithmTypeName(), is("FIXTURE"));
            assertThat(actual.getAlgorithmProps(), is(PropertiesBuilder.build(new Property("chunk-size", "1000"))));
        }
    }
    
    @SuppressWarnings("rawtypes")
    @ParameterizedTest(name = "progress present={0}, status={1}")
    @CsvSource({"false, RUNNING", "true, RUNNING", "true, PREPARING"})
    void assertStartWithUncompletedJob(final boolean progressPresent, final JobStatus status) {
        String latestJobId = "j0202p0000" + PARENT_JOB_ID + "1";
        when(governanceFacade.getJobFacade().getCheck().findLatestCheckJobId(PARENT_JOB_ID)).thenReturn(Optional.of(latestJobId));
        ConsistencyCheckJobItemProgress progress = mock(ConsistencyCheckJobItemProgress.class);
        if (progressPresent) {
            when(progress.getStatus()).thenReturn(status);
        }
        try (
                MockedConstruction<PipelineJobItemManager> ignored = mockConstruction(PipelineJobItemManager.class,
                        (mock, context) -> when(mock.getProgress(latestJobId, 0)).thenReturn(progressPresent ? Optional.of(progress) : Optional.empty()))) {
            assertThrows(UncompletedConsistencyCheckJobExistsException.class,
                    () -> jobAPI.start(new CreateConsistencyCheckJobParameter(PARENT_JOB_ID, null, null, sourceDatabaseType, targetDatabaseType)));
            verifyNoInteractions(checker, jobManager);
            verify(governanceFacade.getJobFacade().getCheck(), never()).persistLatestCheckJobId(anyString(), anyString());
            verify(governanceFacade.getJobFacade().getCheck(), never()).initCheckJobResult(anyString(), anyString());
        }
    }
    
    @Test
    void assertStartWithUnsupportedSource() {
        when(TableDataConsistencyCheckerFactory.newInstance(any(), any())).thenReturn(checker);
        when(checker.getSupportedDatabaseTypes()).thenReturn(Collections.singleton(targetDatabaseType));
        assertThrows(UnsupportedPipelineDatabaseTypeException.class, () -> jobAPI.start(new CreateConsistencyCheckJobParameter(PARENT_JOB_ID, null, null, sourceDatabaseType, targetDatabaseType)));
        verify(checker).close();
        verifyNoInteractions(jobManager);
        verify(governanceFacade.getJobFacade().getCheck(), never()).persistLatestCheckJobId(anyString(), anyString());
        verify(governanceFacade.getJobFacade().getCheck(), never()).initCheckJobResult(anyString(), anyString());
    }
    
    @Test
    void assertStartWithUnsupportedTarget() {
        when(TableDataConsistencyCheckerFactory.newInstance(any(), any())).thenReturn(checker);
        when(checker.getSupportedDatabaseTypes()).thenReturn(Collections.singleton(sourceDatabaseType));
        assertThrows(UnsupportedPipelineDatabaseTypeException.class, () -> jobAPI.start(new CreateConsistencyCheckJobParameter(PARENT_JOB_ID, null, null, sourceDatabaseType, targetDatabaseType)));
        verify(checker).close();
        verifyNoInteractions(jobManager);
        verify(governanceFacade.getJobFacade().getCheck(), never()).persistLatestCheckJobId(anyString(), anyString());
        verify(governanceFacade.getJobFacade().getCheck(), never()).initCheckJobResult(anyString(), anyString());
    }
}
