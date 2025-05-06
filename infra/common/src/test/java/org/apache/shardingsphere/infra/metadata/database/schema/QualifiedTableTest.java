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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class QualifiedTableTest {
    
    @Test
    void assertFormat() {
        assertThat(new QualifiedTable("schema", "t_order").format(), is("schema.t_order"));
        assertThat(new QualifiedTable("SCHEMA", "T_ORDER").format(), is("SCHEMA.T_ORDER"));
        assertThat(new QualifiedTable(null, "t_order").format(), is("t_order"));
    }
    
    @Test
    void assertEqualsTrueWithoutSchema() {
        QualifiedTable actual = new QualifiedTable(null, "t_order");
        QualifiedTable expected = new QualifiedTable(null, "T_ORDER");
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertEqualsTrueWithSchema() {
        QualifiedTable actual = new QualifiedTable("schema", "t_order");
        QualifiedTable expected = new QualifiedTable("SCHEMA", "T_ORDER");
        assertThat(actual, is(expected));
        actual = new QualifiedTable("schema", null);
        expected = new QualifiedTable("SCHEMA", null);
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertEqualsFalse() {
        QualifiedTable actual = new QualifiedTable("schema", "t_order");
        QualifiedTable expected = new QualifiedTable(null, "t_order");
        assertNotEquals(actual, expected);
        actual = new QualifiedTable("schema", "t_order");
        expected = new QualifiedTable("schema", null);
        assertNotEquals(actual, expected);
    }
    
    @Test
    void assertToString() {
        assertThat(new QualifiedTable("foo_schema", "foo_tbl").toString(), is("foo_schema.foo_tbl"));
    }
}
