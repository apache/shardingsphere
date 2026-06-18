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

package org.apache.shardingsphere.database.protocol.mysql.packet.command.query.binary.execute.protocol;

import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLBinaryColumnType;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MySQLBinaryProtocolValueFactoryTest {
    
    @Test
    void assertGetBinaryProtocolValueWithMySQLTypeString() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.STRING), isA(MySQLByteLenencBinaryProtocolValue.class));
    }
    
    @Test
    void assertGetBinaryProtocolValueWithMySQLTypeVarchar() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.VARCHAR), isA(MySQLStringLenencBinaryProtocolValue.class));
    }
    
    @Test
    void assertGetBinaryProtocolValueWithMySQLTypeVarString() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.VAR_STRING), isA(MySQLStringLenencBinaryProtocolValue.class));
    }
    
    @Test
    void assertGetBinaryProtocolValueWithMySQLTypeEnum() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.ENUM), isA(MySQLStringLenencBinaryProtocolValue.class));
    }
    
    @Test
    void assertGetBinaryProtocolValueWithMySQLTypeSet() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.SET), isA(MySQLStringLenencBinaryProtocolValue.class));
    }
    
    @Test
    void assertGetBinaryProtocolValueWithMySQLTypeLongBlob() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.LONG_BLOB), isA(MySQLByteLenencBinaryProtocolValue.class));
    }
    
    @Test
    void assertGetBinaryProtocolValueWithMySQLTypeMediumBlob() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.MEDIUM_BLOB), isA(MySQLByteLenencBinaryProtocolValue.class));
    }
    
    @Test
    void assertGetBinaryProtocolValueWithMySQLTypeBlob() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.BLOB), isA(MySQLByteLenencBinaryProtocolValue.class));
    }
    
    @Test
    void assertGetBinaryProtocolValueWithMySQLTypeTinyBlob() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.TINY_BLOB), isA(MySQLByteLenencBinaryProtocolValue.class));
    }
    
    @Test
    void assertGetBinaryProtocolValueWithMySQLTypeGeometry() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.GEOMETRY), isA(MySQLStringLenencBinaryProtocolValue.class));
    }
    
    @Test
    void assertGetBinaryProtocolValueWithMySQLTypeBit() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.BIT), isA(MySQLStringLenencBinaryProtocolValue.class));
    }
    
    @Test
    void assertGetBinaryProtocolValueWithMySQLTypeDecimal() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.DECIMAL), isA(MySQLStringLenencBinaryProtocolValue.class));
    }
    
    @Test
    void assertGetBinaryProtocolValueWithMySQLTypeNewDecimal() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.NEWDECIMAL), isA(MySQLStringLenencBinaryProtocolValue.class));
    }
    
    @Test
    void assertGetBinaryProtocolValueWithMySQLTypeLongLong() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.LONGLONG), isA(MySQLInt8BinaryProtocolValue.class));
    }
    
    @Test
    void assertGetBinaryProtocolValueWithMySQLTypeLong() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.LONG), isA(MySQLInt4BinaryProtocolValue.class));
    }
    
    @Test
    void assertGetBinaryProtocolValueWithMySQLTypeInt24() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.INT24), isA(MySQLInt4BinaryProtocolValue.class));
    }
    
    @Test
    void assertGetBinaryProtocolValueWithMySQLTypeShort() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.SHORT), isA(MySQLInt2BinaryProtocolValue.class));
    }
    
    @Test
    void assertGetBinaryProtocolValueWithMySQLTypeYear() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.YEAR), isA(MySQLInt2BinaryProtocolValue.class));
    }
    
    @Test
    void assertGetBinaryProtocolValueWithMySQLTypeTiny() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.TINY), isA(MySQLInt1BinaryProtocolValue.class));
    }
    
    @Test
    void assertGetBinaryProtocolValueWithMySQLTypeDouble() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.DOUBLE), isA(MySQLDoubleBinaryProtocolValue.class));
    }
    
    @Test
    void assertGetBinaryProtocolValueWithMySQLTypeFloat() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.FLOAT), isA(MySQLFloatBinaryProtocolValue.class));
    }
    
    @Test
    void assertGetBinaryProtocolValueWithMySQLTypeDate() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.DATE), isA(MySQLDateBinaryProtocolValue.class));
    }
    
    @Test
    void assertGetBinaryProtocolValueWithMySQLTypeDatetime() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.DATETIME), isA(MySQLDateBinaryProtocolValue.class));
    }
    
    @Test
    void assertGetBinaryProtocolValueWithMySQLTypeTimestamp() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.TIMESTAMP), isA(MySQLDateBinaryProtocolValue.class));
    }
    
    @Test
    void assertGetBinaryProtocolValueWithMySQLTypeTime() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.TIME), isA(MySQLTimeBinaryProtocolValue.class));
    }
    
    @Test
    void assertGetBinaryProtocolValueWithMySQLTypeNull() {
        assertNull(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.NULL));
    }
    
    @Test
    void assertGetBinaryProtocolValueWithUnsupportedType() {
        assertThrows(IllegalArgumentException.class, () -> MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(null));
    }
}
