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

package org.apache.shardingsphere.infra.properties;

import org.apache.shardingsphere.infra.config.exception.ShardingSphereConfigurationException;
import org.apache.shardingsphere.infra.properties.fixture.TestTypedProperties;
import org.apache.shardingsphere.infra.properties.fixture.TestTypedPropertyKey;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class TypedPropertiesTest {
    
    @Test
    public void assertGetValue() {
        Properties props = new Properties();
        props.setProperty(TestTypedPropertyKey.BOOLEAN_VALUE.getKey(), Boolean.TRUE.toString());
        props.setProperty(TestTypedPropertyKey.BOOLEAN_OBJECT_VALUE.getKey(), Boolean.TRUE.toString());
        props.setProperty(TestTypedPropertyKey.INT_VALUE.getKey(), "100");
        props.setProperty(TestTypedPropertyKey.INT_OBJECT_VALUE.getKey(), "100");
        props.setProperty(TestTypedPropertyKey.LONG_VALUE.getKey(), "10000");
        props.setProperty(TestTypedPropertyKey.LONG_OBJECT_VALUE.getKey(), "10000");
        props.setProperty(TestTypedPropertyKey.STRING_VALUE.getKey(), "new_value");
        TestTypedProperties actual = new TestTypedProperties(props);
        assertTrue(actual.getValue(TestTypedPropertyKey.BOOLEAN_VALUE));
        assertTrue(actual.getValue(TestTypedPropertyKey.BOOLEAN_OBJECT_VALUE));
        assertThat(actual.getValue(TestTypedPropertyKey.INT_VALUE), is(100));
        assertThat(actual.getValue(TestTypedPropertyKey.INT_OBJECT_VALUE), is(100));
        assertThat(actual.getValue(TestTypedPropertyKey.LONG_VALUE), is(10000L));
        assertThat(actual.getValue(TestTypedPropertyKey.LONG_OBJECT_VALUE), is(10000L));
        assertThat(actual.getValue(TestTypedPropertyKey.STRING_VALUE), is("new_value"));
    }
    
    @Test
    public void assertGetDefaultValue() {
        TestTypedProperties actual = new TestTypedProperties(new Properties());
        assertFalse(actual.getValue(TestTypedPropertyKey.BOOLEAN_VALUE));
        assertFalse(actual.getValue(TestTypedPropertyKey.BOOLEAN_OBJECT_VALUE));
        assertThat(actual.getValue(TestTypedPropertyKey.INT_VALUE), is(10));
        assertThat(actual.getValue(TestTypedPropertyKey.INT_OBJECT_VALUE), is(10));
        assertThat(actual.getValue(TestTypedPropertyKey.LONG_VALUE), is(1000L));
        assertThat(actual.getValue(TestTypedPropertyKey.LONG_OBJECT_VALUE), is(1000L));
        assertThat(actual.getValue(TestTypedPropertyKey.STRING_VALUE), is("value"));
    }
    
    @Test(expected = ShardingSphereConfigurationException.class)
    public void assertGetInvalidValue() {
        Properties props = new Properties();
        props.setProperty(TestTypedPropertyKey.BOOLEAN_VALUE.getKey(), "test");
        props.setProperty(TestTypedPropertyKey.BOOLEAN_OBJECT_VALUE.getKey(), "test");
        props.setProperty(TestTypedPropertyKey.INT_VALUE.getKey(), "test");
        props.setProperty(TestTypedPropertyKey.INT_OBJECT_VALUE.getKey(), "test");
        props.setProperty(TestTypedPropertyKey.LONG_VALUE.getKey(), "test");
        new TestTypedProperties(props);
    }
}
