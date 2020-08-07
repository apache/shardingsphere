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
import com.google.common.base.Joiner;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.transaction.xa.jta.datasource.properties.XADataSourceDefinition;

import javax.sql.DataSource;
import javax.sql.XADataSource;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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
        XADataSource result = null;
        List<ShardingSphereException> exceptions = new LinkedList<>();
        for (String each : xaDataSourceDefinition.getXADriverClassName()) {
            try {
                result = loadXADataSource(each);
            } catch (final ShardingSphereException ex) {
                exceptions.add(ex);
            }
        }
        if (null == result && !exceptions.isEmpty()) {
            if (exceptions.size() > 1) {
                throw new ShardingSphereException("Failed to create [%s] XA DataSource", xaDataSourceDefinition);
            } else {
                throw exceptions.iterator().next();
            }
        }
        return result;
    }
    
    private XADataSource loadXADataSource(final String xaDataSourceClassName) {
        Class<?> xaDataSourceClass;
        try {
            xaDataSourceClass = Thread.currentThread().getContextClassLoader().loadClass(xaDataSourceClassName);
        } catch (final ClassNotFoundException ignored) {
            try {
                xaDataSourceClass = Class.forName(xaDataSourceClassName);
            } catch (final ClassNotFoundException ex) {
                throw new ShardingSphereException("Failed to load [%s]", xaDataSourceClassName);
            }
        }
        try {
            return (XADataSource) xaDataSourceClass.newInstance();
        } catch (final InstantiationException | IllegalAccessException ex) {
            throw new ShardingSphereException("Failed to instance [%s]", xaDataSourceClassName);
        }
    }
    
    private Map<String, Object> getDatabaseAccessConfiguration(final DataSource dataSource) {
        Map<String, Object> result = new HashMap<>(3, 1);
        DataSourcePropertyProvider provider = DataSourcePropertyProviderLoader.getProvider(dataSource);
        try {
            result.put("url", findGetterMethod(dataSource, provider.getURLPropertyName()).invoke(dataSource));
            result.put("user", findGetterMethod(dataSource, provider.getUsernamePropertyName()).invoke(dataSource));
            result.put("password", findGetterMethod(dataSource, provider.getPasswordPropertyName()).invoke(dataSource));
            return result;
        } catch (final ReflectiveOperationException ex) {
            throw new ShardingSphereException("Cannot swap data source type: `%s`, please provide an implementation from SPI `%s`",
                dataSource.getClass().getName(), DataSourcePropertyProvider.class.getName());
        }
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setProperties(final XADataSource xaDataSource, final Map<String, Object> databaseAccessConfiguration) {
        for (Entry<String, Object> entry : databaseAccessConfiguration.entrySet()) {
            Optional<Method> method = findSetterMethod(xaDataSource.getClass().getMethods(), entry.getKey());
            if (method.isPresent()) {
                method.get().invoke(xaDataSource, entry.getValue());
            }
        }
    }
    
    private Method findGetterMethod(final DataSource dataSource, final String propertyName) throws NoSuchMethodException {
        String getterMethodName = Joiner.on("").join(GETTER_PREFIX, CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, propertyName));
        Method method = dataSource.getClass().getMethod(getterMethodName);
        method.setAccessible(true);
        return method;
    }
    
    private Optional<Method> findSetterMethod(final Method[] methods, final String property) {
        String setterMethodName = Joiner.on("").join(SETTER_PREFIX, CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, property));
        for (Method each : methods) {
            if (each.getName().equals(setterMethodName) && 1 == each.getParameterTypes().length) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
    }
}
