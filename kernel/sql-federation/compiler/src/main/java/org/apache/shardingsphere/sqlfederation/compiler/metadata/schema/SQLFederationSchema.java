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

package org.apache.shardingsphere.sqlfederation.compiler.metadata.schema;

import com.cedarsoftware.util.CaseInsensitiveMap;
import lombok.Getter;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeImpl;
import org.apache.calcite.schema.Table;
import org.apache.calcite.schema.impl.AbstractSchema;
import org.apache.calcite.schema.impl.ViewTable;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereView;
import org.apache.shardingsphere.sqlfederation.compiler.sql.type.SQLFederationDataTypeBuilder;
import org.apache.shardingsphere.sqlfederation.compiler.sql.type.SQLFederationDataTypeFactory;

import java.util.Collections;
import java.util.Map;

/**
 * SQL federation schema.
 */
@Getter
public final class SQLFederationSchema extends AbstractSchema {
    
    private final String name;
    
    private final Map<String, Table> tableMap;
    
    public SQLFederationSchema(final String schemaName, final ShardingSphereSchema schema, final DatabaseType protocolType) {
        name = schemaName;
        tableMap = createTableMap(schema, protocolType);
    }
    
    private Map<String, Table> createTableMap(final ShardingSphereSchema schema, final DatabaseType protocolType) {
        Map<String, Table> result = new CaseInsensitiveMap<>(schema.getAllTables().size(), 1F);
        for (ShardingSphereTable each : schema.getAllTables()) {
            if (schema.containsView(each.getName())) {
                result.put(each.getName(), getViewTable(schema, each, protocolType));
            } else {
                result.put(each.getName(), new SQLFederationTable(each, protocolType));
            }
        }
        return result;
    }
    
    private ViewTable getViewTable(final ShardingSphereSchema schema, final ShardingSphereTable table, final DatabaseType protocolType) {
        RelDataType relDataType = SQLFederationDataTypeBuilder.build(table, protocolType, SQLFederationDataTypeFactory.getInstance());
        ShardingSphereView view = schema.getView(table.getName());
        return new ViewTable(new JavaTypeFactoryImpl().getJavaClass(relDataType), RelDataTypeImpl.proto(relDataType), view.getViewDefinition(), Collections.emptyList(), Collections.emptyList());
    }
}
