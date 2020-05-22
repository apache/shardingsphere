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

public final class MySQLColumnTypeTest {
    
    @Test
    public void assertValueOfJDBC() {
        assertThat(MySQLColumnType.valueOfJDBCType(Types.BIT), is(MySQLColumnType.MYSQL_TYPE_BIT));
        assertThat(MySQLColumnType.valueOfJDBCType(Types.TINYINT), is(MySQLColumnType.MYSQL_TYPE_TINY));
        assertThat(MySQLColumnType.valueOfJDBCType(Types.SMALLINT), is(MySQLColumnType.MYSQL_TYPE_SHORT));
        assertThat(MySQLColumnType.valueOfJDBCType(Types.INTEGER), is(MySQLColumnType.MYSQL_TYPE_LONG));
        assertThat(MySQLColumnType.valueOfJDBCType(Types.BIGINT), is(MySQLColumnType.MYSQL_TYPE_LONGLONG));
        assertThat(MySQLColumnType.valueOfJDBCType(Types.FLOAT), is(MySQLColumnType.MYSQL_TYPE_FLOAT));
        assertThat(MySQLColumnType.valueOfJDBCType(Types.REAL), is(MySQLColumnType.MYSQL_TYPE_FLOAT));
        assertThat(MySQLColumnType.valueOfJDBCType(Types.DOUBLE), is(MySQLColumnType.MYSQL_TYPE_DOUBLE));
        assertThat(MySQLColumnType.valueOfJDBCType(Types.NUMERIC), is(MySQLColumnType.MYSQL_TYPE_NEWDECIMAL));
        assertThat(MySQLColumnType.valueOfJDBCType(Types.DECIMAL), is(MySQLColumnType.MYSQL_TYPE_NEWDECIMAL));
        assertThat(MySQLColumnType.valueOfJDBCType(Types.CHAR), is(MySQLColumnType.MYSQL_TYPE_STRING));
        assertThat(MySQLColumnType.valueOfJDBCType(Types.VARCHAR), is(MySQLColumnType.MYSQL_TYPE_VAR_STRING));
        assertThat(MySQLColumnType.valueOfJDBCType(Types.LONGVARCHAR), is(MySQLColumnType.MYSQL_TYPE_VAR_STRING));
        assertThat(MySQLColumnType.valueOfJDBCType(Types.DATE), is(MySQLColumnType.MYSQL_TYPE_DATE));
        assertThat(MySQLColumnType.valueOfJDBCType(Types.TIME), is(MySQLColumnType.MYSQL_TYPE_TIME));
        assertThat(MySQLColumnType.valueOfJDBCType(Types.TIMESTAMP), is(MySQLColumnType.MYSQL_TYPE_TIMESTAMP));
        assertThat(MySQLColumnType.valueOfJDBCType(Types.BINARY), is(MySQLColumnType.MYSQL_TYPE_STRING));
        assertThat(MySQLColumnType.valueOfJDBCType(Types.VARBINARY), is(MySQLColumnType.MYSQL_TYPE_VAR_STRING));
        assertThat(MySQLColumnType.valueOfJDBCType(Types.LONGVARBINARY), is(MySQLColumnType.MYSQL_TYPE_VAR_STRING));
        assertThat(MySQLColumnType.valueOfJDBCType(Types.NULL), is(MySQLColumnType.MYSQL_TYPE_NULL));
        assertThat(MySQLColumnType.valueOfJDBCType(Types.BLOB), is(MySQLColumnType.MYSQL_TYPE_BLOB));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertValueOfJDBCIllegalArgument() {
        MySQLColumnType.valueOfJDBCType(9999);
    }
    
    @Test
    public void assertValueOf() {
        assertThat(MySQLColumnType.valueOf(MySQLColumnType.MYSQL_TYPE_DECIMAL.getValue()), is(MySQLColumnType.MYSQL_TYPE_DECIMAL));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertValueOfWithIllegalArgument() {
        MySQLColumnType.valueOf(-1);
    }
}
