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

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import io.shardingsphere.core.config.DataSourceConfiguration;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.sql.DataSource;
import java.util.Map;

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
        return Maps.transformValues(dataSourceConfigurationMap, new Function<DataSourceConfiguration, DataSource>() {
            
            @Override
            public DataSource apply(final DataSourceConfiguration input) {
                return input.createDataSource();
            }
        });
    }
    
    /**
     * Get data source configuration map.
     *
     * @param dataSourceMap data source map
     * @return data source configuration map
     */
    public static Map<String, DataSourceConfiguration> getDataSourceConfigurationMap(final Map<String, DataSource> dataSourceMap) {
        return Maps.transformValues(dataSourceMap, new Function<DataSource, DataSourceConfiguration>() {
            
            @Override
            public DataSourceConfiguration apply(final DataSource input) {
                return DataSourceConfiguration.getDataSourceConfiguration(input);
            }
        });
    }
}
