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

import javax.sql.DataSource;

/**
 * Pick up properties from datasource then swap to database access configuration.
 *
 * @author zhaojun
 * @param <T> type of data source
 */
public interface DataSourceSwapper<T extends DataSource> {
    
    /**
     * Get data source class.
     *
     * @return data source class
     */
    Class<T> getDataSourceClass();
    
    /**
     * Swap to database access configuration.
     *
     * @param dataSource data source
     * @return database access configuration
     */
    DatabaseAccessConfiguration swap(T dataSource);
}
