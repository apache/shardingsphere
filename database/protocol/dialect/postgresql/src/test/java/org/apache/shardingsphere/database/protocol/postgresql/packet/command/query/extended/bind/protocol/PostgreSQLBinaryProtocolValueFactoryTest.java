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

package org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.protocol;

import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.PostgreSQLBinaryColumnType;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PostgreSQLBinaryProtocolValueFactoryTest {
    
    @Test
    void assertGetStringBinaryProtocolValueByVarchar() {
        PostgreSQLBinaryProtocolValue binaryProtocolValue = PostgreSQLBinaryProtocolValueFactory.getBinaryProtocolValue(PostgreSQLBinaryColumnType.VARCHAR);
        assertThat(binaryProtocolValue, isA(PostgreSQLStringBinaryProtocolValue.class));
    }
    
    @Test
    void assertGetStringBinaryProtocolValueByChar() {
        PostgreSQLBinaryProtocolValue binaryProtocolValue = PostgreSQLBinaryProtocolValueFactory.getBinaryProtocolValue(PostgreSQLBinaryColumnType.CHAR);
        assertThat(binaryProtocolValue, isA(PostgreSQLStringBinaryProtocolValue.class));
    }
    
    @Test
    void assertGetInt8BinaryProtocolValue() {
        PostgreSQLBinaryProtocolValue binaryProtocolValue = PostgreSQLBinaryProtocolValueFactory.getBinaryProtocolValue(PostgreSQLBinaryColumnType.INT8);
        assertThat(binaryProtocolValue, isA(PostgreSQLInt8BinaryProtocolValue.class));
    }
    
    @Test
    void assertGetInt4BinaryProtocolValue() {
        PostgreSQLBinaryProtocolValue binaryProtocolValue = PostgreSQLBinaryProtocolValueFactory.getBinaryProtocolValue(PostgreSQLBinaryColumnType.INT4);
        assertThat(binaryProtocolValue, isA(PostgreSQLInt4BinaryProtocolValue.class));
    }
    
    @Test
    void assertGetInt2BinaryProtocolValue() {
        PostgreSQLBinaryProtocolValue binaryProtocolValue = PostgreSQLBinaryProtocolValueFactory.getBinaryProtocolValue(PostgreSQLBinaryColumnType.INT2);
        assertThat(binaryProtocolValue, isA(PostgreSQLInt2BinaryProtocolValue.class));
    }
    
    @Test
    void assertGetDoubleBinaryProtocolValue() {
        PostgreSQLBinaryProtocolValue binaryProtocolValue = PostgreSQLBinaryProtocolValueFactory.getBinaryProtocolValue(PostgreSQLBinaryColumnType.FLOAT8);
        assertThat(binaryProtocolValue, isA(PostgreSQLDoubleBinaryProtocolValue.class));
    }
    
    @Test
    void assertGetFloatBinaryProtocolValue() {
        PostgreSQLBinaryProtocolValue binaryProtocolValue = PostgreSQLBinaryProtocolValueFactory.getBinaryProtocolValue(PostgreSQLBinaryColumnType.FLOAT4);
        assertThat(binaryProtocolValue, isA(PostgreSQLFloatBinaryProtocolValue.class));
    }
    
    @Test
    void assertGetNumericBinaryProtocolValue() {
        PostgreSQLBinaryProtocolValue binaryProtocolValue = PostgreSQLBinaryProtocolValueFactory.getBinaryProtocolValue(PostgreSQLBinaryColumnType.NUMERIC);
        assertThat(binaryProtocolValue, isA(PostgreSQLNumericBinaryProtocolValue.class));
    }
    
    @Test
    void assertGetDateBinaryProtocolValue() {
        PostgreSQLBinaryProtocolValue binaryProtocolValue = PostgreSQLBinaryProtocolValueFactory.getBinaryProtocolValue(PostgreSQLBinaryColumnType.DATE);
        assertThat(binaryProtocolValue, isA(PostgreSQLDateBinaryProtocolValue.class));
    }
    
    @Test
    void assertGetTimeBinaryProtocolValue() {
        PostgreSQLBinaryProtocolValue binaryProtocolValue = PostgreSQLBinaryProtocolValueFactory.getBinaryProtocolValue(PostgreSQLBinaryColumnType.TIMESTAMP);
        assertThat(binaryProtocolValue, isA(PostgreSQLTimeBinaryProtocolValue.class));
    }
    
    @Test
    void assertGetBoolBinaryProtocolValue() {
        PostgreSQLBinaryProtocolValue binaryProtocolValue = PostgreSQLBinaryProtocolValueFactory.getBinaryProtocolValue(PostgreSQLBinaryColumnType.BOOL);
        assertThat(binaryProtocolValue, isA(PostgreSQLBoolBinaryProtocolValue.class));
    }
    
    @Test
    void assertGetBinaryProtocolValueExThrown() {
        assertThrows(IllegalArgumentException.class, () -> PostgreSQLBinaryProtocolValueFactory.getBinaryProtocolValue(PostgreSQLBinaryColumnType.XML));
    }
}
