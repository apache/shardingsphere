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

package org.apache.shardingsphere.infra.spi.type.ordered;

import org.apache.shardingsphere.infra.spi.type.ordered.fixture.OrderedSPINonSingletonFixture;
import org.apache.shardingsphere.infra.spi.type.ordered.cache.OrderedServicesCache;
import org.apache.shardingsphere.infra.spi.type.ordered.fixture.OrderedInterfaceFixture;
import org.apache.shardingsphere.infra.spi.type.ordered.fixture.OrderedSPIFixture;
import org.apache.shardingsphere.infra.spi.type.ordered.fixture.impl.OrderedInterfaceFixtureImpl;
import org.apache.shardingsphere.infra.spi.type.ordered.fixture.impl.OrderedSPIFixtureImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;

class OrderedSPILoaderTest {
    
    @SuppressWarnings("CollectionWithoutInitialCapacity")
    @AfterEach
    void cleanCache() throws ReflectiveOperationException {
        Plugins.getMemberAccessor().set(OrderedServicesCache.class.getDeclaredField("cache"), OrderedServicesCache.class, new SoftReference<>(new ConcurrentHashMap<>()));
    }
    
    @SuppressWarnings("rawtypes")
    @Test
    void assertGetServicesByClass() {
        Map<Class<?>, OrderedSPIFixture> actual = OrderedSPILoader.getServicesByClass(OrderedSPIFixture.class, Collections.singleton(OrderedInterfaceFixtureImpl.class));
        assertThat(actual.size(), is(1));
        assertThat(actual.get(OrderedInterfaceFixtureImpl.class), isA(OrderedSPIFixtureImpl.class));
    }
    
    @SuppressWarnings("rawtypes")
    @Test
    void assertGetServices() {
        OrderedInterfaceFixtureImpl key = new OrderedInterfaceFixtureImpl();
        Map<OrderedInterfaceFixtureImpl, OrderedSPIFixture> actual = OrderedSPILoader.getServices(OrderedSPIFixture.class, Collections.singleton(key));
        assertThat(actual.size(), is(1));
        assertThat(actual.get(key), isA(OrderedSPIFixtureImpl.class));
    }
    
    @Test
    void assertGetServicesFromCache() {
        OrderedInterfaceFixture key = new OrderedInterfaceFixtureImpl();
        assertThat(OrderedSPILoader.getServices(OrderedSPIFixture.class, Collections.singleton(key)),
                is(OrderedSPILoader.getServices(OrderedSPIFixture.class, Collections.singleton(key))));
    }
    
    @SuppressWarnings("rawtypes")
    @Test
    void assertGetServicesByClassWithSingletonSPIReturnsSameInstance() {
        Map<Class<?>, OrderedSPIFixture> firstCall = OrderedSPILoader.getServicesByClass(OrderedSPIFixture.class, Collections.singleton(OrderedInterfaceFixtureImpl.class));
        Map<Class<?>, OrderedSPIFixture> secondCall = OrderedSPILoader.getServicesByClass(OrderedSPIFixture.class, Collections.singleton(OrderedInterfaceFixtureImpl.class));
        assertThat(firstCall.get(OrderedInterfaceFixtureImpl.class), is(sameInstance(secondCall.get(OrderedInterfaceFixtureImpl.class))));
    }
    
    @SuppressWarnings("rawtypes")
    @Test
    void assertGetServicesWithSingletonSPIReturnsSameInstance() {
        OrderedInterfaceFixtureImpl key = new OrderedInterfaceFixtureImpl();
        Map<OrderedInterfaceFixtureImpl, OrderedSPIFixture> firstCall = OrderedSPILoader.getServices(OrderedSPIFixture.class, Collections.singleton(key));
        Map<OrderedInterfaceFixtureImpl, OrderedSPIFixture> secondCall = OrderedSPILoader.getServices(OrderedSPIFixture.class, Collections.singleton(key));
        assertThat(firstCall.get(key), is(sameInstance(secondCall.get(key))));
    }
    
    @SuppressWarnings("rawtypes")
    @Test
    void assertGetServicesByClassWithNonSingletonSPIReturnsDifferentInstances() {
        Map<Class<?>, OrderedSPINonSingletonFixture> firstCall = OrderedSPILoader.getServicesByClass(OrderedSPINonSingletonFixture.class, Collections.singleton(OrderedInterfaceFixtureImpl.class));
        Map<Class<?>, OrderedSPINonSingletonFixture> secondCall = OrderedSPILoader.getServicesByClass(OrderedSPINonSingletonFixture.class, Collections.singleton(OrderedInterfaceFixtureImpl.class));
        assertThat(firstCall.get(OrderedInterfaceFixtureImpl.class), is(not(sameInstance(secondCall.get(OrderedInterfaceFixtureImpl.class)))));
    }
    
    @SuppressWarnings("rawtypes")
    @Test
    void assertGetServicesWithNonSingletonSPIReturnsDifferentInstances() {
        OrderedInterfaceFixtureImpl key = new OrderedInterfaceFixtureImpl();
        Map<OrderedInterfaceFixtureImpl, OrderedSPINonSingletonFixture> firstCall = OrderedSPILoader.getServices(OrderedSPINonSingletonFixture.class, Collections.singleton(key));
        Map<OrderedInterfaceFixtureImpl, OrderedSPINonSingletonFixture> secondCall = OrderedSPILoader.getServices(OrderedSPINonSingletonFixture.class, Collections.singleton(key));
        assertThat(firstCall.get(key), is(not(sameInstance(secondCall.get(key)))));
    }
}
