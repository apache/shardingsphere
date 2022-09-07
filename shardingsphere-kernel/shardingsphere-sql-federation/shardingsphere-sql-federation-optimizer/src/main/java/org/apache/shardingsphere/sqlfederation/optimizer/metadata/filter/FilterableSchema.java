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

package org.apache.shardingsphere.sqlfederation.optimizer.metadata.filter;

import lombok.Getter;
import org.apache.calcite.schema.Table;
import org.apache.calcite.schema.impl.AbstractSchema;
import org.apache.shardingsphere.sqlfederation.optimizer.executor.TableScanExecutor;
import org.apache.shardingsphere.sqlfederation.optimizer.metadata.statistic.FederationStatistic;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Filterable schema.
 */
@Getter
public final class FilterableSchema extends AbstractSchema {
    
    private final String name;
    
    private final Map<String, Table> tableMap;
    
    public FilterableSchema(final String schemaName, final ShardingSphereSchema schema, final TableScanExecutor executor) {
        name = schemaName;
        tableMap = createTableMap(schema, executor);
    }
    
    private Map<String, Table> createTableMap(final ShardingSphereSchema schema, final TableScanExecutor executor) {
        Map<String, Table> result = new LinkedHashMap<>(schema.getTables().size(), 1);
        for (ShardingSphereTable each : schema.getTables().values()) {
            // TODO implement table statistic logic after using custom operators
            result.put(each.getName(), new FilterableTable(each, executor, new FederationStatistic()));
        }
        return result;
    }
}
