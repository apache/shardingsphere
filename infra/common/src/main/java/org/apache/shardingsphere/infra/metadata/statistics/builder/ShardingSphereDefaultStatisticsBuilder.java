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

package org.apache.shardingsphere.infra.metadata.statistics.builder;

import org.apache.shardingsphere.infra.autogen.version.ShardingSphereVersion;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereDatabaseData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereRowData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereSchemaData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereTableData;

import java.util.Collections;

/**
 * ShardingSphere default statistics builder.
 */
public final class ShardingSphereDefaultStatisticsBuilder {
    
    private static final String SHARDINGSPHERE = "shardingsphere";
    
    private static final String CLUSTER_INFORMATION = "cluster_information";
    
    private static final String SHARDING_TABLE_STATISTICS = "sharding_table_statistics";
    
    /**
     * Build default statistics data.
     *
     * @param database database
     * @return built statistics data
     */
    public ShardingSphereDatabaseData build(final ShardingSphereDatabase database) {
        ShardingSphereDatabaseData result = new ShardingSphereDatabaseData();
        if (database.containsSchema(SHARDINGSPHERE)) {
            ShardingSphereSchemaData schemaData = new ShardingSphereSchemaData();
            buildClusterInformationTable(schemaData);
            buildShardingTableStatisticsTable(schemaData);
            result.putSchema(SHARDINGSPHERE, schemaData);
        }
        return result;
    }
    
    private void buildClusterInformationTable(final ShardingSphereSchemaData schemaData) {
        ShardingSphereTableData tableData = new ShardingSphereTableData(CLUSTER_INFORMATION);
        tableData.getRows().add(new ShardingSphereRowData(Collections.singletonList(ShardingSphereVersion.VERSION)));
        schemaData.putTable(CLUSTER_INFORMATION, tableData);
    }
    
    private void buildShardingTableStatisticsTable(final ShardingSphereSchemaData schemaData) {
        schemaData.putTable(SHARDING_TABLE_STATISTICS, new ShardingSphereTableData(SHARDING_TABLE_STATISTICS));
    }
}
