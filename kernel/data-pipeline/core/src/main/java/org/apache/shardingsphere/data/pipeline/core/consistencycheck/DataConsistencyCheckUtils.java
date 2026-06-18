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

package org.apache.shardingsphere.data.pipeline.core.consistencycheck;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.sql.Array;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

/**
 * Data consistency check utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class DataConsistencyCheckUtils {
    
    /**
     * Whether records equals or not.
     *
     * @param thisRecord this record
     * @param thatRecord that record
     * @param equalsBuilder equals builder
     * @return true if records equals, otherwise false
     */
    public static boolean recordsEquals(final Map<String, Object> thisRecord, final Map<String, Object> thatRecord, final EqualsBuilder equalsBuilder) {
        Iterator<Entry<String, Object>> thisRecordIterator = thisRecord.entrySet().iterator();
        Iterator<Entry<String, Object>> thatRecordIterator = thatRecord.entrySet().iterator();
        int columnIndex = 0;
        while (thisRecordIterator.hasNext() && thatRecordIterator.hasNext()) {
            ++columnIndex;
            Object thisColumnValue = thisRecordIterator.next().getValue();
            Object thatColumnValue = thatRecordIterator.next().getValue();
            if (!isMatched(equalsBuilder, thisColumnValue, thatColumnValue)) {
                log.warn("Record column value not match, columnIndex={}, value1={}, value2={}, value1.class={}, value2.class={}.", columnIndex, thisColumnValue, thatColumnValue,
                        null != thisColumnValue ? thisColumnValue.getClass().getName() : "", null == thatColumnValue ? "" : thatColumnValue.getClass().getName());
                return false;
            }
        }
        return true;
    }
    
    /**
     * Is first unique key value matched.
     *
     * @param thisRecord this record
     * @param thatRecord that record
     * @param uniqueKey unique key
     * @param equalsBuilder equals builder
     * @return true if matched, otherwise false
     */
    public static boolean isFirstUniqueKeyValueMatched(final Map<String, Object> thisRecord, final Map<String, Object> thatRecord, final String uniqueKey, final EqualsBuilder equalsBuilder) {
        if (thisRecord.isEmpty() || thatRecord.isEmpty()) {
            return false;
        }
        return isMatched(equalsBuilder, getFirstUniqueKeyValue(thisRecord, uniqueKey), getFirstUniqueKeyValue(thatRecord, uniqueKey));
    }
    
    /**
     * Whether column values are matched or not.
     *
     * @param equalsBuilder equals builder
     * @param thisColumnValue this column value
     * @param thatColumnValue that column value
     * @return true if matched, otherwise false
     */
    @SneakyThrows(SQLException.class)
    public static boolean isMatched(final EqualsBuilder equalsBuilder, final Object thisColumnValue, final Object thatColumnValue) {
        equalsBuilder.reset();
        if (thisColumnValue instanceof Number && thatColumnValue instanceof Number) {
            return isNumberEquals((Number) thisColumnValue, (Number) thatColumnValue);
        }
        if (thisColumnValue instanceof SQLXML && thatColumnValue instanceof SQLXML) {
            return ((SQLXML) thisColumnValue).getString().equals(((SQLXML) thatColumnValue).getString());
        }
        /*
         * TODO To avoid precision inconsistency issues, the current comparison of Timestamp columns across heterogeneous databases ignores `milliseconds` precision. In the future, different
         * strategies with different database types could be considered.
         */
        if (thisColumnValue instanceof Timestamp && thatColumnValue instanceof Timestamp) {
            return ((Timestamp) thisColumnValue).getTime() / 1000L * 1000L == ((Timestamp) thatColumnValue).getTime() / 1000L * 1000L;
        }
        if (thisColumnValue instanceof Array && thatColumnValue instanceof Array) {
            return Objects.deepEquals(((Array) thisColumnValue).getArray(), ((Array) thatColumnValue).getArray());
        }
        return equalsBuilder.append(thisColumnValue, thatColumnValue).isEquals();
    }
    
    private static boolean isNumberEquals(final Number one, final Number another) {
        if (isInteger(one) && isInteger(another)) {
            return one.longValue() == another.longValue();
        }
        return isBigDecimalEquals(convertToBigDecimal(one), convertToBigDecimal(another));
    }
    
    private static boolean isInteger(final Number value) {
        return value instanceof Long || value instanceof Integer || value instanceof Short || value instanceof Byte;
    }
    
    /**
     * Convert number to BigDecimal.
     *
     * @param value number
     * @return BigDecimal
     */
    public static BigDecimal convertToBigDecimal(final Number value) {
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (isInteger(value)) {
            return BigDecimal.valueOf(value.longValue());
        }
        if (value instanceof Float || value instanceof Double) {
            return BigDecimal.valueOf(value.doubleValue());
        }
        if (value instanceof BigInteger) {
            return new BigDecimal((BigInteger) value);
        }
        return new BigDecimal(value.toString());
    }
    
    /**
     * Check two BigDecimal whether equals or not.
     *
     * <p>Scale will be ignored, so ${@code 332.2} is equals to {@code 332.20}.</p>
     *
     * @param one first BigDecimal
     * @param another second BigDecimal
     * @return equals or not
     */
    public static boolean isBigDecimalEquals(final BigDecimal one, final BigDecimal another) {
        BigDecimal decimalOne;
        BigDecimal decimalTwo;
        if (one.scale() == another.scale()) {
            decimalOne = one;
            decimalTwo = another;
        } else {
            if (one.scale() > another.scale()) {
                decimalOne = one;
                decimalTwo = another.setScale(one.scale(), RoundingMode.UNNECESSARY);
            } else {
                decimalOne = one.setScale(another.scale(), RoundingMode.UNNECESSARY);
                decimalTwo = another;
            }
        }
        return 0 == decimalOne.compareTo(decimalTwo);
    }
    
    /**
     * Compare lists.
     *
     * @param thisList this list
     * @param thatList that list
     * @return true if lists equals, otherwise false
     */
    public static boolean compareLists(final @Nullable Collection<?> thisList, final @Nullable Collection<?> thatList) {
        if (null == thisList && null == thatList) {
            return true;
        }
        if (null == thisList || null == thatList) {
            return false;
        }
        if (thisList.size() != thatList.size()) {
            return false;
        }
        Iterator<?> thisIterator = thisList.iterator();
        Iterator<?> thatIterator = thatList.iterator();
        while (thisIterator.hasNext() && thatIterator.hasNext()) {
            if (!Objects.deepEquals(thisIterator.next(), thatIterator.next())) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Get first unique key value.
     *
     * @param record record
     * @param uniqueKey unique key
     * @return first unique key value
     */
    public static Object getFirstUniqueKeyValue(final Map<String, Object> record, final @Nullable String uniqueKey) {
        return record.isEmpty() || null == uniqueKey ? null : record.get(uniqueKey);
    }
}
