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

package io.shardingsphere.proxy.config;

import io.shardingsphere.core.constant.TransactionType;
import io.shardingsphere.proxy.yaml.YamlProxyConfiguration;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Create raw datasource Map by transaction type.
 *
 * @author zhaojun
 */
public class ProxyRawDataSourceFactory {
    
    /**
     * Crate raw datasource Map by transaction type.
     *
     * @param transactionType transaction type
     * @param yamlProxyConfiguration yaml proxy configuration
     * @return raw datasource map
     */
    public static Map<String, DataSource> create(final TransactionType transactionType, final YamlProxyConfiguration yamlProxyConfiguration) {
        switch (transactionType) {
            case XA:
                return new XaProxyRawDataSource(yamlProxyConfiguration.getDataSources()).build();
            default:
                return new DefaultProxyRawDataSource(yamlProxyConfiguration.getDataSources()).build();
        }
    }
}
