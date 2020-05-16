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

import org.apache.shardingsphere.infra.properties.fixture.TestTypedPropertyKey;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class TypedPropertyValueTest {
    
    @Test
    public void assertGetBooleanValue() throws TypedPropertyValueException {
        assertTrue((Boolean) new TypedPropertyValue(TestTypedPropertyKey.BOOLEAN_VALUE, Boolean.TRUE.toString()).getValue());
        assertTrue((Boolean) new TypedPropertyValue(TestTypedPropertyKey.BOOLEAN_OBJECT_VALUE, Boolean.TRUE.toString()).getValue());
    }
    
    @Test
    public void assertGetInvalidBooleanValue() throws TypedPropertyValueException {
        assertFalse((Boolean) new TypedPropertyValue(TestTypedPropertyKey.BOOLEAN_VALUE, "test").getValue());
    }
    
    @Test
    public void assertGetIntValue() throws TypedPropertyValueException {
        assertThat(new TypedPropertyValue(TestTypedPropertyKey.INT_VALUE, "1000").getValue(), is(1000));
        assertThat(new TypedPropertyValue(TestTypedPropertyKey.INT_OBJECT_VALUE, "1000").getValue(), is(1000));
    }
    
    @Test(expected = TypedPropertyValueException.class)
    public void assertGetInvalidIntValue() throws TypedPropertyValueException {
        new TypedPropertyValue(TestTypedPropertyKey.INT_VALUE, "test");
    }
    
    @Test
    public void assertGetLongValue() throws TypedPropertyValueException {
        assertThat(new TypedPropertyValue(TestTypedPropertyKey.LONG_VALUE, "10000").getValue(), is(10000L));
        assertThat(new TypedPropertyValue(TestTypedPropertyKey.LONG_OBJECT_VALUE, "10000").getValue(), is(10000L));
    }
    
    @Test(expected = TypedPropertyValueException.class)
    public void assertGetInvalidLongValue() throws TypedPropertyValueException {
        new TypedPropertyValue(TestTypedPropertyKey.LONG_VALUE, "test");
    }
    
    @Test
    public void assertGetStringValue() throws TypedPropertyValueException {
        assertThat(new TypedPropertyValue(TestTypedPropertyKey.STRING_VALUE, "new_value").getValue(), is("new_value"));
    }
}
