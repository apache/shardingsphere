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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.schema.pojo.YamlShardingSphereTable;
import org.apache.shardingsphere.infra.yaml.schema.swapper.YamlTableSwapper;
import org.apache.shardingsphere.mode.metadata.persist.version.VersionPersistService;
import org.apache.shardingsphere.mode.node.path.engine.generator.NodePathGenerator;
import org.apache.shardingsphere.mode.node.path.type.database.metadata.schema.TableMetaDataNodePath;
import org.apache.shardingsphere.mode.node.path.version.VersionNodePath;
import org.apache.shardingsphere.mode.persist.service.TableMetaDataPersistService;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Table meta data persist enabled service.
 */
@RequiredArgsConstructor
public final class TableMetaDataPersistEnabledService implements TableMetaDataPersistService {
    
    private final PersistRepository repository;
    
    private final VersionPersistService versionPersistService;
    
    private final YamlTableSwapper swapper = new YamlTableSwapper();
    
    @Override
    public Collection<ShardingSphereTable> load(final String databaseName, final String schemaName) {
        return repository.getChildrenKeys(NodePathGenerator.toPath(new TableMetaDataNodePath(databaseName, schemaName, null))).stream()
                .map(each -> load(databaseName, schemaName, each)).collect(Collectors.toList());
    }
    
    @Override
    public ShardingSphereTable load(final String databaseName, final String schemaName, final String tableName) {
        VersionNodePath versionNodePath = new VersionNodePath(new TableMetaDataNodePath(databaseName, schemaName, tableName));
        int activeVersion = Integer.parseInt(repository.query(versionNodePath.getActiveVersionPath()));
        String tableContent = repository.query(versionNodePath.getVersionPath(activeVersion));
        return swapper.swapToObject(YamlEngine.unmarshal(tableContent, YamlShardingSphereTable.class));
    }
    
    @Override
    public void persist(final String databaseName, final String schemaName, final Collection<ShardingSphereTable> tables) {
        for (ShardingSphereTable each : tables) {
            String tableName = each.getName().toLowerCase();
            VersionNodePath versionNodePath = new VersionNodePath(new TableMetaDataNodePath(databaseName, schemaName, tableName));
            versionPersistService.persist(versionNodePath, YamlEngine.marshal(swapper.swapToYamlConfiguration(each)));
        }
    }
    
    @Override
    public void drop(final String databaseName, final String schemaName, final String tableName) {
        repository.delete(NodePathGenerator.toPath(new TableMetaDataNodePath(databaseName, schemaName, tableName.toLowerCase())));
    }
    
    @Override
    public void drop(final String databaseName, final String schemaName, final Collection<ShardingSphereTable> tables) {
        tables.forEach(each -> drop(databaseName, schemaName, each.getName()));
    }
}
