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

package org.apache.shardingsphere.infra.datasource.pool.props.domain;

import com.google.common.base.CaseFormat;
import com.google.common.base.Objects;
import lombok.Getter;
import org.apache.shardingsphere.infra.datasource.pool.metadata.DataSourcePoolMetaData;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.custom.CustomDataSourcePoolProperties;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.synonym.ConnectionPropertySynonyms;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.synonym.PoolPropertySynonyms;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Data source pool properties.
 */
@Getter
public final class DataSourcePoolProperties {
    
    private static final String UNDERSCORE = "_";
    
    private final String poolClassName;
    
    private final ConnectionPropertySynonyms connectionPropertySynonyms;
    
    private final PoolPropertySynonyms poolPropertySynonyms;
    
    private final CustomDataSourcePoolProperties customProperties;
    
    public DataSourcePoolProperties(final String poolClassName, final Map<String, Object> props) {
        Optional<DataSourcePoolMetaData> metaData = TypedSPILoader.findService(DataSourcePoolMetaData.class, poolClassName);
        this.poolClassName = metaData.map(optional -> optional.getType().toString()).orElse(poolClassName);
        Map<String, String> propertySynonyms = metaData.map(DataSourcePoolMetaData::getPropertySynonyms).orElse(Collections.emptyMap());
        Map<String, Object> effectiveProps = convertToCamelKeys(props);
        connectionPropertySynonyms = new ConnectionPropertySynonyms(effectiveProps, propertySynonyms);
        poolPropertySynonyms = new PoolPropertySynonyms(effectiveProps, propertySynonyms);
        Collection<String> transientFieldNames = metaData.map(DataSourcePoolMetaData::getTransientFieldNames).orElse(Collections.emptyList());
        customProperties = new CustomDataSourcePoolProperties(effectiveProps, getStandardPropertyKeys(), transientFieldNames, propertySynonyms);
    }
    
    private Map<String, Object> convertToCamelKeys(final Map<String, Object> props) {
        Map<String, Object> result = new LinkedHashMap<>(props.size(), 1F);
        for (Entry<String, Object> entry : props.entrySet()) {
            result.put(entry.getKey().contains(UNDERSCORE) ? CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, entry.getKey()) : entry.getKey(), entry.getValue());
        }
        return result;
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
                connectionPropertySynonyms.getStandardProperties().size() + poolPropertySynonyms.getStandardProperties().size() + customProperties.getProperties().size(), 1F);
        result.putAll(connectionPropertySynonyms.getStandardProperties());
        result.putAll(poolPropertySynonyms.getStandardProperties());
        result.putAll(customProperties.getProperties());
        return result;
    }
    
    /**
     * Get all local properties.
     *
     * @return all local properties
     */
    public Map<String, Object> getAllLocalProperties() {
        Map<String, Object> result = new LinkedHashMap<>(
                connectionPropertySynonyms.getLocalProperties().size() + poolPropertySynonyms.getLocalProperties().size() + customProperties.getProperties().size(), 1F);
        result.putAll(connectionPropertySynonyms.getLocalProperties());
        result.putAll(poolPropertySynonyms.getLocalProperties());
        result.putAll(customProperties.getProperties());
        return result;
    }
    
    @Override
    public boolean equals(final Object obj) {
        return this == obj || null != obj && getClass() == obj.getClass() && equalsByProperties((DataSourcePoolProperties) obj);
    }
    
    private boolean equalsByProperties(final DataSourcePoolProperties props) {
        return poolClassName.equals(props.poolClassName) && equalsByLocalProperties(props.getAllLocalProperties());
    }
    
    private boolean equalsByLocalProperties(final Map<String, Object> localProps) {
        for (Entry<String, Object> entry : getAllLocalProperties().entrySet()) {
            if (!localProps.containsKey(entry.getKey())) {
                continue;
            }
            if (entry.getValue() instanceof Map) {
                return entry.getValue().equals(localProps.get(entry.getKey()));
            }
            if (!String.valueOf(entry.getValue()).equals(String.valueOf(localProps.get(entry.getKey())))) {
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
        return Objects.hashCode(poolClassName, stringBuilder.toString());
    }
}
