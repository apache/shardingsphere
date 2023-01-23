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

import org.apache.shardingsphere.infra.util.spi.exception.ServiceProviderNotFoundServerException;
import org.apache.shardingsphere.infra.util.spi.type.typed.fixture.TypedSPIFixture;
import org.apache.shardingsphere.infra.util.spi.type.typed.fixture.impl.TypedSPIFixtureImpl;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public final class TypedSPIRegistryTest {
    
    @Test
    public void assertFindServiceWithoutProperties() {
        assertTrue(TypedSPIRegistry.findService(TypedSPIFixture.class, "TYPED.FIXTURE").isPresent());
    }
    
    @Test
    public void assertFindServiceWithProperties() {
        assertTrue(TypedSPIRegistry.findService(TypedSPIFixture.class, "TYPED.FIXTURE", new Properties()).isPresent());
    }
    
    @Test
    public void assertGetServiceWithoutProperties() {
        assertThat(TypedSPIRegistry.getService(TypedSPIFixture.class, "TYPED.FIXTURE"), instanceOf(TypedSPIFixtureImpl.class));
    }
    
    @Test
    public void assertGetServiceWithProperties() {
        assertThat(((TypedSPIFixtureImpl) TypedSPIRegistry.getService(TypedSPIFixture.class, "TYPED.FIXTURE", PropertiesBuilder.build(new Property("key", "1")))).getValue(), is("1"));
    }
    
    @Test
    public void assertGetServiceWithNullProperties() {
        assertNull(((TypedSPIFixtureImpl) TypedSPIRegistry.getService(TypedSPIFixture.class, "TYPED.FIXTURE", null)).getValue());
    }
    
    @Test
    public void assertGetServiceWithAlias() {
        assertNotNull(TypedSPIRegistry.getService(TypedSPIFixture.class, "TYPED.ALIAS", new Properties()));
    }
    
    @Test(expected = ServiceProviderNotFoundServerException.class)
    public void assertGetServiceWithoutPropertiesWhenTypeIsNotExist() {
        TypedSPIRegistry.getService(TypedSPIFixture.class, "NOT_EXISTED");
    }
    
    @Test(expected = ServiceProviderNotFoundServerException.class)
    public void assertGetServiceWithPropertiesWhenTypeIsNotExist() {
        TypedSPIRegistry.getService(TypedSPIFixture.class, "NOT_EXISTED", new Properties());
    }
}
