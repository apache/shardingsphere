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

package org.apache.shardingsphere.single.distsql.segment;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SingleTableSegmentTest {
    
    @Test
    void assertContainsSchema() {
        assertTrue(new SingleTableSegment("foo_ds", "foo_schema", "foo_tbl").containsSchema());
    }
    
    @Test
    void assertDoesNotContainSchema() {
        assertFalse(new SingleTableSegment("foo_ds", null, "foo_tbl").containsSchema());
    }
    
    @Test
    void assertEqualsWithSelf() {
        SingleTableSegment segment = new SingleTableSegment("foo_ds", "foo_schema", "foo_tbl");
        assertThat(segment, is(segment));
    }
    
    @Test
    void assertEqualsWithoutSchemaName() {
        assertThat(new SingleTableSegment("foo_ds", null, "foo_tbl"), is(new SingleTableSegment("FOO_DS", null, "FOO_TBL")));
    }
    
    @Test
    void assertEqualsWithSchemaName() {
        assertThat(new SingleTableSegment("foo_ds", "foo_schema", "foo_tbl"), is(new SingleTableSegment("FOO_DS", "FOO_SCHEMA", "FOO_TBL")));
    }
    
    @SuppressWarnings("ConstantValue")
    @Test
    void assertNotEqualsWithNull() {
        assertFalse(new SingleTableSegment("foo_ds", "foo_schema", "foo_tbl").equals(null));
    }
    
    @Test
    void assertNotEqualsWithOtherType() {
        assertThat(new SingleTableSegment("foo_ds", "foo_schema", "foo_tbl"), not(new Object()));
    }
    
    @Test
    void assertNotEqualsWithDifferentStorageUnitName() {
        assertThat(new SingleTableSegment("foo_ds", "foo_schema", "foo_tbl"), not(new SingleTableSegment("BAR_DS", "FOO_SCHEMA", "FOO_TBL")));
    }
    
    @Test
    void assertNotEqualsWithDifferentSchemaName() {
        assertThat(new SingleTableSegment("foo_ds", "foo_schema", "foo_tbl"), not(new SingleTableSegment("FOO_DS", "BAR_SCHEMA", "FOO_TBL")));
    }
    
    @Test
    void assertNotEqualsWithDifferentTableName() {
        assertThat(new SingleTableSegment("foo_ds", null, "foo_tbl"), not(new SingleTableSegment("FOO_DS", null, "BAR_TBL")));
    }
    
    @Test
    void assertToStringWithoutSchemaName() {
        assertThat(new SingleTableSegment("foo_ds", null, "foo_tbl").toString(), is("foo_ds.foo_tbl"));
    }
    
    @Test
    void assertToStringWithSchemaName() {
        assertThat(new SingleTableSegment("foo_ds", "foo_schema", "foo_tbl").toString(), is("foo_ds.foo_schema.foo_tbl"));
    }
}
