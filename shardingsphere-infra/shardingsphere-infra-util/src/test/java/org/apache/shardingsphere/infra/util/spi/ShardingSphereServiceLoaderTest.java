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

package org.apache.shardingsphere.infra.util.spi;

import org.apache.shardingsphere.infra.util.spi.fixture.EmptySPIFixture;
import org.apache.shardingsphere.infra.util.spi.fixture.MultitonSPIFixture;
import org.apache.shardingsphere.infra.util.spi.fixture.MultitonSPIFixtureImpl;
import org.apache.shardingsphere.infra.util.spi.fixture.SingletonSPIFixture;
import org.apache.shardingsphere.infra.util.spi.fixture.SingletonSPIFixtureImpl;
import org.junit.Test;

import java.util.Collection;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ShardingSphereServiceLoaderTest {
    
    static {
        ShardingSphereServiceLoader.register(EmptySPIFixture.class);
        ShardingSphereServiceLoader.register(SingletonSPIFixture.class);
        ShardingSphereServiceLoader.register(MultitonSPIFixture.class);
    }
    
    @Test
    public void assertGetServiceInstancesWithUnregisteredSPI() {
        assertTrue(ShardingSphereServiceLoader.getServiceInstances(Object.class).isEmpty());
    }
    
    @Test
    public void assertGetServiceInstancesWithEmptyInstances() {
        assertTrue(ShardingSphereServiceLoader.getServiceInstances(EmptySPIFixture.class).isEmpty());
    }
    
    @Test
    public void assertGetServiceInstancesWithSingletonSPI() {
        Collection<SingletonSPIFixture> actual = ShardingSphereServiceLoader.getServiceInstances(SingletonSPIFixture.class);
        assertThat(actual.size(), is(1));
        SingletonSPIFixture actualInstance = actual.iterator().next();
        assertThat(actualInstance, instanceOf(SingletonSPIFixtureImpl.class));
        assertThat(actualInstance, is(ShardingSphereServiceLoader.getServiceInstances(SingletonSPIFixture.class).iterator().next()));
    }
    
    @Test
    public void assertGetServiceInstancesWithMultitonSPI() {
        Collection<MultitonSPIFixture> actual = ShardingSphereServiceLoader.getServiceInstances(MultitonSPIFixture.class);
        assertThat(actual.size(), is(1));
        MultitonSPIFixture actualInstance = actual.iterator().next();
        assertThat(actualInstance, instanceOf(MultitonSPIFixtureImpl.class));
        assertThat(actualInstance, not(ShardingSphereServiceLoader.getServiceInstances(MultitonSPIFixture.class).iterator().next()));
    }
}
