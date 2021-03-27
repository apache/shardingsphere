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

package org.apache.shardingsphere.infra.executor.exec.tool;

import org.apache.calcite.avatica.util.ByteString;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactoryImpl.JavaType;
import org.apache.calcite.runtime.Geometries;
import org.apache.calcite.sql.type.BasicSqlType;
import org.apache.calcite.sql.type.IntervalSqlType;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class RelDataType2JavaTypeUtils {
    
    /**
     * Copied from org.apache.calcite.jdbc.JavaTypeFactoryImpl#getJavaClass(org.apache.calcite.rel.type.RelDataType).
     * @param type Calcite <code>RelDataType</code>
     * @return Java yype.
     */
    public static Type getJavaClass(final RelDataType type) {
        if (type instanceof JavaType) {
            JavaType javaType = (JavaType) type;
            return javaType.getJavaClass();
        }
        if (type instanceof BasicSqlType || type instanceof IntervalSqlType) {
            Type result = getJavaClassFromBasicSqlType(type);
            if (result != null) {
                return result;
            }
        }
        switch (type.getSqlTypeName()) {
            case MAP:
                return Map.class;
            case ARRAY:
            case MULTISET:
                return List.class;
            default:
                return null;
        }
    }
    
    private static Type getJavaClassFromBasicSqlType(final RelDataType type) {
        switch (type.getSqlTypeName()) {
            case VARCHAR:
            case CHAR:
                return String.class;
            case DATE:
            case TIME:
            case TIME_WITH_LOCAL_TIME_ZONE:
            case INTEGER:
            case INTERVAL_YEAR:
            case INTERVAL_YEAR_MONTH:
            case INTERVAL_MONTH:
                return type.isNullable() ? Integer.class : int.class;
            case TIMESTAMP:
            case TIMESTAMP_WITH_LOCAL_TIME_ZONE:
            case BIGINT:
            case INTERVAL_DAY:
            case INTERVAL_DAY_HOUR:
            case INTERVAL_DAY_MINUTE:
            case INTERVAL_DAY_SECOND:
            case INTERVAL_HOUR:
            case INTERVAL_HOUR_MINUTE:
            case INTERVAL_HOUR_SECOND:
            case INTERVAL_MINUTE:
            case INTERVAL_MINUTE_SECOND:
            case INTERVAL_SECOND:
                return type.isNullable() ? Long.class : long.class;
            case SMALLINT:
                return type.isNullable() ? Short.class : short.class;
            case TINYINT:
                return type.isNullable() ? Byte.class : byte.class;
            case DECIMAL:
                return BigDecimal.class;
            case BOOLEAN:
                return type.isNullable() ? Boolean.class : boolean.class;
            case DOUBLE:
            case FLOAT:
                return type.isNullable() ? Double.class : double.class;
            case REAL:
                return type.isNullable() ? Float.class : float.class;
            case BINARY:
            case VARBINARY:
                return ByteString.class;
            case GEOMETRY:
                return Geometries.Geom.class;
            case SYMBOL:
                return Enum.class;
            case ANY:
                return Object.class;
            case NULL:
                return Void.class;
            default:
                return null;
        }
    }
}
