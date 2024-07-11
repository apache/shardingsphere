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
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereRowData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereTableData;
import org.apache.shardingsphere.infra.metadata.statistics.collector.ShardingSphereStatisticsCollector;
import org.apache.shardingsphere.infra.metadata.statistics.collector.ShardingSphereTableDataCollectorUtils;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Table pg_class data collector.
 */
public final class PgClassTableCollector implements ShardingSphereStatisticsCollector {
    
    private static final String PG_CLASS = "pg_class";
    
    private static final String PUBLIC_SCHEMA = "public";
    
    private static final Long PUBLIC_SCHEMA_OID = 0L;
    
    @Override
    public Optional<ShardingSphereTableData> collect(final String databaseName, final ShardingSphereTable table, final Map<String, ShardingSphereDatabase> databases,
                                                     final RuleMetaData globalRuleMetaData) throws SQLException {
        ShardingSphereTableData result = new ShardingSphereTableData(PG_CLASS);
        long oid = 0L;
        for (Entry<String, ShardingSphereSchema> entry : databases.get(databaseName).getSchemas().entrySet()) {
            if (PUBLIC_SCHEMA.equalsIgnoreCase(entry.getKey())) {
                result.getRows().addAll(collectForSchema(oid++, PUBLIC_SCHEMA_OID, entry.getValue(), table));
            }
        }
        return Optional.of(result);
    }
    
    private Collection<ShardingSphereRowData> collectForSchema(final Long oid, final Long relNamespace, final ShardingSphereSchema schema, final ShardingSphereTable table) {
        Collection<ShardingSphereRowData> result = new LinkedList<>();
        for (Entry<String, ShardingSphereTable> entry : schema.getTables().entrySet()) {
            Map<String, Object> columnValues = new CaseInsensitiveMap<>(4, 1F);
            columnValues.put("oid", oid);
            columnValues.put("relnamespace", relNamespace);
            columnValues.put("relname", entry.getKey());
            columnValues.put("relkind", "r");
            result.add(new ShardingSphereRowData(ShardingSphereTableDataCollectorUtils.createRowValue(columnValues, table)));
        }
        return result;
    }
    
    @Override
    public String getType() {
        return PG_CLASS;
    }
}
