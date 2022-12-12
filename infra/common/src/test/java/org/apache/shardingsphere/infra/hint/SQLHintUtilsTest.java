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

package org.apache.shardingsphere.infra.hint;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public final class SQLHintUtilsTest {
    
    @Test
    public void assertGetSQLHintPropsWithNoProp() {
        assertTrue(SQLHintUtils.getSQLHintProps("/* */").isEmpty());
    }
    
    @Test
    public void assertGetSQLHintPropsWithSingleProp() {
        Properties actual = SQLHintUtils.getSQLHintProps("/* SHARDINGSPHERE_HINT: TABLE_NAME=t_order */");
        assertThat(actual.size(), is(1));
        assertThat(actual.get("TABLE_NAME"), is("t_order"));
    }
    
    @Test
    public void assertGetSQLHintPropsWithMultiProps() {
        Properties actual = SQLHintUtils.getSQLHintProps("/* SHARDINGSPHERE_HINT: TABLE_NAME=t_order, COLUMN_NAME=order_id */");
        assertThat(actual.size(), is(2));
        assertThat(actual.get("TABLE_NAME"), is("t_order"));
        assertThat(actual.get("COLUMN_NAME"), is("order_id"));
    }
    
    @Test
    public void assertGetSQLHintPropsWithWrongFormat() {
        Properties actual = SQLHintUtils.getSQLHintProps("/* SHARDINGSPHERE_HINT: TABLE_NAME=t_order, , DATABASE_NAME:sharding_db, COLUMN_NAME=order_id */");
        assertThat(actual.size(), is(2));
        assertThat(actual.get("TABLE_NAME"), is("t_order"));
        assertThat(actual.get("COLUMN_NAME"), is("order_id"));
    }
    
    @Test
    public void assertGetSplitterSQLHintValue() {
        Collection<String> actual = SQLHintUtils.getSplitterSQLHintValue("  sharding_audit1    sharding_audit2 ");
        assertThat(actual.size(), is(2));
        assertTrue(actual.containsAll(Arrays.asList("sharding_audit1", "sharding_audit2")));
    }
}
