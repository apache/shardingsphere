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

package org.apache.shardingsphere.metadata.persist.service.schema;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.mode.spi.PersistRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TableMetaDataPersistServiceTest {
    
    @Mock
    private PersistRepository repository;
    
    @Test
    void assertPersist() {
        ShardingSphereTable table = new ShardingSphereTable("foo_table", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        new TableMetaDataPersistService(repository).persist("foo_db", "foo_schema", Collections.singletonMap("foo_table", table));
        verify(repository).persist("/metadata/foo_db/schemas/foo_schema/tables/foo_table", "name: foo_table" + System.lineSeparator());
    }
    
    @Test
    void assertLoad() {
        TableMetaDataPersistService tableMetaDataPersistService = new TableMetaDataPersistService(repository);
        when(repository.getChildrenKeys("/metadata/foo_db/schemas/foo_schema/tables")).thenReturn(Collections.singletonList("t_order"));
        when(repository.getDirectly("/metadata/foo_db/schemas/foo_schema/tables/t_order")).thenReturn(readYAML());
        Map<String, ShardingSphereTable> tables = tableMetaDataPersistService.load("foo_db", "foo_schema");
        assertThat(tables.size(), is(1));
        assertThat(tables.get("t_order").getIndexValues().size(), is(1));
        assertThat(tables.get("t_order").getIndexValues().iterator().next().getName(), is("PRIMARY"));
        assertThat(tables.get("t_order").getColumnValues().size(), is(1));
        assertThat(tables.get("t_order").getColumnValues().iterator().next().getName(), is("id"));
    }
    
    @Test
    void assertDelete() {
        new TableMetaDataPersistService(repository).delete("foo_db", "foo_schema", "foo_table");
        verify(repository).delete("/metadata/foo_db/schemas/foo_schema/tables/foo_table");
    }
    
    @SneakyThrows({IOException.class, URISyntaxException.class})
    private String readYAML() {
        return Files.readAllLines(Paths.get(ClassLoader.getSystemResource("yaml/schema/table.yaml").toURI())).stream().map(each -> each + System.lineSeparator()).collect(Collectors.joining());
    }
}
