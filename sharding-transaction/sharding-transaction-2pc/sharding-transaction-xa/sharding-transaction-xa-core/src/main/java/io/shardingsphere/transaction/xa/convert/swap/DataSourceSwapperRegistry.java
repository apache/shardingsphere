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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
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
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class DataSourceSwapperRegistry {
    
    private static final Map<String, DataSourceSwapper> SWAPPERS = new HashMap<>();
    
    static {
        for (DataSourceSwapper each : ServiceLoader.load(DataSourceSwapper.class)) {
            loadSwapper(each);
        }
    }
    
    private static void loadSwapper(final DataSourceSwapper swapper) {
        for (String each : swapper.getDataSourceClassNames()) {
            if (SWAPPERS.containsKey(each)) {
                log.warn("Find more than one {} data source swapper implementation class, use `{}` now", each, SWAPPERS.get(each).getClass().getName());
                continue;
            }
            SWAPPERS.put(each, swapper);
        }
    }
    
    /**
     * Get data source swapper.
     *
     * @param dataSourceClass data source class
     * @return data source swapper
     */
    public static DataSourceSwapper getSwapper(final Class<? extends DataSource> dataSourceClass) {
        return SWAPPERS.containsKey(dataSourceClass.getName()) ? SWAPPERS.get(dataSourceClass.getName()) : new DefaultDataSourceSwapper();
    }
}
