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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Array;
import java.sql.SQLException;
import java.sql.SQLXML;
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
    
    @SneakyThrows(SQLException.class)
    private static boolean isMatched(final EqualsBuilder equalsBuilder, final Object thisColumnValue, final Object thatColumnValue) {
        if (thisColumnValue instanceof SQLXML && thatColumnValue instanceof SQLXML) {
            return ((SQLXML) thisColumnValue).getString().equals(((SQLXML) thatColumnValue).getString());
        }
        if (thisColumnValue instanceof BigDecimal && thatColumnValue instanceof BigDecimal) {
            return isBigDecimalEquals((BigDecimal) thisColumnValue, (BigDecimal) thatColumnValue);
        }
        if (thisColumnValue instanceof Array && thatColumnValue instanceof Array) {
            return Objects.deepEquals(((Array) thisColumnValue).getArray(), ((Array) thatColumnValue).getArray());
        }
        return equalsBuilder.append(thisColumnValue, thatColumnValue).isEquals();
    }
    
    /**
     * Check two BigDecimal whether equals or not.
     *
     * <p>Scale will be ignored, so <code>332.2</code> is equals to <code>332.20</code>.</p>
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
        return decimalOne.equals(decimalTwo);
    }
}
