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

package org.apache.shardingsphere.infra.metadata.data.builder.dialect;

import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereData;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereDatabaseData;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereSchemaData;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereTableData;
import org.apache.shardingsphere.infra.metadata.data.builder.ShardingSphereDataBuilder;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

/**
 * MySQL ShardingSphere data Builder.
 */

public final class MySQLShardingSphereDataBuilder implements ShardingSphereDataBuilder {
    
    private static final String SHARDING_SPHERE = "shardingsphere";
    
    @Override
    public ShardingSphereData build(final ShardingSphereMetaData metaData) {
        ShardingSphereData result = new ShardingSphereData();
        Optional<ShardingSphereSchema> shardingSphereSchema = Optional.ofNullable(metaData.getDatabase(SHARDING_SPHERE)).map(database -> database.getSchema(SHARDING_SPHERE));
        if (!shardingSphereSchema.isPresent()) {
            return result;
        }
        ShardingSphereSchemaData schemaData = new ShardingSphereSchemaData();
        for (Map.Entry<String, ShardingSphereTable> entry : shardingSphereSchema.get().getTables().entrySet()) {
            schemaData.getTableData().put(entry.getKey(), new ShardingSphereTableData(entry.getValue().getName(), new ArrayList<>(entry.getValue().getColumns().values())));
        }
        ShardingSphereDatabaseData databaseData = new ShardingSphereDatabaseData();
        databaseData.getSchemaData().put(SHARDING_SPHERE, schemaData);
        result.getDatabaseData().put(SHARDING_SPHERE, databaseData);
        return result;
    }
    
    @Override
    public String getType() {
        return "MySQL";
    }
}
