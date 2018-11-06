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

package io.shardingsphere.transaction.xa.convert.dialect;

import com.google.common.base.Optional;
import io.shardingsphere.core.metadata.datasource.dialect.PostgreSQLDataSourceMetaData;
import io.shardingsphere.core.rule.DataSourceParameter;
import lombok.RequiredArgsConstructor;

import java.util.Properties;

/**
 * Create PostgreSQL XA property from datasource parameter.
 *
 * @author zhaojun
 */
@RequiredArgsConstructor
public class PGXAProperty {
    
    private final DataSourceParameter dataSourceParameter;
    
    /**
     * Build PostgreSQL XA properties.
     *
     * @return PostgreSQL XA properties
     */
    public Properties build() {
        Properties result = new Properties();
        PostgreSQLDataSourceMetaData pgMetaData = new PostgreSQLDataSourceMetaData(dataSourceParameter.getUrl());
        result.setProperty("user", dataSourceParameter.getUsername());
        result.setProperty("password", Optional.fromNullable(dataSourceParameter.getPassword()).or(""));
        result.setProperty("serverName", pgMetaData.getHostName());
        result.setProperty("portNumber", String.valueOf(pgMetaData.getPort()));
        result.setProperty("databaseName", pgMetaData.getSchemeName());
        return result;
    }
}
