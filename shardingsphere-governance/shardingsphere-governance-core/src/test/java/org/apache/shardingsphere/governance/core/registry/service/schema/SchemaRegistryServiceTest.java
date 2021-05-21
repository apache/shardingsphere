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

package org.apache.shardingsphere.governance.core.registry.service.schema;

import lombok.SneakyThrows;
import org.apache.shardingsphere.governance.core.yaml.schema.pojo.YamlSchema;
import org.apache.shardingsphere.governance.core.yaml.schema.swapper.SchemaYamlSwapper;
import org.apache.shardingsphere.governance.repository.spi.RegistryCenterRepository;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.lang.reflect.Field;
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
public final class SchemaRegistryServiceTest {
    
    private static final String META_DATA_YAML = "yaml/schema.yaml";
    
    @Mock
    private RegistryCenterRepository registryCenterRepository;
    
    private SchemaRegistryService schemaRegistryService;
    
    @Before
    public void setUp() throws ReflectiveOperationException {
        schemaRegistryService = new SchemaRegistryService(registryCenterRepository);
        Field field = schemaRegistryService.getClass().getDeclaredField("repository");
        field.setAccessible(true);
        field.set(schemaRegistryService, registryCenterRepository);
    }
    
    @Test
    public void assertLoadAllSchemaNames() {
        when(registryCenterRepository.get("/metadata")).thenReturn("sharding_db,replica_query_db");
        Collection<String> actual = schemaRegistryService.loadAllSchemaNames();
        assertThat(actual.size(), is(2));
        assertThat(actual, hasItems("sharding_db"));
        assertThat(actual, hasItems("replica_query_db"));
    }
    
    @SneakyThrows({IOException.class, URISyntaxException.class})
    private String readYAML(final String yamlFile) {
        return Files.readAllLines(Paths.get(ClassLoader.getSystemResource(yamlFile).toURI()))
                .stream().filter(each -> !each.startsWith("#")).map(each -> each + System.lineSeparator()).collect(Collectors.joining());
    }
    
    @Test
    public void assertPersistSchema() {
        ShardingSphereSchema schema = new SchemaYamlSwapper().swapToObject(YamlEngine.unmarshal(readYAML(META_DATA_YAML), YamlSchema.class));
        schemaRegistryService.persistSchema("sharding_db", schema);
        verify(registryCenterRepository).persist(eq("/metadata/sharding_db/schema"), anyString());
    }
    
    @Test
    public void assertLoadSchema() {
        when(registryCenterRepository.get("/metadata/sharding_db/schema")).thenReturn(readYAML(META_DATA_YAML));
        Optional<ShardingSphereSchema> schemaOptional = schemaRegistryService.loadSchema("sharding_db");
        assertTrue(schemaOptional.isPresent());
        Optional<ShardingSphereSchema> empty = schemaRegistryService.loadSchema("test");
        assertThat(empty, is(Optional.empty()));
        ShardingSphereSchema schema = schemaOptional.get();
        verify(registryCenterRepository).get(eq("/metadata/sharding_db/schema"));
        assertThat(schema.getAllTableNames(), is(Collections.singleton("t_order")));
        assertThat(schema.get("t_order").getIndexes().keySet(), is(Collections.singleton("primary")));
        assertThat(schema.getAllColumnNames("t_order").size(), is(1));
        assertThat(schema.get("t_order").getColumns().keySet(), is(Collections.singleton("id")));
    }
    
    @Test
    public void assertDeleteSchema() {
        schemaRegistryService.deleteSchema("sharding_db");
        verify(registryCenterRepository).delete(eq("/metadata/sharding_db"));
    }
}
