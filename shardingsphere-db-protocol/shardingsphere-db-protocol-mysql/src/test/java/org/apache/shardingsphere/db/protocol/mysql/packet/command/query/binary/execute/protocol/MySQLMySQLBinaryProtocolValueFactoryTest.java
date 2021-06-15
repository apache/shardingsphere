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
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public final class MySQLMySQLBinaryProtocolValueFactoryTest {
    
    @Test
    public void assertGetBinaryProtocolValueWithMySQLTypeString() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.MYSQL_TYPE_STRING), instanceOf(MySQLStringLenencBinaryProtocolValue.class));
    }
    
    @Test
    public void assertGetBinaryProtocolValueWithMySQLTypeVarchar() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.MYSQL_TYPE_VARCHAR), instanceOf(MySQLStringLenencBinaryProtocolValue.class));
    }
    
    @Test
    public void assertGetBinaryProtocolValueWithMySQLTypeVarString() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.MYSQL_TYPE_VAR_STRING), instanceOf(MySQLStringLenencBinaryProtocolValue.class));
    }
    
    @Test
    public void assertGetBinaryProtocolValueWithMySQLTypeEnum() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.MYSQL_TYPE_ENUM), instanceOf(MySQLStringLenencBinaryProtocolValue.class));
    }
    
    @Test
    public void assertGetBinaryProtocolValueWithMySQLTypeSet() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.MYSQL_TYPE_SET), instanceOf(MySQLStringLenencBinaryProtocolValue.class));
    }
    
    @Test
    public void assertGetBinaryProtocolValueWithMySQLTypeLongBlob() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.MYSQL_TYPE_LONG_BLOB), instanceOf(MySQLStringLenencBinaryProtocolValue.class));
    }
    
    @Test
    public void assertGetBinaryProtocolValueWithMySQLTypeMediumBlob() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.MYSQL_TYPE_MEDIUM_BLOB), instanceOf(MySQLStringLenencBinaryProtocolValue.class));
    }
    
    @Test
    public void assertGetBinaryProtocolValueWithMySQLTypeBlob() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.MYSQL_TYPE_BLOB), instanceOf(MySQLStringLenencBinaryProtocolValue.class));
    }
    
    @Test
    public void assertGetBinaryProtocolValueWithMySQLTypeTinyBlob() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.MYSQL_TYPE_TINY_BLOB), instanceOf(MySQLStringLenencBinaryProtocolValue.class));
    }
    
    @Test
    public void assertGetBinaryProtocolValueWithMySQLTypeGeometry() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.MYSQL_TYPE_GEOMETRY), instanceOf(MySQLStringLenencBinaryProtocolValue.class));
    }
    
    @Test
    public void assertGetBinaryProtocolValueWithMySQLTypeBit() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.MYSQL_TYPE_BIT), instanceOf(MySQLStringLenencBinaryProtocolValue.class));
    }
    
    @Test
    public void assertGetBinaryProtocolValueWithMySQLTypeDecimal() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.MYSQL_TYPE_DECIMAL), instanceOf(MySQLStringLenencBinaryProtocolValue.class));
    }
    
    @Test
    public void assertGetBinaryProtocolValueWithMySQLTypeNewDecimal() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.MYSQL_TYPE_NEWDECIMAL), instanceOf(MySQLStringLenencBinaryProtocolValue.class));
    }
    
    @Test
    public void assertGetBinaryProtocolValueWithMySQLTypeLongLong() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.MYSQL_TYPE_LONGLONG), instanceOf(MySQLInt8BinaryProtocolValue.class));
    }
    
    @Test
    public void assertGetBinaryProtocolValueWithMySQLTypeLong() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.MYSQL_TYPE_LONG), instanceOf(MySQLInt4BinaryProtocolValue.class));
    }
    
    @Test
    public void assertGetBinaryProtocolValueWithMySQLTypeInt24() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.MYSQL_TYPE_INT24), instanceOf(MySQLInt4BinaryProtocolValue.class));
    }
    
    @Test
    public void assertGetBinaryProtocolValueWithMySQLTypeShort() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.MYSQL_TYPE_SHORT), instanceOf(MySQLInt2BinaryProtocolValue.class));
    }
    
    @Test
    public void assertGetBinaryProtocolValueWithMySQLTypeYear() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.MYSQL_TYPE_YEAR), instanceOf(MySQLInt2BinaryProtocolValue.class));
    }
    
    @Test
    public void assertGetBinaryProtocolValueWithMySQLTypeTiny() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.MYSQL_TYPE_TINY), instanceOf(MySQLInt1BinaryProtocolValue.class));
    }
    
    @Test
    public void assertGetBinaryProtocolValueWithMySQLTypeDouble() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.MYSQL_TYPE_DOUBLE), instanceOf(MySQLDoubleBinaryProtocolValue.class));
    }
    
    @Test
    public void assertGetBinaryProtocolValueWithMySQLTypeFloat() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.MYSQL_TYPE_FLOAT), instanceOf(MySQLFloatBinaryProtocolValue.class));
    }
    
    @Test
    public void assertGetBinaryProtocolValueWithMySQLTypeDate() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.MYSQL_TYPE_DATE), instanceOf(MySQLDateBinaryProtocolValue.class));
    }
    
    @Test
    public void assertGetBinaryProtocolValueWithMySQLTypeDatetime() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.MYSQL_TYPE_DATETIME), instanceOf(MySQLDateBinaryProtocolValue.class));
    }
    
    @Test
    public void assertGetBinaryProtocolValueWithMySQLTypeTimestamp() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.MYSQL_TYPE_TIMESTAMP), instanceOf(MySQLDateBinaryProtocolValue.class));
    }
    
    @Test
    public void assertGetBinaryProtocolValueWithMySQLTypeTime() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.MYSQL_TYPE_TIME), instanceOf(MySQLTimeBinaryProtocolValue.class));
    }
    
    @Test
    public void assertGetBinaryProtocolValueWithMySQLTypeNull() {
        assertThat(MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(MySQLBinaryColumnType.MYSQL_TYPE_NULL), is(nullValue()));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertGetBinaryProtocolValueWithUnsupportedType() {
        MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(null);
    }
}
