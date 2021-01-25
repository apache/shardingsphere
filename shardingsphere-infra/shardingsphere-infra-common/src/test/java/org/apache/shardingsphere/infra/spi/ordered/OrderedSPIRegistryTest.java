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
import org.apache.shardingsphere.infra.spi.fixture.OrderedSPIFixture;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class OrderedSPIRegistryTest {
    
    @Before
    public void init() {
        ShardingSphereServiceLoader.register(OrderedSPIFixture.class);
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
}
