/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.execute.protocol;

import io.shardingsphere.shardingproxy.transport.mysql.constant.ColumnType;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

public final class BinaryProtocolValueFactoryTest {
    
    @Test
    public void assertGetBinaryProtocolValueWithMySQLTypeString() {
        assertThat(BinaryProtocolValueFactory.getBinaryProtocolValue(ColumnType.MYSQL_TYPE_STRING), instanceOf(StringLenencBinaryProtocolValue.class));
    }
    
    @Test
    public void assertGetBinaryProtocolValueWithMySQLTypeVarchar() {
        assertThat(BinaryProtocolValueFactory.getBinaryProtocolValue(ColumnType.MYSQL_TYPE_VARCHAR), instanceOf(StringLenencBinaryProtocolValue.class));
    }
    
    @Test
    public void assertGetBinaryProtocolValueWithMySQLTypeVarString() {
        assertThat(BinaryProtocolValueFactory.getBinaryProtocolValue(ColumnType.MYSQL_TYPE_VAR_STRING), instanceOf(StringLenencBinaryProtocolValue.class));
    }
    
    @Test
    public void assertGetBinaryProtocolValueWithMySQLTypeEnum() {
        assertThat(BinaryProtocolValueFactory.getBinaryProtocolValue(ColumnType.MYSQL_TYPE_ENUM), instanceOf(StringLenencBinaryProtocolValue.class));
    }
    
    @Test
    public void assertGetBinaryProtocolValueWithMySQLTypeSet() {
        assertThat(BinaryProtocolValueFactory.getBinaryProtocolValue(ColumnType.MYSQL_TYPE_SET), instanceOf(StringLenencBinaryProtocolValue.class));
    }
    
    @Test
    public void assertGetBinaryProtocolValueWithMySQLTypeLongBlob() {
        assertThat(BinaryProtocolValueFactory.getBinaryProtocolValue(ColumnType.MYSQL_TYPE_LONG_BLOB), instanceOf(StringLenencBinaryProtocolValue.class));
    }
    
    @Test
    public void assertGetBinaryProtocolValueWithMySQLTypeMediumBlob() {
        assertThat(BinaryProtocolValueFactory.getBinaryProtocolValue(ColumnType.MYSQL_TYPE_MEDIUM_BLOB), instanceOf(StringLenencBinaryProtocolValue.class));
    }
    
    @Test
    public void assertGetBinaryProtocolValueWithMySQLTypeBlob() {
        assertThat(BinaryProtocolValueFactory.getBinaryProtocolValue(ColumnType.MYSQL_TYPE_BLOB), instanceOf(StringLenencBinaryProtocolValue.class));
    }
    
    @Test
    public void assertGetBinaryProtocolValueWithMySQLTypeTinyBlob() {
        assertThat(BinaryProtocolValueFactory.getBinaryProtocolValue(ColumnType.MYSQL_TYPE_TINY_BLOB), instanceOf(StringLenencBinaryProtocolValue.class));
    }
    
    @Test
    public void assertGetBinaryProtocolValueWithMySQLTypeGeometry() {
        assertThat(BinaryProtocolValueFactory.getBinaryProtocolValue(ColumnType.MYSQL_TYPE_GEOMETRY), instanceOf(StringLenencBinaryProtocolValue.class));
    }
    
    @Test
    public void assertGetBinaryProtocolValueWithMySQLTypeBit() {
        assertThat(BinaryProtocolValueFactory.getBinaryProtocolValue(ColumnType.MYSQL_TYPE_BIT), instanceOf(StringLenencBinaryProtocolValue.class));
    }
    
    @Test
    public void assertGetBinaryProtocolValueWithMySQLTypeDecimal() {
        assertThat(BinaryProtocolValueFactory.getBinaryProtocolValue(ColumnType.MYSQL_TYPE_DECIMAL), instanceOf(StringLenencBinaryProtocolValue.class));
    }
    
