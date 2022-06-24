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

package org.apache.shardingsphere.infra.federation.optimizer.metadata;

import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class FederationDatabaseMetaDataTest {
    
    @Test
    public void assertPutSchemaMetadata() {
        FederationDatabaseMetaData databaseMetaData = new FederationDatabaseMetaData("foo_db", Collections.emptyMap());
        FederationSchemaMetaData schemaMetaData = mock(FederationSchemaMetaData.class);
        databaseMetaData.putSchemaMetadata("foo_schema", schemaMetaData);
        assertThat(databaseMetaData.getSchemas().get("foo_schema"), is(schemaMetaData));
    }
    
    @Test
    public void assertRemoveSchemaMetadata() {
        Map<String, ShardingSphereSchema> schemas = new HashMap<>(2, 1);
        schemas.put("foo_schema_1", mock(ShardingSphereSchema.class));
        schemas.put("foo_schema_2", mock(ShardingSphereSchema.class));
        FederationDatabaseMetaData databaseMetaData = new FederationDatabaseMetaData("foo_db", schemas);
        assertTrue(databaseMetaData.getSchemas().containsKey("foo_schema_1"));
        databaseMetaData.removeSchemaMetadata("foo_schema_1");
        assertFalse(databaseMetaData.getSchemas().containsKey("foo_schema_1"));
        assertTrue(databaseMetaData.getSchemas().containsKey("foo_schema_2"));
    }
    
    @Test
    public void assertGetSchemaMetadata() {
        Map<String, ShardingSphereSchema> schemas = new HashMap<>(1, 1);
        schemas.put("foo_schema", mock(ShardingSphereSchema.class));
        FederationDatabaseMetaData federationDatabaseMetaData = new FederationDatabaseMetaData("foo_db", schemas);
        assertTrue(federationDatabaseMetaData.getSchemaMetadata("foo_schema").isPresent());
    }
    
    @Test
    public void assertGetSchemaMetadataWhenNotContainsKey() {
        Map<String, ShardingSphereSchema> schemas = new HashMap<>(1, 1);
        schemas.put("foo_schema", mock(ShardingSphereSchema.class));
        FederationDatabaseMetaData federationDatabaseMetaData = new FederationDatabaseMetaData("foo_db", schemas);
        assertFalse(federationDatabaseMetaData.getSchemaMetadata("foo_schema_2").isPresent());
    }
    
    @Test
    public void assertPutTable() {
        FederationDatabaseMetaData databaseMetaData = new FederationDatabaseMetaData("foo_db", Collections.emptyMap());
        ShardingSphereTable table = new ShardingSphereTable("foo_table", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        databaseMetaData.putTable("foo_schema", table);
        assertTrue(databaseMetaData.getSchemaMetadata("foo_schema").isPresent());
        assertTrue(databaseMetaData.getSchemaMetadata("foo_schema").get().getTables().containsKey("foo_table"));
    }
    
    @Test
    public void assertRemoveTableMetadata() {
        FederationDatabaseMetaData databaseMetaData = new FederationDatabaseMetaData("foo_db", Collections.emptyMap());
        ShardingSphereTable table = new ShardingSphereTable("foo_table", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        databaseMetaData.putTable("foo_schema", table);
        databaseMetaData.removeTableMetadata("foo_schema", "foo_table");
        assertTrue(databaseMetaData.getSchemaMetadata("foo_schema").isPresent());
        assertFalse(databaseMetaData.getSchemaMetadata("foo_schema").get().getTables().containsKey("foo_table"));
    }
}
