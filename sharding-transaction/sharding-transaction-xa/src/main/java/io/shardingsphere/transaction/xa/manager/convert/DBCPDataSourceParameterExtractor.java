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

package io.shardingsphere.transaction.xa.manager.convert;

import io.shardingsphere.core.config.DataSourceConfiguration;
import io.shardingsphere.core.constant.PoolType;
import io.shardingsphere.core.rule.DataSourceParameter;

import javax.sql.DataSource;

/**
 * Extract datasource parameter from DBCP connection pool.
 *
 * @author zhaojun
 */
public final class DBCPDataSourceParameterExtractor implements DataSourceParameterExtractor {
    
    private final DataSourceConfiguration dataSourceConfiguration;
    
    public DBCPDataSourceParameterExtractor(final DataSource dataSource) {
        dataSourceConfiguration = DataSourceConfiguration.getDataSourceConfiguration(dataSource);
    }
    
    @Override
    public DataSourceParameter extract() {
        dataSourceConfiguration.getProperties().put("maximumPoolSize", dataSourceConfiguration.getProperties().get("maxTotal"));
        dataSourceConfiguration.getProperties().put("originPoolType", PoolType.find(dataSourceConfiguration.getDataSourceClassName()));
        return dataSourceConfiguration.createDataSourceParameter();
    }
}
