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

package org.apache.shardingsphere.metadata.persist.service;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereView;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.schema.pojo.YamlShardingSphereTable;
import org.apache.shardingsphere.infra.yaml.schema.swapper.YamlTableSwapper;
import org.apache.shardingsphere.metadata.persist.service.database.DatabaseMetaDataPersistService;
import org.apache.shardingsphere.mode.spi.PersistRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatabaseMetaDataPersistServiceTest {
    
    @Mock
    private PersistRepository repository;
    
    @Test
    void assertPersistEmptySchemas() {
        new DatabaseMetaDataPersistService(repository).persist("foo_db", "foo_schema", new ShardingSphereSchema());
        verify(repository).persist(eq("/metadata/foo_db/schemas/foo_schema/tables"), anyString());
    }
    
    @Test
    void assertCompareAndPersistEmptySchemas() {
        new DatabaseMetaDataPersistService(repository).compareAndPersist("foo_db", "foo_schema", new ShardingSphereSchema());
        verify(repository).persist(eq("/metadata/foo_db/schemas/foo_schema/tables"), anyString());
    }
    
    @Test
    void assertPersist() {
        ShardingSphereTable table = new YamlTableSwapper().swapToObject(YamlEngine.unmarshal(readYAML(), YamlShardingSphereTable.class));
        ShardingSphereSchema schema = new ShardingSphereSchema();
        schema.getTables().put("t_order", table);
        new DatabaseMetaDataPersistService(repository).persist("foo_db", "foo_schema", schema);
        verify(repository).persist(eq("/metadata/foo_db/schemas/foo_schema/tables/t_order"), anyString());
    }
    
    @Test
    void assertAddDatabase() {
        new DatabaseMetaDataPersistService(repository).addDatabase("foo_db");
        verify(repository).persist("/metadata/foo_db", "");
    }
    
    @Test
    void assertDropDatabase() {
        new DatabaseMetaDataPersistService(repository).dropDatabase("foo_db");
        verify(repository).delete("/metadata/foo_db");
    }
    
    @Test
    void assertLoadAllDatabaseNames() {
        when(repository.getChildrenKeys("/metadata")).thenReturn(Collections.singletonList("foo_db"));
        Collection<String> actual = new DatabaseMetaDataPersistService(repository).loadAllDatabaseNames();
        assertThat(actual.size(), is(1));
        assertThat(actual, hasItems("foo_db"));
    }
    
    @Test
    void assertAddSchema() {
        new DatabaseMetaDataPersistService(repository).addSchema("foo_db", "foo_schema");
        verify(repository).persist("/metadata/foo_db/schemas/foo_schema/tables", "");
    }
    
    @Test
    void assertDropSchema() {
        new DatabaseMetaDataPersistService(repository).dropSchema("foo_db", "foo_schema");
        verify(repository).delete("/metadata/foo_db/schemas/foo_schema");
    }
    
    @Test
    void assertPersistSchemaMetaData() {
        ShardingSphereTable table = new ShardingSphereTable("FOO_TABLE", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        ShardingSphereView view = new ShardingSphereView("FOO_VIEW", "select id from foo_table");
        new DatabaseMetaDataPersistService(repository).persist("foo_db", "foo_schema",
                new ShardingSphereSchema(Collections.singletonMap("FOO_TABLE", table), Collections.singletonMap("FOO_VIEW", view)));
        verify(repository).persist(eq("/metadata/foo_db/schemas/foo_schema/tables/foo_table"), anyString());
    }
    
    @Test
    void assertLoadSchemas() {
        DatabaseMetaDataPersistService databaseMetaDataPersistService = new DatabaseMetaDataPersistService(repository);
        when(repository.getChildrenKeys("/metadata/foo_db/schemas")).thenReturn(Collections.singletonList("foo_schema"));
        when(repository.getChildrenKeys("/metadata/foo_db/schemas/foo_schema/tables")).thenReturn(Collections.singletonList("t_order"));
        when(repository.getDirectly("/metadata/foo_db/schemas/foo_schema/tables/t_order")).thenReturn(readYAML());
        Map<String, ShardingSphereSchema> schema = databaseMetaDataPersistService.loadSchemas("foo_db");
        assertThat(schema.size(), is(1));
        assertTrue(databaseMetaDataPersistService.loadSchemas("test").isEmpty());
        assertThat(schema.get("foo_schema").getAllTableNames(), is(Collections.singleton("t_order")));
        assertThat(schema.get("foo_schema").getTable("t_order").getIndexValues().size(), is(1));
        assertThat(schema.get("foo_schema").getTable("t_order").getIndexValues().iterator().next().getName(), is("PRIMARY"));
        assertThat(schema.get("foo_schema").getAllColumnNames("t_order").size(), is(1));
        assertThat(schema.get("foo_schema").getTable("t_order").getColumnValues().size(), is(1));
        assertThat(schema.get("foo_schema").getTable("t_order").getColumnValues().iterator().next().getName(), is("id"));
    }
    
    @SneakyThrows({IOException.class, URISyntaxException.class})
    private String readYAML() {
        return Files.readAllLines(Paths.get(ClassLoader.getSystemResource("yaml/schema/table.yaml").toURI())).stream().map(each -> each + System.lineSeparator()).collect(Collectors.joining());
    }
}
