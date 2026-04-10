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

package org.apache.shardingsphere.mcp.metadata.model;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class MCPIndexMetadataTest {
    
    @Test
    void assertMetadata() {
        MCPIndexMetadata actual = new MCPIndexMetadata("foo_db", "foo_schema", "foo_table", "foo_index");
        assertThat(actual.getDatabase(), is("foo_db"));
        assertThat(actual.getSchema(), is("foo_schema"));
        assertThat(actual.getTable(), is("foo_table"));
        assertThat(actual.getIndex(), is("foo_index"));
    }
}
