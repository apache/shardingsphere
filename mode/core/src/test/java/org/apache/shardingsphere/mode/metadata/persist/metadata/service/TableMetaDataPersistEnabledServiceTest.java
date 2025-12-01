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
import org.apache.shardingsphere.mode.metadata.persist.version.VersionPersistService;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TableMetaDataPersistEnabledServiceTest {
    
    private TableMetaDataPersistEnabledService persistService;
    
    @Mock
    private PersistRepository repository;
    
    @BeforeEach
    void setUp() {
        VersionPersistService versionPersistService = new VersionPersistService(repository);
        persistService = new TableMetaDataPersistEnabledService(repository, versionPersistService);
    }
    
    @Test
    void assertLoad() {
        when(repository.getChildrenKeys("/metadata/foo_db/schemas/foo_schema/tables")).thenReturn(Collections.singletonList("foo_tbl"));
        when(repository.query("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl/active_version")).thenReturn("0");
        when(repository.query("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl/versions/0")).thenReturn("{name: foo_tbl}");
        Collection<ShardingSphereTable> actual = persistService.load("foo_db", "foo_schema");
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next().getName(), is("foo_tbl"));
    }
    
    @Test
    void assertPersistWithoutVersion() {
        ShardingSphereTable table = mock(ShardingSphereTable.class);
        when(table.getName()).thenReturn("foo_tbl");
        persistService.persist("foo_db", "foo_schema", Collections.singleton(table));
        verify(repository).persist("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl/versions/0", "name: foo_tbl" + System.lineSeparator());
        verify(repository).persist("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl/active_version", "0");
    }
    
    @Test
    void assertPersistWithVersion() {
        when(repository.getChildrenKeys("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl/versions")).thenReturn(Collections.singletonList("10"));
        ShardingSphereTable table = mock(ShardingSphereTable.class);
        when(table.getName()).thenReturn("foo_tbl");
        persistService.persist("foo_db", "foo_schema", Collections.singleton(table));
        verify(repository).persist("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl/versions/11", "name: foo_tbl" + System.lineSeparator());
        verify(repository).persist("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl/active_version", "11");
    }
    
    @Test
    void assertDropTable() {
        persistService.drop("foo_db", "foo_schema", "foo_tbl");
        verify(repository).delete("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl");
    }
    
    @Test
    void assertDropTables() {
        ShardingSphereTable table = mock(ShardingSphereTable.class);
        when(table.getName()).thenReturn("foo_tbl");
        persistService.drop("foo_db", "foo_schema", Collections.singleton(table));
        verify(repository).delete("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl");
    }
}
