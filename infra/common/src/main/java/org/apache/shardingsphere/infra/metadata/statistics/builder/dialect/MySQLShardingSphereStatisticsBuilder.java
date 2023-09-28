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

package org.apache.shardingsphere.infra.metadata.statistics.builder.dialect;

import org.apache.shardingsphere.infra.autogen.version.ShardingSphereVersion;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereDatabaseData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereSchemaData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereTableData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereRowData;
import org.apache.shardingsphere.infra.metadata.statistics.builder.ShardingSphereStatisticsBuilder;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;

import java.util.*;
import java.util.Map.Entry;

/**
 * ShardingSphere statistics builder for MySQL.
 */

public final class MySQLShardingSphereStatisticsBuilder implements ShardingSphereStatisticsBuilder {

    private static final Set<String> CURRENT_SUPPORT = new HashSet<>(Arrays.asList("PARAMETERS"));

    private static final String SHARDING_SPHERE = "shardingsphere";
    private static final String INFORMATION_SCHEMA = "information_schema";
    private static final String CLUSTER_INFORMATION = "cluster_information";

    @Override
    public ShardingSphereStatistics build(final ShardingSphereMetaData metaData) {
        ShardingSphereStatistics result = new ShardingSphereStatistics();
        Optional<ShardingSphereSchema> shardingSphereSchema = Optional.ofNullable(metaData.getDatabase(SHARDING_SPHERE)).map(database -> database.getSchema(SHARDING_SPHERE));
        if (shardingSphereSchema.isPresent()) {
            ShardingSphereSchemaData schemaData = new ShardingSphereSchemaData();
            for (Entry<String, ShardingSphereTable> entry : shardingSphereSchema.get().getTables().entrySet()) {
                ShardingSphereTableData tableData = new ShardingSphereTableData(entry.getValue().getName());
                if (CLUSTER_INFORMATION.equals(entry.getKey())) {
                    tableData.getRows().add(new ShardingSphereRowData(Collections.singletonList(ShardingSphereVersion.VERSION)));
                }
                schemaData.getTableData().put(entry.getKey(), tableData);
            }
            ShardingSphereDatabaseData databaseData = new ShardingSphereDatabaseData();
            databaseData.getSchemaData().put(SHARDING_SPHERE, schemaData);
            result.getDatabaseData().put(SHARDING_SPHERE, databaseData);
        }

        Optional<ShardingSphereSchema> informationSchemaSchema = Optional.ofNullable(metaData.getDatabase("information_schema")).map(database -> database.getSchema("information_schema"));
        if (informationSchemaSchema.isPresent()) {
            ShardingSphereSchemaData schemaData = new ShardingSphereSchemaData();
            for (Entry<String, ShardingSphereTable> entry : informationSchemaSchema.get().getTables().entrySet()) {
                if (!CURRENT_SUPPORT.contains(entry.getValue().getName())) {
                    continue;
                }
                ShardingSphereTableData tableData = new ShardingSphereTableData(entry.getValue().getName());
                schemaData.getTableData().put(entry.getValue().getName(), tableData);
            }
            ShardingSphereDatabaseData informationSchemaResult = new ShardingSphereDatabaseData();
            informationSchemaResult.getSchemaData().put(INFORMATION_SCHEMA, schemaData);
            result.getDatabaseData().put(INFORMATION_SCHEMA, informationSchemaResult);
        }

        return result;
    }

    @Override
    public String getDatabaseType() {
        return "MySQL";
    }
}
