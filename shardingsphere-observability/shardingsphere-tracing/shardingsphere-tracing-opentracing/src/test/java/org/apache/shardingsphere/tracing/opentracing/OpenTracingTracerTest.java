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

package org.apache.shardingsphere.tracing.opentracing;

import io.opentracing.NoopTracerFactory;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import lombok.SneakyThrows;
import org.apache.shardingsphere.control.panel.spi.engine.ControlPanelFacadeEngine;
import org.apache.shardingsphere.control.panel.spi.opentracing.OpenTracingConfiguration;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.tracing.opentracing.fixture.FooTracer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class OpenTracingTracerTest {
    
    @Before
    public void setUp() {
        System.setProperty(OpenTracingTracer.OPENTRACING_TRACER_CLASS_NAME, FooTracer.class.getName());
        clearGlobalTracer();
    }
    
    @After
    public void tearDown() {
        System.getProperties().remove(OpenTracingTracer.OPENTRACING_TRACER_CLASS_NAME);
    }
    
    @Test
    public void assertDuplicatedLoading() {
        OpenTracingTracer.init(mock(Tracer.class));
        Tracer tracer = OpenTracingTracer.get();
        OpenTracingTracer.init();
        assertThat(OpenTracingTracer.get(), is(tracer));
        OpenTracingTracer.init(mock(Tracer.class));
        assertThat(OpenTracingTracer.get(), is(tracer));
    }
    
    @Test
    public void assertTracer() {
        OpenTracingTracer.init();
        assertThat((GlobalTracer) OpenTracingTracer.get(), isA(GlobalTracer.class));
        assertTrue(GlobalTracer.isRegistered());
        assertThat(OpenTracingTracer.get(), is(OpenTracingTracer.get()));
    }
    
    @Test
    public void assertTracerSpiLoad() {
        OpenTracingConfiguration openTracingConfiguration = new OpenTracingConfiguration();
        new ControlPanelFacadeEngine().init(Collections.singleton(openTracingConfiguration));
        assertThat((GlobalTracer) OpenTracingTracer.get(), isA(GlobalTracer.class));
        assertTrue(GlobalTracer.isRegistered());
        assertThat(OpenTracingTracer.get(), is(OpenTracingTracer.get()));
    }
    
    @Test(expected = ShardingSphereException.class)
    public void assertTracerClassError() {
        System.setProperty(OpenTracingTracer.OPENTRACING_TRACER_CLASS_NAME, "com.foo.FooTracer");
        OpenTracingTracer.init();
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private static void clearGlobalTracer() {
        Field tracerField = GlobalTracer.class.getDeclaredField("tracer");
        tracerField.setAccessible(true);
        tracerField.set(GlobalTracer.class, NoopTracerFactory.create());
    }
}
