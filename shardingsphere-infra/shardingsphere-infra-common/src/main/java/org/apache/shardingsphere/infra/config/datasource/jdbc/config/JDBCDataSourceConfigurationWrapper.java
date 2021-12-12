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

package org.apache.shardingsphere.infra.config.datasource.jdbc.config;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.config.datasource.jdbc.config.impl.ShardingSphereJDBCDataSourceConfiguration;
import org.apache.shardingsphere.infra.config.datasource.jdbc.config.impl.StandardJDBCDataSourceConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * JDBC data source configuration wrapper.
 */
@Getter
@Setter
public final class JDBCDataSourceConfigurationWrapper {
    
    private String type;
    
    private String parameter;
    
    /**
     * Unwrap.
     *
     * @return typed data source configuration
     */
    @SneakyThrows(ReflectiveOperationException.class)
    public JDBCDataSourceConfiguration unwrap() {
        Map<String, Class<?>> classMap = DataSourceConfigurationHolder.getInstances();
        Preconditions.checkArgument(classMap.containsKey(type.toLowerCase()), "Unsupported data source type '%s'", type);
        return (JDBCDataSourceConfiguration) classMap.get(type.toLowerCase()).getConstructor(String.class).newInstance(parameter);
    }
    
    private static class DataSourceConfigurationHolder {
        
        private static final Map<String, Class<?>> INSTANCES = new HashMap<>(2, 1);
        
        static {
            INSTANCES.put(StandardJDBCDataSourceConfiguration.TYPE.toLowerCase(), StandardJDBCDataSourceConfiguration.class);
            INSTANCES.put(ShardingSphereJDBCDataSourceConfiguration.TYPE.toLowerCase(), ShardingSphereJDBCDataSourceConfiguration.class);
        }
        
        private static Map<String, Class<?>> getInstances() {
            return INSTANCES;
        }
    }
}
