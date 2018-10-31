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

package io.shardingsphere.shardingjdbc.jdbc.core.datasource;

import com.zaxxer.hikari.HikariDataSource;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.spi.xa.BackendDataSourceFactory;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

public final class FixedBackendDataSourceFactory implements BackendDataSourceFactory {
    
    @Override
    public Map<String, DataSource> build(final Map<String, DataSource> dataSourceMap, DatabaseType databaseType) {
        Map<String, DataSource> result = new HashMap<>(2, 1);
        result.put("ds1", new HikariDataSource());
        result.put("ds2", new HikariDataSource());
        return result;
    }
}
