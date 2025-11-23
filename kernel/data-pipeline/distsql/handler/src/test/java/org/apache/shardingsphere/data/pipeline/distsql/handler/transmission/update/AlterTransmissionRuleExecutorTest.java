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

package org.apache.shardingsphere.data.pipeline.distsql.handler.transmission.update;

import org.apache.shardingsphere.data.pipeline.core.channel.PipelineChannelCreator;
import org.apache.shardingsphere.data.pipeline.core.channel.memory.MemoryPipelineChannelCreator;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.core.exception.param.PipelineInvalidParameterException;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.PipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.type.PipelineJobType;
import org.apache.shardingsphere.data.pipeline.core.metadata.PipelineProcessConfigurationPersistService;
import org.apache.shardingsphere.data.pipeline.distsql.statement.updatable.AlterTransmissionRuleStatement;
import org.apache.shardingsphere.distsql.handler.engine.update.DistSQLUpdateExecutor;
import org.apache.shardingsphere.distsql.segment.AlgorithmSegment;
import org.apache.shardingsphere.distsql.segment.ReadOrWriteSegment;
import org.apache.shardingsphere.distsql.segment.TransmissionRuleSegment;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlterTransmissionRuleExecutorTest {
    
    private static final String JOB_TYPE = "MIGRATION";
    
    private final AlterTransmissionRuleExecutor executor = (AlterTransmissionRuleExecutor) TypedSPILoader.getService(DistSQLUpdateExecutor.class, AlterTransmissionRuleStatement.class);
    
    @Mock
    private PipelineProcessConfigurationPersistService persistService;
    
    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        Plugins.getMemberAccessor().set(AlterTransmissionRuleExecutor.class.getDeclaredField("processConfigPersistService"), executor, persistService);
    }
    
    @Test
    void assertExecuteUpdate() {
        PipelineJobType<?> jobType = mock(PipelineJobType.class);
        when(jobType.getType()).thenReturn(JOB_TYPE);
        TransmissionRuleSegment segment = new TransmissionRuleSegment();
        segment.setReadSegment(new ReadOrWriteSegment(5, 1000, 200, new AlgorithmSegment("READ_LIMITER", PropertiesBuilder.build(new Property("qps", "50")))));
        segment.setWriteSegment(new ReadOrWriteSegment(3, 500, new AlgorithmSegment("WRITE_LIMITER", PropertiesBuilder.build(new Property("qps", "20")))));
        segment.setStreamChannel(new AlgorithmSegment("MEMORY", PropertiesBuilder.build(new Property("block-queue-size", "1024"))));
        AlterTransmissionRuleStatement sqlStatement = new AlterTransmissionRuleStatement(JOB_TYPE, segment);
        try (MockedStatic<TypedSPILoader> mockedStatic = mockStatic(TypedSPILoader.class)) {
            mockedStatic.when(() -> TypedSPILoader.getService(PipelineJobType.class, JOB_TYPE)).thenReturn(jobType);
            mockedStatic.when(() -> TypedSPILoader.findService(PipelineChannelCreator.class, "MEMORY")).thenReturn(Optional.of(new MemoryPipelineChannelCreator()));
            executor.executeUpdate(sqlStatement, mock());
            ArgumentCaptor<PipelineProcessConfiguration> configCaptor = ArgumentCaptor.forClass(PipelineProcessConfiguration.class);
            verify(persistService).persist(any(PipelineContextKey.class), eq(JOB_TYPE), configCaptor.capture());
            PipelineProcessConfiguration actual = configCaptor.getValue();
            assertThat(actual.getRead().getWorkerThread(), is(5));
            assertThat(actual.getRead().getBatchSize(), is(1000));
            assertThat(actual.getRead().getShardingSize(), is(200));
            assertThat(actual.getRead().getRateLimiter().getType(), is("READ_LIMITER"));
            assertThat(actual.getRead().getRateLimiter().getProps().getProperty("qps"), is("50"));
            assertThat(actual.getWrite().getWorkerThread(), is(3));
            assertThat(actual.getWrite().getBatchSize(), is(500));
            assertThat(actual.getWrite().getRateLimiter().getType(), is("WRITE_LIMITER"));
            assertThat(actual.getWrite().getRateLimiter().getProps().getProperty("qps"), is("20"));
            assertThat(actual.getStreamChannel().getType(), is("MEMORY"));
            assertThat(actual.getStreamChannel().getProps().getProperty("block-queue-size"), is("1024"));
        }
    }
    
    @Test
    void assertExecuteUpdatePersistWhenStreamChannelIsNull() {
        PipelineJobType<?> jobType = mock(PipelineJobType.class);
        when(jobType.getType()).thenReturn(JOB_TYPE);
        AlterTransmissionRuleStatement sqlStatement = new AlterTransmissionRuleStatement(JOB_TYPE, new TransmissionRuleSegment());
        try (MockedStatic<TypedSPILoader> mockedStatic = mockStatic(TypedSPILoader.class)) {
            mockedStatic.when(() -> TypedSPILoader.getService(PipelineJobType.class, JOB_TYPE)).thenReturn(jobType);
            executor.executeUpdate(sqlStatement, mock());
            ArgumentCaptor<PipelineProcessConfiguration> configCaptor = ArgumentCaptor.forClass(PipelineProcessConfiguration.class);
            verify(persistService).persist(any(PipelineContextKey.class), eq(JOB_TYPE), configCaptor.capture());
            PipelineProcessConfiguration actual = configCaptor.getValue();
            assertNull(actual.getRead());
            assertNull(actual.getWrite());
            assertNull(actual.getStreamChannel());
        }
    }
    
    @Test
    void assertExecuteUpdateThrowWhenStreamChannelTypeIsUnknown() {
        TransmissionRuleSegment segment = new TransmissionRuleSegment();
        segment.setStreamChannel(new AlgorithmSegment("UNKNOWN", new Properties()));
        AlterTransmissionRuleStatement sqlStatement = new AlterTransmissionRuleStatement(JOB_TYPE, segment);
        try (MockedStatic<TypedSPILoader> mockedStatic = mockStatic(TypedSPILoader.class)) {
            mockedStatic.when(() -> TypedSPILoader.findService(PipelineChannelCreator.class, "UNKNOWN")).thenReturn(Optional.empty());
            assertThrows(PipelineInvalidParameterException.class, () -> executor.executeUpdate(sqlStatement, mock()));
        }
    }
}
