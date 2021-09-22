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

package org.apache.shardingsphere.infra.optimize.core.metadata;

import lombok.Getter;
import org.apache.calcite.avatica.SqlType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rel.type.RelDataTypeImpl;
import org.apache.calcite.rel.type.RelDataTypeSystem;
import org.apache.calcite.rel.type.RelProtoDataType;
import org.apache.calcite.sql.type.SqlTypeFactoryImpl;
import org.apache.shardingsphere.infra.metadata.schema.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Federation table meta data.
 */
@Getter
public final class FederationTableMetaData {
    
    private static final RelDataTypeFactory TYPE_FACTORY = new SqlTypeFactoryImpl(RelDataTypeSystem.DEFAULT);   
    
    private final String name;
    
    private final RelProtoDataType relProtoDataType;
    
    private final List<String> columnNames = new ArrayList<>();
    
    public FederationTableMetaData(final String name, final TableMetaData tableMetaData) {
        this.name = name;
        relProtoDataType = createRelDataType(tableMetaData);
        columnNames.addAll(tableMetaData.getColumns().values().stream().map(ColumnMetaData::getName).collect(Collectors.toList()));
    }
    
    private RelProtoDataType createRelDataType(final TableMetaData tableMetaData) {
        RelDataTypeFactory.Builder fieldInfo = TYPE_FACTORY.builder();
        for (ColumnMetaData each : tableMetaData.getColumns().values()) {
            Class<?> clazz = SqlType.valueOf(each.getDataType()).clazz;
            fieldInfo.add(each.getName(), null == clazz ? TYPE_FACTORY.createUnknownType() : TYPE_FACTORY.createTypeWithNullability(TYPE_FACTORY.createJavaType(clazz), true));
        }
        return RelDataTypeImpl.proto(fieldInfo.build());
    }
}
