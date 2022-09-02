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

package org.apache.shardingsphere.mode.metadata.persist.service.config.schema;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;
import org.apache.shardingsphere.mode.metadata.persist.service.schema.TableMetaDataPersistService;
import org.apache.shardingsphere.mode.persist.PersistRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class TableMetaDataPersistServiceTest {
    
    @Mock
    private PersistRepository repository;
    
    @Test
    public void assertCompareAndPersist() {
        TableMetaDataPersistService tableMetaDataPersistService = new TableMetaDataPersistService(repository);
        when(repository.getChildrenKeys("/metadata/foo_db/schemas/foo_schema/tables")).thenReturn(Collections.singletonList("t_order"));
        when(repository.get("/metadata/foo_db/schemas/foo_schema/tables/t_order")).thenReturn(readYAML());
        tableMetaDataPersistService.compareAndPersist("foo_db", "foo_schema", Collections.emptyMap());
        verify(repository).delete("/metadata/foo_db/schemas/foo_schema/tables/t_order");
    }
    
    @Test
    public void assertPersist() {
        ShardingSphereTable table = new ShardingSphereTable("foo_table", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        new TableMetaDataPersistService(repository).persist("foo_db", "foo_schema", Collections.singletonMap("foo_table", table));
        verify(repository).persist("/metadata/foo_db/schemas/foo_schema/tables/foo_table", "name: foo_table\n");
    }
    
    @Test
    public void assertLoad() {
        TableMetaDataPersistService tableMetaDataPersistService = new TableMetaDataPersistService(repository);
        when(repository.getChildrenKeys("/metadata/foo_db/schemas/foo_schema/tables")).thenReturn(Collections.singletonList("t_order"));
        when(repository.get("/metadata/foo_db/schemas/foo_schema/tables/t_order")).thenReturn(readYAML());
        Map<String, ShardingSphereTable> tables = tableMetaDataPersistService.load("foo_db", "foo_schema");
        assertThat(tables.size(), is(1));
        assertThat(tables.get("t_order").getIndexes().keySet(), is(Collections.singleton("primary")));
        assertThat(tables.get("t_order").getColumns().size(), is(1));
        assertThat(tables.get("t_order").getColumns().keySet(), is(Collections.singleton("id")));
    }
    
    @Test
    public void assertDelete() {
        new TableMetaDataPersistService(repository).delete("foo_db", "foo_schema", "foo_table");
        verify(repository).delete("/metadata/foo_db/schemas/foo_schema/tables/foo_table");
    }
    
    @SneakyThrows({IOException.class, URISyntaxException.class})
    private String readYAML() {
        return Files.readAllLines(Paths.get(ClassLoader.getSystemResource("yaml/schema/schema.yaml").toURI())).stream().map(each -> each + System.lineSeparator()).collect(Collectors.joining());
    }
}
