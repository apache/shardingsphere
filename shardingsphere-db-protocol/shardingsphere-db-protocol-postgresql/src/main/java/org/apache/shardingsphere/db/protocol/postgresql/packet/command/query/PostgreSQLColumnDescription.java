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

package org.apache.shardingsphere.db.protocol.postgresql.packet.command.query;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.db.protocol.postgresql.constant.PostgreSQLArrayColumnType;
import org.apache.shardingsphere.db.protocol.postgresql.constant.PostgreSQLColumnType;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Column description for PostgreSQL.
 */
@Getter
@Slf4j
public final class PostgreSQLColumnDescription {
    
    private final String columnName;
    
    private final int tableOID = 0;
    
    private final int columnIndex;
    
    private final int typeOID;
    
    private final int columnLength;
    
    private final int typeModifier = -1;
    
    private final int dataFormat = 0;
    
    public PostgreSQLColumnDescription(final String columnName, final int columnIndex, final int columnType, final int columnLength, final ResultSetMetaData resultSetMetaData) {
        this.columnName = columnName;
        this.columnIndex = columnIndex;
        if (Types.ARRAY == columnType && null != resultSetMetaData) {
            String columnTypeName = null;
            try {
                columnTypeName = resultSetMetaData.getColumnTypeName(columnIndex);
            } catch (final SQLException ex) {
                log.error("getColumnTypeName failed, columnName={}, columnIndex={}", columnName, columnIndex, ex);
            }
            typeOID = PostgreSQLArrayColumnType.getTypeOidByColumnTypeName(columnTypeName);
        } else {
            typeOID = PostgreSQLColumnType.valueOfJDBCType(columnType).getValue();
        }
        this.columnLength = columnLength;
    }
}
