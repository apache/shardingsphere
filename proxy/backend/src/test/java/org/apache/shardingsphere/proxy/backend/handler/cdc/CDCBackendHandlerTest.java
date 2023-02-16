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

package org.apache.shardingsphere.proxy.backend.handler.cdc;

import io.netty.channel.Channel;
import org.apache.shardingsphere.data.pipeline.cdc.context.CDCConnectionContext;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.CDCRequest;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.CDCRequest.Type;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.StreamDataRequestBody;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.CDCResponse;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.CDCResponse.Status;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public final class CDCBackendHandlerTest {
    
    private static MockedStatic<PipelineContext> pipelineContextMocked;
    
    private final CDCBackendHandler handler = new CDCBackendHandler();
    
    @BeforeClass
    public static void beforeClass() {
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(MetaDataPersistService.class),
                new ShardingSphereMetaData(getDatabases(), mock(ShardingSphereRuleMetaData.class), new ConfigurationProperties(new Properties())));
        ContextManager contextManager = new ContextManager(metaDataContexts, mock(InstanceContext.class));
        pipelineContextMocked = mockStatic(PipelineContext.class);
        pipelineContextMocked.when(PipelineContext::getContextManager).thenReturn(contextManager);
    }
    
    private static Map<String, ShardingSphereDatabase> getDatabases() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("sharding_db");
        when(database.getProtocolType()).thenReturn(new MySQLDatabaseType());
        Set<ShardingSphereRule> shardingRule = Collections.singleton(mock(ShardingRule.class));
        when(database.getRuleMetaData().getRules()).thenReturn(shardingRule);
        Map<String, ShardingSphereDatabase> result = new LinkedHashMap<>(1, 1);
        result.put("sharding_db", database);
        return result;
    }
    
    @AfterClass
    public static void afterClass() {
        pipelineContextMocked.close();
    }
    
    @Test
    public void assertStreamDataRequestFailed() {
        CDCRequest request = CDCRequest.newBuilder().setRequestId("1").setType(Type.STREAM_DATA).setStreamDataRequestBody(StreamDataRequestBody.newBuilder().setDatabase("none")).build();
        CDCResponse actualResponse = handler.streamData(request.getRequestId(), request.getStreamDataRequestBody(), mock(CDCConnectionContext.class), mock(Channel.class));
        assertThat(actualResponse.getStatus(), is(Status.FAILED));
    }
}
