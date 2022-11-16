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

package org.apache.shardingsphere.infra.util.spi.type.ordered.cache;

import org.apache.shardingsphere.infra.util.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.util.spi.type.ordered.fixture.OrderedInterfaceFixture;
import org.apache.shardingsphere.infra.util.spi.type.ordered.fixture.OrderedInterfaceFixtureImpl;
import org.apache.shardingsphere.infra.util.spi.type.ordered.fixture.OrderedSPIFixture;
import org.apache.shardingsphere.infra.util.spi.type.ordered.fixture.OrderedSPIFixtureImpl;
import org.junit.After;
import org.junit.Test;

import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class OrderedServicesCacheTest {
    
    static {
        ShardingSphereServiceLoader.register(OrderedSPIFixture.class);
    }
    
    @After
    public void cleanCache() throws NoSuchFieldException, IllegalAccessException {
        Field field = OrderedServicesCache.class.getDeclaredField("softCache");
        field.setAccessible(true);
        field.set(null, new SoftReference<>(new ConcurrentHashMap<>()));
    }
    
    @Test
    public void assertFindCachedServices() {
        OrderedInterfaceFixture orderedInterfaceFixture = new OrderedInterfaceFixtureImpl();
        Collection<OrderedInterfaceFixture> customInterfaces = Collections.singleton(orderedInterfaceFixture);
        OrderedSPIFixture<?> cacheOrderedSPIFixture = new OrderedSPIFixtureImpl();
        Map<OrderedInterfaceFixture, OrderedSPIFixture<?>> cachedOrderedServices = new LinkedHashMap<>(customInterfaces.size(), 1);
        cachedOrderedServices.put(orderedInterfaceFixture, cacheOrderedSPIFixture);
        OrderedServicesCache.cacheServices(OrderedSPIFixture.class, customInterfaces, cachedOrderedServices);
        Optional<Map<?, ?>> actual = OrderedServicesCache.findCachedServices(OrderedSPIFixture.class, customInterfaces);
        assertTrue(actual.isPresent());
        assertThat(actual.get().get(orderedInterfaceFixture), is(cacheOrderedSPIFixture));
    }
    
    @Test
    public void assertFindCachedServicesWithDifferentTypesObject() {
        OrderedInterfaceFixture orderedInterfaceFixture = new OrderedInterfaceFixtureImpl();
        Collection<OrderedInterfaceFixture> customInterfaces = Collections.singleton(orderedInterfaceFixture);
        OrderedSPIFixture<?> cacheOrderedSPIFixture = new OrderedSPIFixtureImpl();
        Map<OrderedInterfaceFixture, OrderedSPIFixture<?>> cachedOrderedServices = new LinkedHashMap<>(customInterfaces.size(), 1);
        cachedOrderedServices.put(orderedInterfaceFixture, cacheOrderedSPIFixture);
        OrderedServicesCache.cacheServices(OrderedSPIFixture.class, customInterfaces, cachedOrderedServices);
        OrderedInterfaceFixture newOrderedInterfaceFixture = new OrderedInterfaceFixtureImpl();
        Collection<OrderedInterfaceFixture> newCustomInterfaces = Collections.singleton(newOrderedInterfaceFixture);
        Optional<Map<?, ?>> actual = OrderedServicesCache.findCachedServices(OrderedSPIFixture.class, newCustomInterfaces);
        assertTrue(actual.isPresent());
        assertThat(actual.get().get(orderedInterfaceFixture), is(cacheOrderedSPIFixture));
    }
    
    @Test
    public void assertNotFindCachedServices() {
        assertFalse(OrderedServicesCache.findCachedServices(OrderedSPIFixture.class, Collections.singleton(new OrderedInterfaceFixtureImpl())).isPresent());
    }
}
