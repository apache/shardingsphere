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

package org.apache.shardingsphere.encrypt.rewrite.condition;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EncryptConditionValuesTest {
    
    @Test
    void assertGet() {
        Map<Integer, Integer> positionIndexMap = new HashMap<>(2, 1F);
        positionIndexMap.put(0, 0);
        positionIndexMap.put(2, 1);
        Map<Integer, Object> positionValueMap = Collections.singletonMap(1, 1);
        EncryptCondition condition = mock(EncryptCondition.class);
        when(condition.getPositionIndexMap()).thenReturn(positionIndexMap);
        when(condition.getPositionValueMap()).thenReturn(positionValueMap);
        List<Object> actual = new EncryptConditionValues(condition).get(Arrays.asList("foo", "bar"));
        assertThat(actual.size(), is(3));
        assertThat(actual.get(0), is("foo"));
        assertThat(actual.get(1), is(1));
        assertThat(actual.get(2), is("bar"));
    }
}
