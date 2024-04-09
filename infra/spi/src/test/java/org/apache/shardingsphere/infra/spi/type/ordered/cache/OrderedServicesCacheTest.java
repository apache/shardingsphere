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

package org.apache.shardingsphere.infra.spi.type.ordered.cache;

import org.apache.shardingsphere.infra.spi.type.ordered.fixture.OrderedInterfaceFixture;
import org.apache.shardingsphere.infra.spi.type.ordered.fixture.OrderedSPIFixture;
import org.apache.shardingsphere.infra.spi.type.ordered.fixture.impl.OrderedInterfaceFixtureImpl;
import org.apache.shardingsphere.infra.spi.type.ordered.fixture.impl.OrderedSPIFixtureImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderedServicesCacheTest {
    
    @AfterEach
    void cleanCache() throws ReflectiveOperationException {
        Plugins.getMemberAccessor().set(OrderedServicesCache.class.getDeclaredField("cache"), OrderedServicesCache.class, new SoftReference<>(new ConcurrentHashMap<>()));
    }
    
    @Test
    void assertCacheServicesAndClear() {
        OrderedInterfaceFixture orderedInterfaceFixture = new OrderedInterfaceFixtureImpl();
        Collection<OrderedInterfaceFixture> customInterfaces = Collections.singleton(orderedInterfaceFixture);
        OrderedSPIFixture<?> cacheOrderedSPIFixture = new OrderedSPIFixtureImpl();
        Map<OrderedInterfaceFixture, OrderedSPIFixture<?>> cachedOrderedServices = new LinkedHashMap<>(customInterfaces.size(), 1F);
        cachedOrderedServices.put(orderedInterfaceFixture, cacheOrderedSPIFixture);
        OrderedServicesCache.cacheServices(OrderedSPIFixture.class, customInterfaces, cachedOrderedServices);
        Optional<Map<?, ?>> actual = OrderedServicesCache.findCachedServices(OrderedSPIFixture.class, customInterfaces);
        assertTrue(actual.isPresent());
        assertThat(actual.get().get(orderedInterfaceFixture), is(cacheOrderedSPIFixture));
        OrderedServicesCache.clearCache();
        assertFalse(OrderedServicesCache.findCachedServices(OrderedSPIFixture.class, customInterfaces).isPresent());
    }
    
    @Test
    void assertNotFindCachedServices() {
        assertFalse(OrderedServicesCache.findCachedServices(OrderedSPIFixture.class, Collections.singleton(new OrderedInterfaceFixtureImpl())).isPresent());
    }
}
