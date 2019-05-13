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

package org.apache.shardingsphere.core.rewrite.placeholder;

import org.apache.shardingsphere.core.constant.ShardingOperator;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class WhereEncryptColumnPlaceholderTest {
    
    private WhereEncryptColumnPlaceholder whereEncryptColumnPlaceholder;
    
    @Before
    public void setUp() {
    }
    
    @Test
    public void assertToStringWithoutPlaceholderWithEqual() {
        Map<Integer, Comparable<?>> indexValues = new LinkedHashMap<>();
        indexValues.put(0, "a");
        whereEncryptColumnPlaceholder = new WhereEncryptColumnPlaceholder("column_x", indexValues, Collections.<Integer>emptyList(), ShardingOperator.EQUAL);
        assertThat(whereEncryptColumnPlaceholder.toString(), is("column_x = 'a'"));
        assertThat(whereEncryptColumnPlaceholder.getColumnName(), is("column_x"));
        assertThat(whereEncryptColumnPlaceholder.getOperator(), is(ShardingOperator.EQUAL));
        assertThat(whereEncryptColumnPlaceholder.getIndexValues(), is(indexValues));
        assertThat(whereEncryptColumnPlaceholder.getParameterMarkerIndexes().size(), is(0));
    }
    
    @Test
    public void assertToStringWithPlaceholderWithEqual() {
        whereEncryptColumnPlaceholder = new WhereEncryptColumnPlaceholder("column_x", Collections.<Integer, Comparable<?>>emptyMap(), Collections.singletonList(0), ShardingOperator.EQUAL);
        assertThat(whereEncryptColumnPlaceholder.toString(), is("column_x = ?"));
    }
    
    @Test
    public void assertToStringWithoutPlaceholderWithIn() {
        Map<Integer, Comparable<?>> indexValues = new LinkedHashMap<>();
        indexValues.put(0, "a");
        indexValues.put(1, "b");
        whereEncryptColumnPlaceholder = new WhereEncryptColumnPlaceholder("column_x", indexValues, Collections.<Integer>emptyList(), ShardingOperator.IN);
        assertThat(whereEncryptColumnPlaceholder.toString(), is("column_x IN ('a', 'b')"));
    }
    
    @Test
    public void assertToStringWithPlaceholderWithIn() {
        whereEncryptColumnPlaceholder = new WhereEncryptColumnPlaceholder("column_x", Collections.<Integer, Comparable<?>>emptyMap(), Collections.singletonList(0), ShardingOperator.IN);
        assertThat(whereEncryptColumnPlaceholder.toString(), is("column_x IN (?)"));
    }
}
