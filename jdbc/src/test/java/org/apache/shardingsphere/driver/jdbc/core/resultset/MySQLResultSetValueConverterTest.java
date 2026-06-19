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

package org.apache.shardingsphere.driver.jdbc.core.resultset;

import org.apache.shardingsphere.infra.exception.kernel.data.UnsupportedDataTypeConversionException;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MySQLResultSetValueConverterTest {
    
    private final DialectResultSetValueConverter actual = TypedSPILoader.getService(DialectResultSetValueConverter.class, "MySQL");
    
    @Test
    void assertGetType() {
        assertThat(actual.getType(), is("MySQL"));
    }
    
    @Test
    void assertConvertStringToBoolean() throws SQLException {
        assertTrue((boolean) actual.convertValue("true", Boolean.class));
    }
    
    @Test
    void assertConvertSingleCharacterStringToByte() throws SQLException {
        assertThat(actual.convertValue("1", Byte.class), is((Object) Byte.valueOf((byte) '1')));
    }
    
    @Test
    void assertConvertMultiCharacterStringToByte() {
        assertThrows(UnsupportedDataTypeConversionException.class, () -> actual.convertValue("127", Byte.class));
    }
    
    @Test
    void assertConvertSingleMultiByteCharacterStringToByte() {
        assertThrows(UnsupportedDataTypeConversionException.class, () -> actual.convertValue("你", Byte.class));
    }
    
    @Test
    void assertConvertStringToShort() throws SQLException {
        assertThat(actual.convertValue("32767", Short.class), is((Object) Short.valueOf((short) 32767)));
    }
    
    @Test
    void assertConvertStringToInteger() throws SQLException {
        assertThat(actual.convertValue("123", Integer.class), is((Object) Integer.valueOf(123)));
    }
    
    @Test
    void assertConvertStringWithWhitespacesToInteger() {
        assertThrows(UnsupportedDataTypeConversionException.class, () -> actual.convertValue(" 123 ", Integer.class));
    }
    
    @Test
    void assertConvertLeadingPlusStringToInteger() {
        assertThrows(UnsupportedDataTypeConversionException.class, () -> actual.convertValue("+123", Integer.class));
    }
    
    @Test
    void assertConvertStringToLong() throws SQLException {
        assertThat(actual.convertValue("123456", Long.class), is((Object) Long.valueOf(123456L)));
    }
    
    @Test
    void assertConvertStringToFloat() throws SQLException {
        assertThat(actual.convertValue("3.14", Float.class), is((Object) Float.valueOf(3.14F)));
    }
    
    @Test
    void assertConvertStringToDouble() throws SQLException {
        assertThat(actual.convertValue("3.1415926", Double.class), is((Object) Double.valueOf(3.1415926D)));
    }
    
    @Test
    void assertConvertStringToBigDecimal() throws SQLException {
        assertThat(actual.convertValue("123.45", BigDecimal.class), is((Object) new BigDecimal("123.45")));
    }
    
    @Test
    void assertConvertLeadingPlusStringToBigDecimal() {
        assertThrows(UnsupportedDataTypeConversionException.class, () -> actual.convertValue("+123.45", BigDecimal.class));
    }
    
    @Test
    void assertConvertInvalidStringToInteger() {
        assertThrows(UnsupportedDataTypeConversionException.class, () -> actual.convertValue("abc", Integer.class));
    }
    
    @Test
    void assertConvertNumericStringOneToBoolean() throws SQLException {
        assertTrue((boolean) actual.convertValue("1", boolean.class));
        assertTrue((boolean) actual.convertValue("-1", boolean.class));
    }
    
    @Test
    void assertConvertNumericStringZeroToBoolean() throws SQLException {
        assertFalse((boolean) actual.convertValue("0", boolean.class));
    }
    
    @Test
    void assertConvertNumericStringTwoToBoolean() throws SQLException {
        assertTrue((boolean) actual.convertValue("2", boolean.class));
    }
    
    @Test
    void assertConvertFloatingStringToBoolean() throws SQLException {
        assertTrue((boolean) actual.convertValue("0.1", boolean.class));
    }
    
    @Test
    void assertConvertExponentStringToBoolean() throws SQLException {
        assertTrue((boolean) actual.convertValue("1e3", boolean.class));
    }
    
    @Test
    void assertConvertInvalidStringToBoolean() {
        assertThrows(UnsupportedDataTypeConversionException.class, () -> actual.convertValue("unknown", Boolean.class));
    }
    
    @Test
    void assertConvertEmptyStringToInt() throws SQLException {
        int actualValue = (int) actual.convertValue("", int.class);
        int expectedValue = 0;
        assertThat(actualValue, is(expectedValue));
    }
    
    @Test
    void assertConvertEmptyStringToBoolean() throws SQLException {
        boolean actualValue = (boolean) actual.convertValue("", boolean.class);
        assertFalse(actualValue);
    }
    
    @Test
    void assertConvertDecimalStringToInt() throws SQLException {
        int actualValue = (int) actual.convertValue("123.45", int.class);
        int expectedValue = 123;
        assertThat(actualValue, is(expectedValue));
    }
    
    @Test
    void assertConvertExponentStringToInt() throws SQLException {
        int actualValue = (int) actual.convertValue("1e3", int.class);
        int expectedValue = 1000;
        assertThat(actualValue, is(expectedValue));
    }
    
    @Test
    void assertConvertStringToTimestamp() throws SQLException {
        Timestamp actualValue = (Timestamp) actual.convertValue("2021-12-23 19:30:00", Timestamp.class);
        Timestamp expectedValue = Timestamp.valueOf("2021-12-23 19:30:00");
        assertThat(actualValue, is(expectedValue));
    }
}
