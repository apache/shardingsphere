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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.Map;
import org.apache.groovy.util.Maps;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;
import org.junit.Test;

public class FederationDatabaseMetaDataTest {
    
    @Test
    public void assertPutSchemaMetadata() {
        final FederationDatabaseMetaData federationDatabaseMetaData = new FederationDatabaseMetaData("foo",
                Collections.emptyMap());
        final FederationSchemaMetaData schemaMetaData = mock(FederationSchemaMetaData.class);
        federationDatabaseMetaData.putSchemaMetadata("foo_db", schemaMetaData);
        assertThat(federationDatabaseMetaData.getSchemas().get("foo_db"), is(schemaMetaData));
    }
    
    @Test
    public void assertRemoveSchemaMetadata() {
        final Map<String, ShardingSphereSchema> map = Maps.of(
                "foo_db_1", mock(ShardingSphereSchema.class),
                "foo_db_2", mock(ShardingSphereSchema.class));
        final FederationDatabaseMetaData federationDatabaseMetaData = new FederationDatabaseMetaData("foo", map);
        assertTrue(federationDatabaseMetaData.getSchemas().containsKey("foo_db_1"));
        federationDatabaseMetaData.removeSchemaMetadata("foo_db_1");
        assertFalse(federationDatabaseMetaData.getSchemas().containsKey("foo_db_1"));
        assertTrue(federationDatabaseMetaData.getSchemas().containsKey("foo_db_2"));
    }
    
    @Test
    public void assertGetSchemaMetadata() {
        final ShardingSphereSchema sphereSchema = mock(ShardingSphereSchema.class);
        final Map<String, ShardingSphereSchema> map = Maps.of("foo_db", sphereSchema);
        final FederationDatabaseMetaData federationDatabaseMetaData = new FederationDatabaseMetaData("foo", map);
        assertNotNull(federationDatabaseMetaData.getSchemaMetadata("foo_db").orElse(null));
    }
    
    @Test
    public void assertGetSchemaMetadataByNonexistentKey() {
        final ShardingSphereSchema sphereSchema = mock(ShardingSphereSchema.class);
        final Map<String, ShardingSphereSchema> map = Maps.of("foo_db", sphereSchema);
        final FederationDatabaseMetaData federationDatabaseMetaData = new FederationDatabaseMetaData("foo", map);
        federationDatabaseMetaData.getSchemaMetadata("foo_db_2");
    }
    
    @Test
    public void assertPutTable() {
        final FederationDatabaseMetaData federationDatabaseMetaData = new FederationDatabaseMetaData("foo", Collections.emptyMap());
        final ShardingSphereTable fooTable = new ShardingSphereTable("foo_table", Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList());
        federationDatabaseMetaData.putTable("foo_db", fooTable);
        assertNotNull(federationDatabaseMetaData.getSchemaMetadata("foo_db").orElse(null));
        assertNotNull(federationDatabaseMetaData.getSchemaMetadata("foo_db").map(FederationSchemaMetaData::getTables).map(e -> e.get("foo_table")).orElse(null));
    }
    
    @Test
    public void assertRemoveTableMetadata() {
        final FederationDatabaseMetaData federationDatabaseMetaData = new FederationDatabaseMetaData("foo", Collections.emptyMap());
        final ShardingSphereTable fooTable = new ShardingSphereTable("foo_table", Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList());
        federationDatabaseMetaData.putTable("foo_db", fooTable);
        federationDatabaseMetaData.removeTableMetadata("foo_db", "foo_table");
        assertNull(federationDatabaseMetaData.getSchemaMetadata("foo_db").map(s -> s.getTables().get("foo_table")).orElse(null));
    }
    
}
