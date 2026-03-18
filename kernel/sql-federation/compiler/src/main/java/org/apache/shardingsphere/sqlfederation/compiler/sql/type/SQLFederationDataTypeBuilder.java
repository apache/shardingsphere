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

package org.apache.shardingsphere.sqlfederation.compiler.sql.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.calcite.avatica.SqlType;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rel.type.RelDataTypeFactory.Builder;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;

/**
 * SQL federation data type builder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLFederationDataTypeBuilder {
    
    /**
     * Build rel data type.
     *
     * @param table ShardingSphere table
     * @param protocolType protocol type
     * @param typeFactory type factory
     * @return rel data type
     */
    public static RelDataType build(final ShardingSphereTable table, final DatabaseType protocolType, final RelDataTypeFactory typeFactory) {
        Builder fieldInfoBuilder = typeFactory.builder();
        for (ShardingSphereColumn each : table.getAllColumns()) {
            fieldInfoBuilder.add(each.getName(), getRelDataType(protocolType, each, typeFactory));
        }
        return fieldInfoBuilder.build();
    }
    
    private static RelDataType getRelDataType(final DatabaseType protocolType, final ShardingSphereColumn column, final RelDataTypeFactory typeFactory) {
        Class<?> sqlTypeClass = getSqlTypeClass(protocolType, column);
        RelDataType javaType = typeFactory.createJavaType(sqlTypeClass);
        return typeFactory.createTypeWithNullability(javaType, true);
    }
    
    /**
     * Get SQL type class.
     *
     * @param protocolType protocol type
     * @param column ShardingSphere column
     * @return SQL type class
     */
    public static Class<?> getSqlTypeClass(final DatabaseType protocolType, final ShardingSphereColumn column) {
        return new DatabaseTypeRegistry(protocolType).getDialectDatabaseMetaData().getDataTypeOption().findExtraSQLTypeClass(column.getDataType(), column.isUnsigned())
                .orElseGet(() -> SqlType.valueOf(column.getDataType()).clazz);
    }
}
