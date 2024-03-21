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

package org.apache.shardingsphere.metadata.persist.service.database;

import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.metadata.persist.service.version.MetaDataVersionPersistService;
import org.apache.shardingsphere.mode.spi.PersistRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class DatabaseMetaDataPersistServiceTest {

    @Mock
    private MetaDataVersionPersistService metaDataVersionPersistService;

    private DatabaseMetaDataPersistService databaseMetaDataPersistService;

    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        databaseMetaDataPersistService = new DatabaseMetaDataPersistService(mock(PersistRepository.class),
                metaDataVersionPersistService);
    }

    @Test
    void testAddDatabase() {
        databaseMetaDataPersistService.addDatabase("123");
    }

    @Test
    void testDropDatabase() {
        databaseMetaDataPersistService.dropDatabase("123");
    }

    @Test
    void testLoadAllDatabaseNames() {
        Collection<String> collection = databaseMetaDataPersistService.loadAllDatabaseNames();
        Assertions.assertTrue(collection.isEmpty());
    }

    @Test
    void testAddSchema() {
        databaseMetaDataPersistService.addSchema("123", "234");
    }

    @Test
    void testDropSchema() {
        databaseMetaDataPersistService.dropSchema("123", "234");
    }

    @Test
    void testCompareAndPersist() {
        ShardingSphereSchema schema = new ShardingSphereSchema();
        schema.getTables().put("t_warehouse", new ShardingSphereTable("t_warehouse", Arrays.asList(
                new ShardingSphereColumn("id", Types.INTEGER, true, false, false, true, false, false),
                new ShardingSphereColumn("warehouse_name", Types.VARCHAR, false, false, false, true, false, false)),
                Collections.emptyList(), Collections.emptyList()));
        databaseMetaDataPersistService.compareAndPersist("123", "234", schema);
    }

    @Test
    void testPersist() {
        ShardingSphereSchema schema = new ShardingSphereSchema();
        schema.getTables().put("t_warehouse", new ShardingSphereTable("t_warehouse", Arrays.asList(
                new ShardingSphereColumn("id", Types.INTEGER, true, false, false, true, false, false),
                new ShardingSphereColumn("warehouse_name", Types.VARCHAR, false, false, false, true, false, false)),
                Collections.emptyList(), Collections.emptyList()));
        databaseMetaDataPersistService.persist("123", "234", schema);
    }

    @Test
    void testDelete() {
        ShardingSphereSchema schema = new ShardingSphereSchema();
        schema.getTables().put("t_warehouse", new ShardingSphereTable("t_warehouse", Arrays.asList(
                new ShardingSphereColumn("id", Types.INTEGER, true, false, false, true, false, false),
                new ShardingSphereColumn("warehouse_name", Types.VARCHAR, false, false, false, true, false, false)),
                Collections.emptyList(), Collections.emptyList()));
        databaseMetaDataPersistService.delete("123", "234", schema);
    }

    @Test
    void testLoadSchemas() {
        Map<String, ShardingSphereSchema> loadedSchemas = databaseMetaDataPersistService.loadSchemas("123");
        Assertions.assertTrue(loadedSchemas.isEmpty());
    }

}
