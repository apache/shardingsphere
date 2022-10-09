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

package org.apache.shardingsphere.spring.boot.util;

import org.junit.Test;
import org.springframework.mock.env.MockEnvironment;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public final class PropertyUtilTest {
    
    @Test
    public void assertGetCamelCaseKeys() {
        assertThat(PropertyUtil.getCamelCaseKeys(createToBeConvertedMap()), is(createConvertedMap()));
    }
    
    private Map<String, Object> createToBeConvertedMap() {
        Map<String, Object> result = new LinkedHashMap<>(2, 1);
        result.put("fooKey", "fooValue");
        result.put("bar-key", "barValue");
        return result;
    }
    
    private Map<String, Object> createConvertedMap() {
        Map<String, Object> result = new LinkedHashMap<>(2, 1);
        result.put("fooKey", "fooValue");
        result.put("barKey", "barValue");
        return result;
    }
    
    @Test
    public void assertContainPropertyPrefix() {
        MockEnvironment mockEnvironment = new MockEnvironment();
        mockEnvironment.setProperty("spring.shardingsphere.rules.sharding.sharding-algorithms.table-inline.type", "INLINE");
        assertTrue(PropertyUtil.containPropertyPrefix(mockEnvironment, "spring.shardingsphere.rules.sharding.sharding-algorithms.table-inline"));
    }
}
