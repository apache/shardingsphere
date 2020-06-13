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

package org.apache.shardingsphere.metrics.prometheus;

import com.google.common.base.Strings;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.HTTPServer;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.metrics.api.MetricsTrackerFactory;
import org.apache.shardingsphere.metrics.configuration.config.MetricsConfiguration;
import org.apache.shardingsphere.metrics.spi.MetricsTrackerManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Properties;

/**
 * Prometheus metrics tracker manager.
 */
@Getter
@Setter
@Slf4j
public final class PrometheusMetricsTrackerManager implements MetricsTrackerManager {
    
    private final MetricsTrackerFactory metricsTrackerFactory = new PrometheusMetricsTrackerFactory();
    
    private Properties props = new Properties();
    
    private HTTPServer server;
    
    @Override
    public String getType() {
        return "prometheus";
    }
    
    @SneakyThrows(IOException.class)
    @Override
    public void start(final MetricsConfiguration metricsConfiguration) {
        InetSocketAddress inetSocketAddress;
        if (Strings.isNullOrEmpty(metricsConfiguration.getHost())) {
            inetSocketAddress = new InetSocketAddress(metricsConfiguration.getPort());
        } else {
            inetSocketAddress = new InetSocketAddress(metricsConfiguration.getHost(), metricsConfiguration.getPort());
        }
        server = new HTTPServer(inetSocketAddress, CollectorRegistry.defaultRegistry, true);
        log.info("you start prometheus metrics http server  host is :{}, port is :{} ", inetSocketAddress.getHostString(), inetSocketAddress.getPort());
    }
    
    @Override
    public void stop() {
        server.stop();
    }
}

