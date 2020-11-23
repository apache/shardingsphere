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

package org.apache.shardingsphere.scaling.core.config;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.scaling.core.config.rule.DataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.config.rule.ShardingSphereJDBCDataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.config.rule.StandardJDBCDataSourceConfiguration;

import java.util.Map;

/**
 * Rule configuration.
 */
@Getter
public final class RuleConfiguration {
    
    private final DataSourceConfigurationWrapper source;
    
    private final DataSourceConfigurationWrapper target;
    
    public RuleConfiguration(final DataSourceConfiguration source, final DataSourceConfiguration target) {
        this.source = new DataSourceConfigurationWrapper(source.getConfigType(), new Gson().toJsonTree(source));
        this.target = new DataSourceConfigurationWrapper(target.getConfigType(), new Gson().toJsonTree(target));
    }
    
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataSourceConfigurationWrapper {
        
        private String type;
        
        private JsonElement parameter;
        
        /**
         * Unwrap to {@code DataSourceConfiguration}.
         *
         * @return {@code DataSourceConfiguration}
         */
        public DataSourceConfiguration unwrap() {
            Map<String, Class<?>> instances = DataSourceConfigurationHolder.getInstances();
            Preconditions.checkArgument(instances.containsKey(type.toLowerCase()), "Unsupported Data Source Type:" + type);
            return (DataSourceConfiguration) new Gson().fromJson(parameter, instances.get(type.toLowerCase()));
        }
        
        private static class DataSourceConfigurationHolder {
            
            private static final Map<String, Class<?>> INSTANCES = Maps.newHashMap();
            
            static {
                INSTANCES.put(StandardJDBCDataSourceConfiguration.CONFIG_TYPE.toLowerCase(), StandardJDBCDataSourceConfiguration.class);
                INSTANCES.put(ShardingSphereJDBCDataSourceConfiguration.CONFIG_TYPE.toLowerCase(), ShardingSphereJDBCDataSourceConfiguration.class);
            }
            
            private static Map<String, Class<?>> getInstances() {
                return INSTANCES;
            }
        }
    }
}
