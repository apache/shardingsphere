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

package org.apache.shardingsphere.infra.config.datasource.pool.creator;

import org.apache.shardingsphere.spi.required.RequiredSPI;
import org.apache.shardingsphere.spi.typed.TypedSPI;

import java.util.Map;
import java.util.Properties;

/**
 * Data source pool creation meta data.
 */
public interface DataSourcePoolCreationMetaData extends TypedSPI, RequiredSPI {
    
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
     * Get data source properties field name.
     * 
     * @return data source properties field name
     */
    String getDataSourcePropertiesFieldName();
    
    /**
     * Get JDBC URL field name.
     * 
     * @return JDBC URL field name
     */
    String getJdbcUrlFieldName();
    
    /**
     * Get default data source properties.
     * 
     * @return default data source properties
     */
    Properties getDefaultDataSourceProperties();
}
