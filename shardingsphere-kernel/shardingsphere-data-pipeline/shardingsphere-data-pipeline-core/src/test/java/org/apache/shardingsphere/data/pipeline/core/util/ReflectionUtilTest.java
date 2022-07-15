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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.junit.Test;

public final class ReflectionUtilTest {

    @Test
    public void assertSetFieldValue() throws Exception {
        ReflectionSimple reflectionSimple = new ReflectionSimple();
        ReflectionUtil.setFieldValue(reflectionSimple, "name", "sharding-sphere");
        assertThat(reflectionSimple.getName(), is("sharding-sphere"));
    }

    @Test
    public void assertGetFieldValue() throws Exception {
        ReflectionSimple reflectionSimple = new ReflectionSimple();
        Integer age = ReflectionUtil.getFieldValue(reflectionSimple, "age", Integer.class);
        assertThat(age, is(18));
    }

    @Test
    public void assertGetStaticFieldValue() throws Exception {
        ReflectionSimple.setType("ReflectionSimple");
        String fieldValue = ReflectionUtil.getStaticFieldValue(ReflectionSimple.class, "type", String.class);
        assertThat(fieldValue, is("ReflectionSimple"));
    }

    @Test
    public void assertInvokeMethodAndGetFieldValue() throws Exception {
        ReflectionSimple reflectionSimple = new ReflectionSimple();
        ReflectionUtil.invokeMethod(reflectionSimple, "setName", new Class[]{String.class}, new Object[]{"apache"});
        assertThat(reflectionSimple.getName(), is("apache"));
    }

    private static class ReflectionSimple {

        @Setter
        private static String type;

        @Getter
        @Setter(AccessLevel.PRIVATE)
        private String name;

        @Getter(AccessLevel.PRIVATE)
        private final int age = 18;
    }
}
