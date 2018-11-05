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

package io.shardingsphere.shardingproxy.util;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import io.shardingsphere.core.config.DataSourceConfiguration;
import io.shardingsphere.core.rule.DataSourceParameter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Data source parameter converter.
 *
 * @author panjuan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DataSourceConverter {
    
    /**
     * Get data source map.
     *
     * @param dataSourceConfigurationMap data source configuration map
     * @return data source parameter map
     */
    public static Map<String, DataSourceParameter> getDataSourceParameterMap(final Map<String, DataSourceConfiguration> dataSourceConfigurationMap) {
        return Maps.transformValues(dataSourceConfigurationMap, new Function<DataSourceConfiguration, DataSourceParameter>() {
        
            @Override
            public DataSourceParameter apply(final DataSourceConfiguration input) {
                return input.createDataSourceParameter();
            }
        });
    }
    
    /**
     * Get data source configuration map.
     *
     * @param dataSourceParameterMap data source map
     * @return data source configuration map
     */
    public static Map<String, DataSourceConfiguration> getDataSourceConfigurationMap(final Map<String, DataSourceParameter> dataSourceParameterMap) {
        return Maps.transformValues(dataSourceParameterMap, new Function<DataSourceParameter, DataSourceConfiguration>() {
            
            @Override
            public DataSourceConfiguration apply(final DataSourceParameter input) {
                return DataSourceConfiguration.getDataSourceConfiguration(input);
            }
        });
    }
}
