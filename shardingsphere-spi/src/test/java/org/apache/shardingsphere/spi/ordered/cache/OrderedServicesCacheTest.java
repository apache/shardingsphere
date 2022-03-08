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

package org.apache.shardingsphere.spi.ordered.cache;

import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.fixture.FixtureCustomInterface;
import org.apache.shardingsphere.spi.fixture.FixtureCustomInterfaceImpl;
import org.apache.shardingsphere.spi.fixture.ordered.OrderedSPIFixture;
import org.apache.shardingsphere.spi.fixture.ordered.OrderedSPIFixtureImpl;
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
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
        FixtureCustomInterface fixtureCustomInterface = new FixtureCustomInterfaceImpl();
        Collection<FixtureCustomInterface> customInterfaces = Collections.singleton(fixtureCustomInterface);
        OrderedSPIFixture<?> cacheOrderedSPIFixture = new OrderedSPIFixtureImpl();
        Map<FixtureCustomInterface, OrderedSPIFixture> cachedOrderedServices = new LinkedHashMap<>(customInterfaces.size(), 1);
        cachedOrderedServices.put(fixtureCustomInterface, cacheOrderedSPIFixture);
        OrderedServicesCache.cacheServices(OrderedSPIFixture.class, customInterfaces, cachedOrderedServices);
        Optional<Map<?, ?>> actual = OrderedServicesCache.findCachedServices(OrderedSPIFixture.class, customInterfaces);
        assertTrue(actual.isPresent());
        assertThat(actual.get().get(fixtureCustomInterface), is(cacheOrderedSPIFixture));
    }
    
    @Test
    public void assertNotFindCachedServices() {
        assertFalse(OrderedServicesCache.findCachedServices(OrderedSPIFixture.class, Collections.singleton(new FixtureCustomInterfaceImpl())).isPresent());
    }
}
