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

package org.apache.shardingsphere.infra.federation.optimizer.metadata;

import lombok.Getter;
import org.apache.calcite.avatica.SqlType;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rel.type.RelDataTypeFactory.Builder;
import org.apache.calcite.rel.type.RelDataTypeImpl;
import org.apache.calcite.rel.type.RelDataTypeSystem;
import org.apache.calcite.rel.type.RelProtoDataType;
import org.apache.calcite.sql.type.SqlTypeFactoryImpl;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Federation table meta data.
 */
@Getter
public final class FederationTableMetaData {
    
    private static final RelDataTypeFactory REL_DATA_TYPE_FACTORY = new SqlTypeFactoryImpl(RelDataTypeSystem.DEFAULT);
    
    private final String name;
    
    private final RelProtoDataType relProtoDataType;
    
    private final List<String> columnNames;
    
    public FederationTableMetaData(final String name, final ShardingSphereTable table) {
        this.name = name;
        relProtoDataType = createRelProtoDataType(table);
        // TODO consider using keySet when ShardingSphere supports column name case sensitivity
        columnNames = table.getColumns().values().stream().map(ShardingSphereColumn::getName).collect(Collectors.toList());
    }
    
    private RelProtoDataType createRelProtoDataType(final ShardingSphereTable table) {
        Builder fieldInfoBuilder = REL_DATA_TYPE_FACTORY.builder();
        for (ShardingSphereColumn each : table.getColumns().values()) {
            fieldInfoBuilder.add(each.getName(), getRelDataType(each));
        }
        return RelDataTypeImpl.proto(fieldInfoBuilder.build());
    }
    
    private RelDataType getRelDataType(final ShardingSphereColumn column) {
        Class<?> sqlTypeClass = SqlType.valueOf(column.getDataType()).clazz;
        RelDataType javaType = REL_DATA_TYPE_FACTORY.createJavaType(sqlTypeClass);
        return REL_DATA_TYPE_FACTORY.createTypeWithNullability(javaType, true);
    }
}
