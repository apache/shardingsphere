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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query;

import org.firebirdsql.gds.BlrConstants;
import org.junit.jupiter.api.Test;

import java.sql.Types;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FirebirdBinaryColumnTypeTest {
    
    @Test
    void assertValueOfJDBCAndBLR() {
        assertThat(FirebirdBinaryColumnType.valueOfJDBCType(Types.TINYINT), is(FirebirdBinaryColumnType.SHORT));
        assertThat(FirebirdBinaryColumnType.valueOfJDBCType(Types.SMALLINT), is(FirebirdBinaryColumnType.SHORT));
        assertThat(FirebirdBinaryColumnType.valueOfJDBCType(Types.INTEGER), is(FirebirdBinaryColumnType.LONG));
        assertThat(FirebirdBinaryColumnType.valueOfJDBCType(Types.BIGINT), is(FirebirdBinaryColumnType.INT64));
        assertThat(FirebirdBinaryColumnType.valueOfJDBCType(Types.FLOAT), is(FirebirdBinaryColumnType.FLOAT));
        assertThat(FirebirdBinaryColumnType.valueOfJDBCType(Types.REAL), is(FirebirdBinaryColumnType.FLOAT));
        assertThat(FirebirdBinaryColumnType.valueOfJDBCType(Types.DOUBLE), is(FirebirdBinaryColumnType.DOUBLE));
        assertThat(FirebirdBinaryColumnType.valueOfJDBCType(Types.NUMERIC), is(FirebirdBinaryColumnType.NUMERIC));
        assertThat(FirebirdBinaryColumnType.valueOfJDBCType(Types.DECIMAL), is(FirebirdBinaryColumnType.DECIMAL));
        assertThat(FirebirdBinaryColumnType.valueOfJDBCType(Types.CHAR), is(FirebirdBinaryColumnType.VARYING));
        assertThat(FirebirdBinaryColumnType.valueOfJDBCType(Types.VARCHAR), is(FirebirdBinaryColumnType.VARYING));
        assertThat(FirebirdBinaryColumnType.valueOfJDBCType(Types.LONGVARCHAR), is(FirebirdBinaryColumnType.BLOB));
        assertThat(FirebirdBinaryColumnType.valueOfJDBCType(Types.DATE), is(FirebirdBinaryColumnType.DATE));
        assertThat(FirebirdBinaryColumnType.valueOfJDBCType(Types.TIME), is(FirebirdBinaryColumnType.TIME));
        assertThat(FirebirdBinaryColumnType.valueOfJDBCType(Types.TIMESTAMP), is(FirebirdBinaryColumnType.TIMESTAMP));
        assertThat(FirebirdBinaryColumnType.valueOfJDBCType(Types.BINARY), is(FirebirdBinaryColumnType.TEXT));
        assertThat(FirebirdBinaryColumnType.valueOfJDBCType(Types.VARBINARY), is(FirebirdBinaryColumnType.VARYING));
        assertThat(FirebirdBinaryColumnType.valueOfJDBCType(Types.LONGVARBINARY), is(FirebirdBinaryColumnType.BLOB));
        assertThat(FirebirdBinaryColumnType.valueOfJDBCType(Types.NULL), is(FirebirdBinaryColumnType.NULL));
        assertThat(FirebirdBinaryColumnType.valueOfJDBCType(Types.BLOB), is(FirebirdBinaryColumnType.BLOB));
        assertThat(FirebirdBinaryColumnType.valueOfJDBCType(Types.BOOLEAN), is(FirebirdBinaryColumnType.BOOLEAN));
        assertThat(FirebirdBinaryColumnType.valueOfJDBCType(Types.ARRAY), is(FirebirdBinaryColumnType.ARRAY));
        assertThat(FirebirdBinaryColumnType.valueOfJDBCType(Types.TIME_WITH_TIMEZONE), is(FirebirdBinaryColumnType.TIME_TZ));
        assertThat(FirebirdBinaryColumnType.valueOfJDBCType(Types.TIMESTAMP_WITH_TIMEZONE), is(FirebirdBinaryColumnType.TIMESTAMP_TZ));
        
        assertThat(FirebirdBinaryColumnType.valueOfBLRType(BlrConstants.blr_varying2), is(FirebirdBinaryColumnType.VARYING));
        assertThat(FirebirdBinaryColumnType.valueOfBLRType(BlrConstants.blr_text2), is(FirebirdBinaryColumnType.TEXT));
        assertThat(FirebirdBinaryColumnType.valueOfBLRType(BlrConstants.blr_text), is(FirebirdBinaryColumnType.LEGACY_TEXT));
        assertThat(FirebirdBinaryColumnType.valueOfBLRType(BlrConstants.blr_varying), is(FirebirdBinaryColumnType.LEGACY_VARYING));
        assertThat(FirebirdBinaryColumnType.valueOfBLRType(BlrConstants.blr_double), is(FirebirdBinaryColumnType.DOUBLE));
        assertThat(FirebirdBinaryColumnType.valueOfBLRType(BlrConstants.blr_float), is(FirebirdBinaryColumnType.FLOAT));
        assertThat(FirebirdBinaryColumnType.valueOfBLRType(BlrConstants.blr_d_float), is(FirebirdBinaryColumnType.D_FLOAT));
        assertThat(FirebirdBinaryColumnType.valueOfBLRType(BlrConstants.blr_sql_date), is(FirebirdBinaryColumnType.DATE));
        assertThat(FirebirdBinaryColumnType.valueOfBLRType(BlrConstants.blr_sql_time), is(FirebirdBinaryColumnType.TIME));
        assertThat(FirebirdBinaryColumnType.valueOfBLRType(BlrConstants.blr_timestamp), is(FirebirdBinaryColumnType.TIMESTAMP));
        assertThat(FirebirdBinaryColumnType.valueOfBLRType(BlrConstants.blr_quad), is(FirebirdBinaryColumnType.BLOB));
        assertThat(FirebirdBinaryColumnType.valueOfBLRType(BlrConstants.blr_long), is(FirebirdBinaryColumnType.LONG));
        assertThat(FirebirdBinaryColumnType.valueOfBLRType(BlrConstants.blr_short), is(FirebirdBinaryColumnType.SHORT));
        assertThat(FirebirdBinaryColumnType.valueOfBLRType(BlrConstants.blr_int64), is(FirebirdBinaryColumnType.INT64));
        assertThat(FirebirdBinaryColumnType.valueOfBLRType(BlrConstants.blr_bool), is(FirebirdBinaryColumnType.BOOLEAN));
        assertThat(FirebirdBinaryColumnType.valueOfBLRType(BlrConstants.blr_dec64), is(FirebirdBinaryColumnType.DEC16));
        assertThat(FirebirdBinaryColumnType.valueOfBLRType(BlrConstants.blr_dec128), is(FirebirdBinaryColumnType.DEC34));
        assertThat(FirebirdBinaryColumnType.valueOfBLRType(BlrConstants.blr_int128), is(FirebirdBinaryColumnType.INT128));
        assertThat(FirebirdBinaryColumnType.valueOfBLRType(BlrConstants.blr_timestamp_tz), is(FirebirdBinaryColumnType.TIMESTAMP_TZ));
        assertThat(FirebirdBinaryColumnType.valueOfBLRType(BlrConstants.blr_sql_time_tz), is(FirebirdBinaryColumnType.TIME_TZ));
        assertThat(FirebirdBinaryColumnType.valueOfBLRType(BlrConstants.blr_ex_timestamp_tz), is(FirebirdBinaryColumnType.TIMESTAMP_TZ_EX));
        assertThat(FirebirdBinaryColumnType.valueOfBLRType(BlrConstants.blr_ex_time_tz), is(FirebirdBinaryColumnType.TIME_TZ_EX));
    }
    
    @Test
    void assertValueOfJDBCIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> FirebirdBinaryColumnType.valueOfJDBCType(9999));
    }
    
    @Test
    void assertValueOfBLRTypeIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> FirebirdBinaryColumnType.valueOfBLRType(-100));
    }
    
    @Test
    void assertValueOf() {
        assertThat(FirebirdBinaryColumnType.valueOf(FirebirdBinaryColumnType.DECIMAL.getValue()), is(FirebirdBinaryColumnType.DECIMAL));
    }
    
    @Test
    void assertValueOfWithIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> FirebirdBinaryColumnType.valueOf(-1));
    }
    
    @Test
    void assertValueOfJDBCType() {
        assertThat(FirebirdBinaryColumnType.valueOfJDBCType(Types.BLOB), is(FirebirdBinaryColumnType.BLOB));
    }
}
