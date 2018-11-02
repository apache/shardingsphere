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

package io.shardingsphere.transaction.manager.xa.convert;

import lombok.SneakyThrows;

import javax.sql.DataSource;
import java.lang.reflect.Method;

/**
 * Invoke method of datasource pool through reflect.
 *
 * @author zhaojun
 */
public final class DataSourceReflector {
    
    private final DataSource dataSource;
    
    private final Class clazz;
    
    public DataSourceReflector(final DataSource dataSource) {
        this.dataSource = dataSource;
        this.clazz = dataSource.getClass();
    }
    
    /**
     * Invoke specified method by reflect.
     *
     * @param methodName method name
     * @param type result type
     * @param args parameters
     * @param <T> generic result type
     * @return invoke value
     */
    @SneakyThrows
    public <T> T invoke(final String methodName, final Class<T> type, final Object... args) {
        Method method = clazz.getDeclaredMethod(methodName);
        method.setAccessible(true);
        return (T) method.invoke(dataSource, args);
    }
}
