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

package org.apache.shardingsphere.agent.plugin.tracing.opentracing.service;

import io.opentracing.util.GlobalTracer;
import org.apache.shardingsphere.agent.config.PluginConfiguration;
import org.junit.After;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class OpenTracingPluginBootServiceTest {
    
    private final OpenTracingPluginBootService openTracingPluginBootService = new OpenTracingPluginBootService();
    
    @Test
    public void assertStart() {
        Properties props = new Properties();
        props.setProperty("OPENTRACING_TRACER_CLASS_NAME", "io.opentracing.mock.MockTracer");
        PluginConfiguration configuration = new PluginConfiguration("localhost", 8090, "", props);
        openTracingPluginBootService.start(configuration);
        assertTrue(GlobalTracer.isRegistered());
    }
    
    @Test
    public void assertType() {
        assertThat(openTracingPluginBootService.getType(), is("Opentracing"));
    }
    
    @After
    public void close() {
        openTracingPluginBootService.close();
    }
}
