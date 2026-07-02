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

import org.apache.shardingsphere.infra.spi.type.ordered.fixture.OrderedInterfaceFixture;
import org.apache.shardingsphere.infra.spi.type.ordered.fixture.OrderedSPIFixture;
import org.apache.shardingsphere.infra.spi.type.ordered.fixture.OrderedSPINonSingletonFixture;
import org.apache.shardingsphere.infra.spi.type.ordered.fixture.impl.ChildOrderedInterfaceFixtureImpl;
import org.apache.shardingsphere.infra.spi.type.ordered.fixture.impl.ComparableOrderedInterfaceFixtureImpl;
import org.apache.shardingsphere.infra.spi.type.ordered.fixture.impl.ComparableOrderedSPIFixtureImpl;
import org.apache.shardingsphere.infra.spi.type.ordered.fixture.impl.OrderedInterfaceFixtureImpl;
import org.apache.shardingsphere.infra.spi.type.ordered.fixture.impl.OrderedSPIFixtureImpl;
import org.apache.shardingsphere.infra.spi.type.ordered.fixture.impl.ParentOrderedInterfaceFixtureImpl;
import org.apache.shardingsphere.infra.spi.type.ordered.fixture.impl.ParentOrderedSPIFixtureImpl;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;

@SuppressWarnings("rawtypes")
class OrderedSPILoaderTest {
    
    @Test
    void assertGetOrderedServices() {
        Collection<Class<?>> actualClasses = new LinkedList<>();
        for (OrderedSPIFixture each : OrderedSPILoader.getServices(OrderedSPIFixture.class)) {
            actualClasses.add(each.getClass());
        }
        assertThat(actualClasses, contains(ParentOrderedSPIFixtureImpl.class, OrderedSPIFixtureImpl.class, ComparableOrderedSPIFixtureImpl.class));
    }
    
    @Test
    void assertGetServicesByClassWithSingleClass() {
        Map<Class<?>, OrderedSPIFixture> actual = OrderedSPILoader.getServicesByClass(OrderedSPIFixture.class, Collections.singleton(OrderedInterfaceFixtureImpl.class));
        assertThat(actual.size(), is(1));
        assertThat(actual.get(OrderedInterfaceFixtureImpl.class), isA(OrderedSPIFixtureImpl.class));
    }
    
    @Test
    void assertGetServicesByClassWithMultiClass() {
        Map<Class<?>, OrderedSPIFixture> actual = OrderedSPILoader.getServicesByClass(OrderedSPIFixture.class,
                Arrays.<Class<?>>asList(OrderedInterfaceFixtureImpl.class, ParentOrderedInterfaceFixtureImpl.class));
        assertThat(actual.keySet(), contains(ParentOrderedInterfaceFixtureImpl.class, OrderedInterfaceFixtureImpl.class));
    }
    
    @Test
    void assertGetServicesByClassWithEmptyClass() {
        assertThat(OrderedSPILoader.getServicesByClass(OrderedSPIFixture.class, Collections.<Class<?>>emptyList()).entrySet(), empty());
    }
    
    @Test
    void assertGetServicesByClassWithNoMatchClass() {
        assertThat(OrderedSPILoader.getServicesByClass(OrderedSPIFixture.class, Collections.<Class<?>>singleton(ChildOrderedInterfaceFixtureImpl.class)).entrySet(), empty());
    }
    
    @Test
    void assertGetServicesWithSingleObject() {
        OrderedInterfaceFixtureImpl key = new OrderedInterfaceFixtureImpl();
        Map<OrderedInterfaceFixtureImpl, OrderedSPIFixture> actual = OrderedSPILoader.getServices(OrderedSPIFixture.class, Collections.singleton(key));
        assertThat(actual.size(), is(1));
        assertThat(actual.get(key), isA(OrderedSPIFixtureImpl.class));
    }
    
    @Test
    void assertGetServicesWithMultiObject() {
        OrderedInterfaceFixtureImpl orderedKey = new OrderedInterfaceFixtureImpl();
        ParentOrderedInterfaceFixtureImpl parentKey = new ParentOrderedInterfaceFixtureImpl();
        Map<OrderedInterfaceFixture, OrderedSPIFixture> actual = OrderedSPILoader.getServices(OrderedSPIFixture.class, Arrays.<OrderedInterfaceFixture>asList(orderedKey, parentKey));
        assertThat(actual.keySet(), contains(parentKey, orderedKey));
    }
    
    @Test
    void assertGetServicesWithEmptyObject() {
        assertThat(OrderedSPILoader.getServices(OrderedSPIFixture.class, Collections.<OrderedInterfaceFixture>emptyList()).entrySet(), empty());
    }
    
    @Test
    void assertGetServicesWithExactRuntimeClass() {
        Map<OrderedInterfaceFixture, OrderedSPIFixture> actual = OrderedSPILoader.getServices(OrderedSPIFixture.class,
                Collections.<OrderedInterfaceFixture>singleton(new ChildOrderedInterfaceFixtureImpl()));
        assertThat(actual.entrySet(), empty());
    }
    
