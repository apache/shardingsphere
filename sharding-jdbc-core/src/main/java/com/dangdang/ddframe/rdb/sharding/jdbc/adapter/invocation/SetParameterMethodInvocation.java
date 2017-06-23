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

package com.dangdang.ddframe.rdb.sharding.jdbc.adapter.invocation;

import lombok.Getter;

import java.lang.reflect.Method;

/**
 * 反射调用参数设置方法的工具类.
 * 
 * @author zhangliang
 */
public final class SetParameterMethodInvocation extends JdbcMethodInvocation {
    
    @Getter
    private final int index;
    
    @Getter
    private final Object value;
    
    public SetParameterMethodInvocation(final Method method, final Object[] arguments, final Object value) {
        super(method, arguments);
        this.index = (int) arguments[0];
        this.value = value;
    }
    
    /**
     * 设置参数值.
     * 
     * @param value 参数值
     */
    public void changeValueArgument(final Object value) {
        getArguments()[1] = value;
    }
}
