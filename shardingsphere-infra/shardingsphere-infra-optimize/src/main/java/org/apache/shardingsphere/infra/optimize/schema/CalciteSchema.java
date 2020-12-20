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

package org.apache.shardingsphere.infra.optimize.schema;

import lombok.Getter;
import org.apache.calcite.schema.Table;
import org.apache.calcite.schema.impl.AbstractSchema;
import org.apache.commons.collections4.map.LinkedMap;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNode;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;


/**
 * Calcite schema.
 *
 */
@Getter
public final class CalciteSchema extends AbstractSchema {
    
    private final Map<String, Table> tables = new LinkedMap<>();

    public CalciteSchema(final Map<String, DataSource> dataSources,
                         final Map<String, Collection<DataNode>> dataNodes, final DatabaseType databaseType) throws SQLException {
        for (Entry<String, Collection<DataNode>> entry : dataNodes.entrySet()) {
            tables.put(entry.getKey(), createTable(dataSources, entry.getValue(), databaseType));
        }
    }
    
    private Table createTable(final Map<String, DataSource> dataSources, final Collection<DataNode> dataNodes, final DatabaseType databaseType) throws SQLException {
        Map<String, DataSource> tableDataSources = new LinkedMap<>();
        for (DataNode each : dataNodes) {
            if (dataSources.containsKey(each.getDataSourceName())) {
                tableDataSources.put(each.getDataSourceName(), dataSources.get(each.getDataSourceName()));
            }
        }
        return new CalciteFilterableTable(tableDataSources, dataNodes, databaseType);
    }
    
    @Override
    protected Map<String, Table> getTableMap() {
        return tables;
    }
}
