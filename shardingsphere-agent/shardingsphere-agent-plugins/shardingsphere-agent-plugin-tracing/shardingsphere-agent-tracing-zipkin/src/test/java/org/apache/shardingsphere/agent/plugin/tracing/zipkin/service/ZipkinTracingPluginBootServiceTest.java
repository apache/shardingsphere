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

package org.apache.shardingsphere.agent.plugin.tracing.zipkin.service;

import brave.Tracing;
import org.apache.shardingsphere.agent.config.PluginConfiguration;
import org.junit.After;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Properties;

import static org.junit.Assert.assertNotNull;

public final class ZipkinTracingPluginBootServiceTest {
    
    private final ZipkinTracingPluginBootService zipkinTracingPluginBootService = new ZipkinTracingPluginBootService();
    
    @Test
    public void assertStart() throws ReflectiveOperationException {
        zipkinTracingPluginBootService.start(new PluginConfiguration("localhost", 9441, "", new Properties()));
        Field field = ZipkinTracingPluginBootService.class.getDeclaredField("tracing");
        field.setAccessible(true);
        Tracing tracing = (Tracing) field.get(zipkinTracingPluginBootService);
        assertNotNull(tracing.tracer());
    }
    
    @After
    public void close() {
        zipkinTracingPluginBootService.close();
    }
}
