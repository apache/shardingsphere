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

package org.apache.shardingsphere.data.pipeline.cdc.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.DefaultChannelId;
import org.apache.shardingsphere.data.pipeline.api.type.ShardingSpherePipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.CDCJob;
import org.apache.shardingsphere.data.pipeline.cdc.api.CDCJobAPI;
import org.apache.shardingsphere.data.pipeline.cdc.api.StreamDataParameter;
import org.apache.shardingsphere.data.pipeline.cdc.config.CDCJobConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.constant.CDCSinkType;
import org.apache.shardingsphere.data.pipeline.cdc.context.CDCConnectionContext;
import org.apache.shardingsphere.data.pipeline.cdc.core.importer.CDCImporter;
import org.apache.shardingsphere.data.pipeline.cdc.core.importer.CDCImporterManager;
import org.apache.shardingsphere.data.pipeline.cdc.core.importer.sink.PipelineCDCSocketSink;
import org.apache.shardingsphere.data.pipeline.cdc.exception.CDCExceptionWrapper;
import org.apache.shardingsphere.data.pipeline.cdc.exception.MissingRequiredStreamDataSourceException;
import org.apache.shardingsphere.data.pipeline.cdc.exception.StreamDatabaseNotFoundException;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.AckStreamingRequestBody;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.StreamDataRequestBody;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.StreamDataRequestBody.SchemaTable;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.CDCResponse;
import org.apache.shardingsphere.data.pipeline.cdc.util.CDCSchemaTableUtils;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContextManager;
import org.apache.shardingsphere.data.pipeline.core.datanode.JobDataNodeLine;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineInternalException;
import org.apache.shardingsphere.data.pipeline.core.exception.job.PipelineJobNotFoundException;
import org.apache.shardingsphere.data.pipeline.core.importer.sink.PipelineSink;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobRegistry;
import org.apache.shardingsphere.data.pipeline.core.job.id.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobConfigurationManager;
import org.apache.shardingsphere.data.pipeline.core.util.PipelineDataNodeUtils;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.internal.configuration.plugins.Plugins;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({TypedSPILoader.class, DatabaseTypedSPILoader.class, PipelineContextManager.class, CDCSchemaTableUtils.class, PipelineDataNodeUtils.class, PipelineJobIdUtils.class,
        CDCImporterManager.class})
class CDCBackendHandlerTest {
    
    private CDCJobAPI jobAPI;
    
    private PipelineJobConfigurationManager jobConfigManager;
    
