/*
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

package com.dangdang.ddframe.rdb.sharding.executor.threadlocal;

import com.dangdang.ddframe.rdb.sharding.exception.ShardingJdbcException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 执行器执行时异常处理类.
 * 
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class ExecutorExceptionHandler {
    
    private static final ThreadLocal<Boolean> IS_EXCEPTION_THROWN = new ThreadLocal<>();
    
    /**
     * 设置是否将异常抛出.
     *
     * @param isExceptionThrown 是否将异常抛出
     */
    public static void setExceptionThrown(final boolean isExceptionThrown) {
        ExecutorExceptionHandler.IS_EXCEPTION_THROWN.set(isExceptionThrown);
    }
    
    /**
     * 获取是否将异常抛出.
     * 
     * @return 是否将异常抛出
     */
    public static boolean isExceptionThrown() {
        return null == IS_EXCEPTION_THROWN.get() ? true : IS_EXCEPTION_THROWN.get();
    }
    
    /**
     * 处理异常. 
     * 
     * @param ex 待处理的异常
     */
    public static void handleException(final Exception ex) {
        if (isExceptionThrown()) {
            throw new ShardingJdbcException(ex);
        }
        log.error("exception occur: ", ex);
    }
}
