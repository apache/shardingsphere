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

package org.apache.shardingsphere.spi.type.required;

import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.exception.ServiceProviderNotFoundException;
import org.apache.shardingsphere.spi.type.required.fixture.NoImplRequiredSPIFixture;
import org.apache.shardingsphere.spi.type.required.fixture.RequiredSPIFixture;
import org.apache.shardingsphere.spi.type.required.fixture.RequiredSPIFixtureDefaultTrueImpl;
import org.apache.shardingsphere.spi.type.required.fixture.RequiredSPIImpl;
import org.apache.shardingsphere.spi.type.required.fixture.RequiredSingletonSPIFixture;
import org.apache.shardingsphere.spi.type.required.fixture.RequiredSingletonSPIFixtureImpl;
import org.junit.Test;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public final class RequiredSPIRegistryTest {
    
    static {
        ShardingSphereServiceLoader.register(RequiredSPIFixture.class);
        ShardingSphereServiceLoader.register(RequiredSPI.class);
        ShardingSphereServiceLoader.register(RequiredSingletonSPIFixture.class);
    }
    
    @Test(expected = ServiceProviderNotFoundException.class)
    public void assertRegisteredServiceNotExisted() {
        RequiredSPIRegistry.getRegisteredService(NoImplRequiredSPIFixture.class);
    }
    
    @Test
    public void assertRegisteredServiceOnlyOne() {
        RequiredSPI actualRegisteredService = RequiredSPIRegistry.getRegisteredService(RequiredSPI.class);
        assertTrue(actualRegisteredService instanceof RequiredSPIImpl);
    }
    
    @Test
    public void assertRegisteredServiceMoreThanOne() {
        RequiredSPIFixture actualRegisteredService = RequiredSPIRegistry.getRegisteredService(RequiredSPIFixture.class);
        assertTrue(actualRegisteredService instanceof RequiredSPIFixtureDefaultTrueImpl);
    }
    
    @Test
    public void assertRegisteredServiceSingleton() {
        RequiredSPIFixture actualOne = RequiredSPIRegistry.getRegisteredService(RequiredSingletonSPIFixture.class);
        assertTrue(actualOne instanceof RequiredSingletonSPIFixtureImpl);
        RequiredSPIFixture actualTwo = RequiredSPIRegistry.getRegisteredService(RequiredSingletonSPIFixture.class);
        assertTrue(actualTwo instanceof RequiredSingletonSPIFixtureImpl);
        assertSame(actualOne, actualTwo);
    }
}
