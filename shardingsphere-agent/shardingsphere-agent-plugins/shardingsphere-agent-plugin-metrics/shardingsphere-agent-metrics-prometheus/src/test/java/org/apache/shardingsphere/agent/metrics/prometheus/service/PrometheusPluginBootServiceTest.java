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

import io.prometheus.client.exporter.HTTPServer;
import lombok.SneakyThrows;
import org.apache.shardingsphere.agent.config.PluginConfiguration;
import org.junit.AfterClass;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public final class PrometheusPluginBootServiceTest {
    
    private static PrometheusPluginBootService prometheusPluginBootService = new PrometheusPluginBootService();
    
    @SneakyThrows
    @Test
    public void assertStart() {
        Properties props = new Properties();
        props.setProperty("JVM_INFORMATION_COLLECTOR_ENABLED", "true");
        PluginConfiguration configuration = new PluginConfiguration("localhost", 8090, "", props);
        prometheusPluginBootService.start(configuration);
        Field field = PrometheusPluginBootService.class.getDeclaredField("httpServer");
        field.setAccessible(true);
        HTTPServer httpServer = (HTTPServer) field.get(prometheusPluginBootService);
        assertNotNull(httpServer);
        assertThat(httpServer.getPort(), is(8090));
    }
    
    @Test
    public void assertType() {
        assertThat(prometheusPluginBootService.getType(), is("Prometheus"));
    }
    
    @AfterClass
    public static void close() {
        prometheusPluginBootService.close();
    }
}
