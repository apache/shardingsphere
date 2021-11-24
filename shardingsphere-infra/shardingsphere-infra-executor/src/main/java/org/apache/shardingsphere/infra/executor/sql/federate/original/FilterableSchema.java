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

package org.apache.shardingsphere.infra.executor.sql.federate.original;

import lombok.Getter;
import org.apache.calcite.schema.Table;
import org.apache.calcite.schema.impl.AbstractSchema;
import org.apache.commons.collections4.map.LinkedMap;
import org.apache.shardingsphere.infra.executor.sql.federate.original.table.FilterableTable;
import org.apache.shardingsphere.infra.executor.sql.federate.original.table.FilterableTableScanExecutor;
import org.apache.shardingsphere.infra.federation.metadata.FederationSchemaMetaData;
import org.apache.shardingsphere.infra.federation.metadata.FederationTableMetaData;

import java.util.Map;

/**
 * Filterable schema.
 */
@Getter
public final class FilterableSchema extends AbstractSchema {
    
    private final String name;
    
    private final Map<String, Table> tableMap;
    
    public FilterableSchema(final FederationSchemaMetaData schemaMetaData, final FilterableTableScanExecutor executor) {
        name = schemaMetaData.getName();
        tableMap = createTableMap(schemaMetaData, executor);
    }
    
    private Map<String, Table> createTableMap(final FederationSchemaMetaData schemaMetaData, final FilterableTableScanExecutor executor) {
        Map<String, Table> result = new LinkedMap<>(schemaMetaData.getTables().size(), 1);
        for (FederationTableMetaData each : schemaMetaData.getTables().values()) {
            result.put(each.getName(), new FilterableTable(each, executor, new FederationTableStatistic()));
        }
        return result;
    }
}
