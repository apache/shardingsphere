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

package org.apache.shardingsphere.infra.config.persist.service;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.config.persist.repository.DistMetaDataPersistRepository;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.infra.yaml.schema.pojo.YamlSchema;
import org.apache.shardingsphere.infra.yaml.schema.swapper.SchemaYamlSwapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
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
public final class SchemaMetaDataPersistServiceTest {
    
    @Mock
    private DistMetaDataPersistRepository repository;
    
    @Test
    public void assertPersist() {
        ShardingSphereSchema schema = new SchemaYamlSwapper().swapToObject(YamlEngine.unmarshal(readYAML(), YamlSchema.class));
        new SchemaMetaDataPersistService(repository).persist("foo_db", schema);
        verify(repository).persist(eq("/metadata/foo_db/schema"), anyString());
    }
    
    @Test
    public void assertDelete() {
        new SchemaMetaDataPersistService(repository).delete("foo_db");
        verify(repository).delete("/metadata/foo_db");
    }
    
    @Test
    public void assertLoad() {
        SchemaMetaDataPersistService schemaMetaDataPersistService = new SchemaMetaDataPersistService(repository);
        when(repository.get("/metadata/foo_db/schema")).thenReturn(readYAML());
        Optional<ShardingSphereSchema> schemaOptional = schemaMetaDataPersistService.load("foo_db");
        assertTrue(schemaOptional.isPresent());
        Optional<ShardingSphereSchema> empty = schemaMetaDataPersistService.load("test");
        assertThat(empty, is(Optional.empty()));
        ShardingSphereSchema schema = schemaOptional.get();
        verify(repository).get(eq("/metadata/foo_db/schema"));
        assertThat(schema.getAllTableNames(), is(Collections.singleton("t_order")));
        assertThat(schema.get("t_order").getIndexes().keySet(), is(Collections.singleton("primary")));
        assertThat(schema.getAllColumnNames("t_order").size(), is(1));
        assertThat(schema.get("t_order").getColumns().keySet(), is(Collections.singleton("id")));
    }
    
    @Test
    public void assertLoadAllNames() {
        when(repository.getChildrenKeys("/metadata")).thenReturn(Arrays.asList("foo_db", "bar_db"));
        Collection<String> actual = new SchemaMetaDataPersistService(repository).loadAllNames();
        assertThat(actual.size(), is(2));
        assertThat(actual, hasItems("foo_db"));
        assertThat(actual, hasItems("bar_db"));
    }
    
    @SneakyThrows({IOException.class, URISyntaxException.class})
    private String readYAML() {
        return Files.readAllLines(Paths.get(ClassLoader.getSystemResource("yaml/schema/schema.yaml").toURI())).stream().map(each -> each + System.lineSeparator()).collect(Collectors.joining());
    }
}
