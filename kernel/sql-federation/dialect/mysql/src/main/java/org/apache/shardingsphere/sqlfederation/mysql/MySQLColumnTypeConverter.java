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

package org.apache.shardingsphere.sqlfederation.mysql;

import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.shardingsphere.sqlfederation.resultset.converter.SQLFederationColumnTypeConverter;

/**
 * Column type converter for MySQL.
 */
public final class MySQLColumnTypeConverter implements SQLFederationColumnTypeConverter {
    
    @Override
    public Object convertColumnValue(final Object columnValue) {
        if (columnValue instanceof Boolean) {
            return (Boolean) columnValue ? 1 : 0;
        }
        return columnValue;
    }
    
    @Override
    public int convertColumnType(final SqlTypeName sqlTypeName) {
        int result = sqlTypeName.getJdbcOrdinal();
        if (SqlTypeName.BOOLEAN.getJdbcOrdinal() == result || SqlTypeName.ANY.getJdbcOrdinal() == result) {
            return SqlTypeName.VARCHAR.getJdbcOrdinal();
        }
        return result;
    }
    
    @Override
    public String getDatabaseType() {
        return "MySQL";
    }
}
