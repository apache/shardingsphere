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

package org.apache.shardingsphere.shadow.metadata.converter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Shadow node converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShadowNodeConverter {
    
    private static final String DATA_SOURCES = "data_sources";
    
    private static final String TABLES = "tables";
    
    private static final String SHADOW_ALGORITHMS = "shadow_algorithms";
    
    private static final String DEFAULT_SHADOW_ALGORITHM_NAME = "default_shadow_algorithm_name";
    
    /**
     * Get data source path.
     * 
     * @param dataSourceName data source name
     * @return data source path
     */
    public static String getDataSourcePath(final String dataSourceName) {
        return String.join("/", DATA_SOURCES, dataSourceName);
    }
    
    /**
     * Get table name path.
     * 
     * @param tableName table name
     * @return table name path
     */
    public static String getTableNamePath(final String tableName) {
        return String.join("/", TABLES, tableName);
    }
    
    /**
     * Get shadow algorithm path.
     * 
     * @param shadowAlgorithmName shadow algorithm name
     * @return shadow algorithm path
     */
    public static String getShadowAlgorithmPath(final String shadowAlgorithmName) {
        return String.join("/", SHADOW_ALGORITHMS, shadowAlgorithmName);
    }
    
    /**
     * Get default shadow algorithm path.
     * 
     * @return default shadow algorithm path
     */
    public static String getDefaultShadowAlgorithmPath() {
        return String.join("/", DEFAULT_SHADOW_ALGORITHM_NAME);
    }
}
