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

package org.apache.shardingsphere.mcp.metadata.query;

import org.apache.shardingsphere.mcp.metadata.model.DatabaseMetadataSnapshots;
import org.apache.shardingsphere.mcp.metadata.model.MetadataObject;
import org.apache.shardingsphere.mcp.metadata.model.MetadataObjectType;
import org.apache.shardingsphere.mcp.protocol.exception.MCPUnsupportedException;
import org.apache.shardingsphere.mcp.resource.ResourceTestDataFactory;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MetadataQueryServiceTest {
    
    private final MetadataQueryService metadataQueryService = new MetadataQueryService();
    
    private final DatabaseMetadataSnapshots databaseMetadataSnapshots = ResourceTestDataFactory.createDatabaseMetadataSnapshots();
    
    @Test
    void assertQueryDatabases() {
        List<MetadataObject> actual = metadataQueryService.queryDatabases(databaseMetadataSnapshots);
        assertThat(actual.size(), is(2));
        assertThat(actual.get(0).getName(), is("logic_db"));
        assertThat(actual.get(1).getName(), is("warehouse"));
    }
    
    @Test
    void assertQueryDatabase() {
        Optional<MetadataObject> actual = metadataQueryService.queryDatabase(databaseMetadataSnapshots, "logic_db");
        assertTrue(actual.isPresent());
        assertThat(actual.get().getName(), is("logic_db"));
    }
    
    @Test
    void assertQueryMetadataObjectsBySchemaAndName() {
        List<MetadataObject> actual = metadataQueryService.queryMetadataObjects(databaseMetadataSnapshots, "logic_db",
                MetadataObjectType.TABLE, MetadataObjectQueryCondition.schemaAndObject("public", "orders"));
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getName(), is("orders"));
    }
    
    @Test
    void assertQueryMetadataObjectsByParentAndName() {
        List<MetadataObject> actual = metadataQueryService.queryMetadataObjects(databaseMetadataSnapshots, "logic_db",
                MetadataObjectType.COLUMN, MetadataObjectQueryCondition.parentAndObject("public", "TABLE", "orders", "order_id"));
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getName(), is("order_id"));
    }
    
    @Test
    void assertQueryMetadataObjectsWithUnsupportedIndexType() {
        MCPUnsupportedException actual = assertThrows(MCPUnsupportedException.class, () -> metadataQueryService.queryMetadataObjects(databaseMetadataSnapshots, "warehouse",
                MetadataObjectType.INDEX, MetadataObjectQueryCondition.parent("warehouse", "TABLE", "facts")));
        assertThat(actual.getMessage(), is("Index resources are not supported for the current database."));
    }
}
