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

package org.apache.shardingsphere.mode.metadata.persist.metadata.service;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereView;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.metadata.persist.version.VersionPersistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SchemaMetaDataPersistServiceTest {
    
    private SchemaMetaDataPersistService persistService;
    
    private FixturePersistRepository repository;
    
    private TableMetaDataPersistEnabledService tableMetaDataPersistService;
    
    private ViewMetaDataPersistService viewMetaDataPersistService;
    
    private DatabaseType databaseType;
    
    @BeforeEach
    void setUp() {
        repository = new FixturePersistRepository();
        tableMetaDataPersistService = new TableMetaDataPersistEnabledService(repository, new VersionPersistService(repository));
        viewMetaDataPersistService = new ViewMetaDataPersistService(repository, new VersionPersistService(repository));
        persistService = new SchemaMetaDataPersistService(repository, tableMetaDataPersistService, viewMetaDataPersistService);
        databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    }
    
    @Test
    void assertAdd() {
        persistService.add("foo_db", "foo_schema");
        assertThat(repository.query("/metadata/foo_db/schemas/foo_schema"), is(""));
    }
    
    @Test
    void assertDrop() {
        repository.persist("/metadata/foo_db/schemas/foo_schema/tables", "");
        persistService.drop("foo_db", "foo_schema");
        assertFalse(repository.isExisted("/metadata/foo_db/schemas/foo_schema"));
    }
    
    @Test
    void assertAlterByRefreshWithoutTablesAndViews() {
        persistService.alterByRefresh("foo_db", new ShardingSphereSchema("foo_schema", databaseType));
        assertThat(repository.query("/metadata/foo_db/schemas/foo_schema"), is(""));
        assertTrue(tableMetaDataPersistService.load("foo_db", "foo_schema").isEmpty());
    }
    
    @Test
    void assertAlterByRefreshWithTables() {
        persistService.alterByRefresh("foo_db", new ShardingSphereSchema("foo_schema", databaseType, Collections.singleton(createTable("Foo_Tbl")), Collections.emptyList()));
        assertTrue(repository.isExisted("/metadata/foo_db/schemas/foo_schema/tables/Foo_Tbl/active_version"));
        assertFalse(repository.isExisted("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl"));
    }
    
    @Test
    void assertAlterByRefreshWithViews() {
        persistService.alterByRefresh("foo_db", new ShardingSphereSchema("foo_schema", databaseType, Collections.emptyList(), Collections.singleton(new ShardingSphereView("Foo_View", "select 1"))));
        assertFalse(repository.isExisted("/metadata/foo_db/schemas/foo_schema"));
    }
    
    @Test
    void assertAlterByRefreshWithRawSchemaName() {
        persistService.alterByRefresh("foo_db", new ShardingSphereSchema("Foo_Schema", databaseType));
        assertThat(repository.query("/metadata/foo_db/schemas/Foo_Schema"), is(""));
    }
    
    @Test
    void assertLoad() {
        persistService.add("foo_db", "Foo_Schema");
        tableMetaDataPersistService.persist("foo_db", "Foo_Schema", Collections.singleton(createTable("Foo_Tbl")));
        viewMetaDataPersistService.persist("foo_db", "Foo_Schema", Collections.singleton(new ShardingSphereView("Foo_View", "select 1")));
        Collection<ShardingSphereSchema> actual = persistService.load("foo_db", databaseType);
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next().getTable("Foo_Tbl").getName(), is("Foo_Tbl"));
        assertThat(actual.iterator().next().getView("Foo_View").getName(), is("Foo_View"));
    }
    
    private ShardingSphereTable createTable(final String tableName) {
        return new ShardingSphereTable(tableName,
                Collections.singleton(new ShardingSphereColumn("id", 0, false, false, false, true, false, true)),
                Collections.emptyList(), Collections.emptyList());
    }
}
