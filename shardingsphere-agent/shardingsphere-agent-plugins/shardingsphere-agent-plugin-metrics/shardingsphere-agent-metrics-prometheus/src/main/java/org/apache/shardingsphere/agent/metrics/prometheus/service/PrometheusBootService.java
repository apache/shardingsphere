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
import org.apache.shardingsphere.agent.core.config.PrometheusPluginConfiguration;
import org.apache.shardingsphere.agent.core.constant.AgentConstant;
import org.apache.shardingsphere.agent.core.plugin.service.BootService;
import org.apache.shardingsphere.agent.metrics.prometheus.collector.BuildInfoCollector;

/**
 * Prometheus boot service.
 */
@Slf4j
public class PrometheusBootService implements BootService<PrometheusPluginConfiguration> {
    
    private HTTPServer httpServer;
    
    @Override
    public void setup(final PrometheusPluginConfiguration configuration) {
        registerJvm(configuration);
    }
    
    @Override
    public void start(final PrometheusPluginConfiguration configuration) {
        startServer(configuration);
    }
    
    @Override
    public void cleanup() {
        if (null != httpServer) {
            httpServer.stop();
        }
    }
    
    @Override
    public String getType() {
        return AgentConstant.PLUGIN_NAME_PROMETHEUS;
    }
    
    private void registerJvm(final PrometheusPluginConfiguration configuration) {
        boolean enabled = configuration.isJvmInformationCollectorEnabled();
        if (enabled) {
            new BuildInfoCollector().register();
            DefaultExports.initialize();
        }
    }
    
    private void startServer(final PrometheusPluginConfiguration configuration) {
        int port = configuration.getPort();
        String host = configuration.getHost();
        InetSocketAddress inetSocketAddress;
        if ("".equals(host) || null == host) {
            inetSocketAddress = new InetSocketAddress(port);
        } else {
            inetSocketAddress = new InetSocketAddress(host, port);
        }
        try {
            httpServer = new HTTPServer(inetSocketAddress, CollectorRegistry.defaultRegistry, true);
            log.info(String.format("you start prometheus metrics http server host is: %s , port is: %s", inetSocketAddress.getHostString(), inetSocketAddress.getPort()));
        } catch (final IOException exception) {
            log.error("you start prometheus metrics http server is error", exception);
        }
    }
}
