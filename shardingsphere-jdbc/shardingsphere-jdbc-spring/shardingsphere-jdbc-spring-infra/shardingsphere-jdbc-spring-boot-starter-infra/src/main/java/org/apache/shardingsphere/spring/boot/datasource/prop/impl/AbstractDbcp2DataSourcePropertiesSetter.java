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

package org.apache.shardingsphere.spring.boot.datasource.prop.impl;

import lombok.SneakyThrows;
import org.apache.shardingsphere.spring.boot.datasource.prop.DataSourcePropertiesSetter;
import org.apache.shardingsphere.spring.boot.util.PropertyUtil;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;

/**
 * DBCP datasource properties setter.
 */
public abstract class AbstractDbcp2DataSourcePropertiesSetter implements DataSourcePropertiesSetter {
    
    /**
     * Common DBCP2 add custom connection properties.
     *
     * @param environment environment variable
     * @param prefix properties prefix
     * @param dataSourceName current database name
     * @param dataSource dataSource instance
     */
    @SneakyThrows(ReflectiveOperationException.class)
    public void propertiesSet(final Environment environment, final String prefix, final String dataSourceName, final DataSource dataSource) {
        String datasourcePropPrefix = prefix + dataSourceName.trim() + ".connection-properties";
        if (PropertyUtil.containPropertyPrefix(environment, datasourcePropPrefix)) {
            Map<?, ?> datasourceProperties = PropertyUtil.handle(environment, datasourcePropPrefix, Map.class);
            Method method = dataSource.getClass().getMethod("addConnectionProperty", String.class, String.class);
            for (final Entry<?, ?> entry : datasourceProperties.entrySet()) {
                method.invoke(dataSource, entry.getKey(), entry.getValue());
            }
        }
    }
}
