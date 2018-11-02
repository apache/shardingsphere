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

package io.shardingsphere.spi.xa;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.spi.NewInstanceServiceLoader;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Map;

/**
 * XA backend data source factory.
 *
 * @author zhaojun
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SPIDataSourceMapConverter {
    
    private static final NewInstanceServiceLoader<DataSourceMapConverter> SERVICE_LOADER = NewInstanceServiceLoader.load(DataSourceMapConverter.class);
    
    /**
     * Using data source map converter SPI to convert normal data source.
     * @param dataSourceMap data source map
     * @param databaseType database type
     * @return xa transactional datasource map
     */
    public static Map<String, DataSource> convert(final Map<String, DataSource> dataSourceMap, final DatabaseType databaseType) {
        Collection<DataSourceMapConverter> dataSourceMapConverters = SERVICE_LOADER.newServiceInstances();
        if (dataSourceMapConverters.isEmpty()) {
            throw new ShardingException("Please make DataSourceMapConverter SPI available.");
        }
        return dataSourceMapConverters.iterator().next().convert(dataSourceMap, databaseType);
    }
}
