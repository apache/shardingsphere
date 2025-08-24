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

package org.apache.shardingsphere.infra.spi.type.typed;

import org.apache.shardingsphere.infra.spi.exception.ServiceProviderNotFoundException;
import org.apache.shardingsphere.infra.spi.type.typed.fixture.TypedSPIFixture;
import org.apache.shardingsphere.infra.spi.type.typed.fixture.impl.TypedSPIFixtureImpl;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TypedSPILoaderTest {
    
    @Test
    void assertFindServiceWithoutProperties() {
        assertTrue(TypedSPILoader.findService(TypedSPIFixture.class, "TYPED.FIXTURE").isPresent());
    }
    
    @Test
    void assertFindServiceWithProperties() {
        assertTrue(TypedSPILoader.findService(TypedSPIFixture.class, "TYPED.FIXTURE", new Properties()).isPresent());
    }
    
    @Test
    void assertGetServiceWithoutProperties() {
        assertThat(TypedSPILoader.getService(TypedSPIFixture.class, "TYPED.FIXTURE"), isA(TypedSPIFixtureImpl.class));
    }
    
    @Test
    void assertGetServiceWithProperties() {
        Properties props = new Properties();
        props.setProperty("key", "1");
        assertThat(((TypedSPIFixtureImpl) TypedSPILoader.getService(TypedSPIFixture.class, "TYPED.FIXTURE", props)).getValue(), is("1"));
    }
    
    @Test
    void assertGetServiceWithAlias() {
        assertNotNull(TypedSPILoader.getService(TypedSPIFixture.class, "TYPED.ALIAS"));
    }
    
    @Test
    void assertGetServiceWhenTypeIsNotExist() {
        assertThrows(ServiceProviderNotFoundException.class, () -> TypedSPILoader.getService(TypedSPIFixture.class, "NOT_EXISTED"));
    }
}
