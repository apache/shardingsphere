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
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ReflectionUtilsTest {
    
    @Test
    void assertGetStaticFieldValue() {
        assertThat(ReflectionUtils.getStaticFieldValue(ReflectionFixture.class, "staticValue"), is("static_value"));
    }
    
    @Test
    void assertSetStaticFieldValue() {
        ReflectionUtils.setStaticFieldValue(ReflectionFixture.class, "staticValue", "other_value");
        assertThat(ReflectionFixture.getStaticValue(), is("other_value"));
        ReflectionUtils.setStaticFieldValue(ReflectionFixture.class, "staticValue", "static_value");
    }
    
    @Test
    void assertGetFieldValue() {
        assertThat(ReflectionUtils.getFieldValue(new ReflectionFixture(), "instanceValue").orElse(""), is("instance_value"));
        assertFalse(ReflectionUtils.getFieldValue(new ReflectionFixture(), "not_existed_field").isPresent());
    }
    
    @Test
    void assertInvokeMethod() throws NoSuchMethodException {
        assertThat(ReflectionUtils.invokeMethod(ReflectionFixture.class.getDeclaredMethod("getInstanceValue"), new ReflectionFixture()), is("instance_value"));
    }
}
