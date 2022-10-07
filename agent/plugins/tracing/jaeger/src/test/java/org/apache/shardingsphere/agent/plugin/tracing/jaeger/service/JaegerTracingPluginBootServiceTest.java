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

package org.apache.shardingsphere.agent.plugin.tracing.jaeger.service;

import io.opentracing.noop.NoopTracerFactory;
import io.opentracing.util.GlobalTracer;
import org.apache.shardingsphere.agent.config.PluginConfiguration;
import org.junit.After;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Properties;

import static org.junit.Assert.assertTrue;

public final class JaegerTracingPluginBootServiceTest {
    
    private final JaegerTracingPluginBootService jaegerTracingPluginBootService = new JaegerTracingPluginBootService();
    
    @Test
    public void assertStart() {
        jaegerTracingPluginBootService.start(new PluginConfiguration("localhost", 5775, "", createProperties()));
        assertTrue(GlobalTracer.isRegistered());
    }
    
    private Properties createProperties() {
        Properties result = new Properties();
        result.setProperty("JAEGER_SAMPLER_TYPE", "const");
        result.setProperty("JAEGER_SAMPLER_PARAM", "1");
        result.setProperty("JAEGER_REPORTER_LOG_SPANS", Boolean.TRUE.toString());
        result.setProperty("JAEGER_REPORTER_FLUSH_INTERVAL", "1");
        return result;
    }
    
    @After
    public void close() throws ReflectiveOperationException {
        jaegerTracingPluginBootService.close();
        Field field = GlobalTracer.class.getDeclaredField("tracer");
        field.setAccessible(true);
        field.set(null, NoopTracerFactory.create());
    }
}
