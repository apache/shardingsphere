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

package org.apache.shardingsphere.infra.database.core.connector;

import java.util.Properties;

/**
 * Connection properties.
 */
public interface ConnectionProperties {
    
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
     * Judge whether two of connections are in the same database instance.
     *
     * @param connectionProps connection properties
     * @return connections are in the same database instance or not
     */
    boolean isInSameDatabaseInstance(ConnectionProperties connectionProps);
}
