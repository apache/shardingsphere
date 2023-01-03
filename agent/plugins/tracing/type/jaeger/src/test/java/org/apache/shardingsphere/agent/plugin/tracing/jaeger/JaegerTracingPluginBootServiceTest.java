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

package org.apache.shardingsphere.agent.plugin.tracing.jaeger;

import io.opentracing.noop.NoopTracerFactory;
import io.opentracing.util.GlobalTracer;
import org.apache.shardingsphere.agent.api.PluginConfiguration;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.After;
import org.junit.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import java.util.Properties;

import static org.junit.Assert.assertTrue;

public final class JaegerTracingPluginBootServiceTest {
    
    private final JaegerTracingPluginBootService pluginBootService = new JaegerTracingPluginBootService();
    
    @After
    public void close() throws ReflectiveOperationException {
        pluginBootService.close();
        Plugins.getMemberAccessor().set(GlobalTracer.class.getDeclaredField("tracer"), GlobalTracer.class, NoopTracerFactory.create());
    }
    
    @Test
    public void assertStart() {
        Properties props = PropertiesBuilder.build(
                new Property("JAEGER_SAMPLER_TYPE", "const"),
                new Property("JAEGER_SAMPLER_PARAM", "1"),
                new Property("JAEGER_REPORTER_LOG_SPANS", Boolean.TRUE.toString()),
                new Property("JAEGER_REPORTER_FLUSH_INTERVAL", "1"));
        pluginBootService.start(new PluginConfiguration("localhost", 5775, "", props), true);
        assertTrue(GlobalTracer.isRegistered());
    }
}
