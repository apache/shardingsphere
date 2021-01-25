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
import java.util.Properties;

/**
 * Hikari datasource properties setter.
 */
public final class HikariDataSourcePropertiesSetter implements DataSourcePropertiesSetter {
    
    @Override
    @SneakyThrows(ReflectiveOperationException.class)
    public void propertiesSet(final Environment environment, final String prefix, final String dataSourceName, final DataSource dataSource) {
        Properties props = new Properties();
        String dataSourcePropKey = prefix + dataSourceName.trim() + ".data-source-properties";
        if (PropertyUtil.containPropertyPrefix(environment, dataSourcePropKey)) {
            Map<?, ?> datasourceProperties = PropertyUtil.handle(environment, dataSourcePropKey, Map.class);
            props.putAll(datasourceProperties);
            Method method = dataSource.getClass().getMethod("setDataSourceProperties", Properties.class);
            method.invoke(dataSource, props);
        }
    }
    
    @Override
    public String getType() {
        return "com.zaxxer.hikari.HikariDataSource";
    }
}
