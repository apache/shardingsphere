/*
 * Copyright 1999-2015 dangdang.com.
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

package com.dangdang.ddframe.rdb.sharding.util;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

/**
 * 数字工具类.
 *
 * @author caohao
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class NumberUtil {
    
    /**
     * 将字符串四舍五入并转换为整形.
     * 
     * @param str 待转换的字符串
     * @return 四舍五入后的整形值
     */
    public static int roundHalfUp(final String str) {
        return new BigDecimal(str).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
    }
}