    @Test
    void assertGetServicesWithSameClassObjects() {
        ComparableOrderedInterfaceFixtureImpl firstKey = new ComparableOrderedInterfaceFixtureImpl("foo_same_1");
        ComparableOrderedInterfaceFixtureImpl secondKey = new ComparableOrderedInterfaceFixtureImpl("foo_same_2");
        Map<OrderedInterfaceFixture, OrderedSPIFixture> actual = OrderedSPILoader.getServices(OrderedSPIFixture.class, Arrays.<OrderedInterfaceFixture>asList(firstKey, secondKey));
        assertThat(actual.keySet(), contains(firstKey, secondKey));
    }
    
    @Test
    void assertGetServicesWithCurrentObjectKeys() {
        OrderedSPILoader.getServices(OrderedSPIFixture.class, Arrays.<OrderedInterfaceFixture>asList(new ComparableOrderedInterfaceFixtureImpl("foo_equal_1"),
                new ComparableOrderedInterfaceFixtureImpl("foo_equal_2")));
        ComparableOrderedInterfaceFixtureImpl firstKey = new ComparableOrderedInterfaceFixtureImpl("foo_equal_1");
        ComparableOrderedInterfaceFixtureImpl secondKey = new ComparableOrderedInterfaceFixtureImpl("foo_equal_2");
        Map<OrderedInterfaceFixture, OrderedSPIFixture> actual = OrderedSPILoader.getServices(OrderedSPIFixture.class, Arrays.<OrderedInterfaceFixture>asList(firstKey, secondKey));
        assertThat(actual.keySet(), contains(sameInstance(firstKey), sameInstance(secondKey)));
    }
    
    @Test
    void assertGetServicesByComparator() {
        OrderedInterfaceFixtureImpl orderedKey = new OrderedInterfaceFixtureImpl();
        ParentOrderedInterfaceFixtureImpl parentKey = new ParentOrderedInterfaceFixtureImpl();
        ComparableOrderedInterfaceFixtureImpl comparableKey = new ComparableOrderedInterfaceFixtureImpl("foo_comparator");
        Map<OrderedInterfaceFixture, OrderedSPIFixture> actual = OrderedSPILoader.getServices(OrderedSPIFixture.class,
                Arrays.<OrderedInterfaceFixture>asList(parentKey, orderedKey, comparableKey), Collections.<Integer>reverseOrder());
        assertThat(actual.keySet(), contains(comparableKey, orderedKey, parentKey));
    }
    
    @Test
    void assertGetServicesByClassWithSingletonSPI() {
        Map<Class<?>, OrderedSPIFixture> firstActual = OrderedSPILoader.getServicesByClass(OrderedSPIFixture.class, Collections.singleton(OrderedInterfaceFixtureImpl.class));
        Map<Class<?>, OrderedSPIFixture> secondActual = OrderedSPILoader.getServicesByClass(OrderedSPIFixture.class, Collections.singleton(OrderedInterfaceFixtureImpl.class));
        assertThat(firstActual.get(OrderedInterfaceFixtureImpl.class), is(sameInstance(secondActual.get(OrderedInterfaceFixtureImpl.class))));
    }
    
    @Test
    void assertGetServicesWithSingletonSPI() {
        OrderedInterfaceFixtureImpl key = new OrderedInterfaceFixtureImpl();
        Map<OrderedInterfaceFixtureImpl, OrderedSPIFixture> firstActual = OrderedSPILoader.getServices(OrderedSPIFixture.class, Collections.singleton(key));
        Map<OrderedInterfaceFixtureImpl, OrderedSPIFixture> secondActual = OrderedSPILoader.getServices(OrderedSPIFixture.class, Collections.singleton(key));
        assertThat(firstActual.get(key), is(sameInstance(secondActual.get(key))));
    }
    
    @Test
    void assertGetServicesByClassWithNonSingletonSPI() {
        Map<Class<?>, OrderedSPINonSingletonFixture> firstActual = OrderedSPILoader.getServicesByClass(OrderedSPINonSingletonFixture.class, Collections.singleton(OrderedInterfaceFixtureImpl.class));
        Map<Class<?>, OrderedSPINonSingletonFixture> secondActual = OrderedSPILoader.getServicesByClass(OrderedSPINonSingletonFixture.class, Collections.singleton(OrderedInterfaceFixtureImpl.class));
        assertThat(firstActual.get(OrderedInterfaceFixtureImpl.class), is(not(sameInstance(secondActual.get(OrderedInterfaceFixtureImpl.class)))));
    }
    
    @Test
    void assertGetServicesWithNonSingletonSPI() {
        OrderedInterfaceFixtureImpl key = new OrderedInterfaceFixtureImpl();
        Map<OrderedInterfaceFixtureImpl, OrderedSPINonSingletonFixture> firstActual = OrderedSPILoader.getServices(OrderedSPINonSingletonFixture.class, Collections.singleton(key));
        Map<OrderedInterfaceFixtureImpl, OrderedSPINonSingletonFixture> secondActual = OrderedSPILoader.getServices(OrderedSPINonSingletonFixture.class, Collections.singleton(key));
        assertThat(firstActual.get(key), is(not(sameInstance(secondActual.get(key)))));
    }
}
