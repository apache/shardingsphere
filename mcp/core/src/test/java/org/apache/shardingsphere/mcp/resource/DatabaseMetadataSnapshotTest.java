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

import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DatabaseMetadataSnapshotTest {
    
    @Test
    void assertNewDatabaseMetadataSnapshot() {
        List<MetadataObject> metadataObjects = new LinkedList<>();
        metadataObjects.add(new MetadataObject("logic_db", "public", MetadataObjectType.TABLE, "orders", "", ""));
        DatabaseMetadataSnapshot actual = new DatabaseMetadataSnapshot("MySQL", "8.0.32", metadataObjects, "public");
        metadataObjects.add(new MetadataObject("logic_db", "public", MetadataObjectType.TABLE, "order_items", "", ""));
        assertThat(actual.getMetadataObjects().size(), is(1));
        assertThat(actual.getDefaultSchema(), is("public"));
        assertThrows(UnsupportedOperationException.class,
                () -> actual.getMetadataObjects().add(new MetadataObject("logic_db", "public", MetadataObjectType.TABLE, "items", "", "")));
    }
    
    @Test
    void assertNewDatabaseMetadataSnapshotWithDefaultValues() {
        DatabaseMetadataSnapshot actual = new DatabaseMetadataSnapshot("MySQL", List.of());
        assertThat(actual.getDatabaseVersion(), is(""));
        assertThat(actual.getDefaultSchema(), is(""));
        assertThat(actual.getMetadataObjects().size(), is(0));
    }
}
