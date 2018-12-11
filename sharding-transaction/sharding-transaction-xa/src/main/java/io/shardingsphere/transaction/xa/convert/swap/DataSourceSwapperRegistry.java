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

package io.shardingsphere.transaction.xa.convert.swap;

import com.google.common.base.Splitter;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Data source swapper registry.
 *
 * @author zhaojun
 */
@Slf4j
public class DataSourceSwapperRegistry {
    
    private static final Map<String, DataSourceSwapper> DATA_SOURCE_SWAPPER_MAP = new HashMap<>();
    
    private static final DataSourceSwapperRegistry INSTANCE = new DataSourceSwapperRegistry();
    
    static {
        load();
    }
    
    /**
     * Load data source swapper.
     */
    public static void load() {
        for (DataSourceSwapper each : ServiceLoader.load(DataSourceSwapper.class)) {
            loadOneSwapper(each);
        }
    }
    
    private static void loadOneSwapper(final DataSourceSwapper swapper) {
        for (String each : Splitter.on(":").split(swapper.originClassName())) {
            if (DATA_SOURCE_SWAPPER_MAP.containsKey(each)) {
                log.warn("Find more than one {} data source swapper implementation class, use `{}` now",
                    each, DATA_SOURCE_SWAPPER_MAP.get(each).getClass().getName());
                continue;
            }
            DATA_SOURCE_SWAPPER_MAP.put(each, swapper);
        }
    }
    
    /**
     * Get instance of data source swapper registry.
     *
     * @return instance of data source swapper registry
     */
    public static DataSourceSwapperRegistry getInstance() {
        return INSTANCE;
    }
    
    /**
     * Get data source swapper by class name.
     *
     * @param dataSource pool data source
     * @return data source swapper implement
     */
    public DataSourceSwapper getSwapper(final DataSource dataSource) {
        DataSourceSwapper result = DATA_SOURCE_SWAPPER_MAP.get(dataSource.getClass().getName());
        return null == result ? new DefaultDataSourceSwapper() : result;
    }
}
