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

package org.apache.shardingsphere.infra.util.spi.type.typed;

import org.apache.shardingsphere.infra.util.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.util.spi.exception.ServiceProviderNotFoundException;
import org.apache.shardingsphere.infra.util.spi.type.typed.fixture.TypedSPIFixture;
import org.apache.shardingsphere.infra.util.spi.type.typed.fixture.TypedSPIFixtureImpl;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class TypedSPIRegistryTest {
    
    static {
        ShardingSphereServiceLoader.register(TypedSPIFixture.class);
    }
    
    @Test
    public void assertFindRegisteredServiceWithoutProperties() {
        assertTrue(TypedSPIRegistry.findRegisteredService(TypedSPIFixture.class, "TYPED.FIXTURE").isPresent());
    }
    
    @Test
    public void assertFindRegisteredServiceWithProperties() {
        assertTrue(TypedSPIRegistry.findRegisteredService(TypedSPIFixture.class, "TYPED.FIXTURE", new Properties()).isPresent());
    }
    
    @Test
    public void assertGetRegisteredServiceWithoutProperties() {
        assertThat(TypedSPIRegistry.getRegisteredService(TypedSPIFixture.class, "TYPED.FIXTURE"), instanceOf(TypedSPIFixtureImpl.class));
    }
    
    @Test
    public void assertGetRegisteredServiceWithProperties() {
        Properties props = new Properties();
        props.put("key", 1);
        TypedSPIFixtureImpl actual = (TypedSPIFixtureImpl) TypedSPIRegistry.getRegisteredService(TypedSPIFixture.class, "TYPED.FIXTURE", props);
        assertThat(actual.getValue(), is("1"));
    }
    
    @Test
    public void assertGetRegisteredServiceWithNullProperties() {
        TypedSPIFixtureImpl actual = (TypedSPIFixtureImpl) TypedSPIRegistry.getRegisteredService(TypedSPIFixture.class, "TYPED.FIXTURE", null);
        assertNull(actual.getValue());
    }
    
    @Test
    public void assertGetRegisteredServiceWithAlias() {
        assertNotNull(TypedSPIRegistry.getRegisteredService(TypedSPIFixture.class, "TYPED.ALIAS", new Properties()));
    }
    
    @Test(expected = ServiceProviderNotFoundException.class)
    public void assertGetRegisteredServiceWithoutPropertiesWhenTypeIsNotExist() {
        TypedSPIRegistry.getRegisteredService(TypedSPIFixture.class, "NOT_EXISTED");
    }
    
    @Test(expected = ServiceProviderNotFoundException.class)
    public void assertGetRegisteredServiceWithPropertiesWhenTypeIsNotExist() {
        TypedSPIRegistry.getRegisteredService(TypedSPIFixture.class, "NOT_EXISTED", new Properties());
    }
}
