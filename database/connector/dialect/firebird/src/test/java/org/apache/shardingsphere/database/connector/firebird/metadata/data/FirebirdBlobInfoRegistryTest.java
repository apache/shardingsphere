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

package org.apache.shardingsphere.database.connector.firebird.metadata.data;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.OptionalInt;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FirebirdBlobInfoRegistryTest {
    
    @Test
    void assertRefreshAndFindBlobSubtype() {
        FirebirdBlobInfoRegistry.refreshTable("schema_a", "table_a", Collections.singletonMap("blob_col", 2));
        OptionalInt actual = FirebirdBlobInfoRegistry.findBlobSubtype("schema_a", "table_a", "BLOB_COL");
        assertTrue(actual.isPresent());
        assertThat(actual.getAsInt(), is(2));
        assertTrue(FirebirdBlobInfoRegistry.isBlobColumn("schema_a", "table_a", "blob_col"));
        FirebirdBlobInfoRegistry.refreshTable("schema_a", "table_a", Collections.emptyMap());
    }
    
    @Test
    void assertRefreshTableRemovesEntryWhenEmpty() {
        FirebirdBlobInfoRegistry.refreshTable("schema_b", "table_b", Collections.singletonMap("blob_col", 1));
        FirebirdBlobInfoRegistry.refreshTable("schema_b", "table_b", Collections.emptyMap());
        assertFalse(FirebirdBlobInfoRegistry.isBlobColumn("schema_b", "table_b", "blob_col"));
    }
    
    @Test
    void assertTrimLogicTableNameMatches() {
        FirebirdBlobInfoRegistry.refreshTable("schema_c", "table_1", Collections.singletonMap("blob_col", 3));
        assertTrue(FirebirdBlobInfoRegistry.isBlobColumn("schema_c", "table_2", "blob_col"));
        FirebirdBlobInfoRegistry.refreshTable("schema_c", "table_1", Collections.emptyMap());
    }
}