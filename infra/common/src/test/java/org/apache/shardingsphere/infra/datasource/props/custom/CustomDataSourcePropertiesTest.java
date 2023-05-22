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

package org.apache.shardingsphere.infra.datasource.props.custom;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class CustomDataSourcePropertiesTest {
    
    @Test
    void assertGetProperties() {
        Map<String, Object> actual = new CustomDataSourceProperties(
                createProperties(), Arrays.asList("username", "password", "closed"), Collections.singletonList("closed"), Collections.singletonMap("username", "user")).getProperties();
        assertThat(actual.size(), is(3));
        assertThat(actual.get("foo"), is("bar"));
        assertThat(((Properties) actual.get("fooProperties")).size(), is(2));
        assertThat(((Properties) actual.get("fooProperties")).getProperty("foo1"), is("fooValue1"));
        assertThat(((Properties) actual.get("fooProperties")).getProperty("foo2"), is("fooValue2"));
        assertThat(((Properties) actual.get("barProperties")).size(), is(2));
        assertThat(((Properties) actual.get("barProperties")).getProperty("bar1"), is("barValue1"));
        assertThat(((Properties) actual.get("barProperties")).getProperty("bar2"), is("barValue2"));
    }
    
    private Map<String, Object> createProperties() {
        Map<String, Object> result = new LinkedHashMap<>(8, 1F);
        result.put("user", "root");
        result.put("password", "root");
        result.put("closed", false);
        result.put("foo", "bar");
        result.put("fooProperties.foo1", "fooValue1");
        result.put("fooProperties.foo2", "fooValue2");
        result.put("barProperties.bar1", "barValue1");
        result.put("barProperties.bar2", "barValue2");
        return result;
    }
}
