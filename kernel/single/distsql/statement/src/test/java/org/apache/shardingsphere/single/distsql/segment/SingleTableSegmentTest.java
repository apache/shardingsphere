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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SingleTableSegmentTest {
    
    @Test
    void assertContainsSchema() {
        assertTrue(new SingleTableSegment("foo_ds", "foo_schema", "foo_tbl").getSchemaName().isPresent());
    }
    
    @Test
    void assertDoesNotContainSchema() {
        assertFalse(new SingleTableSegment("foo_ds", "foo_tbl").getSchemaName().isPresent());
    }
    
    @Test
    void assertEqualsWithNotSingleTableSegment() {
        assertThat(new SingleTableSegment("foo_ds", "foo_schema", "foo_tbl"), not(new Object()));
    }
    
    @Test
    void assertNotEqualsWithoutSchemaAndDifferentStorageUnitName() {
        assertThat(new SingleTableSegment("foo_ds", "foo_tbl"), not(new SingleTableSegment("bar_ds", "foo_tbl")));
    }
    
    @Test
    void assertNotEqualsWithoutSchemaAndDifferentTableName() {
        assertThat(new SingleTableSegment("foo_ds", "foo_tbl"), not(new SingleTableSegment("foo_ds", "bar_tbl")));
    }
    
    @Test
    void assertNotEqualsWithSchemaAndDifferentStorageUnitName() {
        assertThat(new SingleTableSegment("foo_ds", "foo_schema", "foo_tbl"), not(new SingleTableSegment("bar_ds", "foo_schema", "foo_tbl")));
    }
    
    @Test
    void assertNotEqualsWithDifferentSchema() {
        assertThat(new SingleTableSegment("foo_ds", "foo_schema", "foo_tbl"), not(new SingleTableSegment("foo_ds", "bar_schema", "foo_tbl")));
    }
    
    @Test
    void assertNotEqualsWithSchemaAndDifferentTableName() {
        assertThat(new SingleTableSegment("foo_ds", "foo_schema", "foo_tbl"), not(new SingleTableSegment("foo_ds", "foo_schema", "bar_tbl")));
    }
    
    @Test
    void assertNotEqualsWithMismatchedSchema() {
        assertThat(new SingleTableSegment("foo_ds", "foo_schema", "foo_tbl"), not(new SingleTableSegment("foo_ds", "foo_tbl")));
        assertThat(new SingleTableSegment("foo_ds", "foo_tbl"), not(new SingleTableSegment("foo_ds", "foo_schema", "foo_tbl")));
    }
    
    @Test
    void assertEqualsWithoutSchema() {
        assertThat(new SingleTableSegment("foo_ds", "foo_tbl").hashCode(), is(new SingleTableSegment("foo_ds", "foo_tbl").hashCode()));
    }
    
    @Test
    void assertEqualsWithSchema() {
        assertThat(new SingleTableSegment("foo_ds", "foo_schema", "foo_tbl").hashCode(), is(new SingleTableSegment("foo_ds", "foo_schema", "foo_tbl").hashCode()));
    }
    
    @Test
    void assertHashCode() {
        assertThat(new SingleTableSegment("foo_ds", "foo_tbl"), is(new SingleTableSegment("foo_ds", "foo_tbl")));
        assertThat(new SingleTableSegment("foo_ds", "foo_schema", "foo_tbl"), is(new SingleTableSegment("foo_ds", "foo_schema", "foo_tbl")));
    }
    
    @Test
    void assertToStringWithoutSchemaName() {
        assertThat(new SingleTableSegment("foo_ds", "foo_tbl").toString(), is("foo_ds.foo_tbl"));
    }
    
    @Test
    void assertToStringWithSchemaName() {
        assertThat(new SingleTableSegment("foo_ds", "foo_schema", "foo_tbl").toString(), is("foo_ds.foo_schema.foo_tbl"));
    }
}
