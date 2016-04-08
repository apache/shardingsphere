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

package com.dangdang.ddframe.rdb.sharding.merger.common;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.SQLException;

import com.dangdang.ddframe.rdb.sharding.jdbc.AbstractShardingResultSet;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * 结果归并动态代理抽象类.
 * 
 * @author zhangliang
 * 
 * @param <T> 结果集类型
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractMergerInvokeHandler<T extends AbstractShardingResultSet> implements InvocationHandler {
    
    private final T resultSet;
    
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        if (!isGetDataMethod(method, args)) {
            return method.invoke(resultSet, args);
        }
        return doMerge(resultSet, method, new ResultSetQueryIndex(args[0]));
    }
    
    private boolean isGetDataMethod(final Method method, final Object[] args) {
        return method.getName().startsWith("get") && null != args && 1 == args.length;
    }
    
    protected abstract Object doMerge(T resultSet, Method method, ResultSetQueryIndex resultSetQueryIndex) throws ReflectiveOperationException, SQLException;
    
    protected Object invokeOriginal(final Method method, final ResultSetQueryIndex resultSetQueryIndex) throws ReflectiveOperationException {
        return method.invoke(resultSet, resultSetQueryIndex.getRawQueryIndex());
    }
}
