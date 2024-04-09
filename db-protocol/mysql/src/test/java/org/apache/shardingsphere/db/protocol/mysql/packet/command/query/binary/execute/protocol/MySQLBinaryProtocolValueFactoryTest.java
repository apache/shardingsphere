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

package org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.execute.protocol;

import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLBinaryColumnType;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MySQLBinaryProtocolValueFactoryTest {
    
    @Test
    void assertGetBinaryProtocolValueWithMySQLTypeString() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.STRING), instanceOf(MySQLByteLenencBinaryProtocolValue.class));
    }
    
    @Test
    void assertGetBinaryProtocolValueWithMySQLTypeVarchar() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.VARCHAR), instanceOf(MySQLStringLenencBinaryProtocolValue.class));
    }
    
    @Test
    void assertGetBinaryProtocolValueWithMySQLTypeVarString() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.VAR_STRING), instanceOf(MySQLStringLenencBinaryProtocolValue.class));
    }
    
    @Test
    void assertGetBinaryProtocolValueWithMySQLTypeEnum() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.ENUM), instanceOf(MySQLStringLenencBinaryProtocolValue.class));
    }
    
    @Test
    void assertGetBinaryProtocolValueWithMySQLTypeSet() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.SET), instanceOf(MySQLStringLenencBinaryProtocolValue.class));
    }
    
    @Test
    void assertGetBinaryProtocolValueWithMySQLTypeLongBlob() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.LONG_BLOB), instanceOf(MySQLByteLenencBinaryProtocolValue.class));
    }
    
    @Test
    void assertGetBinaryProtocolValueWithMySQLTypeMediumBlob() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.MEDIUM_BLOB), instanceOf(MySQLByteLenencBinaryProtocolValue.class));
    }
    
    @Test
    void assertGetBinaryProtocolValueWithMySQLTypeBlob() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.BLOB), instanceOf(MySQLByteLenencBinaryProtocolValue.class));
    }
    
    @Test
    void assertGetBinaryProtocolValueWithMySQLTypeTinyBlob() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.TINY_BLOB), instanceOf(MySQLByteLenencBinaryProtocolValue.class));
    }
    
    @Test
    void assertGetBinaryProtocolValueWithMySQLTypeGeometry() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.GEOMETRY), instanceOf(MySQLStringLenencBinaryProtocolValue.class));
    }
    
    @Test
    void assertGetBinaryProtocolValueWithMySQLTypeBit() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.BIT), instanceOf(MySQLStringLenencBinaryProtocolValue.class));
    }
    
    @Test
    void assertGetBinaryProtocolValueWithMySQLTypeDecimal() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.DECIMAL), instanceOf(MySQLStringLenencBinaryProtocolValue.class));
    }
    
    @Test
    void assertGetBinaryProtocolValueWithMySQLTypeNewDecimal() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.NEWDECIMAL), instanceOf(MySQLStringLenencBinaryProtocolValue.class));
    }
    
    @Test
    void assertGetBinaryProtocolValueWithMySQLTypeLongLong() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.LONGLONG), instanceOf(MySQLInt8BinaryProtocolValue.class));
    }
    
    @Test
    void assertGetBinaryProtocolValueWithMySQLTypeLong() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.LONG), instanceOf(MySQLInt4BinaryProtocolValue.class));
    }
    
    @Test
    void assertGetBinaryProtocolValueWithMySQLTypeInt24() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.INT24), instanceOf(MySQLInt4BinaryProtocolValue.class));
    }
    
    @Test
    void assertGetBinaryProtocolValueWithMySQLTypeShort() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.SHORT), instanceOf(MySQLInt2BinaryProtocolValue.class));
    }
    
    @Test
    void assertGetBinaryProtocolValueWithMySQLTypeYear() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.YEAR), instanceOf(MySQLInt2BinaryProtocolValue.class));
    }
    
    @Test
    void assertGetBinaryProtocolValueWithMySQLTypeTiny() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.TINY), instanceOf(MySQLInt1BinaryProtocolValue.class));
    }
    
    @Test
    void assertGetBinaryProtocolValueWithMySQLTypeDouble() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.DOUBLE), instanceOf(MySQLDoubleBinaryProtocolValue.class));
    }
    
    @Test
    void assertGetBinaryProtocolValueWithMySQLTypeFloat() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.FLOAT), instanceOf(MySQLFloatBinaryProtocolValue.class));
    }
    
    @Test
    void assertGetBinaryProtocolValueWithMySQLTypeDate() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.DATE), instanceOf(MySQLDateBinaryProtocolValue.class));
    }
    
    @Test
    void assertGetBinaryProtocolValueWithMySQLTypeDatetime() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.DATETIME), instanceOf(MySQLDateBinaryProtocolValue.class));
    }
    
    @Test
    void assertGetBinaryProtocolValueWithMySQLTypeTimestamp() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.TIMESTAMP), instanceOf(MySQLDateBinaryProtocolValue.class));
    }
    
    @Test
    void assertGetBinaryProtocolValueWithMySQLTypeTime() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.TIME), instanceOf(MySQLTimeBinaryProtocolValue.class));
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
