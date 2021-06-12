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

package org.apache.shardingsphere.infra.spi.ordered;

import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.fixture.FixtureCustomInterface;
import org.apache.shardingsphere.infra.spi.fixture.FixtureCustomInterfaceImpl;
import org.apache.shardingsphere.infra.spi.fixture.ordered.OrderedSPIFixture;
import org.apache.shardingsphere.infra.spi.fixture.ordered.OrderedSPIFixtureImpl;
import org.apache.shardingsphere.infra.spi.ordered.cache.OrderedServicesCache;
import org.junit.After;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class OrderedSPIRegistryTest {
    
    static {
        ShardingSphereServiceLoader.register(OrderedSPIFixture.class);
    }
    
    @After
    public void cleanCache() throws NoSuchFieldException, IllegalAccessException {
        Field field = OrderedServicesCache.class.getDeclaredField("CACHED_SERVICES");
        field.setAccessible(true);
        Field modifiers = Field.class.getDeclaredField("modifiers");
        modifiers.setAccessible(true);
        modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(null, new ConcurrentHashMap<>());
    }
    
    @SuppressWarnings("rawtypes")
    @Test
    public void assertGetRegisteredServicesByClass() {
        Map<Class<?>, OrderedSPIFixture> actual = OrderedSPIRegistry.getRegisteredServicesByClass(Collections.singleton(FixtureCustomInterfaceImpl.class), OrderedSPIFixture.class);
        assertThat(actual.size(), is(1));
        assertThat(actual.get(FixtureCustomInterfaceImpl.class), instanceOf(OrderedSPIFixtureImpl.class));
    }
    
    @SuppressWarnings("rawtypes")
    @Test
    public void assertGetRegisteredServices() {
        FixtureCustomInterfaceImpl key = new FixtureCustomInterfaceImpl();
        Map<FixtureCustomInterfaceImpl, OrderedSPIFixture> actual = OrderedSPIRegistry.getRegisteredServices(Collections.singleton(key), OrderedSPIFixture.class);
        assertThat(actual.size(), is(1));
        assertThat(actual.get(key), instanceOf(OrderedSPIFixtureImpl.class));
    }

    @Test
    public void assertGetRegisteredServicesFromCache() {
        FixtureCustomInterface key = new FixtureCustomInterfaceImpl();
        assertThat(OrderedSPIRegistry.getRegisteredServices(Collections.singleton(key), OrderedSPIFixture.class), 
                is(OrderedSPIRegistry.getRegisteredServices(Collections.singleton(key), OrderedSPIFixture.class)));
    }
}
