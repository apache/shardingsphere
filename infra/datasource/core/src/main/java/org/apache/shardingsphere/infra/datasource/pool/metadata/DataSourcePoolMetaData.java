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

import org.apache.shardingsphere.infra.spi.annotation.SingletonSPI;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPI;

import java.util.Collection;
import java.util.Map;

/**
 * Data source pool meta data.
 */
@SingletonSPI
public interface DataSourcePoolMetaData extends TypedSPI {
    
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
     * Get transient field names.
     *
     * @return transient field names
     */
    Collection<String> getTransientFieldNames();
    
    /**
     * Get data source pool field meta data.
     * 
     * @return data source pool field meta data
     */
    DataSourcePoolFieldMetaData getFieldMetaData();
    
    /**
     * Get data source pool properties validator.
     * 
     * @return data source pool properties validator
     */
    DataSourcePoolPropertiesValidator getDataSourcePoolPropertiesValidator();
    
}
