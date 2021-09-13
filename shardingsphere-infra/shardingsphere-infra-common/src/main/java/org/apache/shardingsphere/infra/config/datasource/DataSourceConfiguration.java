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

package org.apache.shardingsphere.infra.config.datasource;

import com.google.common.base.Objects;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Data source configuration.
 */
@RequiredArgsConstructor
@Getter
public final class DataSourceConfiguration {
    
    public static final String CUSTOM_POOL_PROPS_KEY = "customPoolProps";
    
    private final String dataSourceClassName;
    
    private final Map<String, Object> props = new LinkedHashMap<>();
    
    private final Properties customPoolProps = new Properties();
    
    /**
     * Add property synonym to shared configuration.
     *
     * @param originalName original key for data source configuration property
     * @param synonym property synonym for configuration
     */
    public void addPropertySynonym(final String originalName, final String synonym) {
        if (props.containsKey(originalName)) {
            props.put(synonym, props.get(originalName));
        }
        // TODO fixes by #6709
        if (props.containsKey(synonym)) {
            props.put(originalName, props.get(synonym));
        }
    }
    
    /**
     * Get all props.
     * 
     * @return map of all props
     */
    public Map<String, Object> getAllProps() {
        Map<String, Object> result = new HashMap<>(props);
        result.putAll((Map) customPoolProps);
        return result;
    }
    
    @Override
    public boolean equals(final Object obj) {
        return this == obj || null != obj && getClass() == obj.getClass() && equalsByProperties((DataSourceConfiguration) obj);
    }
    
    private boolean equalsByProperties(final DataSourceConfiguration dataSourceConfig) {
        if (!dataSourceClassName.equals(dataSourceConfig.dataSourceClassName)) {
            return false;
        }
        for (Entry<String, Object> entry : props.entrySet()) {
            if (!dataSourceConfig.props.containsKey(entry.getKey())) {
                continue;
            }
            if (!String.valueOf(entry.getValue()).equals(String.valueOf(dataSourceConfig.props.get(entry.getKey())))) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Entry<String, Object> entry : props.entrySet()) {
            stringBuilder.append(entry.getKey()).append(entry.getValue());
        }
        return Objects.hashCode(dataSourceClassName, stringBuilder.toString());
    }
}
