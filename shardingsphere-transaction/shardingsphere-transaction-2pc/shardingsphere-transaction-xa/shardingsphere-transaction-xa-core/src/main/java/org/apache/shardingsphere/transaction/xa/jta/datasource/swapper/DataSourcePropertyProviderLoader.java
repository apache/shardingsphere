/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.transaction.xa.jta.datasource.swapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.transaction.xa.jta.datasource.swapper.impl.DefaultDataSourcePropertyProvider;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Data source property provider loader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataSourcePropertyProviderLoader {
    
    private static final Map<String, DataSourcePropertyProvider> DATA_SOURCE_PROPERTY_PROVIDERS = new HashMap<>();
    
    static {
        for (DataSourcePropertyProvider each : ServiceLoader.load(DataSourcePropertyProvider.class)) {
            DATA_SOURCE_PROPERTY_PROVIDERS.put(each.getDataSourceClassName(), each);
        }
    }
    
    /**
     * Get data source property provider.
     *
     * @param dataSource data source
     * @return data source property provider
     */
    public static DataSourcePropertyProvider getProvider(final DataSource dataSource) {
        String dataSourceClassName = dataSource.getClass().getName();
        return DATA_SOURCE_PROPERTY_PROVIDERS.containsKey(dataSourceClassName) ? DATA_SOURCE_PROPERTY_PROVIDERS.get(dataSourceClassName) : new DefaultDataSourcePropertyProvider();
    }
}
