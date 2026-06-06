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
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.driver.jdbc.type.util.ResultSetUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.sql.SQLFeatureNotSupportedException;
import java.util.regex.Pattern;

/**
 * MySQL result set value converter.
 */
public final class MySQLResultSetValueConverter implements DialectResultSetValueConverter {
    
    private static final Pattern FLOATING_POINT_PATTERN = Pattern.compile("-?\\d*\\.\\d*");
    
    private static final Pattern INTEGER_PATTERN = Pattern.compile("-?\\d+");
    
    @Override
    public Object convertValue(final Object value, final Class<?> convertType) throws SQLFeatureNotSupportedException {
        if (!(value instanceof String)) {
            return ResultSetUtils.convertValue(value, convertType);
        }
        String stringValue = (String) value;
        try {
            switch (convertType.getName()) {
                case "boolean":
                case "java.lang.Boolean":
                    return stringValue.isEmpty() ? false : convertBooleanValue(stringValue, convertType);
                case "byte":
                case "java.lang.Byte":
                    return stringValue.isEmpty() ? (byte) 0 : convertByteValue(stringValue, convertType);
                case "short":
                case "java.lang.Short":
                    return stringValue.isEmpty() ? (short) 0 : convertIntegralValue(stringValue, convertType).shortValueExact();
                case "int":
                case "java.lang.Integer":
                    return stringValue.isEmpty() ? 0 : convertIntegralValue(stringValue, convertType).intValueExact();
                case "long":
                case "java.lang.Long":
                    return stringValue.isEmpty() ? 0L : convertIntegralValue(stringValue, convertType).longValueExact();
                case "double":
                case "java.lang.Double":
                    return stringValue.isEmpty() ? 0.0D : convertNumericValue(stringValue, convertType).doubleValue();
                case "float":
                case "java.lang.Float":
                    return stringValue.isEmpty() ? 0.0F : convertNumericValue(stringValue, convertType).floatValue();
                case "java.math.BigDecimal":
                    return stringValue.isEmpty() ? BigDecimal.ZERO : convertNumericValue(stringValue, convertType);
                default:
                    return ResultSetUtils.convertValue(value, convertType);
            }
        } catch (final NumberFormatException | ArithmeticException ignored) {
            throw new UnsupportedDataTypeConversionException(convertType, value);
        }
    }
    
    @Override
    public String getType() {
        return "MySQL";
    }
    
    private byte convertByteValue(final String value, final Class<?> convertType) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        if (1 != bytes.length) {
            throw new UnsupportedDataTypeConversionException(convertType, value);
        }
        return bytes[0];
    }
    
    private Boolean convertBooleanValue(final String value, final Class<?> convertType) {
        if ("Y".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value) || "T".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value)) {
            return true;
        }
        if ("N".equalsIgnoreCase(value) || "no".equalsIgnoreCase(value) || "F".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            return false;
        }
        if (value.contains("e") || value.contains("E") || FLOATING_POINT_PATTERN.matcher(value).matches()) {
            return convertDoubleToBoolean(Double.parseDouble(value));
        }
        if (INTEGER_PATTERN.matcher(value).matches()) {
            return isSignedLong(value) ? longToBoolean(Long.parseLong(value)) : convertBigIntegerToBoolean(new BigInteger(value));
        }
        throw new UnsupportedDataTypeConversionException(convertType, value);
    }
    
    private BigDecimal convertIntegralValue(final String value, final Class<?> convertType) {
        if (value.contains("e") || value.contains("E") || FLOATING_POINT_PATTERN.matcher(value).matches()) {
            return BigDecimal.valueOf(Double.parseDouble(value)).setScale(0, RoundingMode.DOWN);
        }
        if (INTEGER_PATTERN.matcher(value).matches()) {
            return convertIntegerValue(value);
        }
        throw new UnsupportedDataTypeConversionException(convertType, value);
    }
    
    private BigDecimal convertNumericValue(final String value, final Class<?> convertType) {
        if (value.contains("e") || value.contains("E") || FLOATING_POINT_PATTERN.matcher(value).matches()) {
            return BigDecimal.valueOf(Double.parseDouble(value));
        }
        if (INTEGER_PATTERN.matcher(value).matches()) {
            return convertIntegerValue(value);
        }
        throw new UnsupportedDataTypeConversionException(convertType, value);
    }
    
    private BigDecimal convertIntegerValue(final String value) {
        return isSignedLong(value) ? BigDecimal.valueOf(Long.parseLong(value)) : new BigDecimal(new BigInteger(value));
    }
    
    private boolean isSignedLong(final String value) {
        return '-' == value.charAt(0) || value.length() <= 18 && value.charAt(0) >= '0' && value.charAt(0) <= '8';
    }
    
    private boolean longToBoolean(final long value) {
        return -1L == value || value > 0L;
    }
    
    private boolean convertDoubleToBoolean(final double value) {
        return value > 0.0D || -1.0D == value;
    }
    
    private boolean convertBigIntegerToBoolean(final BigInteger value) {
        return value.signum() > 0 || BigInteger.valueOf(-1L).equals(value);
    }
}
