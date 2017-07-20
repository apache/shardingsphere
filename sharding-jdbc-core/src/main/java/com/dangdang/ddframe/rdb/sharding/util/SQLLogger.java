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
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * SQL日志对象.
 * 
 * @author zhangliang 
 */
@Slf4j(topic = "Sharding-JDBC-SQL")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLLogger {
    
    /**
     * 记录日志.
     * 
     * @param pattern 格式
     * @param arguments 参数列表
     */
    public static void log(final String pattern, final Object... arguments) {
        log.info(pattern, arguments);
    }
}
