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

package org.apache.shardingsphere.ha.spi;

import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithm;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;

/**
 * HA type.
 */
public interface HAType extends ShardingSphereAlgorithm {
    
    /**
     * Check HA config.
     *
     * @param dataSourceMap Data source map
     * @throws SQLException SQL Exception
     */
    void checkHAConfig(Map<String, DataSource> dataSourceMap, final String schemaName) throws SQLException;
    
    /**
     * Update primary data source.
     *
     * @param dataSourceMap Data source map
     */
    void updatePrimaryDataSource(Map<String, DataSource> dataSourceMap, final String schemaName);
    
    /**
     * Periodical monitor.
     *
     * @param dataSourceMap Data source map
     */
    void periodicalMonitor(Map<String, DataSource> dataSourceMap, final String schemaName);
}