    private CDCBackendHandler backendHandler;
    
    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        jobAPI = mock(CDCJobAPI.class);
        jobConfigManager = mock(PipelineJobConfigurationManager.class);
        when(TypedSPILoader.getService(any(), any())).thenReturn(jobAPI);
        backendHandler = new CDCBackendHandler();
        Plugins.getMemberAccessor().set(CDCBackendHandler.class.getDeclaredField("jobAPI"), backendHandler, jobAPI);
        Plugins.getMemberAccessor().set(CDCBackendHandler.class.getDeclaredField("jobConfigManager"), backendHandler, jobConfigManager);
    }
    
    @Test
    void assertGetDatabaseNameByJobId() {
        CDCJobConfiguration jobConfig = mock(CDCJobConfiguration.class);
        when(jobConfig.getDatabaseName()).thenReturn("foo_db");
        when(jobConfigManager.getJobConfiguration("foo_job")).thenReturn(jobConfig);
        assertThat(backendHandler.getDatabaseNameByJobId("foo_job"), is("foo_db"));
    }
    
    @Test
    void assertStreamDataWhenSchemaAvailable() {
        ShardingSphereDatabase database = mockDatabase();
        mockDialectMetaData(false);
        Map<String, Set<String>> schemaTableNames = Collections.singletonMap("foo_schema", Collections.singleton("foo_tbl"));
        when(CDCSchemaTableUtils.parseTableExpressions(eq(database), anyCollection())).thenReturn(schemaTableNames);
        DataNode fooDataNode = new DataNode("foo_ds", "foo_schema", "foo_tbl");
        when(PipelineDataNodeUtils.buildTableAndDataNodesMap(database, schemaTableNames)).thenReturn(Collections.singletonMap("foo_tbl", Collections.singletonList(fooDataNode)));
        when(jobAPI.create(any(StreamDataParameter.class), eq(CDCSinkType.SOCKET), any(Properties.class))).thenReturn("foo_job");
        when(jobConfigManager.getJobConfiguration("foo_job")).thenReturn(createJobConfiguration());
        StreamDataRequestBody requestBody = StreamDataRequestBody.newBuilder().setDatabase("foo_db")
                .addSourceSchemaTable(SchemaTable.newBuilder().setSchema("foo_schema").setTable("foo_tbl")).build();
        CDCResponse actualResponse = backendHandler.streamData("foo_req", requestBody, createConnectionContext(), mock());
        assertThat(actualResponse.getStatus(), is(CDCResponse.Status.SUCCEED));
        assertThat(actualResponse.getResponseCase(), is(CDCResponse.ResponseCase.STREAM_DATA_RESULT));
        assertThat(actualResponse.getStreamDataResult().getStreamingId(), is("foo_job"));
        ArgumentCaptor<StreamDataParameter> parameterCaptor = ArgumentCaptor.forClass(StreamDataParameter.class);
        verify(jobAPI).create(parameterCaptor.capture(), eq(CDCSinkType.SOCKET), any(Properties.class));
        assertThat(parameterCaptor.getValue().getSchemaTableNames(), is(Collections.singletonList("foo_schema.foo_tbl")));
        assertThat(parameterCaptor.getValue().getTableAndDataNodesMap().get("foo_tbl"), is(Collections.singletonList(fooDataNode)));
    }
    
    @Test
    void assertStreamDataWithoutSchema() {
        ShardingSphereDatabase database = mockDatabase();
        mockDialectMetaData(true);
        Map<String, Set<String>> schemaTableNames = Collections.singletonMap("", Collections.singleton("foo_tbl"));
        when(CDCSchemaTableUtils.parseTableExpressions(eq(database), anyCollection())).thenReturn(schemaTableNames);
        when(PipelineDataNodeUtils.buildTableAndDataNodesMap(database, schemaTableNames)).thenReturn(Collections.singletonMap("foo_tbl", Collections.singletonList(mock(DataNode.class))));
        when(jobAPI.create(any(StreamDataParameter.class), eq(CDCSinkType.SOCKET), any(Properties.class))).thenReturn("foo_job");
        when(jobConfigManager.getJobConfiguration("foo_job")).thenReturn(createJobConfiguration());
        StreamDataRequestBody requestBody = StreamDataRequestBody.newBuilder().setDatabase("foo_db").addSourceSchemaTable(SchemaTable.newBuilder().setTable("foo_tbl")).build();
        CDCResponse actualResponse = backendHandler.streamData("foo_req", requestBody, createConnectionContext(), mock());
        assertThat(actualResponse.getStatus(), is(CDCResponse.Status.SUCCEED));
        assertThat(actualResponse.getStreamDataResult().getStreamingId(), is("foo_job"));
        ArgumentCaptor<StreamDataParameter> parameterCaptor = ArgumentCaptor.forClass(StreamDataParameter.class);
        verify(jobAPI).create(parameterCaptor.capture(), eq(CDCSinkType.SOCKET), any(Properties.class));
        assertThat(parameterCaptor.getValue().getSchemaTableNames(), is(Collections.singletonList("foo_tbl")));
    }
    
    @Test
    void assertStreamDataWhenTableNamesMissing() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        mockProxyContext(database);
        mockDialectMetaData(false);
        when(CDCSchemaTableUtils.parseTableExpressions(eq(database), anyCollection())).thenReturn(Collections.emptyMap());
        StreamDataRequestBody requestBody = StreamDataRequestBody.newBuilder().setDatabase("foo_db").addSourceSchemaTable(SchemaTable.newBuilder().setTable("foo_tbl")).build();
        CDCExceptionWrapper actualException = assertThrows(CDCExceptionWrapper.class, () -> backendHandler.streamData("foo_req", requestBody, createConnectionContext(), mock()));
        assertThat(actualException.getCause(), isA(MissingRequiredStreamDataSourceException.class));
        verifyNoInteractions(jobAPI);
    }
    
    @Test
    void assertStreamDataWhenDatabaseMissing() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts().getMetaData().getDatabase("missing_db")).thenReturn(null);
        when(PipelineContextManager.getProxyContext()).thenReturn(contextManager);
        StreamDataRequestBody requestBody = StreamDataRequestBody.newBuilder().setDatabase("missing_db").build();
        CDCExceptionWrapper actualException = assertThrows(CDCExceptionWrapper.class, () -> backendHandler.streamData("foo_req", requestBody, createConnectionContext(), mock()));
        assertThat(actualException.getCause(), isA(StreamDatabaseNotFoundException.class));
    }
    
    @Test
    void assertStartStreamingSuccess() {
        CDCJobConfiguration jobConfig = createJobConfiguration();
        when(jobConfigManager.getJobConfiguration("foo_job")).thenReturn(jobConfig);
        mockProxyContext(mock(ShardingSphereDatabase.class));
        Channel channel = mock(Channel.class);
        CDCConnectionContext connectionContext = createConnectionContext();
        try (MockedStatic<PipelineJobRegistry> jobRegistryMocked = mockStatic(PipelineJobRegistry.class)) {
            jobRegistryMocked.when(() -> PipelineJobRegistry.stop("foo_job")).thenThrow(new IllegalStateException("stop should be owned by CDCJobAPI"));
            backendHandler.startStreaming("foo_job", connectionContext, channel);
            ArgumentCaptor<PipelineSink> sinkCaptor = ArgumentCaptor.forClass(PipelineSink.class);
            verify(jobAPI).start(eq("foo_job"), sinkCaptor.capture());
            assertThat(((PipelineCDCSocketSink) sinkCaptor.getValue()).getChannel(), is(channel));
            assertThat(connectionContext.getJobId(), is("foo_job"));
            jobRegistryMocked.verifyNoInteractions();
        }
    }
    
    @Test
    void assertStartStreamingWhenJobConfigMissing() {
        assertThrows(PipelineJobNotFoundException.class, () -> backendHandler.startStreaming("foo_job", createConnectionContext(), mock(Channel.class)));
    }
    
    @Test
    void assertStopStreamingWhenJobIdEmpty() {
        backendHandler.stopStreaming("", DefaultChannelId.newInstance());
        verifyNoInteractions(jobAPI);
    }
    
    @Test
    void assertStopStreamingWhenJobMissing() {
        try (MockedStatic<PipelineJobRegistry> jobRegistryMocked = mockStatic(PipelineJobRegistry.class)) {
            jobRegistryMocked.when(() -> PipelineJobRegistry.get("foo_job")).thenReturn(null);
            backendHandler.stopStreaming("foo_job", DefaultChannelId.newInstance());
            verifyNoInteractions(jobAPI);
        }
    }
    
    @Test
    void assertStopStreamingWhenChannelIdNotMatch() {
        ChannelId targetChannelId = DefaultChannelId.newInstance();
        Channel channel = mockChannel(DefaultChannelId.newInstance());
        CDCJob job = mock(CDCJob.class);
        when(job.getSink()).thenReturn(new PipelineCDCSocketSink(channel, mock(ShardingSphereDatabase.class), Collections.emptyList()));
        try (MockedStatic<PipelineJobRegistry> jobRegistryMocked = mockStatic(PipelineJobRegistry.class)) {
            jobRegistryMocked.when(() -> PipelineJobRegistry.get("foo_job")).thenReturn(job);
            backendHandler.stopStreaming("foo_job", targetChannelId);
            verifyNoInteractions(jobAPI);
        }
    }
    
    @Test
    void assertStopStreamingWhenChannelIdMatch() {
        ChannelId targetChannelId = DefaultChannelId.newInstance();
        Channel channel = mockChannel(targetChannelId);
        CDCJob job = mock(CDCJob.class);
        when(job.getSink()).thenReturn(new PipelineCDCSocketSink(channel, mock(ShardingSphereDatabase.class), Collections.emptyList()));
        try (MockedStatic<PipelineJobRegistry> jobRegistryMocked = mockStatic(PipelineJobRegistry.class)) {
            jobRegistryMocked.when(() -> PipelineJobRegistry.get("foo_job")).thenReturn(job);
            backendHandler.stopStreaming("foo_job", targetChannelId);
            jobRegistryMocked.verify(() -> PipelineJobRegistry.stop("foo_job"));
            verify(jobAPI).disable("foo_job");
        }
    }
    
    @Test
    void assertDropStreamingWhenJobNotDisabled() {
        JobConfigurationPOJO jobConfigPOJO = new JobConfigurationPOJO();
        jobConfigPOJO.setDisabled(false);
        when(PipelineJobIdUtils.getElasticJobConfigurationPOJO("foo_job")).thenReturn(jobConfigPOJO);
        assertThrows(PipelineInternalException.class, () -> backendHandler.dropStreaming("foo_job"));
        verifyNoInteractions(jobAPI);
    }
    
    @Test
    void assertDropStreamingWhenJobDisabled() {
        JobConfigurationPOJO jobConfigPOJO = new JobConfigurationPOJO();
        jobConfigPOJO.setDisabled(true);
        when(PipelineJobIdUtils.getElasticJobConfigurationPOJO("foo_job")).thenReturn(jobConfigPOJO);
        backendHandler.dropStreaming("foo_job");
        verify(jobAPI).drop("foo_job");
    }
    
    @Test
    void assertProcessAckWhenImporterMissing() {
        when(CDCImporterManager.getImporter("importer")).thenReturn(null);
        backendHandler.processAck(AckStreamingRequestBody.newBuilder().setAckId("importer_random").build());
        verifyNoInteractions(jobAPI);
    }
    
    @Test
    void assertProcessAckWhenImporterExists() {
        CDCImporter importer = mock(CDCImporter.class);
        when(CDCImporterManager.getImporter("importer")).thenReturn(importer);
        backendHandler.processAck(AckStreamingRequestBody.newBuilder().setAckId("importer_random").build());
        verify(importer).ack("importer_random");
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class);
        DatabaseType protocolType = mock(DatabaseType.class);
        when(result.getProtocolType()).thenReturn(protocolType);
        mockProxyContext(result);
        return result;
    }
    
    private void mockProxyContext(final ShardingSphereDatabase database) {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        MetaDataContexts metaDataContexts = mock(MetaDataContexts.class, RETURNS_DEEP_STUBS);
        when(metaDataContexts.getMetaData().getDatabase("foo_db")).thenReturn(database);
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        when(PipelineContextManager.getProxyContext()).thenReturn(contextManager);
    }
    
    private void mockDialectMetaData(final boolean supportGlobalCSN) {
        DialectDatabaseMetaData databaseMetaData = mock(DialectDatabaseMetaData.class, RETURNS_DEEP_STUBS);
        lenient().when(databaseMetaData.getTransactionOption().isSupportGlobalCSN()).thenReturn(supportGlobalCSN);
        when(DatabaseTypedSPILoader.getService(any(), any())).thenReturn(databaseMetaData);
    }
    
    private Channel mockChannel(final ChannelId channelId) {
        Channel result = mock(Channel.class);
        when(result.id()).thenReturn(channelId);
        return result;
    }
    
    private CDCConnectionContext createConnectionContext() {
        return new CDCConnectionContext(new ShardingSphereUser("test_user", "", "localhost"));
    }
    
    private CDCJobConfiguration createJobConfiguration() {
        return new CDCJobConfiguration("foo_job", "foo_db", new ArrayList<>(Collections.singletonList("foo_schema.foo_tbl")), false, mock(DatabaseType.class),
                mock(ShardingSpherePipelineDataSourceConfiguration.class), new JobDataNodeLine(Collections.emptyList()), Collections.singletonList(new JobDataNodeLine(Collections.emptyList())),
                false, new CDCJobConfiguration.SinkConfiguration(CDCSinkType.SOCKET, new Properties()), 1, 0);
    }
}
