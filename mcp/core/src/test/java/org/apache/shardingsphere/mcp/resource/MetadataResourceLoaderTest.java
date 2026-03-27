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

import org.apache.shardingsphere.mcp.capability.DatabaseCapabilityAssembler;
import org.apache.shardingsphere.mcp.protocol.ErrorCode;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MetadataResourceLoaderTest {
    
    @Test
    void assertConstructWithNullCapabilityAssembler() {
        MetadataResourceLoader actual = assertDoesNotThrow(() -> new MetadataResourceLoader(null));
        assertNotNull(actual);
    }
    
    @Test
    void assertLoadDatabases() {
        MetadataResourceLoader loader = createLoader();
        ResourceLoadResult actual = loader.load(createMetadataCatalog(), new ResourceRequest("", "", MetadataObjectType.DATABASE, "", "", ""));
        assertTrue(actual.isSuccessful());
        assertThat(actual.getMetadataObjects().size(), is(3));
        assertThat(actual.getMetadataObjects().get(0).getName(), is("analytics_db"));
        assertThat(actual.getMetadataObjects().get(2).getName(), is("warehouse"));
    }
    
    @Test
    void assertLoadDatabaseByObjectName() {
        MetadataResourceLoader loader = createLoader();
        ResourceLoadResult actual = loader.load(createMetadataCatalog(), new ResourceRequest("", "", MetadataObjectType.DATABASE, "logic_db", "", ""));
        assertTrue(actual.isSuccessful());
        assertThat(actual.getMetadataObjects().size(), is(1));
        assertThat(actual.getMetadataObjects().get(0).getName(), is("logic_db"));
    }
    
    @Test
    void assertLoadTableMetadata() {
        MetadataResourceLoader loader = createLoader();
        ResourceLoadResult actual = loader.load(createMetadataCatalog(), new ResourceRequest("logic_db", "public", MetadataObjectType.TABLE, "", "", ""));
        assertTrue(actual.isSuccessful());
        assertThat(actual.getMetadataObjects().size(), is(2));
        assertThat(actual.getMetadataObjects().get(0).getName(), is("order_items"));
        assertThat(actual.getMetadataObjects().get(1).getName(), is("orders"));
    }
    
    @Test
    void assertLoadColumnsByParentObject() {
        MetadataResourceLoader loader = createLoader();
        ResourceLoadResult actual = loader.load(createMetadataCatalog(), new ResourceRequest("logic_db", "public", MetadataObjectType.COLUMN, "", "TABLE", "orders"));
        assertTrue(actual.isSuccessful());
        assertThat(actual.getMetadataObjects().size(), is(2));
        assertThat(actual.getMetadataObjects().get(0).getName(), is("order_id"));
        assertThat(actual.getMetadataObjects().get(0).getParentObjectName(), is("orders"));
    }
    
    @Test
    void assertLoadWithUnsupportedIndexResource() {
        MetadataResourceLoader loader = createLoader();
        ResourceLoadResult actual = loader.load(createMetadataCatalog(), new ResourceRequest("warehouse", "warehouse", MetadataObjectType.INDEX, "", "TABLE", "facts"));
        assertFalse(actual.isSuccessful());
        assertTrue(actual.getErrorCode().isPresent());
        assertThat(actual.getErrorCode().get(), is(ErrorCode.UNSUPPORTED));
        assertThat(actual.getMessage(), is("Index resources are not supported for the current database."));
    }
    
    @Test
    void assertLoadWithExcludedObjectType() {
        MetadataResourceLoader loader = createLoader();
        ResourceLoadResult actual = loader.load(createMetadataCatalog(), new ResourceRequest("logic_db", "public", MetadataObjectType.MATERIALIZED_VIEW, "", "", ""));
        assertTrue(actual.isSuccessful());
        assertThat(actual.getMetadataObjects().size(), is(0));
    }
    
    @Test
    void assertLoadWithUnknownDatabase() {
        MetadataResourceLoader loader = createLoader();
        IllegalStateException actual = assertThrows(IllegalStateException.class,
                () -> loader.load(createMetadataCatalog(), new ResourceRequest("missing_db", "public", MetadataObjectType.TABLE, "", "", "")));
        assertThat(actual.getMessage(), is("Database does not exist."));
    }
    
    @Test
    void assertReplaceDatabaseSnapshot() {
        MetadataCatalog metadataCatalog = createMetadataCatalog();
        metadataCatalog.replaceDatabaseSnapshot("logic_db", "MySQL",
                List.of(new MetadataObject("logic_db", "public", MetadataObjectType.TABLE, "orders_archive", "", "")),
                new RuntimeDatabaseDescriptor("MySQL", "", Set.of(MetadataObjectType.TABLE), "public"));
        assertThat(metadataCatalog.getDatabaseTypes().size(), is(3));
        assertThat(metadataCatalog.getMetadataObjects().stream().filter(each -> "logic_db".equals(each.getDatabase())).map(MetadataObject::getName).toList(),
                is(List.of("orders_archive")));
        assertThat(metadataCatalog.getMetadataObjects().stream().filter(each -> "analytics_db".equals(each.getDatabase())).map(MetadataObject::getName).toList(),
                is(List.of("public", "metrics", "metric_id")));
    }
    
    private MetadataCatalog createMetadataCatalog() {
        Map<String, String> databaseTypes = new LinkedHashMap<>();
        databaseTypes.put("logic_db", "MySQL");
        databaseTypes.put("analytics_db", "PostgreSQL");
        databaseTypes.put("warehouse", "Hive");
        LinkedList<MetadataObject> metadataObjects = new LinkedList<>();
        metadataObjects.add(new MetadataObject("logic_db", "public", MetadataObjectType.SCHEMA, "public", "", ""));
        metadataObjects.add(new MetadataObject("logic_db", "public", MetadataObjectType.TABLE, "orders", "", ""));
        metadataObjects.add(new MetadataObject("logic_db", "public", MetadataObjectType.TABLE, "order_items", "", ""));
        metadataObjects.add(new MetadataObject("logic_db", "public", MetadataObjectType.VIEW, "active_orders", "", ""));
        metadataObjects.add(new MetadataObject("logic_db", "public", MetadataObjectType.COLUMN, "order_id", "TABLE", "orders"));
        metadataObjects.add(new MetadataObject("logic_db", "public", MetadataObjectType.COLUMN, "status", "TABLE", "orders"));
        metadataObjects.add(new MetadataObject("logic_db", "public", MetadataObjectType.INDEX, "idx_orders_status", "TABLE", "orders"));
        metadataObjects.add(new MetadataObject("logic_db", "public", MetadataObjectType.MATERIALIZED_VIEW, "mv_orders", "", ""));
        metadataObjects.add(new MetadataObject("logic_db", "public", MetadataObjectType.SEQUENCE, "order_seq", "", ""));
        metadataObjects.add(new MetadataObject("analytics_db", "public", MetadataObjectType.SCHEMA, "public", "", ""));
        metadataObjects.add(new MetadataObject("analytics_db", "public", MetadataObjectType.TABLE, "metrics", "", ""));
        metadataObjects.add(new MetadataObject("analytics_db", "public", MetadataObjectType.COLUMN, "metric_id", "TABLE", "metrics"));
        metadataObjects.add(new MetadataObject("warehouse", "warehouse", MetadataObjectType.SCHEMA, "warehouse", "", ""));
        metadataObjects.add(new MetadataObject("warehouse", "warehouse", MetadataObjectType.TABLE, "facts", "", ""));
        metadataObjects.add(new MetadataObject("warehouse", "warehouse", MetadataObjectType.COLUMN, "fact_id", "TABLE", "facts"));
        return new MetadataCatalog(databaseTypes, metadataObjects);
    }
    
    private MetadataResourceLoader createLoader() {
        return new MetadataResourceLoader(new DatabaseCapabilityAssembler(new MetadataCatalog(Collections.emptyMap(), Collections.emptyList())));
    }
}
