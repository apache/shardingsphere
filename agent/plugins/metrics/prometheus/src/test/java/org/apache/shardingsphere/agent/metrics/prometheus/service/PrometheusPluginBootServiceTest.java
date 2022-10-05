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

package org.apache.shardingsphere.agent.metrics.prometheus.service;

import org.apache.shardingsphere.agent.config.PluginConfiguration;
import org.apache.shardingsphere.agent.metrics.prometheus.ProxyContextRestorer;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;
import org.apache.shardingsphere.infra.lock.LockContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.schedule.ScheduleContext;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.standalone.workerid.generator.StandaloneWorkerIdGenerator;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.junit.AfterClass;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Properties;

import static org.mockito.Mockito.mock;

public final class PrometheusPluginBootServiceTest extends ProxyContextRestorer {
    
    private static final PrometheusPluginBootService PROMETHEUS_PLUGIN_BOOT_SERVICE = new PrometheusPluginBootService();
    
    @AfterClass
    public static void close() {
        PROMETHEUS_PLUGIN_BOOT_SERVICE.close();
    }
    
    @Test
    public void assertStart() throws IOException {
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(MetaDataPersistService.class), new ShardingSphereMetaData());
        InstanceContext instanceContext = new InstanceContext(
                new ComputeNodeInstance(mock(InstanceMetaData.class)), new StandaloneWorkerIdGenerator(), new ModeConfiguration("Standalone", null), mock(LockContext.class),
                new EventBusContext(), mock(ScheduleContext.class));
        ProxyContext.init(new ContextManager(metaDataContexts, instanceContext));
        PROMETHEUS_PLUGIN_BOOT_SERVICE.start(new PluginConfiguration("localhost", 8090, "", createProperties()));
        new Socket().connect(new InetSocketAddress("localhost", 8090));
    }
    
    private Properties createProperties() {
        Properties result = new Properties();
        result.setProperty("JVM_INFORMATION_COLLECTOR_ENABLED", Boolean.TRUE.toString());
        return result;
    }
}
