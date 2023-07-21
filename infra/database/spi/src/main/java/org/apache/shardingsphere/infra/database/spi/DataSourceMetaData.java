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

package org.apache.shardingsphere.infra.database.spi;

import java.util.Properties;

/**
 * Data source meta data.
 */
public interface DataSourceMetaData {
    
    /**
     * Get host name.
     * 
     * @return host name
     */
    String getHostname();
    
    /**
     * Get port.
     * 
     * @return port
     */
    int getPort();
    
    /**
     * Get catalog.
     *
     * @return catalog
     */
    String getCatalog();
    
    /**
     * Get schema.
     * 
     * @return schema
     */
    String getSchema();
    
    /**
     * Get query properties.
     * 
     * @return query properties
     */
    Properties getQueryProperties();
    
    /**
     * Get default query properties.
     *
     * @return default query properties
     */
    Properties getDefaultQueryProperties();
    
    /**
     * Judge whether two of data sources are in the same database instance.
     *
     * @param dataSourceMetaData data source meta data
     * @return data sources are in the same database instance or not
     */
    default boolean isInSameDatabaseInstance(final DataSourceMetaData dataSourceMetaData) {
        return getHostname().equals(dataSourceMetaData.getHostname()) && getPort() == dataSourceMetaData.getPort();
    }
}
