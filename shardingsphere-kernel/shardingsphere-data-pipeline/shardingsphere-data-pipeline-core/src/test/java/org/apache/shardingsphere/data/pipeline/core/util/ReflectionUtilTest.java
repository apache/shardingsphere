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

package org.apache.shardingsphere.data.pipeline.core.util;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ReflectionUtilTest {
    
    @Test
    public void assertSetFieldValue() throws NoSuchFieldException, IllegalAccessException {
        ReflectionFixture reflectionFixture = new ReflectionFixture();
        ReflectionUtil.setFieldValue(reflectionFixture, "value", "foo");
        assertThat(reflectionFixture.getValue(), is("foo"));
    }
    
    @Test
    public void assertGetFieldValue() throws NoSuchFieldException, IllegalAccessException {
        ReflectionFixture reflectionFixture = new ReflectionFixture("bar");
        assertThat(ReflectionUtil.getFieldValue(reflectionFixture, "value", String.class), is("bar"));
    }
    
    @Test
    public void assertInvokeMethod() throws Exception {
        ReflectionFixture reflectionFixture = new ReflectionFixture();
        ReflectionUtil.invokeMethod(reflectionFixture, "setValue", new Class[]{String.class}, new Object[]{"new_value"});
        assertThat(reflectionFixture.getValue(), is("new_value"));
    }
    
    @AllArgsConstructor
    @NoArgsConstructor
    private static final class ReflectionFixture {
        
        @Getter
        @Setter(AccessLevel.PRIVATE)
        private String value;
    }
}
