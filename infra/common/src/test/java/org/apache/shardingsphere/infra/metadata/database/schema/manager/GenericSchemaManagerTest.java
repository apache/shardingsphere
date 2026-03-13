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

package org.apache.shardingsphere.infra.metadata.database.schema.manager;

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.TableType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GenericSchemaManagerTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertGetToBeAlteredSchemasWithTablesAdded() {
        ShardingSphereSchema reloadSchema = new ShardingSphereSchema("foo_schema", databaseType,
                Collections.singleton(new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList())), Collections.emptyList());
        ShardingSphereDatabase reloadDatabase = createDatabase(Collections.singleton(reloadSchema));
        ShardingSphereSchema currentSchemas = new ShardingSphereSchema("foo_schema", databaseType, Collections.emptyList(), Collections.emptyList());
        ShardingSphereDatabase currentDatabase = createDatabase(Collections.singleton(currentSchemas));
        Collection<ShardingSphereSchema> actual = GenericSchemaManager.getToBeAlteredSchemasWithTablesAdded(reloadDatabase, currentDatabase);
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next().getAllTables().size(), is(1));
        assertTrue(actual.iterator().next().containsTable("foo_tbl"));
    }
    
    @Test
    void assertGetToBeAlteredSchemasWithTablesDropped() {
        ShardingSphereSchema reloadSchema = new ShardingSphereSchema("foo_schema", databaseType, Collections.emptyList(), Collections.emptyList());
        ShardingSphereDatabase reloadDatabase = createDatabase(Collections.singleton(reloadSchema));
        ShardingSphereSchema currentSchema = new ShardingSphereSchema("foo_schema", databaseType,
                Collections.singleton(new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList())), Collections.emptyList());
        ShardingSphereDatabase currentDatabase = createDatabase(Collections.singleton(currentSchema));
        Collection<ShardingSphereSchema> actual = GenericSchemaManager.getToBeAlteredSchemasWithTablesDropped(reloadDatabase, currentDatabase);
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next().getAllTables().size(), is(1));
        assertTrue(actual.iterator().next().containsTable("foo_tbl"));
    }
    
    @Test
    void assertGetToBeAddedTables() {
        ShardingSphereSchema reloadSchema = new ShardingSphereSchema("foo_schema", databaseType,
                Collections.singleton(new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), TableType.TABLE)), Collections.emptyList());
        Collection<ShardingSphereTable> actual = GenericSchemaManager.getToBeAddedTables(
                reloadSchema, new ShardingSphereSchema("foo_schema", databaseType, Collections.emptyList(), Collections.emptyList()));
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next().getName(), is("foo_tbl"));
    }
    
    @Test
    void assertGetToBeDroppedTables() {
        ShardingSphereSchema reloadSchema = new ShardingSphereSchema("foo_schema", databaseType, Collections.emptyList(), Collections.emptyList());
        ShardingSphereSchema currentSchema = new ShardingSphereSchema("foo_schema", databaseType,
                Collections.singleton(new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), TableType.TABLE)), Collections.emptyList());
        Collection<ShardingSphereTable> actual = GenericSchemaManager.getToBeDroppedTables(reloadSchema, currentSchema);
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next().getName(), is("foo_tbl"));
    }
    
    @Test
    void assertGetToBeDroppedSchemaNames() {
        ShardingSphereSchema currentSchema = new ShardingSphereSchema("foo_schema", databaseType);
        ShardingSphereDatabase currentDatabase = createDatabase(Collections.singleton(currentSchema));
        Collection<String> actual = GenericSchemaManager.getToBeDroppedSchemaNames(createDatabase(Collections.emptyList()), currentDatabase);
        assertThat(actual, is(Collections.singleton("foo_schema")));
    }
    
    private ShardingSphereDatabase createDatabase(final Collection<ShardingSphereSchema> schemas) {
        return new ShardingSphereDatabase("foo_db", databaseType, new ResourceMetaData(Collections.emptyMap(), Collections.emptyMap()),
                new RuleMetaData(Collections.emptyList()), schemas, new ConfigurationProperties(new Properties()));
    }
}
