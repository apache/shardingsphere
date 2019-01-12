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

package io.shardingsphere.transaction.xa.swapper.impl;

import com.zaxxer.hikari.HikariDataSource;
import io.shardingsphere.core.config.DatabaseAccessConfiguration;
import io.shardingsphere.transaction.xa.swapper.DataSourceSwapper;

/**
 * Hikari CP swapper.
 *
 * @author zhaojun
 */
public final class HikariCPSwapper implements DataSourceSwapper<HikariDataSource> {
    
    @Override
    public Class<HikariDataSource> getDataSourceClass() {
        return HikariDataSource.class;
    }
    
    @Override
    public DatabaseAccessConfiguration swap(final HikariDataSource dataSource) {
        return new DatabaseAccessConfiguration(dataSource.getJdbcUrl(), dataSource.getUsername(), dataSource.getPassword());
    }
}
