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

import com.dangdang.ddframe.rdb.sharding.exception.ShardingJdbcException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.AbstractList;
import java.util.ArrayList;

/**
 * 参数列表.
 * 对外提供参数值统一访问方式
 * 
 * @author gaohongtao
 */
@RequiredArgsConstructor
public class ParameterList extends AbstractList<Object> {
    
    @Getter
    private final ArrayList<JdbcMethodInvocation> jdbcMethodInvocations = new ArrayList<>();
    
    private final Class<?> targetClass;
    
    /**
     * 使用索引记录方法调用.
     * 
     * @param index 索引
     * @param methodName 方法名称
     * @param argumentTypes 参数类型
     * @param arguments 参数
     */
    public final void recordMethodInvocation(final int index, final String methodName, final Class<?>[] argumentTypes, final Object[] arguments) {
        jdbcMethodInvocations.ensureCapacity(index);
        int max = jdbcMethodInvocations.size();
        while (max++ <= index - 1) {
            jdbcMethodInvocations.add(null);
        }
        try {
            jdbcMethodInvocations.set(index - 1, new JdbcMethodInvocation(targetClass.getMethod(methodName, argumentTypes), arguments));
        } catch (final NoSuchMethodException ex) {
            throw new ShardingJdbcException(ex);
        }
    }
    
    /**
     * 回放记录的方法调用.
     *
     * @param target 目标对象
     */
    public final void replayMethodsInvocation(final Object target) {
        for (JdbcMethodInvocation each : jdbcMethodInvocations) {
            each.invoke(target);
        }
    }
    
    @Override
    public boolean add(final Object o) {
        int index = jdbcMethodInvocations.size() + 1;
        recordMethodInvocation(index, "setObject", new Class[]{int.class, Object.class}, new Object[]{index, o});
        return true;
    }
    
    /**
     * 根据索引设置列表中的值.
     * 
     * @param index 索引
     * @param element 元素值
     * @return 原有元素值
     */
    @Override
    public Object set(final int index, final Object element) {
        Object origin = jdbcMethodInvocations.get(index).getArguments()[1];
        jdbcMethodInvocations.get(index).getArguments()[1] = element;
        return origin;
    }
    
    /**
     * 获取索引位置的调用对象.
     *
     * @param index 索引
     * @return 调用对象
     */
    @Override
    public Object get(final int index) {
        JdbcMethodInvocation invocation = jdbcMethodInvocations.get(index);
        if (null == invocation) {
            return null;
        }
        if ("setNull".equals(invocation.getMethod().getName())) {
            return null;
        }
        return jdbcMethodInvocations.get(index).getArguments()[1];
    }
    
    /**
     * 列表中元素的数量.
     * 
     * @return 列表中元素数量
     */
    @Override
    public int size() {
        return jdbcMethodInvocations.size();
    }
    
    /**
     * 清空容器.
     */
    @Override
    public void clear() {
        jdbcMethodInvocations.clear();
    }
}
