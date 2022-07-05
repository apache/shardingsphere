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

package org.apache.shardingsphere.mode.metadata.persist.service;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.infra.yaml.schema.pojo.YamlTableMetaData;
import org.apache.shardingsphere.infra.yaml.schema.swapper.TableMetaDataYamlSwapper;
import org.apache.shardingsphere.mode.persist.PersistRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class DatabaseMetaDataPersistServiceTest {
    
    @Mock
    private PersistRepository repository;
    
    @Test
    public void assertPersist() {
        ShardingSphereTable table = new TableMetaDataYamlSwapper().swapToObject(YamlEngine.unmarshal(readYAML(), YamlTableMetaData.class));
        ShardingSphereSchema schema = new ShardingSphereSchema();
        schema.getTables().put("t_order", table);
        new DatabaseMetaDataPersistService(repository).persistMetaData("foo_db", "foo_schema", schema);
        verify(repository).persist(eq("/metadata/foo_db/schemas/foo_schema/tables/t_order"), anyString());
    }
    
    @Test
    public void assertDeleteDatabase() {
        new DatabaseMetaDataPersistService(repository).deleteDatabase("foo_db");
        verify(repository).delete("/metadata/foo_db");
    }
    
    @Test
    public void assertPersistDatabase() {
        new DatabaseMetaDataPersistService(repository).persistDatabase("foo_db");
        verify(repository).persist("/metadata/foo_db", "");
    }
    
    @Test
    public void assertLoad() {
        DatabaseMetaDataPersistService databaseMetaDataPersistService = new DatabaseMetaDataPersistService(repository);
        when(repository.getChildrenKeys("/metadata/foo_db/schemas/foo_schema/tables")).thenReturn(Collections.singletonList("t_order"));
        when(repository.get("/metadata/foo_db/schemas/foo_schema/tables/t_order")).thenReturn(readYAML());
        Optional<ShardingSphereSchema> schema = databaseMetaDataPersistService.load("foo_db", "foo_schema");
        assertTrue(schema.isPresent());
        Optional<ShardingSphereSchema> empty = databaseMetaDataPersistService.load("test", "test");
        assertThat(empty, is(Optional.empty()));
        assertThat(schema.get().getAllTableNames(), is(Collections.singleton("t_order")));
        assertThat(schema.get().get("t_order").getIndexes().keySet(), is(Collections.singleton("primary")));
        assertThat(schema.get().getAllColumnNames("t_order").size(), is(1));
        assertThat(schema.get().get("t_order").getColumns().keySet(), is(Collections.singleton("id")));
    }
    
    @Test
    public void assertLoadAllDatabaseNames() {
        when(repository.getChildrenKeys("/metadata")).thenReturn(Collections.singletonList("foo_db"));
        Collection<String> actual = new DatabaseMetaDataPersistService(repository).loadAllDatabaseNames();
        assertThat(actual.size(), is(1));
        assertThat(actual, hasItems("foo_db"));
    }
    
    @Test
    public void assertPersistTableMetaData() {
        ShardingSphereTable table = new ShardingSphereTable("FOO_TABLE", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        new DatabaseMetaDataPersistService(repository).persistTable("foo_db", "foo_schema", table);
        verify(repository).persist(eq("/metadata/foo_db/schemas/foo_schema/tables/foo_table"), anyString());
    }
    
    @SneakyThrows({IOException.class, URISyntaxException.class})
    private String readYAML() {
        return Files.readAllLines(Paths.get(ClassLoader.getSystemResource("yaml/schema/schema.yaml").toURI())).stream().map(each -> each + System.lineSeparator()).collect(Collectors.joining());
    }
}
