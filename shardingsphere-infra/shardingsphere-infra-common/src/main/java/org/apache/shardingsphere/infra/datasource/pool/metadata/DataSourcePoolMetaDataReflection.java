/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.infra.datasource.pool.metadata;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;

/**
 * Data source pool meta data reflection.
 */
@RequiredArgsConstructor
public final class DataSourcePoolMetaDataReflection {
    
    private final DataSource targetDataSource;
    
    /**
     * Get JDBC URL.
     *
     * @return got JDBC URL
     */
    public String getJdbcUrl() {
        return getFieldValue("jdbcUrl", "url");
    }
    
    /**
     * Get username.
     * 
     * @return got username
     */
    public String getUsername() {
        return getFieldValue("username", "user");
    }
    
    /**
     * Get password.
     *
     * @return got password
     */
    public String getPassword() {
        return getFieldValue("password");
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private <T> T getFieldValue(final String... fieldNames) {
        for (String each : fieldNames) {
            Optional<T> result = findFieldValue(each);
            if (result.isPresent()) {
                return result.get();
            }
        }
        throw new ReflectiveOperationException(String.format("Can not find field names `%s`", Arrays.asList(fieldNames)));
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    private <T> Optional<T> findFieldValue(final String fieldName) {
        Class<?> dataSourceClass = targetDataSource.getClass();
        Field field = null;
        boolean isFound = false;
        while (!isFound) {
            try {
                field = dataSourceClass.getDeclaredField(fieldName);
                isFound = true;
            } catch (final ReflectiveOperationException ignored) {
                dataSourceClass = dataSourceClass.getSuperclass();
                if (Object.class == dataSourceClass) {
                    return Optional.empty();
                }
            }
        }
        field.setAccessible(true);
        return Optional.of((T) field.get(targetDataSource));
    }
}
