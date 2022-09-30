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

import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.BasicColumnValueReader;
import org.postgresql.util.PGobject;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.Collections;

/**
 * Column value reader for PostgreSQL.
 */
public final class PostgreSQLColumnValueReader extends BasicColumnValueReader {
    
    private static final Collection<String> TYPE_ALIASES = Collections.singletonList("openGauss");
    
    private static final String PG_MONEY_TYPE = "money";
    
    private static final String PG_BIT_TYPE = "bit";
    
    @Override
    public Object readValue(final ResultSet resultSet, final ResultSetMetaData resultSetMetaData, final int columnIndex) throws SQLException {
        if (isPgMoneyType(resultSetMetaData, columnIndex)) {
            return resultSet.getBigDecimal(columnIndex);
        }
        if (isPgBitType(resultSetMetaData, columnIndex)) {
            PGobject result = new PGobject();
            result.setType("bit");
            Object resultSetObject = resultSet.getObject(columnIndex);
            result.setValue(null == resultSetObject ? null : (Boolean) resultSetObject ? "1" : "0");
            return result;
        }
        return super.readValue(resultSet, resultSetMetaData, columnIndex);
    }
    
    private boolean isPgMoneyType(final ResultSetMetaData resultSetMetaData, final int index) throws SQLException {
        return PG_MONEY_TYPE.equalsIgnoreCase(resultSetMetaData.getColumnTypeName(index));
    }
    
    private boolean isPgBitType(final ResultSetMetaData resultSetMetaData, final int index) throws SQLException {
        if (Types.BIT == resultSetMetaData.getColumnType(index)) {
            return PG_BIT_TYPE.equalsIgnoreCase(resultSetMetaData.getColumnTypeName(index));
        }
        return false;
    }
    
    @Override
    public String getType() {
        return "PostgreSQL";
    }
    
    @Override
    public Collection<String> getTypeAliases() {
        return TYPE_ALIASES;
    }
}
