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

package org.apache.shardingsphere.infra.datasource.pool.metadata;

import org.apache.shardingsphere.spi.typed.TypedSPI;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;

/**
 * Data source pool meta data.
 * 
 * @param <T> type of target data source
 */
public interface DataSourcePoolMetaData<T extends DataSource> extends TypedSPI {
    
    /**
     * Get default properties.
     *
     * @return default properties
     */
    Map<String, Object> getDefaultProperties();
    
    /**
     * Get invalid properties.
     * 
     * @return invalid properties
     */
    Map<String, Object> getInvalidProperties();
    
    /**
     * Get property synonyms.
     * 
     * @return property synonyms
     */
    Map<String, String> getPropertySynonyms();
    
    /**
     * Get JDBC URL.
     * 
     * @param targetDataSource target data source
     * @return JDBC URL
     */
    String getJdbcUrl(T targetDataSource);
    
    /**
     * Get JDBC URL properties field name.
     *
     * @return JDBC URL properties field name
     */
    String getJdbcUrlPropertiesFieldName();
    
    /**
     * Get JDBC URL properties.
     * 
     * @param targetDataSource target data source
     * @return JDBC URL properties
     */
    Properties getJdbcUrlProperties(T targetDataSource);
    
    /**
     * Append JDBC URL properties.
     * 
     * @param key key
     * @param value value
     * @param targetDataSource target data source
     */
    void appendJdbcUrlProperties(String key, String value, T targetDataSource);
    
    /**
     * Get transient field names.
     * 
     * @return transient field names
     */
    Collection<String> getTransientFieldNames();
}
