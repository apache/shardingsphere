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

package org.apache.shardingsphere.infra.config.datasource.creator;

import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.spi.typed.TypedSPI;

import javax.sql.DataSource;

/**
 * Data source creator.
 */
public interface DataSourceCreator extends TypedSPI {
    
    /**
     * Create data source configuration by data source.
     * 
     * @param dataSource data source
     * @return data source configuration
     */
    DataSourceConfiguration createDataSourceConfiguration(DataSource dataSource);
    
    /**
     * Create data source by data source configuration.
     * 
     * @param dataSourceConfig data source configuration
     * @return data source
     */
    DataSource createDataSource(DataSourceConfiguration dataSourceConfig);
}
