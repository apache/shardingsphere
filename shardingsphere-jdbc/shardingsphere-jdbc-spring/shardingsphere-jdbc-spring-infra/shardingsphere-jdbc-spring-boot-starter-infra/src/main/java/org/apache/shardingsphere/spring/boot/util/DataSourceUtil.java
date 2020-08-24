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

package org.apache.shardingsphere.spring.boot.util;

import com.google.common.base.CaseFormat;
import com.google.common.collect.Sets;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Data source utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataSourceUtil {
    
    private static final String SET_METHOD_PREFIX = "set";
    
    private static final Collection<Class<?>> GENERAL_CLASS_TYPE;
    
    static {
        GENERAL_CLASS_TYPE = Sets.newHashSet(boolean.class, Boolean.class, int.class, Integer.class, long.class, Long.class, String.class, Collection.class);
    }
    
    /**
     * Get data source.
     *
     * @param dataSourceClassName data source class name
     * @param dataSourceProperties data source properties
     * @return data source instance
     * @throws ReflectiveOperationException reflective operation exception
     */
    public static DataSource getDataSource(final String dataSourceClassName, final Map<String, Object> dataSourceProperties) throws ReflectiveOperationException {
        DataSource result = (DataSource) Class.forName(dataSourceClassName).newInstance();
        for (Entry<String, Object> entry : dataSourceProperties.entrySet()) {
            callSetterMethod(result, getSetterMethodName(entry.getKey()), null == entry.getValue() ? null : entry.getValue().toString());
        }
        return result;
    }
    
    private static String getSetterMethodName(final String propertyName) {
        if (propertyName.contains("-")) {
            return CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL, SET_METHOD_PREFIX + "-" + propertyName);
        }
        return SET_METHOD_PREFIX + String.valueOf(propertyName.charAt(0)).toUpperCase() + propertyName.substring(1);
    }
    
    private static void callSetterMethod(final DataSource dataSource, final String methodName, final String setterValue) {
        for (Class<?> each : GENERAL_CLASS_TYPE) {
            try {
                Method method = dataSource.getClass().getMethod(methodName, each);
                if (boolean.class == each || Boolean.class == each) {
                    method.invoke(dataSource, Boolean.valueOf(setterValue));
                } else if (int.class == each || Integer.class == each) {
                    method.invoke(dataSource, Integer.parseInt(setterValue));
                } else if (long.class == each || Long.class == each) {
                    method.invoke(dataSource, Long.parseLong(setterValue));
                } else if (Collection.class == each) {
                    method.invoke(dataSource, Arrays.asList(setterValue.split(",")));
                } else {
                    method.invoke(dataSource, setterValue);
                }
                return;
            } catch (final ReflectiveOperationException ignored) {
            }
        }
    }
}
