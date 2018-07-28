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

package io.shardingsphere.proxy.backend.jdbc.datasource;

import io.shardingsphere.core.constant.TransactionType;
import io.shardingsphere.core.rule.DataSourceParameter;
import io.shardingsphere.jdbc.orchestration.internal.OrchestrationProxyConfiguration;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Backend data source factory.
 *
 * @author zhaojun
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BackendDataSourceFactory {
    
    /**
     * Create backend data source map.
     *
     * @param transactionType transaction type
     * @param config proxy configuration
     * @return data source map
     */
    public static Map<String, DataSource> createDataSourceMap(final TransactionType transactionType, final OrchestrationProxyConfiguration config) {
        Map<String, DataSource> result = new LinkedHashMap<>(config.getDataSources().size());
        for (Entry<String, DataSourceParameter> entry : config.getDataSources().entrySet()) {
            result.put(entry.getKey(), createDataSource(transactionType, entry.getKey(), entry.getValue()));
        }
        return result;
    }
    
    private static DataSource createDataSource(final TransactionType transactionType, final String dataSourceName, final DataSourceParameter dataSourceParameter) {
        switch (transactionType) {
            case XA:
                return new XABackendDataSource().build(dataSourceName, dataSourceParameter);
            default:
                return new RawBackendDataSource().build(dataSourceName, dataSourceParameter);
        }
    }
}
