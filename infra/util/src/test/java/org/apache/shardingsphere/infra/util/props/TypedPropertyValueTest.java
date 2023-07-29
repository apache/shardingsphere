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

package org.apache.shardingsphere.infra.util.props;

import org.apache.shardingsphere.infra.util.props.exception.TypedPropertyValueException;
import org.apache.shardingsphere.infra.util.props.fixture.TypedPropertyEnumFixture;
import org.apache.shardingsphere.infra.util.props.fixture.TypedPropertyKeyFixture;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.spi.type.typed.fixture.TypedSPIFixture;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TypedPropertyValueTest {
    
    @Test
    void assertGetBooleanValue() throws TypedPropertyValueException {
        assertTrue((Boolean) new TypedPropertyValue(TypedPropertyKeyFixture.BOOLEAN_VALUE, Boolean.TRUE.toString()).getValue());
        assertTrue((Boolean) new TypedPropertyValue(TypedPropertyKeyFixture.BOOLEAN_OBJECT_VALUE, Boolean.TRUE.toString()).getValue());
    }
    
    @Test
    void assertGetInvalidBooleanValue() throws TypedPropertyValueException {
        assertFalse((Boolean) new TypedPropertyValue(TypedPropertyKeyFixture.BOOLEAN_VALUE, "test").getValue());
    }
    
    @Test
    void assertGetIntValue() throws TypedPropertyValueException {
        assertThat(new TypedPropertyValue(TypedPropertyKeyFixture.INT_VALUE, "1000").getValue(), is(1000));
        assertThat(new TypedPropertyValue(TypedPropertyKeyFixture.INT_OBJECT_VALUE, "1000").getValue(), is(1000));
    }
    
    @Test
    void assertGetInvalidIntValue() {
        assertThrows(TypedPropertyValueException.class, () -> new TypedPropertyValue(TypedPropertyKeyFixture.INT_VALUE, "test"));
    }
    
    @Test
    void assertGetLongValue() throws TypedPropertyValueException {
        assertThat(new TypedPropertyValue(TypedPropertyKeyFixture.LONG_VALUE, "10000").getValue(), is(10000L));
        assertThat(new TypedPropertyValue(TypedPropertyKeyFixture.LONG_OBJECT_VALUE, "10000").getValue(), is(10000L));
    }
    
    @Test
    void assertGetInvalidLongValue() {
        assertThrows(TypedPropertyValueException.class, () -> new TypedPropertyValue(TypedPropertyKeyFixture.LONG_VALUE, "test"));
    }
    
    @Test
    void assertGetStringValue() throws TypedPropertyValueException {
        assertThat(new TypedPropertyValue(TypedPropertyKeyFixture.STRING_VALUE, "new_value").getValue(), is("new_value"));
    }
    
    @Test
    void assertGetEnumValue() throws TypedPropertyValueException {
        assertThat(new TypedPropertyValue(TypedPropertyKeyFixture.ENUM_VALUE, TypedPropertyEnumFixture.FOO.name()).getValue(), is(TypedPropertyEnumFixture.FOO));
    }
    
    @Test
    void assertGetInvalidEnumValue() {
        assertThrows(TypedPropertyValueException.class, () -> new TypedPropertyValue(TypedPropertyKeyFixture.ENUM_VALUE, "BAR"));
    }
    
    @Test
    void assertGetTypedSPI() throws TypedPropertyValueException {
        assertThat(new TypedPropertyValue(TypedPropertyKeyFixture.TYPED_SPI_VALUE, "TYPED.FIXTURE").getValue(), is(TypedSPILoader.getService(TypedSPIFixture.class, "TYPED.FIXTURE")));
    }
}
