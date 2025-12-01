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

package org.apache.shardingsphere.proxy.frontend.state.impl;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.embedded.EmbeddedChannel;
import lombok.SneakyThrows;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.builder.ShardingSphereStatisticsFactory;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.CommandExecutorTask;
import org.apache.shardingsphere.proxy.frontend.executor.ConnectionThreadExecutorGroup;
import org.apache.shardingsphere.proxy.frontend.spi.DatabaseProtocolFrontendEngine;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.internal.configuration.plugins.Plugins;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
class OKProxyStateTest {
    
    private ChannelHandlerContext context;
    
    @BeforeEach
    void setup() {
        context = mock(ChannelHandlerContext.class);
        when(context.channel()).thenReturn(new EmbeddedChannel());
    }
    
    @AfterEach
    void tearDown() {
        context.channel().close().syncUninterruptibly();
    }
    
    @Test
    void assertExecuteWithDistributedTransaction() {
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        ConnectionSession connectionSession = mock(ConnectionSession.class, RETURNS_DEEP_STUBS);
        when(connectionSession.getConnectionId()).thenReturn(1);
        ExecutorService executorService = registerMockExecutorService(1);
        new OKProxyState().execute(context, null, mock(DatabaseProtocolFrontendEngine.class), connectionSession);
        verify(executorService).execute(any(CommandExecutorTask.class));
        ConnectionThreadExecutorGroup.getInstance().unregisterAndAwaitTermination(1);
    }
    
    private ContextManager mockContextManager() {
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        when(metaData.getDatabase("foo_db")).thenReturn(mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS));
        when(metaData.getAllDatabases()).thenReturn(Collections.singleton(mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS)));
        when(metaData.getAllDatabases().iterator().next().getProtocolType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        when(metaData.getProps().<Integer>getValue(ConfigurationPropertyKey.KERNEL_EXECUTOR_SIZE)).thenReturn(0);
        when(metaData.getProps().<Boolean>getValue(ConfigurationPropertyKey.PERSIST_SCHEMAS_TO_REPOSITORY_ENABLED)).thenReturn(true);
        TransactionRule transactionRule = mock(TransactionRule.class);
        when(transactionRule.getDefaultType()).thenReturn(TransactionType.XA);
        when(metaData.getGlobalRuleMetaData()).thenReturn(new RuleMetaData(Collections.singletonList(transactionRule)));
        ComputeNodeInstanceContext computeNodeInstanceContext = mock(ComputeNodeInstanceContext.class);
        when(computeNodeInstanceContext.getModeConfiguration()).thenReturn(mock(ModeConfiguration.class));
        return new ContextManager(new MetaDataContexts(metaData, ShardingSphereStatisticsFactory.create(metaData, new ShardingSphereStatistics())), computeNodeInstanceContext, mock(), mock());
    }
    
    @SuppressWarnings({"unchecked", "SameParameterValue"})
    @SneakyThrows(ReflectiveOperationException.class)
    private ExecutorService registerMockExecutorService(final int connectionId) {
        Map<Integer, ExecutorService> executorServices = (Map<Integer, ExecutorService>) Plugins.getMemberAccessor()
                .get(ConnectionThreadExecutorGroup.class.getDeclaredField("executorServices"), ConnectionThreadExecutorGroup.getInstance());
        ExecutorService result = mock(ExecutorService.class);
        executorServices.put(connectionId, result);
        return result;
    }
}
