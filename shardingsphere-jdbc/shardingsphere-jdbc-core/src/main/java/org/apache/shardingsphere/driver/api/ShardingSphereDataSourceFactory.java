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

package org.apache.shardingsphere.driver.api;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.database.DefaultSchema;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

/**
 * ShardingSphere data source factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingSphereDataSourceFactory {
    
    /**
     * Create ShardingSphere data source.
     *
     * @param schemaName schema name
     * @param dataSourceMap data source map
     * @param configs rule configurations
     * @param props properties for data source
     * @return ShardingSphere data source
     * @throws SQLException SQL exception
     */
    public static DataSource createDataSource(final String schemaName, 
                                              final Map<String, DataSource> dataSourceMap, final Collection<RuleConfiguration> configs, final Properties props) throws SQLException {
        return new ShardingSphereDataSource(schemaName, dataSourceMap, configs, props);
    }
    
    /**
     * Create ShardingSphere data source.
     *
     * @param dataSourceMap data source map
     * @param configs rule configurations
     * @param props properties for data source
     * @return ShardingSphere data source
     * @throws SQLException SQL exception
     */
    public static DataSource createDataSource(final Map<String, DataSource> dataSourceMap, final Collection<RuleConfiguration> configs, final Properties props) throws SQLException {
        return new ShardingSphereDataSource(DefaultSchema.LOGIC_NAME, dataSourceMap, configs, props);
    }
    
    /**
     * Create ShardingSphere data source.
     *
     * @param dataSource data source
     * @param configs rule configurations
     * @param props properties for data source
     * @return ShardingSphere data source
     * @throws SQLException SQL exception
     */
    public static DataSource createDataSource(final DataSource dataSource, final Collection<RuleConfiguration> configs, final Properties props) throws SQLException {
        return createDataSource(DefaultSchema.LOGIC_NAME, Collections.singletonMap(DefaultSchema.LOGIC_NAME, dataSource), configs, props);
    }
    
    /**
     * Create ShardingSphere data source.
     *
     * @param schemaName schema name
     * @param dataSource data source
     * @param configs rule configurations
     * @param props properties for data source
     * @return ShardingSphere data source
     * @throws SQLException SQL exception
     */
    public static DataSource createDataSource(final String schemaName, final DataSource dataSource, final Collection<RuleConfiguration> configs, final Properties props) throws SQLException {
        return createDataSource(schemaName, Collections.singletonMap(schemaName, dataSource), configs, props);
    }
}
