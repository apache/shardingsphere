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

import org.apache.shardingsphere.infra.version.ShardingSphereVersion;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.statistics.DatabaseStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.RowStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.SchemaStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.TableStatistics;

import java.util.Collections;

/**
 * ShardingSphere default statistics builder.
 */
public final class ShardingSphereDefaultStatisticsBuilder {
    
    private static final String SHARDINGSPHERE = "shardingsphere";
    
    private static final String CLUSTER_INFORMATION = "cluster_information";
    
    /**
     * Build default database statistics.
     *
     * @param database database
     * @return built database statistics
     */
    public DatabaseStatistics build(final ShardingSphereDatabase database) {
        DatabaseStatistics result = new DatabaseStatistics();
        if (database.containsSchema(SHARDINGSPHERE)) {
            SchemaStatistics schemaStatistics = new SchemaStatistics();
            buildClusterInformationTable(schemaStatistics);
            result.putSchemaStatistics(SHARDINGSPHERE, schemaStatistics);
        }
        return result;
    }
    
    private void buildClusterInformationTable(final SchemaStatistics schemaStatistics) {
        TableStatistics tableStatistics = new TableStatistics(CLUSTER_INFORMATION);
        tableStatistics.getRows().add(new RowStatistics(Collections.singletonList(ShardingSphereVersion.VERSION)));
        schemaStatistics.putTableStatistics(CLUSTER_INFORMATION, tableStatistics);
    }
}
