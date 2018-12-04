/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingjdbc.orchestration.internal.util;

import io.shardingsphere.core.config.DataSourceConfiguration;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Data source converter.
 *
 * @author panjuan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataSourceConverter {
    
    /**
     * Get data source map.
     *
     * @param dataSourceConfigurationMap data source configuration map
     * @return data source map
     */
    public static Map<String, DataSource> getDataSourceMap(final Map<String, DataSourceConfiguration> dataSourceConfigurationMap) {
        Map<String, DataSource> result = new LinkedHashMap<>(dataSourceConfigurationMap.size(), 1);
        for (Entry<String, DataSourceConfiguration> entry : dataSourceConfigurationMap.entrySet()) {
            result.put(entry.getKey(), entry.getValue().createDataSource());
        }
        return result;
    }
    
    /**
     * Get data source configuration map.
     *
     * @param dataSourceMap data source map
     * @return data source configuration map
     */
    public static Map<String, DataSourceConfiguration> getDataSourceConfigurationMap(final Map<String, DataSource> dataSourceMap) {
        Map<String, DataSourceConfiguration> result = new LinkedHashMap<>(dataSourceMap.size(), 1);
        for (Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            result.put(entry.getKey(), DataSourceConfiguration.getDataSourceConfiguration(entry.getValue()));
        }
        return result;
    }
}
