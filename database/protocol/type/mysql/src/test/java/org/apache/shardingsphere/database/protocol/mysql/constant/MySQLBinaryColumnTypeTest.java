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

package org.apache.shardingsphere.database.protocol.mysql.constant;

import org.junit.jupiter.api.Test;

import java.sql.Types;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MySQLBinaryColumnTypeTest {
    
    @Test
    void assertValueOfJDBC() {
        assertThat(MySQLBinaryColumnType.valueOfJDBCType(Types.BIT), is(MySQLBinaryColumnType.BIT));
        assertThat(MySQLBinaryColumnType.valueOfJDBCType(Types.TINYINT), is(MySQLBinaryColumnType.TINY));
        assertThat(MySQLBinaryColumnType.valueOfJDBCType(Types.SMALLINT), is(MySQLBinaryColumnType.SHORT));
        assertThat(MySQLBinaryColumnType.valueOfJDBCType(Types.INTEGER), is(MySQLBinaryColumnType.LONG));
        assertThat(MySQLBinaryColumnType.valueOfJDBCType(Types.BIGINT), is(MySQLBinaryColumnType.LONGLONG));
        assertThat(MySQLBinaryColumnType.valueOfJDBCType(Types.FLOAT), is(MySQLBinaryColumnType.FLOAT));
        assertThat(MySQLBinaryColumnType.valueOfJDBCType(Types.REAL), is(MySQLBinaryColumnType.FLOAT));
        assertThat(MySQLBinaryColumnType.valueOfJDBCType(Types.DOUBLE), is(MySQLBinaryColumnType.DOUBLE));
        assertThat(MySQLBinaryColumnType.valueOfJDBCType(Types.NUMERIC), is(MySQLBinaryColumnType.NEWDECIMAL));
        assertThat(MySQLBinaryColumnType.valueOfJDBCType(Types.DECIMAL), is(MySQLBinaryColumnType.NEWDECIMAL));
        assertThat(MySQLBinaryColumnType.valueOfJDBCType(Types.CHAR), is(MySQLBinaryColumnType.STRING));
        assertThat(MySQLBinaryColumnType.valueOfJDBCType(Types.VARCHAR), is(MySQLBinaryColumnType.VAR_STRING));
        assertThat(MySQLBinaryColumnType.valueOfJDBCType(Types.LONGVARCHAR), is(MySQLBinaryColumnType.VAR_STRING));
        assertThat(MySQLBinaryColumnType.valueOfJDBCType(Types.DATE), is(MySQLBinaryColumnType.DATE));
        assertThat(MySQLBinaryColumnType.valueOfJDBCType(Types.TIME), is(MySQLBinaryColumnType.TIME));
        assertThat(MySQLBinaryColumnType.valueOfJDBCType(Types.TIMESTAMP), is(MySQLBinaryColumnType.TIMESTAMP));
        assertThat(MySQLBinaryColumnType.valueOfJDBCType(Types.BINARY), is(MySQLBinaryColumnType.LONG_BLOB));
        assertThat(MySQLBinaryColumnType.valueOfJDBCType(Types.VARBINARY), is(MySQLBinaryColumnType.TINY_BLOB));
        assertThat(MySQLBinaryColumnType.valueOfJDBCType(Types.LONGVARBINARY), is(MySQLBinaryColumnType.BLOB));
        assertThat(MySQLBinaryColumnType.valueOfJDBCType(Types.NULL), is(MySQLBinaryColumnType.NULL));
        assertThat(MySQLBinaryColumnType.valueOfJDBCType(Types.BLOB), is(MySQLBinaryColumnType.BLOB));
    }
    
    @Test
    void assertValueOfJDBCIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> MySQLBinaryColumnType.valueOfJDBCType(9999));
    }
    
    @Test
    void assertValueOf() {
        assertThat(MySQLBinaryColumnType.valueOf(MySQLBinaryColumnType.DECIMAL.getValue()), is(MySQLBinaryColumnType.DECIMAL));
    }
    
    @Test
    void assertValueOfWithIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> MySQLBinaryColumnType.valueOf(-1));
    }
}
