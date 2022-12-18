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

package org.apache.shardingsphere.mode.metadata.persist.service.schema;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereTableData;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.data.pojo.YamlShardingSphereRowData;
import org.apache.shardingsphere.infra.yaml.data.swapper.YamlShardingSphereRowDataSwapper;
import org.apache.shardingsphere.mode.metadata.persist.node.ShardingSphereDataNode;
import org.apache.shardingsphere.mode.persist.PersistRepository;

import java.util.ArrayList;
import java.util.Collection;

/**
 * ShardingSphere table data persist service.
 */
@RequiredArgsConstructor
public final class ShardingSphereTableDataPersistService {
    
    private final PersistRepository repository;
    
    public void persist(String databaseName, String schemaName, String tableName, Collection<YamlShardingSphereRowData> rows) {
        repository.persist(ShardingSphereDataNode.getTablePath(databaseName, schemaName, tableName.toLowerCase()), YamlEngine.marshal(rows));
    }
    
    public void delete(String databaseName, String schemaName, String tableName) {
        repository.delete(ShardingSphereDataNode.getTablePath(databaseName, schemaName, tableName.toLowerCase()));
    }
    
    public ShardingSphereTableData load(String databaseName, String schemaName, String tableName, ShardingSphereTable table) {
        ShardingSphereTableData result = new ShardingSphereTableData(tableName);
        YamlShardingSphereRowDataSwapper swapper = new YamlShardingSphereRowDataSwapper(new ArrayList<>(table.getColumns().values()));
        String value = repository.getDirectly(ShardingSphereDataNode.getTablePath(databaseName, schemaName, tableName));
        if (!Strings.isNullOrEmpty(value)) {
            result.getRows().add(swapper.swapToObject(YamlEngine.unmarshal(value, YamlShardingSphereRowData.class)));
        }
        return result;
    }
}
