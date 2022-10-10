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

package org.apache.shardingsphere.sqlfederation.optimizer.metadata.memory;

import org.apache.calcite.schema.Table;
import org.apache.calcite.schema.impl.AbstractSchema;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereTableData;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Memory schema.
 */
public final class MemorySchema extends AbstractSchema {
    
    private Map<String, Table> tableMap;
    
    public MemorySchema(final Map<String, ShardingSphereTableData> tableData, final ShardingSphereSchema schema) {
        createTableMap(tableData, schema);
    }
    
    private void createTableMap(final Map<String, ShardingSphereTableData> tableData, final ShardingSphereSchema schema) {
        tableMap = new LinkedHashMap<>();
        schema.getTables().forEach((key, value) -> tableMap.put(key, new MemoryTable(value, tableData.get(key).getRows())));
    }
    
    @Override
    public Map<String, Table> getTableMap() {
        return tableMap;
    }
}