    @Test
    public void assertGetBinaryProtocolValueWithMySQLTypeNewDecimal() {
        assertThat(BinaryProtocolValueFactory.getBinaryProtocolValue(ColumnType.MYSQL_TYPE_NEWDECIMAL), instanceOf(StringLenencBinaryProtocolValue.class));
    }
    
    @Test
    public void assertGetBinaryProtocolValueWithMySQLTypeLongLong() {
        assertThat(BinaryProtocolValueFactory.getBinaryProtocolValue(ColumnType.MYSQL_TYPE_LONGLONG), instanceOf(Int8BinaryProtocolValue.class));
    }
    
    @Test
    public void assertGetBinaryProtocolValueWithMySQLTypeLong() {
        assertThat(BinaryProtocolValueFactory.getBinaryProtocolValue(ColumnType.MYSQL_TYPE_LONG), instanceOf(Int4BinaryProtocolValue.class));
    }
    
    @Test
    public void assertGetBinaryProtocolValueWithMySQLTypeInt24() {
        assertThat(BinaryProtocolValueFactory.getBinaryProtocolValue(ColumnType.MYSQL_TYPE_INT24), instanceOf(Int4BinaryProtocolValue.class));
    }
    
    @Test
    public void assertGetBinaryProtocolValueWithMySQLTypeShort() {
        assertThat(BinaryProtocolValueFactory.getBinaryProtocolValue(ColumnType.MYSQL_TYPE_SHORT), instanceOf(Int2BinaryProtocolValue.class));
    }
    
    @Test
    public void assertGetBinaryProtocolValueWithMySQLTypeYear() {
        assertThat(BinaryProtocolValueFactory.getBinaryProtocolValue(ColumnType.MYSQL_TYPE_YEAR), instanceOf(Int2BinaryProtocolValue.class));
    }
    
    @Test
    public void assertGetBinaryProtocolValueWithMySQLTypeTiny() {
        assertThat(BinaryProtocolValueFactory.getBinaryProtocolValue(ColumnType.MYSQL_TYPE_TINY), instanceOf(Int1BinaryProtocolValue.class));
    }
    
    @Test
    public void assertGetBinaryProtocolValueWithMySQLTypeDouble() {
        assertThat(BinaryProtocolValueFactory.getBinaryProtocolValue(ColumnType.MYSQL_TYPE_DOUBLE), instanceOf(DoubleBinaryProtocolValue.class));
    }
    
    @Test
    public void assertGetBinaryProtocolValueWithMySQLTypeFloat() {
        assertThat(BinaryProtocolValueFactory.getBinaryProtocolValue(ColumnType.MYSQL_TYPE_FLOAT), instanceOf(FloatBinaryProtocolValue.class));
    }
    
    @Test
    public void assertGetBinaryProtocolValueWithMySQLTypeDate() {
        assertThat(BinaryProtocolValueFactory.getBinaryProtocolValue(ColumnType.MYSQL_TYPE_DATE), instanceOf(DateBinaryProtocolValue.class));
    }
    
    @Test
    public void assertGetBinaryProtocolValueWithMySQLTypeDatetime() {
        assertThat(BinaryProtocolValueFactory.getBinaryProtocolValue(ColumnType.MYSQL_TYPE_DATETIME), instanceOf(DateBinaryProtocolValue.class));
    }
    
    @Test
    public void assertGetBinaryProtocolValueWithMySQLTypeTimestamp() {
        assertThat(BinaryProtocolValueFactory.getBinaryProtocolValue(ColumnType.MYSQL_TYPE_TIMESTAMP), instanceOf(DateBinaryProtocolValue.class));
    }
    
    @Test
    public void assertGetBinaryProtocolValueWithMySQLTypeTime() {
        assertThat(BinaryProtocolValueFactory.getBinaryProtocolValue(ColumnType.MYSQL_TYPE_TIME), instanceOf(TimeBinaryProtocolValue.class));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertGetBinaryProtocolValueWithUnsupportedType() {
        BinaryProtocolValueFactory.getBinaryProtocolValue(ColumnType.MYSQL_TYPE_NULL);
    }
}
