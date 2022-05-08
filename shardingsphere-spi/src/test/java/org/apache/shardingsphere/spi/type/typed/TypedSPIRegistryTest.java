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
import org.apache.shardingsphere.spi.type.typed.fixture.TypedSPIFixture;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public final class TypedSPIRegistryTest {
    
    @Before
    public void init() {
        ShardingSphereServiceLoader.register(TypedSPIFixture.class);
    }
    
    @Test
    public void assertFindRegisteredService() {
        assertTrue(TypedSPIRegistry.findRegisteredService(TypedSPIFixture.class, "TYPED.FIXTURE").isPresent());
    }
    
    @Test
    public void assertGetStatelessRegisteredService() {
        assertNotNull(TypedSPIRegistry.getRegisteredService(TypedSPIFixture.class, "TYPED.FIXTURE"));
    }
    
    @Test
    public void assertFindRegisteredServiceWithProperties() {
        assertTrue(TypedSPIRegistry.findRegisteredService(TypedSPIFixture.class, "TYPED.FIXTURE", createProperties()).isPresent());
    }
    
    @Test
    public void assertGetStatelessRegisteredServiceWithProperties() {
        assertNotNull(TypedSPIRegistry.getRegisteredService(TypedSPIFixture.class, "TYPED.FIXTURE", createProperties()));
    }
    
    private Properties createProperties() {
        Properties result = new Properties();
        result.put("key1", 1);
        result.put("key2", 2L);
        return result;
    }
    
    @Test
    public void assertGetStatefulRegisteredServiceWithAlias() {
        assertNotNull(TypedSPIRegistry.getRegisteredService(TypedSPIFixture.class, "TYPED.ALIAS", new Properties()));
    }
    
    @Test(expected = ServiceProviderNotFoundException.class)
    public void assertGetStatefulRegisteredServiceWhenTypeIsNotExist() {
        TypedSPIRegistry.getRegisteredService(TypedSPIFixture.class, "NOT_EXISTED", new Properties());
    }
    
    @Test(expected = ServiceProviderNotFoundException.class)
    public void assertGetStatelessRegisteredServiceWhenTypeIsNotExist() {
        TypedSPIRegistry.getRegisteredService(TypedSPIFixture.class, "NOT_EXISTED");
    }
}
