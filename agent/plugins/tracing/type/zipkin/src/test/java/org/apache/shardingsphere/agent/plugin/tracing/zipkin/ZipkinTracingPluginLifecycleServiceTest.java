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

package org.apache.shardingsphere.agent.plugin.tracing.zipkin;

import brave.Tracing;
import org.apache.shardingsphere.agent.api.PluginConfiguration;
import org.junit.After;
import org.junit.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import java.util.Properties;

import static org.junit.Assert.assertNotNull;

public final class ZipkinTracingPluginLifecycleServiceTest {
    
    private final ZipkinTracingPluginLifecycleService pluginLifecycleService = new ZipkinTracingPluginLifecycleService();
    
    @After
    public void close() {
        pluginLifecycleService.close();
    }
    
    @Test
    public void assertStart() throws ReflectiveOperationException {
        pluginLifecycleService.start(new PluginConfiguration("localhost", 9441, "", new Properties()), true);
        Tracing tracing = (Tracing) Plugins.getMemberAccessor().get(ZipkinTracingPluginLifecycleService.class.getDeclaredField("tracing"), pluginLifecycleService);
        assertNotNull(tracing.tracer());
    }
}
