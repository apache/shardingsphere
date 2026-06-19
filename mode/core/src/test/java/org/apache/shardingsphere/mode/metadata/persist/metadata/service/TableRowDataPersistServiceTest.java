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
import org.apache.shardingsphere.infra.metadata.statistics.TableStatistics;
import org.apache.shardingsphere.infra.yaml.data.pojo.YamlRowStatistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TableRowDataPersistServiceTest {
    
    private TableRowDataPersistService persistService;
    
    private FixturePersistRepository repository;
    
    @BeforeEach
    void setUp() {
        repository = new FixturePersistRepository();
        persistService = new TableRowDataPersistService(repository);
    }
    
    @Test
    void assertPersistWithoutRows() {
        persistService.persist("foo_db", "foo_schema", "Foo_Tbl", Collections.emptyList());
        assertThat(repository.query("/statistics/databases/foo_db/schemas/foo_schema/tables/Foo_Tbl"), is(""));
    }
    
    @Test
    void assertPersistWithRows() {
        YamlRowStatistics rowData = new YamlRowStatistics();
        rowData.setUniqueKey("foo_key");
        persistService.persist("foo_db", "foo_schema", "Foo_Tbl", Collections.singletonList(rowData));
        assertThat(repository.query("/statistics/databases/foo_db/schemas/foo_schema/tables/Foo_Tbl/foo_key"), is("uniqueKey: foo_key" + System.lineSeparator()));
    }
    
    @Test
    void assertDelete() {
        repository.persist("/statistics/databases/foo_db/schemas/foo_schema/tables/Foo_Tbl/foo_key", "value");
        YamlRowStatistics rowData = new YamlRowStatistics();
        rowData.setUniqueKey("foo_key");
        persistService.delete("foo_db", "foo_schema", "Foo_Tbl", Collections.singletonList(rowData));
        assertFalse(repository.isExisted("/statistics/databases/foo_db/schemas/foo_schema/tables/Foo_Tbl/foo_key"));
    }
    
    @Test
    void assertLoadWithRowData() {
        repository.persist("/statistics/databases/foo_db/schemas/foo_schema/tables/Foo_Tbl/foo_key", "uniqueKey: foo_key" + System.lineSeparator());
        TableStatistics actual = persistService.load("foo_db", "foo_schema", createTable("Foo_Tbl"));
        assertThat(actual.getName(), is("Foo_Tbl"));
        assertThat(actual.getRows().size(), is(1));
        assertThat(actual.getRows().iterator().next().getUniqueKey(), is("foo_key"));
    }
    
    @Test
    void assertLoadWithoutRowData() {
        repository.persist("/statistics/databases/foo_db/schemas/foo_schema/tables/Foo_Tbl/foo_key", "");
        TableStatistics actual = persistService.load("foo_db", "foo_schema", createTable("Foo_Tbl"));
        assertThat(actual.getName(), is("Foo_Tbl"));
        assertTrue(actual.getRows().isEmpty());
    }
    
    @Test
    void assertLoadReturnsEmptyWithDifferentPathCase() {
        repository.persist("/statistics/databases/foo_db/schemas/foo_schema/tables/foo_tbl/foo_key", "uniqueKey: foo_key" + System.lineSeparator());
        TableStatistics actual = persistService.load("foo_db", "foo_schema", createTable("Foo_Tbl"));
        assertThat(actual.getName(), is("Foo_Tbl"));
        assertTrue(actual.getRows().isEmpty());
    }
    
    private ShardingSphereTable createTable(final String tableName) {
        return new ShardingSphereTable(tableName,
                Collections.singleton(new ShardingSphereColumn("id", 0, false, false, false, true, false, true)),
                Collections.emptyList(), Collections.emptyList());
    }
}
