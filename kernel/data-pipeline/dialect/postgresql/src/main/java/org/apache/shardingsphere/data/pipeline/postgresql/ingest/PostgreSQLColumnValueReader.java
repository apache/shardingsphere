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

package org.apache.shardingsphere.data.pipeline.postgresql.ingest;

import org.apache.shardingsphere.data.pipeline.spi.ingest.dumper.DialectColumnValueReader;
import org.postgresql.util.PGobject;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Optional;

/**
 * Column value reader for PostgreSQL.
 */
public final class PostgreSQLColumnValueReader implements DialectColumnValueReader {
    
    private static final String PG_MONEY_TYPE = "money";
    
    private static final String PG_BIT_TYPE = "bit";
    
    @Override
    public Optional<Object> read(final ResultSet resultSet, final ResultSetMetaData metaData, final int columnIndex) throws SQLException {
        if (isMoneyType(metaData, columnIndex)) {
            return Optional.ofNullable(resultSet.getBigDecimal(columnIndex));
        }
        if (isBitType(metaData, columnIndex)) {
            return Optional.of(getBitObject(resultSet, columnIndex));
        }
        return Optional.empty();
    }
    
    private boolean isMoneyType(final ResultSetMetaData metaData, final int columnIndex) throws SQLException {
        return PG_MONEY_TYPE.equalsIgnoreCase(metaData.getColumnTypeName(columnIndex));
    }
    
    private boolean isBitType(final ResultSetMetaData metaData, final int columnIndex) throws SQLException {
        return Types.BIT == metaData.getColumnType(columnIndex) && PG_BIT_TYPE.equalsIgnoreCase(metaData.getColumnTypeName(columnIndex));
    }
    
    private static PGobject getBitObject(final ResultSet resultSet, final int columnIndex) throws SQLException {
        PGobject result = new PGobject();
        result.setType("bit");
        Object bitValue = resultSet.getObject(columnIndex);
        if (null != bitValue) {
            result.setValue((Boolean) bitValue ? "1" : "0");
        }
        return result;
    }
    
    @Override
    public String getDatabaseType() {
        return "PostgreSQL";
    }
}
