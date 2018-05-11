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

package io.shardingsphere.core.util;

import io.shardingsphere.core.exception.ShardingException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Number utility class.
 *
 * @author caohao
 * @author zhangliang
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class NumberUtil {
    
    /**
     * Round half up.
     *
     * @param obj object to be converted
     * @return rounded half up number
     */
    public static int roundHalfUp(final Object obj) {
        if (obj instanceof Short) {
            return (short) obj;
        }
        if (obj instanceof Integer) {
            return (int) obj;
        }
        if (obj instanceof Long) {
            return ((Long) obj).intValue();
        }
        if (obj instanceof Double) {
            return new BigDecimal((double) obj).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
        }
        if (obj instanceof Float) {
            return new BigDecimal((float) obj).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
        }
        if (obj instanceof String) {
            return new BigDecimal((String) obj).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
        }
        throw new ShardingException("Invalid value to transfer: %s", obj);
    }
    
    /**
     * Get exactly number value and type.
     * 
     * @param value string to be converted
     * @param radix radix
     * @return exactly number value and type
     */
    public static Number getExactlyNumber(final String value, final int radix) {
        BigInteger result = new BigInteger(value, radix);
        if (result.compareTo(new BigInteger(String.valueOf(Integer.MIN_VALUE))) >= 0 && result.compareTo(new BigInteger(String.valueOf(Integer.MAX_VALUE))) <= 0) {
            return result.intValue();
        }
        if (result.compareTo(new BigInteger(String.valueOf(Long.MIN_VALUE))) >= 0 && result.compareTo(new BigInteger(String.valueOf(Long.MAX_VALUE))) <= 0) {
            return result.longValue();
        }
        return result;
    }
}
