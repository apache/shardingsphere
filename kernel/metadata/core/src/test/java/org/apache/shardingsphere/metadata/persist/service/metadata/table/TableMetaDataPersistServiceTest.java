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

package org.apache.shardingsphere.metadata.persist.service.metadata.table;

import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.metadata.persist.service.version.MetaDataVersionPersistService;
import org.apache.shardingsphere.mode.spi.PersistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TableMetaDataPersistServiceTest {
    
    private TableMetaDataPersistService persistService;
    
    @Mock
    private PersistRepository repository;
    
    @BeforeEach
    void setUp() {
        MetaDataVersionPersistService metaDataVersionPersistService = new MetaDataVersionPersistService(repository);
        persistService = new TableMetaDataPersistService(repository, metaDataVersionPersistService);
    }
    
    @Test
    void assertLoad() {
        when(repository.getChildrenKeys("/metadata/foo_db/schemas/foo_schema/tables")).thenReturn(Collections.singletonList("foo_tbl"));
        when(repository.query("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl/active_version")).thenReturn("0");
        when(repository.query("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl/versions/0")).thenReturn("{name: foo_tbl}");
        Map<String, ShardingSphereTable> actual = persistService.load("foo_db", "foo_schema");
        assertThat(actual.size(), is(1));
        assertThat(actual.get("foo_tbl").getName(), is("foo_tbl"));
    }
    
    @Test
    void assertPersistWithoutVersion() {
        when(repository.query("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl/active_version")).thenReturn("", "0");
        persistService.persist("foo_db", "foo_schema", Collections.singletonMap("foo_tbl", mock(ShardingSphereTable.class)));
        verify(repository).persist("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl/versions/0", "{}" + System.lineSeparator());
        verify(repository).persist("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl/active_version", "0");
    }
    
    @Test
    void assertPersistWithVersion() {
        when(repository.getChildrenKeys("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl/versions")).thenReturn(Collections.singletonList("10"));
        when(repository.query("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl/active_version")).thenReturn("10");
        persistService.persist("foo_db", "foo_schema", Collections.singletonMap("foo_tbl", mock(ShardingSphereTable.class)));
        verify(repository).persist("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl/versions/11", "{}" + System.lineSeparator());
        verify(repository).persist("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl/active_version", "11");
    }
    
    @Test
    void assertDropTable() {
        persistService.drop("foo_db", "foo_schema", "foo_tbl");
        verify(repository).delete("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl");
    }
    
    @Test
    void assertDropTables() {
        persistService.drop("foo_db", "foo_schema", Collections.singletonMap("foo_tbl", mock(ShardingSphereTable.class)));
        verify(repository).delete("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl");
    }
}
