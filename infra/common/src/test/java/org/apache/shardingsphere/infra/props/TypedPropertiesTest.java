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

package org.apache.shardingsphere.infra.props;

import org.apache.shardingsphere.infra.props.exception.TypedPropertiesServerException;
import org.apache.shardingsphere.infra.props.fixture.TypedPropertiesFixture;
import org.apache.shardingsphere.infra.props.fixture.TypedPropertyKeyFixture;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TypedPropertiesTest {
    
    @Test
    void assertGetValue() {
        Properties props = createProperties();
        TypedPropertiesFixture actual = new TypedPropertiesFixture(props);
        assertTrue((Boolean) actual.getValue(TypedPropertyKeyFixture.BOOLEAN_VALUE));
        assertTrue((Boolean) actual.getValue(TypedPropertyKeyFixture.BOOLEAN_OBJECT_VALUE));
        assertThat(actual.getValue(TypedPropertyKeyFixture.INT_VALUE), is(100));
        assertThat(actual.getValue(TypedPropertyKeyFixture.INT_OBJECT_VALUE), is(100));
        assertThat(actual.getValue(TypedPropertyKeyFixture.LONG_VALUE), is(10000L));
        assertThat(actual.getValue(TypedPropertyKeyFixture.LONG_OBJECT_VALUE), is(10000L));
        assertThat(actual.getValue(TypedPropertyKeyFixture.STRING_VALUE), is("new_value"));
        assertThat(actual.getProps(), is(props));
    }
    
    private Properties createProperties() {
        return PropertiesBuilder.build(
                new Property(TypedPropertyKeyFixture.BOOLEAN_VALUE.getKey(), Boolean.TRUE.toString()),
                new Property(TypedPropertyKeyFixture.BOOLEAN_OBJECT_VALUE.getKey(), Boolean.TRUE.toString()),
                new Property(TypedPropertyKeyFixture.INT_VALUE.getKey(), "100"),
                new Property(TypedPropertyKeyFixture.INT_OBJECT_VALUE.getKey(), "100"),
                new Property(TypedPropertyKeyFixture.LONG_VALUE.getKey(), "10000"),
                new Property(TypedPropertyKeyFixture.LONG_OBJECT_VALUE.getKey(), "10000"),
                new Property(TypedPropertyKeyFixture.STRING_VALUE.getKey(), "new_value"));
    }
    
    @Test
    void assertGetDefaultValue() {
        TypedPropertiesFixture actual = new TypedPropertiesFixture(new Properties());
        assertFalse((Boolean) actual.getValue(TypedPropertyKeyFixture.BOOLEAN_VALUE));
        assertFalse((Boolean) actual.getValue(TypedPropertyKeyFixture.BOOLEAN_OBJECT_VALUE));
        assertThat(actual.getValue(TypedPropertyKeyFixture.INT_VALUE), is(10));
        assertThat(actual.getValue(TypedPropertyKeyFixture.INT_OBJECT_VALUE), is(10));
        assertThat(actual.getValue(TypedPropertyKeyFixture.LONG_VALUE), is(1000L));
        assertThat(actual.getValue(TypedPropertyKeyFixture.LONG_OBJECT_VALUE), is(1000L));
        assertThat(actual.getValue(TypedPropertyKeyFixture.STRING_VALUE), is("value"));
    }
    
    @Test
    void assertGetInvalidValue() {
        Properties props = PropertiesBuilder.build(
                new Property(TypedPropertyKeyFixture.BOOLEAN_VALUE.getKey(), "test"),
                new Property(TypedPropertyKeyFixture.BOOLEAN_OBJECT_VALUE.getKey(), "test"),
                new Property(TypedPropertyKeyFixture.INT_VALUE.getKey(), "test"),
                new Property(TypedPropertyKeyFixture.INT_OBJECT_VALUE.getKey(), "test"),
                new Property(TypedPropertyKeyFixture.LONG_VALUE.getKey(), "test"));
        assertThrows(TypedPropertiesServerException.class, () -> new TypedPropertiesFixture(props));
    }
}
