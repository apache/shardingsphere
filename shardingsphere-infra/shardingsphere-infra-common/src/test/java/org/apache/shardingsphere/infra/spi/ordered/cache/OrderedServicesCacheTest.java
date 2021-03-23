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

package org.apache.shardingsphere.infra.spi.ordered.cache;

import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.fixture.FixtureCustomInterface;
import org.apache.shardingsphere.infra.spi.fixture.FixtureCustomInterfaceImpl;
import org.apache.shardingsphere.infra.spi.fixture.OrderedSPIFixture;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Collection;
import java.util.LinkedList;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class OrderedServicesCacheTest {

    @Before
    public void init() {
        ShardingSphereServiceLoader.register(OrderedSPIFixture.class);
    }

    @Test
    public void assertFindCachedServices() {
        Collection<FixtureCustomInterface> types = new LinkedList<>();
        types.add(new FixtureCustomInterfaceImpl());
        Collection<OrderedSPIFixture> registeredServices = ShardingSphereServiceLoader.getSingletonServiceInstances(OrderedSPIFixture.class);
        Map<FixtureCustomInterface, OrderedSPIFixture> services = new LinkedHashMap<>(registeredServices.size(), 1);
        for (OrderedSPIFixture each : registeredServices) {
            types.stream().filter(type -> each.getTypeClass() == type.getClass()).forEach(type -> services.put(type, each));
        }
        OrderedServicesCache.cacheServices(types, OrderedSPIFixture.class, services);
        Optional<CachedOrderedServices> cachedServices = OrderedServicesCache.findCachedServices(types, OrderedSPIFixture.class);
        if (cachedServices.isPresent()) {
            assertThat(cachedServices.get().getTypes().size(), is(1));
            assertThat(cachedServices.get().getServices().size(), is(1));
        }
    }

}
