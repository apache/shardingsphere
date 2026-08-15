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

package org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended;

import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.PostgreSQLBinaryColumnType;
import org.junit.jupiter.api.Test;
import org.postgresql.util.PGobject;

import java.sql.Types;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PostgreSQLPreparedStatementParameterTypeTest {
    
    @Test
    void assertUnresolved() {
        PostgreSQLPreparedStatementParameterType actual = PostgreSQLPreparedStatementParameterType.unresolved();
        assertFalse(actual.isResolved());
        assertFalse(actual.isNativeType());
    }
    
    @Test
    void assertValueOfProtocolType() {
        PostgreSQLPreparedStatementParameterType actual = PostgreSQLPreparedStatementParameterType.valueOf(PostgreSQLBinaryColumnType.INT4);
        assertTrue(actual.isResolved());
        assertFalse(actual.isNativeType());
        assertThat(actual.decode("123"), is(123));
    }
    
    @Test
    void assertValueOfJDBCMetadataWithStandardType() {
        PostgreSQLPreparedStatementParameterType actual = PostgreSQLPreparedStatementParameterType.valueOf(Types.INTEGER, "int4", null);
        assertTrue(actual.isResolved());
        assertFalse(actual.isNativeType());
        assertThat(actual.getProtocolType(), is(PostgreSQLBinaryColumnType.INT4));
    }
    
    @Test
    void assertValueOfJDBCMetadataWithCustomType() {
        PostgreSQLPreparedStatementParameterType actual = PostgreSQLPreparedStatementParameterType.valueOf(Types.OTHER, "my_enum", 12345);
        assertTrue(actual.isResolved());
        assertTrue(actual.isNativeType());
        assertThat(actual.getProtocolType(), is(PostgreSQLBinaryColumnType.UNSPECIFIED));
        assertThat(actual.getWireOID(), is(12345));
        
        Object decoded = actual.decode("my_value");
        assertThat(decoded, isA(PGobject.class));
        assertThat(((PGobject) decoded).getType(), is("my_enum"));
        assertThat(((PGobject) decoded).getValue(), is("my_value"));
    }
    
    @Test
    void assertDecodeJSON() {
        PostgreSQLPreparedStatementParameterType actual = PostgreSQLPreparedStatementParameterType.valueOf(PostgreSQLBinaryColumnType.JSON);
        Object decoded = actual.decode("{\"key\":\"value\"}");
        assertThat(decoded, isA(PGobject.class));
        assertThat(((PGobject) decoded).getType(), is("json"));
        assertThat(((PGobject) decoded).getValue(), is("{\"key\":\"value\"}"));
    }
    
    @Test
    void assertDecodeJSONB() {
        PostgreSQLPreparedStatementParameterType actual = PostgreSQLPreparedStatementParameterType.valueOf(PostgreSQLBinaryColumnType.JSONB);
        Object decoded = actual.decode("{\"key\":\"value\"}");
        assertThat(decoded, isA(PGobject.class));
        assertThat(((PGobject) decoded).getType(), is("jsonb"));
        assertThat(((PGobject) decoded).getValue(), is("{\"key\":\"value\"}"));
    }
    
    @Test
    void assertDecodeUUID() {
        PostgreSQLPreparedStatementParameterType actual = PostgreSQLPreparedStatementParameterType.valueOf(PostgreSQLBinaryColumnType.UUID);
        Object decoded = actual.decode("00000000-0000-0000-0000-000000000000");
        assertThat(decoded, is("00000000-0000-0000-0000-000000000000"));
    }
}
