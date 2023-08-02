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

package org.apache.shardingsphere.infra.datasource.props;

import com.google.common.base.Objects;
import lombok.Getter;
import org.apache.shardingsphere.infra.datasource.pool.metadata.DataSourcePoolMetaData;
import org.apache.shardingsphere.infra.datasource.props.custom.CustomDataSourceProperties;
import org.apache.shardingsphere.infra.datasource.props.synonym.ConnectionPropertySynonyms;
import org.apache.shardingsphere.infra.datasource.props.synonym.PoolPropertySynonyms;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Data source properties.
 */
@Getter
public final class DataSourceProperties {
    
    private final String dataSourceClassName;
    
    private final ConnectionPropertySynonyms connectionPropertySynonyms;
    
    private final PoolPropertySynonyms poolPropertySynonyms;
    
    private final CustomDataSourceProperties customDataSourceProperties;
    
    public DataSourceProperties(final String dataSourceClassName, final Map<String, Object> props) {
        this.dataSourceClassName = dataSourceClassName;
        Optional<DataSourcePoolMetaData> poolMetaData = TypedSPILoader.findService(DataSourcePoolMetaData.class, dataSourceClassName);
        Map<String, String> propertySynonyms = poolMetaData.isPresent() ? poolMetaData.get().getPropertySynonyms() : Collections.emptyMap();
        connectionPropertySynonyms = new ConnectionPropertySynonyms(props, propertySynonyms);
        poolPropertySynonyms = new PoolPropertySynonyms(props, propertySynonyms);
        customDataSourceProperties = new CustomDataSourceProperties(
                props, getStandardPropertyKeys(), poolMetaData.isPresent() ? poolMetaData.get().getTransientFieldNames() : Collections.emptyList(), propertySynonyms);
    }
    
    private Collection<String> getStandardPropertyKeys() {
        Collection<String> result = new LinkedList<>(connectionPropertySynonyms.getStandardPropertyKeys());
        result.addAll(poolPropertySynonyms.getStandardPropertyKeys());
        return result;
    }
    
    /**
     * Get all standard properties.
     * 
     * @return all standard properties
     */
    public Map<String, Object> getAllStandardProperties() {
        Map<String, Object> result = new LinkedHashMap<>(
                connectionPropertySynonyms.getStandardProperties().size() + poolPropertySynonyms.getStandardProperties().size() + customDataSourceProperties.getProperties().size(), 1F);
        result.putAll(connectionPropertySynonyms.getStandardProperties());
        result.putAll(poolPropertySynonyms.getStandardProperties());
        result.putAll(customDataSourceProperties.getProperties());
        return result;
    }
    
    /**
     * Get all local properties.
     *
     * @return all local properties
     */
    public Map<String, Object> getAllLocalProperties() {
        Map<String, Object> result = new LinkedHashMap<>(
                connectionPropertySynonyms.getLocalProperties().size() + poolPropertySynonyms.getLocalProperties().size() + customDataSourceProperties.getProperties().size(), 1F);
        result.putAll(connectionPropertySynonyms.getLocalProperties());
        result.putAll(poolPropertySynonyms.getLocalProperties());
        result.putAll(customDataSourceProperties.getProperties());
        return result;
    }
    
    @Override
    public boolean equals(final Object obj) {
        return this == obj || null != obj && getClass() == obj.getClass() && equalsByProperties((DataSourceProperties) obj);
    }
    
    private boolean equalsByProperties(final DataSourceProperties dataSourceProps) {
        if (!dataSourceClassName.equals(dataSourceProps.dataSourceClassName)) {
            return false;
        }
        for (Entry<String, Object> entry : getAllLocalProperties().entrySet()) {
            if (!dataSourceProps.getAllLocalProperties().containsKey(entry.getKey())) {
                continue;
            }
            if (entry.getValue() instanceof Map) {
                return entry.getValue().equals(dataSourceProps.getAllLocalProperties().get(entry.getKey()));
            }
            if (!String.valueOf(entry.getValue()).equals(String.valueOf(dataSourceProps.getAllLocalProperties().get(entry.getKey())))) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Entry<String, Object> entry : getAllLocalProperties().entrySet()) {
            stringBuilder.append(entry.getKey()).append(entry.getValue());
        }
        return Objects.hashCode(dataSourceClassName, stringBuilder.toString());
    }
}
