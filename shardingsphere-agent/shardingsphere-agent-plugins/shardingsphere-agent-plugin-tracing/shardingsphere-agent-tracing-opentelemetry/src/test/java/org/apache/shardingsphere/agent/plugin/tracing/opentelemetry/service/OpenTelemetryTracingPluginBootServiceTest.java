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

package org.apache.shardingsphere.agent.plugin.tracing.opentelemetry.service;

import io.opentelemetry.api.GlobalOpenTelemetry;
import org.apache.shardingsphere.agent.config.PluginConfiguration;
import org.junit.After;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;

public final class OpenTelemetryTracingPluginBootServiceTest {

    private final OpenTelemetryTracingPluginBootService openTelemetryTracingPluginBootService = new OpenTelemetryTracingPluginBootService();

    @Test
    public void assertStart() {
        Properties props = new Properties();
        props.setProperty("otel.resource.attributes", "service.name=shardingsphere-agent");
        props.setProperty("otel.traces.exporter", "zipkin");
        PluginConfiguration configuration = new PluginConfiguration(null, 0, null, props);
        openTelemetryTracingPluginBootService.start(configuration);
        assertNotNull(GlobalOpenTelemetry.getTracerProvider());
        assertNotNull(GlobalOpenTelemetry.getTracer("shardingsphere-agent"));
    }

    @Test
    public void assertType() {
        assertThat(openTelemetryTracingPluginBootService.getType(), is("OpenTelemetry"));
    }

    @After
    public void close() {
        openTelemetryTracingPluginBootService.close();
        GlobalOpenTelemetry.resetForTest();
    }
}
