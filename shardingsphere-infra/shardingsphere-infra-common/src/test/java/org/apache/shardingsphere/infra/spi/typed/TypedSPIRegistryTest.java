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

package org.apache.shardingsphere.infra.spi.typed;

import java.util.Properties;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.exception.ServiceProviderNotFoundException;
import org.apache.shardingsphere.infra.spi.fixture.NoImplTypedSPIFixture;
import org.apache.shardingsphere.infra.spi.fixture.TypedSPIFixture;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public final class TypedSPIRegistryTest {
    
    @Before
    public void init() {
        ShardingSphereServiceLoader.register(TypedSPIFixture.class);
    }
    
    @Test
    public void assertGetRegisteredService() {
        String type = "FIXTURE";
        TypedSPIFixture actual = TypedSPIRegistry.getRegisteredService(TypedSPIFixture.class, type, new Properties());
        assertNotNull(actual);
    }
    
    @Test
    public void assertPropertiesGetRegisteredService() {
        Properties properties = new Properties();
        properties.put("key1", 1);
        properties.put("key2", 2L);
        String type = "FIXTURE";
        TypedSPIFixture actual = TypedSPIRegistry.getRegisteredService(TypedSPIFixture.class, type, properties);
        assertNotNull(actual);
        assertThat(actual.getProps().getProperty("key1"), is("1"));
        assertThat(actual.getProps().getProperty("key2"), is("2"));
    }
    
    @Test
    public void assertGetRegisteredServiceBySPIClass() {
        TypedSPIFixture actual = TypedSPIRegistry.getRegisteredService(TypedSPIFixture.class);
        assertNotNull(actual);
    }
    
    @Test(expected = ServiceProviderNotFoundException.class)
    public void assertGetRegisteredServiceWhenTypeIsNotExist() {
        String type = "TEST_FIXTURE";
        TypedSPIRegistry.getRegisteredService(TypedSPIFixture.class, type, new Properties());
    }
    
    @Test(expected = ServiceProviderNotFoundException.class)
    public void assertGetRegisteredServiceWhenSPIClassIsNotExist() {
        TypedSPIRegistry.getRegisteredService(NoImplTypedSPIFixture.class);
    }
}
