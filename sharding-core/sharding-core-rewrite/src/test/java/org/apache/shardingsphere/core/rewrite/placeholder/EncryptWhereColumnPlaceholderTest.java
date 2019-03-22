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

import com.google.common.collect.Lists;
import org.apache.shardingsphere.core.constant.ShardingOperator;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class EncryptWhereColumnPlaceholderTest {
    
    private EncryptWhereColumnPlaceholder encryptWhereColumnPlaceholder;
    
    @Before
    public void setUp() {
    }
    
    @Test
    public void assertToStringWithoutPlaceholderWithEqual() {
        Map<Integer, Comparable<?>> indexValues = new LinkedHashMap<>();
        indexValues.put(0, "a");
        encryptWhereColumnPlaceholder = new EncryptWhereColumnPlaceholder("table_x", "column_x", indexValues, Collections.<Integer>emptyList(), ShardingOperator.EQUAL);
        assertThat(encryptWhereColumnPlaceholder.toString(), is("column_x = 'a'"));
        assertThat(encryptWhereColumnPlaceholder.getLogicTableName(), is("table_x"));
        assertThat(encryptWhereColumnPlaceholder.getColumnName(), is("column_x"));
        assertThat(encryptWhereColumnPlaceholder.getOperator(), is(ShardingOperator.EQUAL));
        assertThat(encryptWhereColumnPlaceholder.getIndexValues(), is(indexValues));
        assertThat(encryptWhereColumnPlaceholder.getPlaceholderIndexes().size(), is(0));
    }
    
    @Test
    public void assertToStringWithPlaceholderWithEqual() {
        encryptWhereColumnPlaceholder = new EncryptWhereColumnPlaceholder("table_x", "column_x", Collections.<Integer, Comparable<?>>emptyMap(), Collections.singletonList(0), ShardingOperator.EQUAL);
        assertThat(encryptWhereColumnPlaceholder.toString(), is("column_x = ?"));
    }
    
    @Test
    public void assertToStringWithoutPlaceholderWithBetween() {
        Map<Integer, Comparable<?>> indexValues = new LinkedHashMap<>();
        indexValues.put(0, "a");
        indexValues.put(1, "b");
        encryptWhereColumnPlaceholder = new EncryptWhereColumnPlaceholder("table_x", "column_x", indexValues, Collections.<Integer>emptyList(), ShardingOperator.BETWEEN);
        assertThat(encryptWhereColumnPlaceholder.toString(), is("column_x BETWEEN 'a' AND 'b'"));
    }
    
    @Test
    public void assertToStringWithFirstPlaceholderWithBetween() {
        Map<Integer, Comparable<?>> indexValues = new LinkedHashMap<>();
        indexValues.put(0, "a");
        encryptWhereColumnPlaceholder = new EncryptWhereColumnPlaceholder("table_x", "column_x", indexValues, Collections.singletonList(1), ShardingOperator.BETWEEN);
        assertThat(encryptWhereColumnPlaceholder.toString(), is("column_x BETWEEN 'a' AND ?"));
    }
    
    @Test
    public void assertToStringWithSecondPlaceholderWithBetween() {
        Map<Integer, Comparable<?>> indexValues = new LinkedHashMap<>();
        indexValues.put(0, "a");
        encryptWhereColumnPlaceholder = new EncryptWhereColumnPlaceholder("table_x", "column_x", indexValues, Collections.singletonList(0), ShardingOperator.BETWEEN);
        assertThat(encryptWhereColumnPlaceholder.toString(), is("column_x BETWEEN ? AND 'a'"));
    }
    
    @Test
    public void assertToStringWithTwoPlaceholderWithBetween() {
        encryptWhereColumnPlaceholder = new EncryptWhereColumnPlaceholder("table_x", "column_x", Collections.<Integer, Comparable<?>>emptyMap(), Lists.newArrayList(0, 1), ShardingOperator.BETWEEN);
        assertThat(encryptWhereColumnPlaceholder.toString(), is("column_x BETWEEN ? AND ?"));
    }
    
    @Test
    public void assertToStringWithoutPlaceholderWithIn() {
        Map<Integer, Comparable<?>> indexValues = new LinkedHashMap<>();
        indexValues.put(0, "a");
        indexValues.put(1, "b");
        encryptWhereColumnPlaceholder = new EncryptWhereColumnPlaceholder("table_x", "column_x", indexValues, Collections.<Integer>emptyList(), ShardingOperator.IN);
        assertThat(encryptWhereColumnPlaceholder.toString(), is("column_x IN ('a', 'b')"));
    }
    
    @Test
    public void assertToStringWithPlaceholderWithIn() {
        encryptWhereColumnPlaceholder = new EncryptWhereColumnPlaceholder("table_x", "column_x", Collections.<Integer, Comparable<?>>emptyMap(), Collections.singletonList(0), ShardingOperator.IN);
        assertThat(encryptWhereColumnPlaceholder.toString(), is("column_x IN (?)"));
    }
}
