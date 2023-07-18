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

package org.apache.shardingsphere.transaction.xa.jta.datasource.swapper;

import com.google.common.base.CaseFormat;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.datasource.ShardingSphereStorageDataSourceWrapper;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.transaction.xa.jta.datasource.properties.XADataSourceDefinition;
import org.apache.shardingsphere.transaction.xa.jta.exception.XADataSourceInitializeException;

import javax.sql.DataSource;
import javax.sql.XADataSource;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Data source swapper.
 */
@RequiredArgsConstructor
public final class DataSourceSwapper {
    
    private static final String GETTER_PREFIX = "get";
    
    private static final String SETTER_PREFIX = "set";
    
    private final XADataSourceDefinition xaDataSourceDefinition;
    
    /**
     * Swap data source to database access configuration.
     * 
     * @param dataSource data source
     * @return XADataSource XA data source
     */
    public XADataSource swap(final DataSource dataSource) {
        XADataSource result = createXADataSource();
        setProperties(result, getDatabaseAccessConfiguration(dataSource));
        return result;
    }
    
    private XADataSource createXADataSource() {
        for (String each : xaDataSourceDefinition.getXADriverClassNames()) {
            try {
                return loadXADataSource(each);
            } catch (final ReflectiveOperationException ignored) {
            }
        }
        throw new XADataSourceInitializeException(xaDataSourceDefinition);
    }
    
    private XADataSource loadXADataSource(final String xaDataSourceClassName) throws ReflectiveOperationException {
        Class<?> xaDataSourceClass;
        try {
            xaDataSourceClass = Thread.currentThread().getContextClassLoader().loadClass(xaDataSourceClassName);
        } catch (final ClassNotFoundException ignored) {
            xaDataSourceClass = Class.forName(xaDataSourceClassName);
        }
        return (XADataSource) xaDataSourceClass.getDeclaredConstructor().newInstance();
    }
    
    private Map<String, Object> getDatabaseAccessConfiguration(final DataSource dataSource) {
        if (dataSource instanceof ShardingSphereStorageDataSourceWrapper) {
            ShardingSphereStorageDataSourceWrapper dataSourceWrapper = (ShardingSphereStorageDataSourceWrapper) dataSource;
            return getDatabaseAccessConfiguration(dataSourceWrapper, TypedSPILoader.getService(DataSourcePropertyProvider.class, dataSourceWrapper.getDataSource().getClass().getName()));
        }
        DataSourcePropertyProvider provider = TypedSPILoader.getService(DataSourcePropertyProvider.class, dataSource.getClass().getName());
        return getDatabaseAccessConfiguration(dataSource, provider);
    }
    
    private Map<String, Object> getDatabaseAccessConfiguration(final ShardingSphereStorageDataSourceWrapper dataSourceWrapper, final DataSourcePropertyProvider provider) {
        try {
            Map<String, Object> result = new HashMap<>(3, 1F);
            result.put("url", dataSourceWrapper.getUrl());
            result.put("user", findGetterMethod(dataSourceWrapper.getDataSource(), provider.getUsernamePropertyName()).invoke(dataSourceWrapper.getDataSource()));
            result.put("password", findGetterMethod(dataSourceWrapper.getDataSource(), provider.getPasswordPropertyName()).invoke(dataSourceWrapper.getDataSource()));
            return result;
        } catch (final ReflectiveOperationException ignored) {
            throw new XADataSourceInitializeException(xaDataSourceDefinition);
        }
    }
    
    private Map<String, Object> getDatabaseAccessConfiguration(final DataSource dataSource, final DataSourcePropertyProvider provider) {
        try {
            Map<String, Object> result = new HashMap<>(3, 1F);
            result.put("url", findGetterMethod(dataSource, provider.getURLPropertyName()).invoke(dataSource));
            result.put("user", findGetterMethod(dataSource, provider.getUsernamePropertyName()).invoke(dataSource));
            result.put("password", findGetterMethod(dataSource, provider.getPasswordPropertyName()).invoke(dataSource));
            return result;
        } catch (final ReflectiveOperationException ignored) {
            throw new XADataSourceInitializeException(xaDataSourceDefinition);
        }
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setProperties(final XADataSource xaDataSource, final Map<String, Object> databaseAccessConfig) {
        for (Entry<String, Object> entry : databaseAccessConfig.entrySet()) {
            Optional<Method> method = findSetterMethod(xaDataSource.getClass().getMethods(), entry.getKey());
            if (method.isPresent()) {
                method.get().invoke(xaDataSource, entry.getValue());
            }
        }
    }
    
    private Method findGetterMethod(final DataSource dataSource, final String propertyName) throws NoSuchMethodException {
        String getterMethodName = GETTER_PREFIX + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, propertyName);
        Method result = dataSource.getClass().getMethod(getterMethodName);
        result.setAccessible(true);
        return result;
    }
    
    private Optional<Method> findSetterMethod(final Method[] methods, final String property) {
        String setterMethodName = SETTER_PREFIX + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, property);
        return Arrays.stream(methods)
                .filter(each -> each.getName().equals(setterMethodName) && 1 == each.getParameterTypes().length)
                .findFirst();
    }
}
