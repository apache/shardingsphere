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

import org.apache.shardingsphere.infra.util.props.exception.TypedPropertiesServerException;
import org.apache.shardingsphere.infra.util.props.fixture.TypedPropertiesFixture;
import org.apache.shardingsphere.infra.util.props.fixture.TypedPropertyKeyFixture;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public final class TypedPropertiesTest {
    
    @Test
    public void assertGetValue() {
        TypedPropertiesFixture actual = new TypedPropertiesFixture(createProperties());
        assertTrue(actual.getValue(TypedPropertyKeyFixture.BOOLEAN_VALUE));
        assertTrue(actual.getValue(TypedPropertyKeyFixture.BOOLEAN_OBJECT_VALUE));
        assertThat(actual.getValue(TypedPropertyKeyFixture.INT_VALUE), is(100));
        assertThat(actual.getValue(TypedPropertyKeyFixture.INT_OBJECT_VALUE), is(100));
        assertThat(actual.getValue(TypedPropertyKeyFixture.LONG_VALUE), is(10000L));
        assertThat(actual.getValue(TypedPropertyKeyFixture.LONG_OBJECT_VALUE), is(10000L));
        assertThat(actual.getValue(TypedPropertyKeyFixture.STRING_VALUE), is("new_value"));
    }
    
    private Properties createProperties() {
        Properties result = new Properties();
        result.setProperty(TypedPropertyKeyFixture.BOOLEAN_VALUE.getKey(), Boolean.TRUE.toString());
        result.setProperty(TypedPropertyKeyFixture.BOOLEAN_OBJECT_VALUE.getKey(), Boolean.TRUE.toString());
        result.setProperty(TypedPropertyKeyFixture.INT_VALUE.getKey(), "100");
        result.setProperty(TypedPropertyKeyFixture.INT_OBJECT_VALUE.getKey(), "100");
        result.setProperty(TypedPropertyKeyFixture.LONG_VALUE.getKey(), "10000");
        result.setProperty(TypedPropertyKeyFixture.LONG_OBJECT_VALUE.getKey(), "10000");
        result.setProperty(TypedPropertyKeyFixture.STRING_VALUE.getKey(), "new_value");
        return result;
    }
    
    @Test
    public void assertGetDefaultValue() {
        TypedPropertiesFixture actual = new TypedPropertiesFixture(new Properties());
        assertFalse(actual.getValue(TypedPropertyKeyFixture.BOOLEAN_VALUE));
        assertFalse(actual.getValue(TypedPropertyKeyFixture.BOOLEAN_OBJECT_VALUE));
        assertThat(actual.getValue(TypedPropertyKeyFixture.INT_VALUE), is(10));
        assertThat(actual.getValue(TypedPropertyKeyFixture.INT_OBJECT_VALUE), is(10));
        assertThat(actual.getValue(TypedPropertyKeyFixture.LONG_VALUE), is(1000L));
        assertThat(actual.getValue(TypedPropertyKeyFixture.LONG_OBJECT_VALUE), is(1000L));
        assertThat(actual.getValue(TypedPropertyKeyFixture.STRING_VALUE), is("value"));
    }
    
    @Test(expected = TypedPropertiesServerException.class)
    public void assertGetInvalidValue() {
        Properties props = new Properties();
        props.setProperty(TypedPropertyKeyFixture.BOOLEAN_VALUE.getKey(), "test");
        props.setProperty(TypedPropertyKeyFixture.BOOLEAN_OBJECT_VALUE.getKey(), "test");
        props.setProperty(TypedPropertyKeyFixture.INT_VALUE.getKey(), "test");
        props.setProperty(TypedPropertyKeyFixture.INT_OBJECT_VALUE.getKey(), "test");
        props.setProperty(TypedPropertyKeyFixture.LONG_VALUE.getKey(), "test");
        new TypedPropertiesFixture(props);
    }
}
