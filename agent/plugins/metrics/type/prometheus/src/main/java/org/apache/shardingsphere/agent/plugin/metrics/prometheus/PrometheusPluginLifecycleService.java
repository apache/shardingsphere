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
import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.client.hotspot.DefaultExports;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.agent.api.PluginConfiguration;
import org.apache.shardingsphere.agent.plugin.core.config.validator.PluginConfigurationValidator;
import org.apache.shardingsphere.agent.plugin.core.context.PluginContext;
import org.apache.shardingsphere.agent.plugin.metrics.core.exporter.impl.BuildInfoExporter;
import org.apache.shardingsphere.agent.plugin.metrics.core.exporter.impl.jdbc.JDBCMetaDataInfoExporter;
import org.apache.shardingsphere.agent.plugin.metrics.core.exporter.impl.jdbc.JDBCStateExporter;
import org.apache.shardingsphere.agent.plugin.metrics.core.exporter.impl.proxy.ProxyMetaDataInfoExporter;
import org.apache.shardingsphere.agent.plugin.metrics.core.exporter.impl.proxy.ProxyStateExporter;
import org.apache.shardingsphere.agent.plugin.metrics.prometheus.datasource.HikariMonitor;
import org.apache.shardingsphere.agent.plugin.metrics.prometheus.exoprter.PrometheusMetricsExporter;
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
        PluginContext.getInstance().setEnhancedForProxy(isEnhancedForProxy);
        PluginConfigurationValidator.validatePort(getType(), pluginConfig);
        startServer(pluginConfig, isEnhancedForProxy);
    }
    
    private void startServer(final PluginConfiguration pluginConfig, final boolean isEnhancedForProxy) {
        registerCollector(Boolean.parseBoolean(pluginConfig.getProps().getProperty(KEY_JVM_INFORMATION_COLLECTOR_ENABLED)), isEnhancedForProxy);
        InetSocketAddress socketAddress = getSocketAddress(pluginConfig);
        HikariMonitor hikaricpMonitor = new HikariMonitor();
        hikaricpMonitor.startScheduleMonitor(pluginConfig.getProps().getProperty("hikaricp-jmx-port"));
        try {
            httpServer = new HTTPServer(socketAddress, CollectorRegistry.defaultRegistry, true);
            log.info("Prometheus metrics HTTP server `{}:{}` start success.", socketAddress.getHostString(), socketAddress.getPort());
        } catch (final IOException ex) {
            log.error("Prometheus metrics HTTP server start fail.", ex);
        }
    }
    
    private void registerCollector(final boolean isCollectJVMInformation, final boolean isEnhancedForProxy) {
        new PrometheusMetricsExporter(new BuildInfoExporter()).register();
        if (isEnhancedForProxy) {
            registerCollectorForProxy();
        } else {
            registerCollectorForJDBC();
        }
        if (isCollectJVMInformation) {
            DefaultExports.initialize();
        }
    }
    
    private void registerCollectorForProxy() {
        new PrometheusMetricsExporter(new ProxyStateExporter()).register();
        new PrometheusMetricsExporter(new ProxyMetaDataInfoExporter()).register();
    }
    
    private void registerCollectorForJDBC() {
        new PrometheusMetricsExporter(new JDBCStateExporter()).register();
        new PrometheusMetricsExporter(new JDBCMetaDataInfoExporter()).register();
    }
    
    private InetSocketAddress getSocketAddress(final PluginConfiguration pluginConfig) {
        return isNullOrEmpty(pluginConfig.getHost()) ? new InetSocketAddress(pluginConfig.getPort()) : new InetSocketAddress(pluginConfig.getHost(), pluginConfig.getPort());
    }
    
    private boolean isNullOrEmpty(final String value) {
        return null == value || value.isEmpty();
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
