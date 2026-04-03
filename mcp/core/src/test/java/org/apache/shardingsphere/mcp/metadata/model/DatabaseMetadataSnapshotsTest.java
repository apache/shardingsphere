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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class DatabaseMetadataSnapshotsTest {
    
    @Test
    void assertReplaceSnapshot() {
        DatabaseMetadataSnapshots databaseMetadataSnapshots = createDatabaseMetadataSnapshots();
        databaseMetadataSnapshots.replaceSnapshot("logic_db", new DatabaseMetadataSnapshot("MySQL",
                "", List.of(new MetadataObject("logic_db", "public", MetadataObjectType.TABLE, "orders_archive", "", ""))));
        assertThat(databaseMetadataSnapshots.getDatabaseTypes().size(), is(3));
        assertThat(databaseMetadataSnapshots.getMetadataObjects().stream().filter(each -> "logic_db".equals(each.getDatabase())).map(MetadataObject::getName).toList(),
                is(List.of("orders_archive")));
        assertThat(databaseMetadataSnapshots.getMetadataObjects().stream().filter(each -> "analytics_db".equals(each.getDatabase())).map(MetadataObject::getName).toList(),
                is(List.of("public", "metrics", "metric_id")));
    }
    
    private DatabaseMetadataSnapshots createDatabaseMetadataSnapshots() {
        Map<String, DatabaseMetadataSnapshot> result = new LinkedHashMap<>();
        result.put("logic_db", new DatabaseMetadataSnapshot("MySQL", "", List.of(
                new MetadataObject("logic_db", "public", MetadataObjectType.SCHEMA, "public", "", ""),
                new MetadataObject("logic_db", "public", MetadataObjectType.TABLE, "orders", "", ""))));
        result.put("analytics_db", new DatabaseMetadataSnapshot("PostgreSQL", "", List.of(
                new MetadataObject("analytics_db", "public", MetadataObjectType.SCHEMA, "public", "", ""),
                new MetadataObject("analytics_db", "public", MetadataObjectType.TABLE, "metrics", "", ""),
                new MetadataObject("analytics_db", "public", MetadataObjectType.COLUMN, "metric_id", "TABLE", "metrics"))));
        result.put("warehouse", new DatabaseMetadataSnapshot("Hive", "", List.of(
                new MetadataObject("warehouse", "warehouse", MetadataObjectType.SCHEMA, "warehouse", "", ""),
                new MetadataObject("warehouse", "warehouse", MetadataObjectType.TABLE, "facts", "", ""))));
        return new DatabaseMetadataSnapshots(result);
    }
}
