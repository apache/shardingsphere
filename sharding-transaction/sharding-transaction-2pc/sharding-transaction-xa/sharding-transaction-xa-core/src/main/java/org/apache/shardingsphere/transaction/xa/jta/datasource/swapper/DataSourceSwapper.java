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
import org.apache.shardingsphere.underlying.common.config.DatabaseAccessConfiguration;
import org.apache.shardingsphere.underlying.common.exception.ShardingSphereException;

import javax.sql.DataSource;
import java.lang.reflect.Method;

/**
 * Data source swapper.
 *
 * @author zhangliang
 */
public final class DataSourceSwapper {
    
    /**
     * Swap data source to database access configuration.
     * 
     * @param dataSource data source
     * @return database access configuration
     */
    public DatabaseAccessConfiguration swap(final DataSource dataSource) {
        DataSourcePropertyProvider provider = DataSourcePropertyProviderLoader.getProvider(dataSource);
        try {
            String url = (String) findGetterMethod(dataSource, provider.getURLPropertyName()).invoke(dataSource);
            String username = (String) findGetterMethod(dataSource, provider.getUsernamePropertyName()).invoke(dataSource);
            String password = (String) findGetterMethod(dataSource, provider.getPasswordPropertyName()).invoke(dataSource);
            return new DatabaseAccessConfiguration(url, username, password);
        } catch (final ReflectiveOperationException ex) {
            throw new ShardingSphereException("Cannot swap data source type: `%s`, please provide an implementation from SPI `%s`", 
                    dataSource.getClass().getName(), DataSourcePropertyProvider.class.getName());
        }
    }
    
    private Method findGetterMethod(final DataSource dataSource, final String propertyName) throws NoSuchMethodException {
        String getterMethodName = Joiner.on("").join("get", CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, propertyName));
        Method method = dataSource.getClass().getMethod(getterMethodName);
        method.setAccessible(true);
        return method;
    }
}
