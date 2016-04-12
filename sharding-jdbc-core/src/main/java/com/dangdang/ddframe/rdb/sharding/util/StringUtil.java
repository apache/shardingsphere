/**
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.util;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/**
 * 字符串工具类.
 *
 * @author zhangliang
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StringUtil {
    
    /**
     * 判断是否为boolean值.
     * 
     * @param str 待判断的字符串
     * @return 是否为boolean值
     */
    public static boolean isBooleanValue(final String str) {
        return Boolean.TRUE.toString().equalsIgnoreCase(str) || Boolean.FALSE.toString().equalsIgnoreCase(str);
    }
    
    /**
     * 判断是否为int值.
     * 
     * @param str 待判断的字符串
     * @return 是否为int值
     */
    public static boolean isIntValue(final String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (final NumberFormatException ex) {
            return false;
        }
    }
    
    /**
     * 判断是否为long值.
     *
     * @param str 待判断的字符串
     * @return 是否为long值
     */
    public static boolean isLongValue(final String str) {
        try {
            Long.parseLong(str);
            return true;
        } catch (final NumberFormatException ex) {
            return false;
        }
    }
}
