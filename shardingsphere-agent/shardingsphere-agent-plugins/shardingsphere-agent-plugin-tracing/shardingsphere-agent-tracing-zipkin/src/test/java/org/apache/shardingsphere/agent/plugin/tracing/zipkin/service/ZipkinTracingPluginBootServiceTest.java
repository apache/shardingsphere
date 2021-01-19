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
import java.lang.reflect.Field;
import java.util.Properties;
import lombok.SneakyThrows;
import org.apache.shardingsphere.agent.config.PluginConfiguration;

import org.junit.AfterClass;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public final class ZipkinTracingPluginBootServiceTest {
    
    private static ZipkinTracingPluginBootService zipkinTracingPluginBootService = new ZipkinTracingPluginBootService();
    
    @SneakyThrows
    @Test
    public void assertStart() {
        PluginConfiguration configuration = new PluginConfiguration("localhost", 9441, "", new Properties());
        zipkinTracingPluginBootService.start(configuration);
        Field field = ZipkinTracingPluginBootService.class.getDeclaredField("tracing");
        field.setAccessible(true);
        Tracing tracing = (Tracing) field.get(zipkinTracingPluginBootService);
        assertNotNull(tracing.tracer());
    }
    
    @Test
    public void assertType() {
        assertThat(zipkinTracingPluginBootService.getType(), is("Zipkin"));
    }
    
    @AfterClass
    public static void close() {
        zipkinTracingPluginBootService.close();
    }
}
