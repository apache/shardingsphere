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

package org.apache.shardingsphere.infra.metadata.statistics.collector.tables;

import com.cedarsoftware.util.CaseInsensitiveMap;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereRowData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereTableData;
import org.apache.shardingsphere.infra.metadata.statistics.collector.ShardingSphereStatisticsCollector;
import org.apache.shardingsphere.infra.metadata.statistics.collector.ShardingSphereTableDataCollectorUtils;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Table pg_namespace data collector.
 */
public final class PgNamespaceTableCollector implements ShardingSphereStatisticsCollector {
    
    private static final String PG_NAMESPACE = "pg_namespace";
    
    private static final String PUBLIC_SCHEMA = "public";
    
    private static final Long PUBLIC_SCHEMA_OID = 0L;
    
    @Override
    public Optional<ShardingSphereTableData> collect(final String databaseName, final ShardingSphereTable table, final ShardingSphereMetaData metaData) throws SQLException {
        ShardingSphereTableData result = new ShardingSphereTableData(PG_NAMESPACE);
        long oid = 1L;
        for (ShardingSphereSchema each : metaData.getDatabase(databaseName).getAllSchemas()) {
            result.getRows().add(new ShardingSphereRowData(getRow(PUBLIC_SCHEMA.equalsIgnoreCase(each.getName()) ? PUBLIC_SCHEMA_OID : oid++, each.getName(), table)));
        }
        return Optional.of(result);
    }
    
    private List<Object> getRow(final Long oid, final String schemaName, final ShardingSphereTable table) {
        Map<String, Object> columnValues = new CaseInsensitiveMap<>(2, 1F);
        columnValues.put("oid", oid);
        columnValues.put("nspname", schemaName);
        return ShardingSphereTableDataCollectorUtils.createRowValue(columnValues, table);
    }
    
    @Override
    public String getType() {
        return PG_NAMESPACE;
    }
}
