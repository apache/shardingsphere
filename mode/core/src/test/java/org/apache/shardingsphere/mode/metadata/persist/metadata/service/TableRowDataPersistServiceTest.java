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

import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.statistics.TableStatistics;
import org.apache.shardingsphere.infra.yaml.data.pojo.YamlRowStatistics;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TableRowDataPersistServiceTest {
    
    private TableRowDataPersistService persistService;
    
    @Mock
    private PersistRepository repository;
    
    @BeforeEach
    void setUp() {
        persistService = new TableRowDataPersistService(repository);
    }
    
    @Test
    void assertPersistWithoutRows() {
        persistService.persist("foo_db", "foo_schema", "foo_tbl", Collections.emptyList());
        verify(repository).persist("/statistics/databases/foo_db/schemas/foo_schema/tables/foo_tbl", "");
    }
    
    @Test
    void assertPersistWithRows() {
        YamlRowStatistics rowData = new YamlRowStatistics();
        rowData.setUniqueKey("foo_key");
        persistService.persist("foo_db", "foo_schema", "foo_tbl", Collections.singletonList(rowData));
        verify(repository).persist("/statistics/databases/foo_db/schemas/foo_schema/tables/foo_tbl/foo_key", "uniqueKey: foo_key" + System.lineSeparator());
    }
    
    @Test
    void assertDelete() {
        YamlRowStatistics rowData = new YamlRowStatistics();
        rowData.setUniqueKey("foo_key");
        persistService.delete("foo_db", "foo_schema", "foo_tbl", Collections.singletonList(rowData));
        verify(repository).delete("/statistics/databases/foo_db/schemas/foo_schema/tables/foo_tbl/foo_key");
    }
    
    @Test
    void assertLoadWithRowData() {
        when(repository.getChildrenKeys("/statistics/databases/foo_db/schemas/foo_schema/tables/foo_tbl")).thenReturn(Collections.singletonList("foo_tbl"));
        when(repository.query("/statistics/databases/foo_db/schemas/foo_schema/tables/foo_tbl/foo_tbl")).thenReturn("uniqueKey: foo_key" + System.lineSeparator());
        ShardingSphereTable table = mock(ShardingSphereTable.class);
        when(table.getName()).thenReturn("foo_tbl");
        TableStatistics actual = persistService.load("foo_db", "foo_schema", table);
        assertThat(actual.getName(), is("foo_tbl"));
        assertThat(actual.getRows().size(), is(1));
        assertThat(actual.getRows().iterator().next().getUniqueKey(), is("foo_key"));
    }
    
    @Test
    void assertLoadWithoutRowData() {
        when(repository.getChildrenKeys("/statistics/databases/foo_db/schemas/foo_schema/tables/foo_tbl")).thenReturn(Collections.singletonList("foo_tbl"));
        when(repository.query("/statistics/databases/foo_db/schemas/foo_schema/tables/foo_tbl/foo_tbl")).thenReturn("");
        ShardingSphereTable table = mock(ShardingSphereTable.class);
        when(table.getName()).thenReturn("foo_tbl");
        TableStatistics actual = persistService.load("foo_db", "foo_schema", table);
        assertThat(actual.getName(), is("foo_tbl"));
        assertTrue(actual.getRows().isEmpty());
    }
}
