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

package org.apache.shardingsphere.scaling.core.config.datasource;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.util.Map;

@Getter
@Setter
public class ScalingDataSourceConfigurationWrap {
    
    private String type;
    
    private String parameter;
    
    /**
     * Unwrap.
     *
     * @return scaling data source configuration
     */
    @SneakyThrows(ReflectiveOperationException.class)
    public ScalingDataSourceConfiguration unwrap() {
        Map<String, Class<?>> classMap = DataSourceConfigurationHolder.getInstances();
        Preconditions.checkArgument(classMap.containsKey(type.toLowerCase()), String.format("Unsupported data source type '%s'", type));
        return (ScalingDataSourceConfiguration) classMap.get(type.toLowerCase()).getConstructor(String.class).newInstance(parameter);
    }
    
    private static class DataSourceConfigurationHolder {
        
        private static final Map<String, Class<?>> INSTANCES = Maps.newHashMap();
        
        static {
            INSTANCES.put(StandardJDBCDataSourceConfiguration.TYPE.toLowerCase(), StandardJDBCDataSourceConfiguration.class);
            INSTANCES.put(ShardingSphereJDBCDataSourceConfiguration.TYPE.toLowerCase(), ShardingSphereJDBCDataSourceConfiguration.class);
        }
        
        private static Map<String, Class<?>> getInstances() {
            return INSTANCES;
        }
    }
}
