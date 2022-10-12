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

package org.apache.shardingsphere.sqlfederation.optimizer.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.calcite.avatica.SqlType;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rel.type.RelDataTypeFactory.Builder;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;

/**
 * SQL federation data type util.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLFederationDataTypeUtil {
    
    /**
     * Create rel data type.
     * 
     * @param table ShardingSphere table
     * @param typeFactory type factory
     * @return rel data type
     */
    public static RelDataType createRelDataType(final ShardingSphereTable table, final RelDataTypeFactory typeFactory) {
        Builder fieldInfoBuilder = typeFactory.builder();
        for (ShardingSphereColumn each : table.getColumns().values()) {
            fieldInfoBuilder.add(each.getName(), getRelDataType(each, typeFactory));
        }
        return fieldInfoBuilder.build();
    }
    
    private static RelDataType getRelDataType(final ShardingSphereColumn column, final RelDataTypeFactory typeFactory) {
        Class<?> sqlTypeClass = SqlType.valueOf(column.getDataType()).clazz;
        RelDataType javaType = typeFactory.createJavaType(sqlTypeClass);
        return typeFactory.createTypeWithNullability(javaType, true);
    }
}
