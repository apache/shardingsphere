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

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.HTTPServer;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.shardingsphere.metrics.api.MetricsTrackerFactory;
import org.apache.shardingsphere.metrics.spi.MetricsTrackerManager;

import java.net.InetSocketAddress;
import java.util.Properties;

/**
 * Prometheus metrics tracker manager.
 */
@Getter
@Setter
public final class PrometheusMetricsTrackerManager implements MetricsTrackerManager {
    
    private Properties properties = new Properties();
    
    private MetricsTrackerFactory metricsTrackerFactory = new PrometheusMetricsTrackerFactory();
    
    @Override
    public String getType() {
        return "prometheus";
    }
    
    @SneakyThrows
    @Override
    public void init(final int port) {
        new HTTPServer(new InetSocketAddress(port), CollectorRegistry.defaultRegistry, true);
    }
}

