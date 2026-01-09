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

import org.junit.jupiter.api.Test;

import java.sql.Types;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class PostgreSQLColumnTypeOtherHandlingTest {
    
    @Test
    void assertValueOfJDBCTypeForJson() {
        PostgreSQLColumnType sqlColumnType = PostgreSQLColumnType.valueOfJDBCType(Types.OTHER, "json");
        assertThat(sqlColumnType, is(PostgreSQLColumnType.JSON));
    }
    
    @Test
    void assertValueOfJDBCTypeForJsonb() {
        PostgreSQLColumnType sqlColumnType = PostgreSQLColumnType.valueOfJDBCType(Types.OTHER, "jsonb");
        assertThat(sqlColumnType, is(PostgreSQLColumnType.JSONB));
    }
    
    @Test
    void assertValueOfJDBCTypeForUuid() {
        PostgreSQLColumnType sqlColumnType = PostgreSQLColumnType.valueOfJDBCType(Types.OTHER, "uuid");
        assertThat(sqlColumnType, is(PostgreSQLColumnType.UUID));
    }
    
    @Test
    void assertValueOfJDBCTypeForVarbit() {
        PostgreSQLColumnType sqlColumnType = PostgreSQLColumnType.valueOfJDBCType(Types.OTHER, "varbit");
        assertThat(sqlColumnType, is(PostgreSQLColumnType.VARBIT));
    }
    
    @Test
    void assertValueOfJDBCTypeForBitVarying() {
        PostgreSQLColumnType sqlColumnType = PostgreSQLColumnType.valueOfJDBCType(Types.OTHER, "bit varying");
        assertThat(sqlColumnType, is(PostgreSQLColumnType.VARBIT));
    }
    
    @Test
    void assertValueOfJDBCTypeForCustomUDT() {
        // For custom UDT types not specifically handled, it defaults to JSON according to the mapping
        PostgreSQLColumnType sqlColumnType = PostgreSQLColumnType.valueOfJDBCType(Types.OTHER, "custom_udt");
        // This shows the issue: non-UDT OTHER types default to JSON
        assertThat(sqlColumnType, is(PostgreSQLColumnType.JSON));
    }
    
    @Test
    void assertValueOfJDBCTypeForGenericOther() {
        PostgreSQLColumnType sqlColumnType = PostgreSQLColumnType.valueOfJDBCType(Types.OTHER);
        assertThat(sqlColumnType, is(PostgreSQLColumnType.JSON));
    }
    
    // Note: isJSON and isJSONB are private methods and cannot be tested directly.
    // Their functionality is tested indirectly through the valueOfJDBCType methods.
}