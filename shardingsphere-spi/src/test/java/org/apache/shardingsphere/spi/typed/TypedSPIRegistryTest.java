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

package org.apache.shardingsphere.spi.typed;

import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.exception.ServiceProviderNotFoundException;
import org.apache.shardingsphere.spi.fixture.typed.TypedSPIFixture;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

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
        TypedSPIFixture actual = TypedSPIRegistry.getRegisteredService(TypedSPIFixture.class, "FIXTURE", new Properties());
        assertNotNull(actual);
    }
    
    @Test
    public void assertGetRegisteredServiceWithProperties() {
        Properties properties = new Properties();
        properties.put("key1", 1);
        properties.put("key2", 2L);
        TypedSPIFixture actual = TypedSPIRegistry.getRegisteredService(TypedSPIFixture.class, "FIXTURE", properties);
        assertNotNull(actual);
        assertThat(actual.getProps().getProperty("key1"), is("1"));
        assertThat(actual.getProps().getProperty("key2"), is("2"));
    }
    
    @Test(expected = ServiceProviderNotFoundException.class)
    public void assertGetRegisteredServiceWhenTypeIsNotExist() {
        TypedSPIRegistry.getRegisteredService(TypedSPIFixture.class, "NOT_EXISTED", new Properties());
    }
}
