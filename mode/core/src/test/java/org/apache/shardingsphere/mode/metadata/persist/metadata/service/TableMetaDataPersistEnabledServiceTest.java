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

import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.mode.metadata.persist.version.VersionPersistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TableMetaDataPersistEnabledServiceTest {
    
    private TableMetaDataPersistEnabledService persistService;
    
    private FixturePersistRepository repository;
    
    @BeforeEach
    void setUp() {
        repository = new FixturePersistRepository();
        persistService = new TableMetaDataPersistEnabledService(repository, new VersionPersistService(repository));
    }
    
    @Test
    void assertLoad() {
        repository.persist("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl/active_version", "0");
        repository.persist("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl/versions/0", "{name: foo_tbl}");
        Collection<ShardingSphereTable> actual = persistService.load("foo_db", "foo_schema");
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next().getName(), is("foo_tbl"));
    }
    
    @Test
    void assertLoadWhenActiveVersionIsEmpty() {
        repository.persist("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl/active_version", "");
        assertFalse(persistService.load("foo_db", "foo_schema", "foo_tbl").isPresent());
    }
    
    @Test
    void assertLoadWhenTableContentIsEmpty() {
        repository.persist("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl/active_version", "0");
        repository.persist("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl/versions/0", "");
        assertFalse(persistService.load("foo_db", "foo_schema", "foo_tbl").isPresent());
    }
    
    @Test
    void assertLoadWithRawNamePath() {
        repository.persist("/metadata/foo_db/schemas/foo_schema/tables/Foo_Tbl/active_version", "0");
        repository.persist("/metadata/foo_db/schemas/foo_schema/tables/Foo_Tbl/versions/0", "{name: Foo_Tbl, columns: {Foo_Col: {name: Foo_Col, dataType: 0}}}");
        ShardingSphereTable actual = persistService.load("foo_db", "foo_schema", "Foo_Tbl").orElse(null);
        assertThat(actual.getName(), is("Foo_Tbl"));
        assertTrue(actual.containsColumn("foo_col"));
    }
    
    @Test
    void assertLoadWithRawNamePathForCollection() {
        repository.persist("/metadata/foo_db/schemas/foo_schema/tables/Foo_Tbl/active_version", "1");
        repository.persist("/metadata/foo_db/schemas/foo_schema/tables/Foo_Tbl/versions/1", "{name: Foo_Tbl, columns: {Foo_Col: {name: Foo_Col, dataType: 0}}}");
        Collection<ShardingSphereTable> actual = persistService.load("foo_db", "foo_schema");
        assertThat(actual.size(), is(1));
        assertTrue(actual.iterator().next().containsColumn("foo_col"));
    }
    
    @Test
    void assertPersistWithoutVersion() {
        persistService.persist("foo_db", "foo_schema", Collections.singleton(createTable("Foo_Tbl")));
        assertThat(repository.query("/metadata/foo_db/schemas/foo_schema/tables/Foo_Tbl/active_version"), is("0"));
        assertThat(repository.query("/metadata/foo_db/schemas/foo_schema/tables/Foo_Tbl/versions/0"), containsString("name: Foo_Tbl"));
    }
    
    @Test
    void assertPersistWithVersion() {
        repository.persist("/metadata/foo_db/schemas/foo_schema/tables/Foo_Tbl/versions/10", "old");
        persistService.persist("foo_db", "foo_schema", Collections.singleton(createTable("Foo_Tbl")));
        assertThat(repository.query("/metadata/foo_db/schemas/foo_schema/tables/Foo_Tbl/active_version"), is("11"));
        assertThat(repository.query("/metadata/foo_db/schemas/foo_schema/tables/Foo_Tbl/versions/11"), containsString("name: Foo_Tbl"));
    }
    
    @Test
    void assertDropTable() {
        repository.persist("/metadata/foo_db/schemas/foo_schema/tables/Foo_Tbl/active_version", "0");
        persistService.drop("foo_db", "foo_schema", "Foo_Tbl");
        assertFalse(repository.isExisted("/metadata/foo_db/schemas/foo_schema/tables/Foo_Tbl"));
    }
    
    @Test
    void assertDropTables() {
        repository.persist("/metadata/foo_db/schemas/foo_schema/tables/Foo_Tbl/active_version", "0");
        persistService.drop("foo_db", "foo_schema", Collections.singleton(createTable("Foo_Tbl")));
        assertFalse(repository.isExisted("/metadata/foo_db/schemas/foo_schema/tables/Foo_Tbl"));
    }
    
    private ShardingSphereTable createTable(final String tableName) {
        return new ShardingSphereTable(tableName,
                Arrays.asList(new ShardingSphereColumn("id", 0, false, false, false, true, false, true)),
                Collections.emptyList(), Collections.emptyList());
    }
}
