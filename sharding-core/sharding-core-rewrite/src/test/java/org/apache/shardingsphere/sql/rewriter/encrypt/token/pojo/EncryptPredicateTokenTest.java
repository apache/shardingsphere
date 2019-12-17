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

package org.apache.shardingsphere.sql.rewriter.encrypt.token.pojo;

import org.apache.shardingsphere.core.constant.ShardingOperator;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class EncryptPredicateTokenTest {
    
    @Test
    public void assertToStringWithoutPlaceholderWithoutTableOwnerWithEqual() {
        Map<Integer, Object> indexValues = new LinkedHashMap<>();
        indexValues.put(0, "a");
        EncryptPredicateRightValueToken actual = new EncryptPredicateRightValueToken(0, 0, indexValues, Collections.<Integer>emptyList(), ShardingOperator.EQUAL);
        assertThat(actual.toString(), is("'a'"));
    }
    
    @Test
    public void assertToStringWithPlaceholderWithoutTableOwnerWithEqual() {
        EncryptPredicateRightValueToken actual = new EncryptPredicateRightValueToken(0, 0, Collections.<Integer, Object>emptyMap(), Collections.singletonList(0), ShardingOperator.EQUAL);
        assertThat(actual.toString(), is("?"));
    }
    
    @Test
    public void assertToStringWithoutPlaceholderWithoutTableOwnerWithIn() {
        Map<Integer, Object> indexValues = new LinkedHashMap<>();
        indexValues.put(0, "a");
        indexValues.put(1, "b");
        EncryptPredicateRightValueToken actual = new EncryptPredicateRightValueToken(0, 0, indexValues, Collections.<Integer>emptyList(), ShardingOperator.IN);
        assertThat(actual.toString(), is("('a', 'b')"));
    }
    
    @Test
    public void assertToStringWithPlaceholderWithoutTableOwnerWithIn() {
        EncryptPredicateRightValueToken actual = new EncryptPredicateRightValueToken(0, 0, Collections.<Integer, Object>emptyMap(), Collections.singletonList(0), ShardingOperator.IN);
        assertThat(actual.toString(), is("(?)"));
    }
}
