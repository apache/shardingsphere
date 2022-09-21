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

import com.google.common.base.Preconditions;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.client.hotspot.DefaultExports;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.agent.config.PluginConfiguration;
import org.apache.shardingsphere.agent.metrics.api.MetricsPool;
import org.apache.shardingsphere.agent.metrics.prometheus.collector.BuildInfoCollector;
import org.apache.shardingsphere.agent.metrics.prometheus.collector.MetaDataInfoCollector;
import org.apache.shardingsphere.agent.metrics.prometheus.collector.ProxyInfoCollector;
import org.apache.shardingsphere.agent.metrics.prometheus.wrapper.PrometheusWrapperFactory;
import org.apache.shardingsphere.agent.spi.boot.PluginBootService;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Prometheus plugin boot service.
 */
@Slf4j
public final class PrometheusPluginBootService implements PluginBootService {
    
    private static final String KEY_JVM_INFORMATION_COLLECTOR_ENABLED = "jvm-information-collector-enabled";
    
    private HTTPServer httpServer;
    
    @Override
    public void start(final PluginConfiguration pluginConfig) {
        Preconditions.checkState(pluginConfig.getPort() > 0, "Prometheus config error, host is null or port is `%s`", pluginConfig.getPort());
        startServer(pluginConfig);
        MetricsPool.setMetricsFactory(new PrometheusWrapperFactory());
    }
    
    private void startServer(final PluginConfiguration pluginConfig) {
        registerCollector(Boolean.parseBoolean(pluginConfig.getProps().getProperty(KEY_JVM_INFORMATION_COLLECTOR_ENABLED)));
        InetSocketAddress socketAddress = getSocketAddress(pluginConfig.getHost(), pluginConfig.getPort());
        try {
            httpServer = new HTTPServer(socketAddress, CollectorRegistry.defaultRegistry, true);
            log.info("Prometheus metrics HTTP server `{}:{}` start success", socketAddress.getHostString(), socketAddress.getPort());
        } catch (final IOException ex) {
            log.error("Prometheus metrics HTTP server start fail", ex);
        }
    }
    
    private void registerCollector(final boolean enabled) {
        new ProxyInfoCollector().register();
        new BuildInfoCollector().register();
        new MetaDataInfoCollector().register();
        if (enabled) {
            DefaultExports.initialize();
        }
    }
    
    private InetSocketAddress getSocketAddress(final String host, final int port) {
        return null == host || "".equals(host) ? new InetSocketAddress(port) : new InetSocketAddress(host, port);
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
