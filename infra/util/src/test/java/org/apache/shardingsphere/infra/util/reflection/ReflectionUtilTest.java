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

package org.apache.shardingsphere.infra.util.reflection;

import org.apache.shardingsphere.infra.util.reflection.fixture.ReflectionFixture;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class ReflectionUtilTest {
    
    @Test
    public void assertGetStaticFieldValue() {
        assertThat(ReflectionUtil.getStaticFieldValue(ReflectionFixture.class, "staticValue"), is("foo"));
    }
    
    @Test
    public void assertSetStaticFieldValue() {
        ReflectionUtil.setStaticFieldValue(ReflectionFixture.class, "staticValue", "bar");
        assertThat(ReflectionFixture.getStaticValue(), is("bar"));
        ReflectionUtil.setStaticFieldValue(ReflectionFixture.class, "staticValue", "foo");
    }
    
    @Test
    public void assertGetFieldValue() {
        ReflectionFixture reflectionFixture = new ReflectionFixture();
        assertThat(ReflectionUtil.getFieldValue(reflectionFixture, "fooField").get(), is("foo_value"));
        assertThat(ReflectionUtil.getFieldValue(reflectionFixture, "barField").get(), is("bar_value"));
        assertThat(ReflectionUtil.getFieldValue(new ReflectionFixture(), "foo_field").isPresent(), is(false));
    }
    
    @Test
    public void assertInvokeMethod() throws NoSuchMethodException {
        ReflectionFixture reflectionFixture = new ReflectionFixture();
        assertThat(ReflectionUtil.invokeMethod(reflectionFixture.getClass().getDeclaredMethod("getFooField"), reflectionFixture), is("foo_value"));
        assertThat(ReflectionUtil.invokeMethod(reflectionFixture.getClass().getDeclaredMethod("getBarField"), reflectionFixture), is("bar_value"));
        assertThat(ReflectionUtil.invokeMethod(
                reflectionFixture.getClass().getDeclaredMethod("getContactValue", String.class, String.class),
                reflectionFixture, "foo", "bar"), is("foo_bar"));
    }
}
