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

package org.apache.shardingsphere.infra.util.spi.type.ordered;

import org.apache.shardingsphere.infra.util.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.util.spi.type.ordered.fixture.OrderedInterfaceFixture;
import org.apache.shardingsphere.infra.util.spi.type.ordered.fixture.OrderedInterfaceFixtureImpl;
import org.apache.shardingsphere.infra.util.spi.type.ordered.fixture.OrderedSPIFixture;
import org.apache.shardingsphere.infra.util.spi.type.ordered.fixture.OrderedSPIFixtureImpl;
import org.apache.shardingsphere.infra.util.spi.type.ordered.cache.OrderedServicesCache;
import org.junit.After;
import org.junit.Test;

import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class OrderedSPIRegistryTest {
    
    static {
        ShardingSphereServiceLoader.register(OrderedSPIFixture.class);
    }
    
    @After
    public void cleanCache() throws NoSuchFieldException, IllegalAccessException {
        Field field = OrderedServicesCache.class.getDeclaredField("softCache");
        field.setAccessible(true);
        field.set(null, new SoftReference<>(new ConcurrentHashMap<>()));
    }
    
    @SuppressWarnings("rawtypes")
    @Test
    public void assertGetRegisteredServicesByClass() {
        Map<Class<?>, OrderedSPIFixture> actual = OrderedSPIRegistry.getRegisteredServicesByClass(OrderedSPIFixture.class, Collections.singleton(OrderedInterfaceFixtureImpl.class));
        assertThat(actual.size(), is(1));
        assertThat(actual.get(OrderedInterfaceFixtureImpl.class), instanceOf(OrderedSPIFixtureImpl.class));
    }
    
    @SuppressWarnings("rawtypes")
    @Test
    public void assertGetRegisteredServices() {
        OrderedInterfaceFixtureImpl key = new OrderedInterfaceFixtureImpl();
        Map<OrderedInterfaceFixtureImpl, OrderedSPIFixture> actual = OrderedSPIRegistry.getRegisteredServices(OrderedSPIFixture.class, Collections.singleton(key));
        assertThat(actual.size(), is(1));
        assertThat(actual.get(key), instanceOf(OrderedSPIFixtureImpl.class));
    }
    
    @Test
    public void assertGetRegisteredServicesFromCache() {
        OrderedInterfaceFixture key = new OrderedInterfaceFixtureImpl();
        assertThat(OrderedSPIRegistry.getRegisteredServices(OrderedSPIFixture.class, Collections.singleton(key)),
                is(OrderedSPIRegistry.getRegisteredServices(OrderedSPIFixture.class, Collections.singleton(key))));
    }
}
