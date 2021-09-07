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

package org.apache.shardingsphere.infra.config.datasource;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.config.datasource.creator.DataSourceCreatorFactory;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Data source converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataSourceConverter {
    
    static {
        ShardingSphereServiceLoader.register(JDBCParameterDecorator.class);
    }
    
    /**
     * Get data source.
     * 
     * @param dataSourceConfiguration data source configuration
     * @return data source
     */
    public static DataSource getDataSource(final DataSourceConfiguration dataSourceConfiguration) {
        return JDBCParameterDecoratorHelper.decorate(DataSourceCreatorFactory.getDataSourceCreator(dataSourceConfiguration.getDataSourceClassName())
                .createDataSource(dataSourceConfiguration));
    }
    
    /**
     * Get data source configuration.
     * 
     * @param dataSource data source
     * @return data source configuration
     */
    public static DataSourceConfiguration getDataSourceConfiguration(final DataSource dataSource) {
        return DataSourceCreatorFactory.getDataSourceCreator(dataSource.getClass().getName()).createDataSourceConfiguration(dataSource);
    }
    
    /**
     * Get data source map.
     *
     * @param dataSourceConfigMap data source configuration map
     * @return data source map
     */
    public static Map<String, DataSource> getDataSourceMap(final Map<String, DataSourceConfiguration> dataSourceConfigMap) {
        return dataSourceConfigMap.entrySet().stream().collect(Collectors.toMap(Entry::getKey,
            entry -> getDataSource(entry.getValue()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    /**
     * Get data source configuration map.
     *
     * @param dataSourceMap data source map
     * @return data source configuration map
     */
    public static Map<String, DataSourceConfiguration> getDataSourceConfigurationMap(final Map<String, DataSource> dataSourceMap) {
        return dataSourceMap.entrySet().stream().collect(
                Collectors.toMap(Entry::getKey, entry -> getDataSourceConfiguration(entry.getValue()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
}
