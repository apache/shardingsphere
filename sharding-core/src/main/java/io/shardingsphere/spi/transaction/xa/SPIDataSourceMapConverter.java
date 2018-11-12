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
import io.shardingsphere.spi.NewInstanceServiceLoader;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Map;

/**
 * SPI data source map converter.
 *
 * @author zhaojun
 */
@Slf4j
public final class SPIDataSourceMapConverter implements DataSourceMapConverter {
    
    private static DataSourceMapConverter dataSourceMapConverter;
    
    static {
        init();
    }
    
    private static void init() {
        Collection<DataSourceMapConverter> converters = NewInstanceServiceLoader.load(DataSourceMapConverter.class);
        if (converters.isEmpty()) {
            log.info("Could not find XA DataSourceConverter, XA transaction will not be effective");
            return;
        }
        dataSourceMapConverter = converters.iterator().next();
    }
    
    /**
     * Convert normal datasource to xa transactional datasource.
     *
     * @param dataSourceMap data source map
     * @param databaseType database type
     * @return xa transactional datasource map
     */
    @Override
    public Map<String, DataSource> convert(final Map<String, DataSource> dataSourceMap, final DatabaseType databaseType) {
        return null != dataSourceMapConverter ? dataSourceMapConverter.convert(dataSourceMap, databaseType) : null;
    }
}
