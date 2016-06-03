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

package com.dangdang.ddframe.rdb.sharding.jdbc.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.dangdang.ddframe.rdb.sharding.exception.ShardingJdbcException;

import lombok.RequiredArgsConstructor;

/**
 * 反射调用JDBC相关方法的工具类.
 * 
 * @author gaohongtao
 */
@RequiredArgsConstructor
public final class JdbcMethodInvocation {
    
    private final Method method;
    
    private final Object[] arguments;
    
    /**
     *  调用方法.
     * 
     * @param target 目标对象
     */
    public void invoke(final Object target) {
        try {
            method.invoke(target, arguments);
        } catch (final IllegalAccessException | InvocationTargetException ex) {
            throw new ShardingJdbcException("Invoke jdbc method exception", ex);
        }
    }
}
