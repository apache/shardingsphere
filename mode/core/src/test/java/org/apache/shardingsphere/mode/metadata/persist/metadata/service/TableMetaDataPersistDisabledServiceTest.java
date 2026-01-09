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

import com.google.common.base.Joiner;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.schema.swapper.YamlTableSwapper;
import org.apache.shardingsphere.mode.node.path.engine.generator.NodePathGenerator;
import org.apache.shardingsphere.mode.node.path.type.global.state.coordinator.CoordinatorType;
import org.apache.shardingsphere.mode.node.path.type.global.state.coordinator.table.TableCoordinatorTypeNodePath;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TableMetaDataPersistDisabledServiceTest {
    
    private TableMetaDataPersistDisabledService persistService;
    
    @Mock
    private PersistRepository repository;
    
    @BeforeEach
    void setUp() {
        persistService = new TableMetaDataPersistDisabledService(repository);
    }
    
    @Test
    void assertLoadTables() {
        assertTrue(persistService.load("foo_db", "foo_schema").isEmpty());
    }
    
    @Test
    void assertLoadTable() {
        assertNull(persistService.load("foo_db", "foo_schema", "foo_tbl"));
    }
    
    @Test
    void assertPersist() {
        ShardingSphereTable table = new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        String qualifiedTableName = Joiner.on(".").join("foo_db", "foo_schema", "foo_tbl");
        TableCoordinatorTypeNodePath tableCoordinatorTypeNodePath = new TableCoordinatorTypeNodePath(qualifiedTableName, CoordinatorType.CREATE.name());
        String expectedPersistPath = NodePathGenerator.toPath(tableCoordinatorTypeNodePath);
        String expectedDeletePath = NodePathGenerator.toPath(tableCoordinatorTypeNodePath.getTableCoordinatorPath());
        String expectedContent = YamlEngine.marshal(new YamlTableSwapper().swapToYamlConfiguration(table));
        persistService.persist("foo_db", "foo_schema", Collections.singleton(table));
        verify(repository).persist(expectedPersistPath, expectedContent);
        verify(repository).delete(expectedDeletePath);
    }
    
    @Test
    void assertDrop() {
        ShardingSphereTable table = mock(ShardingSphereTable.class);
        when(table.getName()).thenReturn("foo_tbl");
        String qualifiedTableName = Joiner.on(".").join("foo_db", "foo_schema", "foo_tbl");
        TableCoordinatorTypeNodePath tableCoordinatorTypeNodePath = new TableCoordinatorTypeNodePath(qualifiedTableName, CoordinatorType.DROP.name());
        String expectedPersistPath = NodePathGenerator.toPath(tableCoordinatorTypeNodePath);
        String expectedDeletePath = NodePathGenerator.toPath(tableCoordinatorTypeNodePath.getTableCoordinatorPath());
        persistService.drop("foo_db", "foo_schema", Collections.singleton(table));
        verify(repository).persist(expectedPersistPath, "foo_tbl");
        verify(repository).delete(expectedDeletePath);
    }
}
