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

package org.apache.shardingsphere.spi.type.typed;

import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.exception.ServiceProviderNotFoundException;
import org.apache.shardingsphere.spi.type.typed.fixture.stateful.StatefulTypedSPIFixture;
import org.apache.shardingsphere.spi.type.typed.fixture.stateless.StatelessTypedSPIFixture;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class TypedSPIRegistryTest {
    
    @Before
    public void init() {
        ShardingSphereServiceLoader.register(StatelessTypedSPIFixture.class);
        ShardingSphereServiceLoader.register(StatefulTypedSPIFixture.class);
    }
    
    @Test
    public void assertFindStatelessRegisteredService() {
        assertTrue(TypedSPIRegistry.findRegisteredService(StatelessTypedSPIFixture.class, "Stateless_Fixture").isPresent());
    }
    
    @Test
    public void assertGetStatelessRegisteredService() {
        assertNotNull(TypedSPIRegistry.getRegisteredService(StatelessTypedSPIFixture.class, "Stateless_Fixture"));
    }
    
    @Test
    public void assertFindStatefulRegisteredService() {
        Optional<StatefulTypedSPIFixture> actual = TypedSPIRegistry.findRegisteredService(StatefulTypedSPIFixture.class, "Stateful_Fixture", createProperties());
        assertTrue(actual.isPresent());
        assertProperties(actual.get());
    }
    
    @Test
    public void assertGetStatefulRegisteredService() {
        StatefulTypedSPIFixture actual = TypedSPIRegistry.getRegisteredService(StatefulTypedSPIFixture.class, "Stateful_Fixture", createProperties());
        assertNotNull(actual);
        assertProperties(actual);
    }
    
    private Properties createProperties() {
        Properties result = new Properties();
        result.put("key1", 1);
        result.put("key2", 2L);
        return result;
    }
    
    private void assertProperties(final StatefulTypedSPIFixture actual) {
        assertThat(actual.getProps().getProperty("key1"), is("1"));
        assertThat(actual.getProps().getProperty("key2"), is("2"));
    }
    
    @Test
    public void assertGetStatefulRegisteredServiceWithAlias() {
        assertNotNull(TypedSPIRegistry.getRegisteredService(StatefulTypedSPIFixture.class, "Stateful_Alias", null));
    }
    
    @Test(expected = ServiceProviderNotFoundException.class)
    public void assertGetStatefulRegisteredServiceWhenTypeIsNotExist() {
        TypedSPIRegistry.getRegisteredService(StatefulTypedSPIFixture.class, "NOT_EXISTED", new Properties());
    }
    
    @Test(expected = ServiceProviderNotFoundException.class)
    public void assertGetStatelessRegisteredServiceWhenTypeIsNotExist() {
        TypedSPIRegistry.getRegisteredService(StatelessTypedSPIFixture.class, "NOT_EXISTED");
    }
}
