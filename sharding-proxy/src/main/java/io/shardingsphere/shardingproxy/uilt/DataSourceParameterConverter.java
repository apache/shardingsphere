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

package io.shardingsphere.shardingproxy.uilt;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import io.shardingsphere.core.config.DataSourceConfiguration;
import io.shardingsphere.core.rule.DataSourceParameter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Data source parameter converter.
 *
 * @author panjuan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DataSourceParameterConverter {
    
    /**
     * Get data source parameter map.
     *
     * @param dataSourceConfigurationMap data source configuration map
     * @return data source parameter map
     */
    public static Map<String, DataSourceParameter> getDataSourceParameterMap(final Map<String, DataSourceConfiguration> dataSourceConfigurationMap) {
        Map<String, DataSourceParameter> result = new HashMap<>(dataSourceConfigurationMap.size(), 1);
        for (Entry<String, DataSourceConfiguration> entry : dataSourceConfigurationMap.entrySet()) {
            result.put(entry.getKey(), getDataSourceParameter(entry.getValue()));
        }
        return result;
    }
    
    private static DataSourceParameter getDataSourceParameter(final DataSourceConfiguration dataSourceConfiguration) {
        DataSourceParameter result = new DataSourceParameter();
        for (Field each : result.getClass().getDeclaredFields()) {
            try {
                each.setAccessible(true);
                each.set(result, dataSourceConfiguration.getProperties().get(each.getName()));
            } catch (final ReflectiveOperationException ignored) {
            }
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
        return Maps.transformValues(dataSourceMap, new Function<DataSource, DataSourceConfiguration>() {
            
            @Override
            public DataSourceConfiguration apply(final DataSource input) {
                return DataSourceConfiguration.getDataSourceConfiguration(input);
            }
        });
    }
}
