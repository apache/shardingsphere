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

import com.google.common.base.Strings;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.client.hotspot.DefaultExports;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.agent.api.PluginConfiguration;
import org.apache.shardingsphere.agent.plugin.metrics.core.MetricsPool;
import org.apache.shardingsphere.agent.plugin.metrics.prometheus.collector.BuildInfoCollector;
import org.apache.shardingsphere.agent.plugin.metrics.prometheus.collector.MetaDataInfoCollector;
import org.apache.shardingsphere.agent.plugin.metrics.prometheus.collector.ProxyInfoCollector;
import org.apache.shardingsphere.agent.plugin.metrics.prometheus.wrapper.PrometheusWrapperFactory;
import org.apache.shardingsphere.agent.plugin.core.config.validator.PluginConfigurationValidator;
import org.apache.shardingsphere.agent.spi.PluginLifecycleService;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Prometheus plugin lifecycle service.
 */
@Slf4j
public final class PrometheusPluginLifecycleService implements PluginLifecycleService {
    
    private static final String KEY_JVM_INFORMATION_COLLECTOR_ENABLED = "jvm-information-collector-enabled";
    
    private HTTPServer httpServer;
    
    @Override
    public void start(final PluginConfiguration pluginConfig, final boolean isEnhancedForProxy) {
        PluginConfigurationValidator.validatePort(getType(), pluginConfig);
        startServer(pluginConfig, isEnhancedForProxy);
        MetricsPool.setMetricsFactory(new PrometheusWrapperFactory());
    }
    
    private void startServer(final PluginConfiguration pluginConfig, final boolean isEnhancedForProxy) {
        registerCollector(Boolean.parseBoolean(pluginConfig.getProps().getProperty(KEY_JVM_INFORMATION_COLLECTOR_ENABLED)), isEnhancedForProxy);
        InetSocketAddress socketAddress = getSocketAddress(pluginConfig);
        try {
            httpServer = new HTTPServer(socketAddress, CollectorRegistry.defaultRegistry, true);
            log.info("Prometheus metrics HTTP server `{}:{}` start success.", socketAddress.getHostString(), socketAddress.getPort());
        } catch (final IOException ex) {
            log.error("Prometheus metrics HTTP server start fail.", ex);
        }
    }
    
    private void registerCollector(final boolean isCollectJVMInformation, final boolean isEnhancedForProxy) {
        new BuildInfoCollector(isEnhancedForProxy).register();
        if (isEnhancedForProxy) {
            new ProxyInfoCollector().register();
            new MetaDataInfoCollector().register();
        }
        if (isCollectJVMInformation) {
            DefaultExports.initialize();
        }
    }
    
    private InetSocketAddress getSocketAddress(final PluginConfiguration pluginConfig) {
        return Strings.isNullOrEmpty(pluginConfig.getHost()) ? new InetSocketAddress(pluginConfig.getPort()) : new InetSocketAddress(pluginConfig.getHost(), pluginConfig.getPort());
    }
    
    @Override
    public void close() {
        if (null != httpServer) {
            httpServer.stop();
        }
    }
    
    @Override
    public String getType() {
        return "Prometheus";
    }
}
