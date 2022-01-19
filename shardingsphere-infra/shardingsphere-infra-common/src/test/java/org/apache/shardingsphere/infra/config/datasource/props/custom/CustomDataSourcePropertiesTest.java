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

package org.apache.shardingsphere.infra.config.datasource.props.custom;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class CustomDataSourcePropertiesTest {
    
    @Test
    public void assertGetProperties() {
        Map<String, Object> actual = new CustomDataSourceProperties(
                createProperties(), Arrays.asList("username", "password", "closed"), Collections.singletonList("closed"), createPropertySynonyms()).getProperties();
        assertThat(actual.size(), is(1));
        assertThat(actual.get("foo"), is("bar"));
    }
    
    private Map<String, Object> createProperties() {
        Map<String, Object> result = new LinkedHashMap<>(3, 1);
        result.put("user", "root");
        result.put("password", "root");
        result.put("closed", false);
        result.put("foo", "bar");
        return result;
    }
    
    private Map<String, String> createPropertySynonyms() {
        Map<String, String> result = new LinkedHashMap<>(1, 1);
        result.put("username", "user");
        return result;
    }
}
