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

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.client.hotspot.DefaultExports;
import java.io.IOException;
import java.net.InetSocketAddress;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.agent.config.PluginConfiguration;
import org.apache.shardingsphere.agent.exception.PluginConfigurationException;
import org.apache.shardingsphere.agent.metrics.api.reporter.MetricsReporter;
import org.apache.shardingsphere.agent.metrics.prometheus.collector.BuildInfoCollector;
import org.apache.shardingsphere.agent.metrics.prometheus.register.PrometheusMetricsRegister;
import org.apache.shardingsphere.agent.spi.boot.PluginBootService;

/**
 * Prometheus plugin boot service.
 */
@Slf4j
public final class PrometheusPluginBootService implements PluginBootService {
    
    private HTTPServer httpServer;
    
    @Override
    public void start(final PluginConfiguration pluginConfiguration) {
        if (!checkConfig(pluginConfiguration)) {
            throw new PluginConfigurationException("prometheus config error, host is null or port is %s", pluginConfiguration.getPort());
        }
        startServer(pluginConfiguration);
        MetricsReporter.register(PrometheusMetricsRegister.getInstance());
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
    
    private boolean checkConfig(final PluginConfiguration pluginConfiguration) {
        return pluginConfiguration.getPort() > 0;
    }
    
    private void startServer(final PluginConfiguration configuration) {
        boolean enabled = Boolean.parseBoolean(configuration.getProps().getProperty("JVM_INFORMATION_COLLECTOR_ENABLED"));
        registerJvm(enabled);
        int port = configuration.getPort();
        String host = configuration.getHost();
        InetSocketAddress inetSocketAddress;
        if (null == host || "".equalsIgnoreCase(host)) {
            inetSocketAddress = new InetSocketAddress(port);
        } else {
            inetSocketAddress = new InetSocketAddress(host, port);
        }
        try {
            httpServer = new HTTPServer(inetSocketAddress, CollectorRegistry.defaultRegistry, true);
            log.info(String.format("Prometheus metrics HTTP server `%s:%s` start success.", inetSocketAddress.getHostString(), inetSocketAddress.getPort()));
        } catch (final IOException ex) {
            log.error("Prometheus metrics HTTP server start fail", ex);
        }
    }
    
    private void registerJvm(final boolean enabled) {
        if (enabled) {
            new BuildInfoCollector().register();
            DefaultExports.initialize();
        }
    }
}
