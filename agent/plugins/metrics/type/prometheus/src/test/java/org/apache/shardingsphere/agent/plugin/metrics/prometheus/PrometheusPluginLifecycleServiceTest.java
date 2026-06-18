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

package org.apache.shardingsphere.agent.plugin.metrics.prometheus;

import io.prometheus.client.CollectorRegistry;
import org.apache.shardingsphere.agent.api.PluginConfiguration;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.mode.exclusive.ExclusiveOperatorEngine;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.standalone.workerid.StandaloneWorkerIdGenerator;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
class PrometheusPluginLifecycleServiceTest {
    
    private final PrometheusPluginLifecycleService pluginLifecycleService = new PrometheusPluginLifecycleService();
    
    @AfterEach
    void close() {
        pluginLifecycleService.close();
        CollectorRegistry.defaultRegistry.clear();
    }
    
    @Test
    void assertStart() throws IOException {
        final ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        pluginLifecycleService.start(new PluginConfiguration("localhost", 8090, "", PropertiesBuilder.build(new Property("jvm-information-collector-enabled", Boolean.TRUE.toString()))), true);
        try (Socket socket = new Socket()) {
            assertDoesNotThrow(() -> socket.connect(new InetSocketAddress("localhost", 8090)));
        }
    }
    
    @Test
    void assertStartForJDBCWithNullHost() throws IOException {
        int port;
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            port = serverSocket.getLocalPort();
        }
        pluginLifecycleService.start(new PluginConfiguration(null, port, "", PropertiesBuilder.build()), false);
        try (Socket socket = new Socket()) {
            assertDoesNotThrow(() -> socket.connect(new InetSocketAddress("localhost", port)));
        }
    }
    
    @Test
    void assertStartWhenPortIsOccupied() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            assertDoesNotThrow(() -> pluginLifecycleService.start(new PluginConfiguration(null, serverSocket.getLocalPort(), "", PropertiesBuilder.build()), false));
        }
    }
    
    private ContextManager mockContextManager() {
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(Collections.emptyList(), new ResourceMetaData(Collections.emptyMap()),
                new RuleMetaData(Collections.emptyList()), new ConfigurationProperties(new Properties()));
        MetaDataContexts metaDataContexts = new MetaDataContexts(metaData, new ShardingSphereStatistics());
        ComputeNodeInstanceContext computeNodeInstanceContext = new ComputeNodeInstanceContext(
                new ComputeNodeInstance(mock(InstanceMetaData.class)), new ModeConfiguration("Standalone", null), new EventBusContext());
        computeNodeInstanceContext.init(new StandaloneWorkerIdGenerator());
        return new ContextManager(metaDataContexts, computeNodeInstanceContext, mock(ExclusiveOperatorEngine.class), mock(PersistRepository.class));
    }
}
