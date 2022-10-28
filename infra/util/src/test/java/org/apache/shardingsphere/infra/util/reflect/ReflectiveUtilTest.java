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

package org.apache.shardingsphere.infra.util.reflect;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class ReflectiveUtilTest {
    
    @Test
    public void assertGetFieldValue() throws IllegalAccessError {
        ReflectiveFixture reflectiveFixture = new ReflectiveFixture("bar");
        assertThat(ReflectiveUtil.getFieldValue(reflectiveFixture, "value"), is("bar"));
    }
    
    @Test
    public void assertSetField() throws IllegalAccessError {
        ReflectiveFixture reflectiveFixture = new ReflectiveFixture();
        ReflectiveUtil.setField(reflectiveFixture, "value", "foo");
        assertThat(ReflectiveUtil.getFieldValue(reflectiveFixture, "value"), is("foo"));
    }
    
    @Test
    public void assertSetStaticField() throws IllegalAccessError {
        ReflectiveFixture reflectiveFixture = new ReflectiveFixture();
        ReflectiveUtil.setStaticField(reflectiveFixture.getClass(), "staticValue", "foo");
        assertThat(ReflectiveUtil.getFieldValue(reflectiveFixture, "staticValue"), is("foo"));
    }
    
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter(AccessLevel.PRIVATE)
    private static final class ReflectiveFixture {
        
        private static String staticValue;
        
        private String value;
    }
}
