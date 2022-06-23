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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;
import org.junit.Test;

public final class FederationDatabaseMetaDataTest {
    
    @Test
    public void assertPutSchemaMetadata() {
        FederationDatabaseMetaData federationDatabaseMetaData = new FederationDatabaseMetaData("foo",
                Collections.emptyMap());
        FederationSchemaMetaData schemaMetaData = mock(FederationSchemaMetaData.class);
        federationDatabaseMetaData.putSchemaMetadata("foo_db", schemaMetaData);
        assertThat(federationDatabaseMetaData.getSchemas().get("foo_db"), is(schemaMetaData));
    }
    
    @Test
    public void assertRemoveSchemaMetadata() {
        Map<String, ShardingSphereSchema> map = new HashMap<>();
        map.put("foo_db_1", mock(ShardingSphereSchema.class));
        map.put("foo_db_2", mock(ShardingSphereSchema.class));
        FederationDatabaseMetaData federationDatabaseMetaData = new FederationDatabaseMetaData("foo", map);
        assertTrue(federationDatabaseMetaData.getSchemas().containsKey("foo_db_1"));
        federationDatabaseMetaData.removeSchemaMetadata("foo_db_1");
        assertFalse(federationDatabaseMetaData.getSchemas().containsKey("foo_db_1"));
        assertTrue(federationDatabaseMetaData.getSchemas().containsKey("foo_db_2"));
    }
    
    @Test
    public void assertGetSchemaMetadata() {
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        Map<String, ShardingSphereSchema> map = new HashMap<>();
        map.put("foo_db", schema);
        FederationDatabaseMetaData federationDatabaseMetaData = new FederationDatabaseMetaData("foo", map);
        assertTrue(federationDatabaseMetaData.getSchemaMetadata("foo_db").isPresent());
    }
    
    @Test
    public void assertGetSchemaMetadataByNonexistentKey() {
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        Map<String, ShardingSphereSchema> map = new HashMap<>();
        map.put("foo_db", schema);
        FederationDatabaseMetaData federationDatabaseMetaData = new FederationDatabaseMetaData("foo", map);
        assertFalse(federationDatabaseMetaData.getSchemaMetadata("foo_db_2").isPresent());
    }
    
    @Test
    public void assertPutTable() {
        FederationDatabaseMetaData federationDatabaseMetaData = new FederationDatabaseMetaData("foo", Collections.emptyMap());
        ShardingSphereTable fooTable = new ShardingSphereTable("foo_table", Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList());
        federationDatabaseMetaData.putTable("foo_db", fooTable);
        assertTrue(federationDatabaseMetaData.getSchemaMetadata("foo_db").isPresent());
        assertTrue(federationDatabaseMetaData.getSchemaMetadata("foo_db").map(FederationSchemaMetaData::getTables).map(e -> e.get("foo_table")).isPresent());
    }
    
    @Test
    public void assertRemoveTableMetadata() {
        FederationDatabaseMetaData federationDatabaseMetaData = new FederationDatabaseMetaData("foo", Collections.emptyMap());
        ShardingSphereTable fooTable = new ShardingSphereTable("foo_table", Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList());
        federationDatabaseMetaData.putTable("foo_db", fooTable);
        federationDatabaseMetaData.removeTableMetadata("foo_db", "foo_table");
        assertFalse(federationDatabaseMetaData.getSchemaMetadata("foo_db").map(s -> s.getTables().get("foo_table")).isPresent());
    }
}
