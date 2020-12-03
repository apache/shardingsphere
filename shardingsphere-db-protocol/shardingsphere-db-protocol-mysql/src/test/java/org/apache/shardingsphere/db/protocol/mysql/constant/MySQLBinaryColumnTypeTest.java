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

package org.apache.shardingsphere.db.protocol.mysql.constant;

import org.junit.Test;

import java.sql.Types;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class MySQLBinaryColumnTypeTest {
    
    @Test
    public void assertValueOfJDBC() {
        assertThat(MySQLBinaryColumnType.valueOfJDBCType(Types.BIT), is(MySQLBinaryColumnType.MYSQL_TYPE_BIT));
        assertThat(MySQLBinaryColumnType.valueOfJDBCType(Types.TINYINT), is(MySQLBinaryColumnType.MYSQL_TYPE_TINY));
        assertThat(MySQLBinaryColumnType.valueOfJDBCType(Types.SMALLINT), is(MySQLBinaryColumnType.MYSQL_TYPE_SHORT));
        assertThat(MySQLBinaryColumnType.valueOfJDBCType(Types.INTEGER), is(MySQLBinaryColumnType.MYSQL_TYPE_LONG));
        assertThat(MySQLBinaryColumnType.valueOfJDBCType(Types.BIGINT), is(MySQLBinaryColumnType.MYSQL_TYPE_LONGLONG));
        assertThat(MySQLBinaryColumnType.valueOfJDBCType(Types.FLOAT), is(MySQLBinaryColumnType.MYSQL_TYPE_FLOAT));
        assertThat(MySQLBinaryColumnType.valueOfJDBCType(Types.REAL), is(MySQLBinaryColumnType.MYSQL_TYPE_FLOAT));
        assertThat(MySQLBinaryColumnType.valueOfJDBCType(Types.DOUBLE), is(MySQLBinaryColumnType.MYSQL_TYPE_DOUBLE));
        assertThat(MySQLBinaryColumnType.valueOfJDBCType(Types.NUMERIC), is(MySQLBinaryColumnType.MYSQL_TYPE_NEWDECIMAL));
        assertThat(MySQLBinaryColumnType.valueOfJDBCType(Types.DECIMAL), is(MySQLBinaryColumnType.MYSQL_TYPE_NEWDECIMAL));
        assertThat(MySQLBinaryColumnType.valueOfJDBCType(Types.CHAR), is(MySQLBinaryColumnType.MYSQL_TYPE_STRING));
        assertThat(MySQLBinaryColumnType.valueOfJDBCType(Types.VARCHAR), is(MySQLBinaryColumnType.MYSQL_TYPE_VAR_STRING));
        assertThat(MySQLBinaryColumnType.valueOfJDBCType(Types.LONGVARCHAR), is(MySQLBinaryColumnType.MYSQL_TYPE_VAR_STRING));
        assertThat(MySQLBinaryColumnType.valueOfJDBCType(Types.DATE), is(MySQLBinaryColumnType.MYSQL_TYPE_DATE));
        assertThat(MySQLBinaryColumnType.valueOfJDBCType(Types.TIME), is(MySQLBinaryColumnType.MYSQL_TYPE_TIME));
        assertThat(MySQLBinaryColumnType.valueOfJDBCType(Types.TIMESTAMP), is(MySQLBinaryColumnType.MYSQL_TYPE_TIMESTAMP));
        assertThat(MySQLBinaryColumnType.valueOfJDBCType(Types.BINARY), is(MySQLBinaryColumnType.MYSQL_TYPE_STRING));
        assertThat(MySQLBinaryColumnType.valueOfJDBCType(Types.VARBINARY), is(MySQLBinaryColumnType.MYSQL_TYPE_VAR_STRING));
        assertThat(MySQLBinaryColumnType.valueOfJDBCType(Types.LONGVARBINARY), is(MySQLBinaryColumnType.MYSQL_TYPE_VAR_STRING));
        assertThat(MySQLBinaryColumnType.valueOfJDBCType(Types.NULL), is(MySQLBinaryColumnType.MYSQL_TYPE_NULL));
        assertThat(MySQLBinaryColumnType.valueOfJDBCType(Types.BLOB), is(MySQLBinaryColumnType.MYSQL_TYPE_BLOB));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertValueOfJDBCIllegalArgument() {
        MySQLBinaryColumnType.valueOfJDBCType(9999);
    }
    
    @Test
    public void assertValueOf() {
        assertThat(MySQLBinaryColumnType.valueOf(MySQLBinaryColumnType.MYSQL_TYPE_DECIMAL.getValue()), is(MySQLBinaryColumnType.MYSQL_TYPE_DECIMAL));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertValueOfWithIllegalArgument() {
        MySQLBinaryColumnType.valueOf(-1);
    }
}
