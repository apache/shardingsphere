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

package io.shardingsphere.transaction.xa.manager;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.spi.xa.DataSourceMapConverter;
import io.shardingsphere.transaction.manager.xa.XATransactionManager;
import io.shardingsphere.transaction.xa.manager.convert.DataSourceParameterFactory;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * XA backend data source converter.
 * 
 * @author zhaojun
 */
public final class XABackendDataSourceConverter implements DataSourceMapConverter {
    
    private static XATransactionManager xaManager = XATransactionManagerSPILoader.getInstance().getTransactionManager();
    
    @Override
    public Map<String, DataSource> build(final Map<String, DataSource> dataSourceMap, final DatabaseType databaseType) {
        Map<String, DataSource> result = new HashMap<>(dataSourceMap.size(), 1);
        for (Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            DataSource dataSource = xaManager.wrapDataSource(XADataSourceFactory.build(databaseType), entry.getKey(), DataSourceParameterFactory.build(entry.getValue()));
            result.put(entry.getKey(), dataSource);
        }
        return result;
    }
}
