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

package org.apache.shardingsphere.infra.metadata.database.schema;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

class QualifiedTableTest {
    
    @Test
    void assertFormat() {
        assertThat(new QualifiedTable("schema", "t_order").format(), is("schema.t_order"));
        assertThat(new QualifiedTable("SCHEMA", "T_ORDER").format(), is("SCHEMA.T_ORDER"));
        assertThat(new QualifiedTable(null, "t_order").format(), is("t_order"));
    }
    
    @Test
    void assertEqualsTrueWithoutSchema() {
        assertThat(new QualifiedTable(null, "t_order"), is(new QualifiedTable(null, "T_ORDER")));
        assertThat(new QualifiedTable("schema", "t_order"), is(new QualifiedTable("SCHEMA", "T_ORDER")));
        assertThat(new QualifiedTable("schema", null), is(new QualifiedTable("SCHEMA", null)));
        assertThat(new QualifiedTable("schema", "t_order"), not(new QualifiedTable(null, "t_order")));
        assertThat(new QualifiedTable("schema", "t_order"), not(new QualifiedTable("schema", null)));
        assertThat(new QualifiedTable("schema", "table"), not(new QualifiedTable("schema", null)));
        assertThat(new QualifiedTable(null, "table"), not(new QualifiedTable(null, null)));
        assertThat(new QualifiedTable(null, null), is(new QualifiedTable(null, null)));
        assertThat(new QualifiedTable("schema", "table"), not((Object) null));
        assertThat(new QualifiedTable("schema", "table"), not(new Object()));
    }
    
    @Test
    void assertHashCode() {
        assertThat(new QualifiedTable("schema", "table").hashCode(), is(new QualifiedTable("SCHEMA", "TABLE").hashCode()));
        assertThat(new QualifiedTable(null, "table").hashCode(), is(new QualifiedTable(null, "TABLE").hashCode()));
        assertThat(new QualifiedTable("schema", null).hashCode(), is(new QualifiedTable("SCHEMA", null).hashCode()));
    }
    
    @Test
    void assertToString() {
        assertThat(new QualifiedTable("foo_schema", "foo_tbl").toString(), is("foo_schema.foo_tbl"));
    }
}
