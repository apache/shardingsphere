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
import org.apache.calcite.adapter.java.JavaTypeFactory;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeImpl;
import org.apache.calcite.schema.Table;
import org.apache.calcite.schema.impl.AbstractSchema;
import org.apache.calcite.schema.impl.ViewTable;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereView;
import org.apache.shardingsphere.sqlfederation.optimizer.executor.TableScanExecutor;
import org.apache.shardingsphere.sqlfederation.optimizer.metadata.statistic.FederationStatistic;
import org.apache.shardingsphere.sqlfederation.optimizer.util.SQLFederationDataTypeUtil;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Filterable schema.
 */
@Getter
public final class FilterableSchema extends AbstractSchema {
    
    private final String name;
    
    private final Map<String, Table> tableMap;
    
    public FilterableSchema(final String schemaName, final ShardingSphereSchema schema, final DatabaseType protocolType, final JavaTypeFactory javaTypeFactory, final TableScanExecutor executor) {
        name = schemaName;
        tableMap = createTableMap(schema, protocolType, javaTypeFactory, executor);
    }
    
    private Map<String, Table> createTableMap(final ShardingSphereSchema schema, final DatabaseType protocolType, final JavaTypeFactory javaTypeFactory, final TableScanExecutor executor) {
        Map<String, Table> result = new LinkedHashMap<>(schema.getTables().size(), 1);
        for (ShardingSphereTable each : schema.getTables().values()) {
            if (schema.containsView(each.getName())) {
                result.put(each.getName(), getViewTable(schema, protocolType, each, javaTypeFactory));
            } else {
                // TODO implement table statistic logic after using custom operators
                result.put(each.getName(), new FilterableTable(each, executor, new FederationStatistic(), protocolType));
            }
        }
        return result;
    }
    
    private static ViewTable getViewTable(final ShardingSphereSchema schema, final DatabaseType protocolType, final ShardingSphereTable table, final JavaTypeFactory javaTypeFactory) {
        RelDataType relDataType = SQLFederationDataTypeUtil.createRelDataType(table, protocolType, javaTypeFactory);
        ShardingSphereView view = schema.getView(table.getName());
        return new ViewTable(javaTypeFactory.getJavaClass(relDataType), RelDataTypeImpl.proto(relDataType), view.getViewDefinition(), Collections.emptyList(), Collections.emptyList());
    }
}
