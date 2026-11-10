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

package org.apache.shardingsphere.data.pipeline.opengauss.ingest.inventory;

import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.column.DialectInventoryColumnValueReader;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Optional;

/**
 * Inventory column value reader for openGauss.
 */
public final class OpenGaussInventoryColumnValueReader implements DialectInventoryColumnValueReader {
    
    private static final String MONEY_DATA_TYPE = "money";
    
    private static final String BIT_DATA_TYPE = "bit";
    
    private static final String BOOL_DATA_TYPE = "bool";
    
    @Override
    public Optional<Object> read(final ResultSet resultSet, final ResultSetMetaData metaData, final int columnIndex) throws SQLException {
        if (isMoneyType(metaData, columnIndex)) {
            return Optional.ofNullable(resultSet.getBigDecimal(columnIndex));
        }
        if (isBitType(metaData, columnIndex)) {
            // openGauss JDBC driver can't parse bit(n) correctly when n > 1, so JDBC url already add bitToString, there just return string
            return Optional.ofNullable(resultSet.getString(columnIndex));
        }
        if (isBoolType(metaData, columnIndex)) {
            return Optional.of(resultSet.getBoolean(columnIndex));
        }
        return Optional.empty();
    }
    
    private boolean isMoneyType(final ResultSetMetaData metaData, final int columnIndex) throws SQLException {
        return MONEY_DATA_TYPE.equalsIgnoreCase(metaData.getColumnTypeName(columnIndex));
    }
    
    private boolean isBitType(final ResultSetMetaData metaData, final int columnIndex) throws SQLException {
        return Types.BIT == metaData.getColumnType(columnIndex) && BIT_DATA_TYPE.equalsIgnoreCase(metaData.getColumnTypeName(columnIndex));
    }
    
    private boolean isBoolType(final ResultSetMetaData metaData, final int columnIndex) throws SQLException {
        return BOOL_DATA_TYPE.equalsIgnoreCase(metaData.getColumnTypeName(columnIndex));
    }
    
    @Override
    public String getDatabaseType() {
        return "openGauss";
    }
}
