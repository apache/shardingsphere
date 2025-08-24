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

package org.apache.shardingsphere.infra.spi;

import org.apache.shardingsphere.infra.spi.fixture.empty.EmptySPIFixture;
import org.apache.shardingsphere.infra.spi.fixture.multiton.MultitonSPIFixture;
import org.apache.shardingsphere.infra.spi.fixture.multiton.impl.MultitonSPIFixtureImpl;
import org.apache.shardingsphere.infra.spi.fixture.singleton.SingletonSPIFixture;
import org.apache.shardingsphere.infra.spi.fixture.singleton.impl.SingletonSPIFixtureImpl;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShardingSphereServiceLoaderTest {
    
    @Test
    void assertGetServiceInstancesWithNullValue() {
        assertThrows(NullPointerException.class, () -> ShardingSphereServiceLoader.getServiceInstances(null));
    }
    
    @Test
    void assertGetServiceInstancesWithEmptyInstances() {
        assertTrue(ShardingSphereServiceLoader.getServiceInstances(EmptySPIFixture.class).isEmpty());
    }
    
    @Test
    void assertGetServiceInstancesWithSingletonSPI() {
        Collection<SingletonSPIFixture> actual = ShardingSphereServiceLoader.getServiceInstances(SingletonSPIFixture.class);
        assertThat(actual.size(), is(1));
        SingletonSPIFixture actualInstance = actual.iterator().next();
        assertThat(actualInstance, isA(SingletonSPIFixtureImpl.class));
        assertThat(actualInstance, is(ShardingSphereServiceLoader.getServiceInstances(SingletonSPIFixture.class).iterator().next()));
    }
    
    @Test
    void assertGetServiceInstancesWithMultitonSPI() {
        Collection<MultitonSPIFixture> actual = ShardingSphereServiceLoader.getServiceInstances(MultitonSPIFixture.class);
        assertThat(actual.size(), is(1));
        MultitonSPIFixture actualInstance = actual.iterator().next();
        assertThat(actualInstance, isA(MultitonSPIFixtureImpl.class));
        assertThat(actualInstance, not(ShardingSphereServiceLoader.getServiceInstances(MultitonSPIFixture.class).iterator().next()));
    }
}
