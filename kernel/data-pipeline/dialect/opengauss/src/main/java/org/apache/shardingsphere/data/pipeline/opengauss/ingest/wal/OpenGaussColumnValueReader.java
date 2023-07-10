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

package org.apache.shardingsphere.data.pipeline.opengauss.ingest.wal;

import org.apache.shardingsphere.data.pipeline.core.dumper.AbstractColumnValueReader;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Column value reader for openGauss.
 */
public final class OpenGaussColumnValueReader extends AbstractColumnValueReader {
    
    private static final String MONEY_TYPE = "money";
    
    private static final String BIT_TYPE = "bit";
    
    private static final String BOOL_TYPE = "bool";
    
    @Override
    protected Object doReadValue(final ResultSet resultSet, final ResultSetMetaData metaData, final int columnIndex) throws SQLException {
        if (isMoneyType(metaData, columnIndex)) {
            return resultSet.getBigDecimal(columnIndex);
        }
        if (isBitType(metaData, columnIndex)) {
            // openGauss JDBC driver can't parse bit(n) correctly when n > 1, so JDBC url already add bitToString, there just return string
            return resultSet.getString(columnIndex);
        }
        if (isBoolType(metaData, columnIndex)) {
            return resultSet.getBoolean(columnIndex);
        }
        return super.defaultDoReadValue(resultSet, metaData, columnIndex);
    }
    
    private boolean isMoneyType(final ResultSetMetaData resultSetMetaData, final int index) throws SQLException {
        return MONEY_TYPE.equalsIgnoreCase(resultSetMetaData.getColumnTypeName(index));
    }
    
    private boolean isBoolType(final ResultSetMetaData resultSetMetaData, final int index) throws SQLException {
        return BOOL_TYPE.equalsIgnoreCase(resultSetMetaData.getColumnTypeName(index));
    }
    
    private boolean isBitType(final ResultSetMetaData resultSetMetaData, final int index) throws SQLException {
        if (Types.BIT == resultSetMetaData.getColumnType(index)) {
            return BIT_TYPE.equalsIgnoreCase(resultSetMetaData.getColumnTypeName(index));
        }
        return false;
    }
    
    @Override
    public String getDatabaseType() {
        return "openGauss";
    }
}
