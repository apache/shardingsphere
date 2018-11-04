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

package io.shardingsphere.spi.transaction.xa;

import io.shardingsphere.core.constant.DatabaseType;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Data source map converter SPI.
 *
 * @author zhaojun
 */
public interface DataSourceMapConverter {
    
    /**
     * Do convert data source map.
     *
     * @param dataSourceMap data source map
     * @param databaseType database type
     * @return data source map
     */
    Map<String, DataSource> convert(Map<String, DataSource> dataSourceMap, DatabaseType databaseType);
}

