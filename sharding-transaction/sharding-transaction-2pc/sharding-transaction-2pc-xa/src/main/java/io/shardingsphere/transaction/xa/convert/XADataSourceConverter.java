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

package io.shardingsphere.transaction.xa.convert;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.rule.DataSourceParameter;
import io.shardingsphere.transaction.api.TransactionType;
import io.shardingsphere.transaction.spi.TransactionalDataSourceConverter;
import io.shardingsphere.transaction.spi.xa.XATransactionManager;
import io.shardingsphere.transaction.xa.convert.datasource.XADataSourceFactory;
import io.shardingsphere.transaction.xa.convert.swap.DataSourceSwapperRegistry;
import io.shardingsphere.transaction.xa.manager.XATransactionManagerSPILoader;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Data source converter for XA.
 * 
 * @author zhaojun
 */
public final class XADataSourceConverter implements TransactionalDataSourceConverter {
    
    private final XATransactionManager xaTransactionManager = XATransactionManagerSPILoader.getInstance().getTransactionManager();
    
    @Override
    public TransactionType getType() {
        return TransactionType.XA;
    }
    
    @Override
    public Map<String, DataSource> convert(final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap) {
        Map<String, DataSource> result = new HashMap<>(dataSourceMap.size(), 1);
        try {
            for (Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
                DataSourceParameter parameter = DataSourceSwapperRegistry.getSwapper(entry.getValue().getClass()).swap(entry.getValue());
                DataSource dataSource = xaTransactionManager.wrapDataSource(databaseType, XADataSourceFactory.build(databaseType), entry.getKey(), parameter);
                result.put(entry.getKey(), dataSource);
            }
            return result;
        } catch (final Exception ex) {
            return result;
        }
    }
}
