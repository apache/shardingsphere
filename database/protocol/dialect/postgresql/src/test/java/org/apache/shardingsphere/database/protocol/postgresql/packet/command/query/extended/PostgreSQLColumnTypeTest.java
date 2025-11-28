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

package org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended;

import org.apache.shardingsphere.database.protocol.postgresql.exception.PostgreSQLProtocolException;
import org.junit.jupiter.api.Test;

import java.sql.Types;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PostgreSQLColumnTypeTest {
    
    @Test
    void assertValueOfJDBCTypeForTinyint() {
        PostgreSQLColumnType sqlColumnType = PostgreSQLColumnType.valueOfJDBCType(Types.TINYINT);
        assertThat(sqlColumnType, is(PostgreSQLColumnType.INT2));
    }
    
    @Test
    void assertValueOfJDBCTypeForSmallint() {
        PostgreSQLColumnType sqlColumnType = PostgreSQLColumnType.valueOfJDBCType(Types.SMALLINT);
        assertThat(sqlColumnType, is(PostgreSQLColumnType.INT2));
    }
    
    @Test
    void assertValueOfJDBCTypeForInteger() {
        PostgreSQLColumnType sqlColumnType = PostgreSQLColumnType.valueOfJDBCType(Types.INTEGER);
        assertThat(sqlColumnType, is(PostgreSQLColumnType.INT4));
    }
    
    @Test
    void assertValueOfJDBCTypeForBigint() {
        PostgreSQLColumnType sqlColumnType = PostgreSQLColumnType.valueOfJDBCType(Types.BIGINT);
        assertThat(sqlColumnType, is(PostgreSQLColumnType.INT8));
    }
    
    @Test
    void assertValueOfJDBCTypeForNumeric() {
        PostgreSQLColumnType sqlColumnType = PostgreSQLColumnType.valueOfJDBCType(Types.NUMERIC);
        assertThat(sqlColumnType, is(PostgreSQLColumnType.NUMERIC));
    }
    
    @Test
    void assertValueOfJDBCTypeForDecimal() {
        PostgreSQLColumnType sqlColumnType = PostgreSQLColumnType.valueOfJDBCType(Types.DECIMAL);
        assertThat(sqlColumnType, is(PostgreSQLColumnType.NUMERIC));
    }
    
    @Test
    void assertValueOfJDBCTypeForReal() {
        PostgreSQLColumnType sqlColumnType = PostgreSQLColumnType.valueOfJDBCType(Types.REAL);
        assertThat(sqlColumnType, is(PostgreSQLColumnType.FLOAT4));
    }
    
    @Test
    void assertValueOfJDBCTypeForDouble() {
        PostgreSQLColumnType sqlColumnType = PostgreSQLColumnType.valueOfJDBCType(Types.DOUBLE);
        assertThat(sqlColumnType, is(PostgreSQLColumnType.FLOAT8));
    }
    
    @Test
    void assertValueOfJDBCTypeForChar() {
        PostgreSQLColumnType sqlColumnType = PostgreSQLColumnType.valueOfJDBCType(Types.CHAR);
        assertThat(sqlColumnType, is(PostgreSQLColumnType.CHAR));
    }
    
    @Test
    void assertValueOfJDBCTypeForVarchar() {
        PostgreSQLColumnType sqlColumnType = PostgreSQLColumnType.valueOfJDBCType(Types.VARCHAR);
        assertThat(sqlColumnType, is(PostgreSQLColumnType.VARCHAR));
    }
    
    @Test
    void assertValueOfJDBCTypeForBinary() {
        PostgreSQLColumnType sqlColumnType = PostgreSQLColumnType.valueOfJDBCType(Types.BINARY);
        assertThat(sqlColumnType, is(PostgreSQLColumnType.BYTEA));
    }
    
    @Test
    void assertValueOfJDBCTypeForBit() {
        PostgreSQLColumnType sqlColumnType = PostgreSQLColumnType.valueOfJDBCType(Types.BIT);
        assertThat(sqlColumnType, is(PostgreSQLColumnType.BIT));
    }
    
    @Test
    void assertValueOfPgTypeForBit() {
        PostgreSQLColumnType sqlColumnType = PostgreSQLColumnType.valueOfJDBCType(Types.BIT, "bit");
        assertThat(sqlColumnType, is(PostgreSQLColumnType.BIT));
    }
    
    @Test
    void assertValueOfPgTypeForBool() {
        PostgreSQLColumnType sqlColumnType = PostgreSQLColumnType.valueOfJDBCType(Types.BIT, "bool");
        assertThat(sqlColumnType, is(PostgreSQLColumnType.BOOL));
    }
    
    @Test
    void assertValueOfJDBCTypeForDate() {
        PostgreSQLColumnType sqlColumnType = PostgreSQLColumnType.valueOfJDBCType(Types.DATE);
        assertThat(sqlColumnType, is(PostgreSQLColumnType.DATE));
    }
    
    @Test
    void assertValueOfJDBCTypeForTime() {
        PostgreSQLColumnType sqlColumnType = PostgreSQLColumnType.valueOfJDBCType(Types.TIME);
        assertThat(sqlColumnType, is(PostgreSQLColumnType.TIME));
    }
    
    @Test
    void assertValueOfJDBCTypeForTimestamp() {
        PostgreSQLColumnType sqlColumnType = PostgreSQLColumnType.valueOfJDBCType(Types.TIMESTAMP);
        assertThat(sqlColumnType, is(PostgreSQLColumnType.TIMESTAMP));
    }
    
    @Test
    void assertValueOfJDBCTypeForOther() {
        PostgreSQLColumnType sqlColumnType = PostgreSQLColumnType.valueOfJDBCType(Types.OTHER);
        assertThat(sqlColumnType, is(PostgreSQLColumnType.UDT_GENERIC));
    }
    
    @Test
    void assertValueOfJDBCTypeForSQLXML() {
        PostgreSQLColumnType sqlColumnType = PostgreSQLColumnType.valueOfJDBCType(Types.SQLXML);
        assertThat(sqlColumnType, is(PostgreSQLColumnType.XML));
    }
    
    @Test
    void assertValueOfJDBCTypeForBoolean() {
        PostgreSQLColumnType sqlColumnType = PostgreSQLColumnType.valueOfJDBCType(Types.BOOLEAN);
        assertThat(sqlColumnType, is(PostgreSQLColumnType.BOOL));
    }
    
    @Test
    void assertValueOfJDBCTypeExThrown() {
        assertThrows(IllegalArgumentException.class, () -> PostgreSQLColumnType.valueOfJDBCType(Types.REF_CURSOR));
    }
    
    @Test
    void assertValueOf() {
        PostgreSQLColumnType sqlColumnType = PostgreSQLColumnType.valueOf(PostgreSQLColumnType.INT8.getValue());
        assertThat(sqlColumnType, is(PostgreSQLColumnType.INT8));
    }
    
    @Test
    void assertValueOfExThrown() {
        assertThrows(PostgreSQLProtocolException.class, () -> PostgreSQLColumnType.valueOf(9999));
    }

    @Test
    void assertValueOfJDBCTypeForVarBit() {
        PostgreSQLColumnType columnType = PostgreSQLColumnType.valueOfJDBCType(Types.OTHER, "varbit");
        assertThat(columnType,is(PostgreSQLColumnType.VARBIT));
    }

    @Test
    void assertValueOfJDBCTypeForUdt() {
        PostgreSQLColumnType columnType = PostgreSQLColumnType.valueOfJDBCType(Types.OTHER, "post_type");
        assertThat(columnType,is(PostgreSQLColumnType.JSON));
    }


    @Test
    void assertGetValue() {
        assertThat(PostgreSQLColumnType.INT8.getValue(), is(20));
    }
}
