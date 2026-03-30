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

package org.apache.shardingsphere.mcp.resource;

import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DatabaseMetadataSnapshotTest {
    
    @Test
    void assertConstruct() {
        DatabaseMetadataSnapshot actual = new DatabaseMetadataSnapshot("MySQL", Collections.emptyList());
        assertThat(actual.getDatabaseType(), is("MySQL"));
        assertThat(actual.getDatabaseVersion(), is(""));
        assertThat(actual.getMetadataObjects(), is(Collections.emptyList()));
    }
    
    @Test
    void assertConstructWithDatabaseVersion() {
        DatabaseMetadataSnapshot actual = new DatabaseMetadataSnapshot("MySQL", "8.0.32", Collections.emptyList());
        assertThat(actual.getDatabaseType(), is("MySQL"));
        assertThat(actual.getDatabaseVersion(), is("8.0.32"));
        assertThat(actual.getMetadataObjects(), is(Collections.emptyList()));
    }
    
    @Test
    void assertConstructWithNullDatabaseType() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class,
                () -> new DatabaseMetadataSnapshot(null, Collections.emptyList()));
        assertThat(actual.getMessage(), is("databaseType cannot be null."));
    }
    
    @Test
    void assertConstructWithEmptyDatabaseType() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class,
                () -> new DatabaseMetadataSnapshot("", Collections.emptyList()));
        assertThat(actual.getMessage(), is("databaseType cannot be empty."));
    }
    
    @Test
    void assertConstructWithBlankDatabaseType() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class,
                () -> new DatabaseMetadataSnapshot("   ", Collections.emptyList()));
        assertThat(actual.getMessage(), is("databaseType cannot be empty."));
    }
}
