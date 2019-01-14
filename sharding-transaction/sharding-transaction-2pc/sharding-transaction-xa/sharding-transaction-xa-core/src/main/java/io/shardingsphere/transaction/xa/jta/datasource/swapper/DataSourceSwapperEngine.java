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

package io.shardingsphere.transaction.xa.jta.datasource.swapper;

import io.shardingsphere.core.config.DatabaseAccessConfiguration;
import io.shardingsphere.transaction.xa.jta.datasource.swapper.impl.DefaultDataSourceSwapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Data source swapper engine.
 *
 * @author zhaojun
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@SuppressWarnings("unchecked")
public final class DataSourceSwapperEngine {
    
    private static final Map<Class<? extends DataSource>, DataSourceSwapper> SWAPPERS = new HashMap<>();
    
    static {
        for (DataSourceSwapper each : ServiceLoader.load(DataSourceSwapper.class)) {
            SWAPPERS.put(each.getDataSourceClass(), each);
        }
    }
    
    /**
     * Swap to database access configuration.
     *
     * @param dataSource data source
     * @return database access configuration
     */
    public static DatabaseAccessConfiguration swap(final DataSource dataSource) {
        DataSourceSwapper swapper = SWAPPERS.containsKey(dataSource.getClass()) ? SWAPPERS.get(dataSource.getClass()) : new DefaultDataSourceSwapper();
        return swapper.swap(dataSource);
    }
}
