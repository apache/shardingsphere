/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.opentracing;

import com.google.common.collect.HashMultimap;
import com.google.common.eventbus.EventBus;
import io.opentracing.NoopTracerFactory;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.core.event.ShardingEventBusInstance;
import io.shardingsphere.opentracing.fixture.FooTracer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class ShardingTracerTest {
    
    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        System.setProperty("shardingsphere.opentracing.tracer.class", FooTracer.class.getName());
        clearGlobalTracer();
        unregisterEventBus();
    }
    
    @After
    public void tearDown() {
        System.getProperties().remove("shardingsphere.opentracing.tracer.class");
    }
    
    @Test
    public void assertDuplicatedLoading() {
        ShardingTracer.init(mock(Tracer.class));
        Tracer t1 = ShardingTracer.get();
        ShardingTracer.init();
        assertEquals(t1, ShardingTracer.get());
        ShardingTracer.init(mock(Tracer.class));
        assertEquals(t1, ShardingTracer.get());
    }
    
    @Test
    public void assertTracer() {
        ShardingTracer.init();
        assertThat((GlobalTracer) ShardingTracer.get(), isA(GlobalTracer.class));
        assertTrue(GlobalTracer.isRegistered());
        assertThat(ShardingTracer.get(), is(ShardingTracer.get()));
    }
    
    @Test(expected = ShardingException.class)
    public void assertTracerClassError() {
        System.setProperty("shardingsphere.opentracing.tracer.class", "com.foo.FooTracer");
        ShardingTracer.init();
    }
    
    private static void clearGlobalTracer() throws NoSuchFieldException, IllegalAccessException {
        Field tracerField = GlobalTracer.class.getDeclaredField("tracer");
        tracerField.setAccessible(true);
        tracerField.set(GlobalTracer.class, NoopTracerFactory.create());
    }
    
    private static void unregisterEventBus() throws NoSuchFieldException, IllegalAccessException {
        Field subscribersByTypeField = EventBus.class.getDeclaredField("subscribersByType");
        subscribersByTypeField.setAccessible(true);
        subscribersByTypeField.set(ShardingEventBusInstance.getInstance(), HashMultimap.create());
    }
}
