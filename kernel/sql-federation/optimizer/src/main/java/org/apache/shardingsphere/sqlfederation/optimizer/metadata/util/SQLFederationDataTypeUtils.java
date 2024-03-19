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

package org.apache.shardingsphere.sqlfederation.optimizer.metadata.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.calcite.avatica.SqlType;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rel.type.RelDataTypeFactory.Builder;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.database.mysql.type.MySQLDatabaseType;
import org.apache.shardingsphere.infra.database.opengauss.type.OpenGaussDatabaseType;
import org.apache.shardingsphere.infra.database.postgresql.type.PostgreSQLDatabaseType;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;

import java.math.BigInteger;
import java.sql.Types;
import java.util.Optional;

/**
 * SQL federation data type utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLFederationDataTypeUtils {
    
    /**
     * Create rel data type.
     *
     * @param table ShardingSphere table
     * @param protocolType protocol type
     * @param typeFactory type factory
     * @return rel data type
     */
    public static RelDataType createRelDataType(final ShardingSphereTable table, final DatabaseType protocolType, final RelDataTypeFactory typeFactory) {
        Builder fieldInfoBuilder = typeFactory.builder();
        for (ShardingSphereColumn each : table.getColumnValues()) {
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
        Optional<Class<?>> typeClazz = Optional.empty();
        if (protocolType instanceof MySQLDatabaseType) {
            typeClazz = findMySQLTypeClass(column);
        }
        if (protocolType instanceof PostgreSQLDatabaseType || protocolType instanceof OpenGaussDatabaseType) {
            typeClazz = findPostgreSQLTypeClass(column);
        }
        return typeClazz.orElseGet(() -> SqlType.valueOf(column.getDataType()).clazz);
    }
    
    private static Optional<Class<?>> findPostgreSQLTypeClass(final ShardingSphereColumn column) {
        if (Types.SMALLINT == column.getDataType()) {
            return Optional.of(Integer.class);
        }
        return Optional.empty();
    }
    
    private static Optional<Class<?>> findMySQLTypeClass(final ShardingSphereColumn column) {
        if (Types.TINYINT == column.getDataType() || Types.SMALLINT == column.getDataType()) {
            return Optional.of(Integer.class);
        }
        if (Types.INTEGER == column.getDataType()) {
            return column.isUnsigned() ? Optional.of(Long.class) : Optional.of(Integer.class);
        }
        if (Types.BIGINT == column.getDataType()) {
            return column.isUnsigned() ? Optional.of(BigInteger.class) : Optional.of(Long.class);
        }
        return Optional.empty();
    }
}
