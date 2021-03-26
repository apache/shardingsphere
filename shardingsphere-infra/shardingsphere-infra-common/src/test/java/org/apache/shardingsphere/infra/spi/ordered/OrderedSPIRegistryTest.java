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

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.fixture.FixtureCustomInterface;
import org.apache.shardingsphere.infra.spi.fixture.FixtureCustomInterfaceImpl;
import org.apache.shardingsphere.infra.spi.fixture.OrderedSPIFixture;
import org.apache.shardingsphere.infra.spi.fixture.OrderedSPIFixtureImpl;
import org.apache.shardingsphere.infra.spi.ordered.cache.CachedOrderedServices;
import org.apache.shardingsphere.infra.spi.ordered.cache.OrderedServicesCache;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class OrderedSPIRegistryTest {

    private Collection<FixtureCustomInterface> customInterfaceCollection;

    private FixtureCustomInterface fixtureCustomInterface;

    private OrderedSPIFixture cacheOrderedSPIFixture;

    @Before
    public void init() {
        ShardingSphereServiceLoader.register(OrderedSPIFixture.class);

        customInterfaceCollection = new LinkedList<>();
        fixtureCustomInterface = new FixtureCustomInterfaceImpl();
        customInterfaceCollection.add(fixtureCustomInterface);
        cacheOrderedSPIFixture = new OrderedSPIFixtureImpl();
        Map<FixtureCustomInterface, OrderedSPIFixture> result = new LinkedHashMap<>(customInterfaceCollection.size(), 1);
        result.put(fixtureCustomInterface, cacheOrderedSPIFixture);
        OrderedServicesCache.cacheServices(customInterfaceCollection, OrderedSPIFixture.class, result);
    }

    @Test
    public void assertGetRegisteredServicesByCache() {
        Optional<CachedOrderedServices> actual = OrderedServicesCache.findCachedServices(customInterfaceCollection, OrderedSPIFixture.class);
        assertThat(cacheOrderedSPIFixture, is(actual.get().getServices().get(fixtureCustomInterface)));
    }
    
    @Test
    public void assertGetRegisteredServicesByClass() {
        Collection<FixtureCustomInterface> customInterfaceCollection = new LinkedList<>();
        customInterfaceCollection.add(new FixtureCustomInterfaceImpl());
        Collection<Class<?>> collection = customInterfaceCollection.stream().map(Object::getClass).collect(Collectors.toList());
        Map<Class<?>, OrderedSPIFixture> actual = OrderedSPIRegistry.getRegisteredServicesByClass(collection, OrderedSPIFixture.class);
        assertThat(actual.size(), is(1));
    }
    
    @Test
    public void assertGetRegisteredServices() {
        Collection<FixtureCustomInterface> collection = new LinkedList<>();
        collection.add(new FixtureCustomInterfaceImpl());
        Map<FixtureCustomInterface, OrderedSPIFixture> actual = OrderedSPIRegistry.getRegisteredServices(collection, OrderedSPIFixture.class);
        assertThat(actual.size(), is(1));
    }

    @After
    @SneakyThrows
    public void clean() {
        Field field = OrderedServicesCache.class.getDeclaredField("CACHED_SERVICES");
        field.setAccessible(true);
        Field modifiers = Field.class.getDeclaredField("modifiers");
        modifiers.setAccessible(true);
        modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(null, new ConcurrentHashMap<>());
    }

}
