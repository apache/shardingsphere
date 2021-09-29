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

package org.apache.shardingsphere.proxy.backend.text.distsql.rdl.resource;

import com.zaxxer.hikari.HikariDataSource;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.parser.segment.DataSourceSegment;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceParameter;
import org.apache.shardingsphere.infra.database.type.DatabaseType;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Resource segments converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResourceSegmentsConverter {
    
    /**
     * Convert resource segments to data source configuration map.
     *
     * @param databaseType database type
     * @param resources data source segments
     * @return data source configuration map
     */
    public static Map<String, DataSourceConfiguration> convert(final DatabaseType databaseType, final Collection<DataSourceSegment> resources) {
        Map<String, DataSourceConfiguration> result = new LinkedHashMap<>(resources.size(), 1);
        for (DataSourceSegment each : resources) {
            DataSourceParameter dataSource = new DataSourceParameter();
            dataSource.setUrl(getURL(databaseType, each));
            dataSource.setUsername(each.getUser());
            dataSource.setPassword(each.getPassword());
            dataSource.setCustomPoolProps(each.getProperties());
            result.put(each.getName(), createDataSourceConfiguration(databaseType, each));
        }
        return result;
    }
    
    private static DataSourceConfiguration createDataSourceConfiguration(final DatabaseType databaseType, final DataSourceSegment segment) {
        DataSourceConfiguration result = new DataSourceConfiguration(HikariDataSource.class.getName());
        result.getProps().put("jdbcUrl", getURL(databaseType, segment));
        result.getProps().put("username", segment.getUser());
        result.getProps().put("password", segment.getPassword());
        result.getProps().put("connectionTimeout", DataSourceParameter.DEFAULT_CONNECTION_TIMEOUT_MILLISECONDS);
        result.getProps().put("idleTimeout", DataSourceParameter.DEFAULT_IDLE_TIMEOUT_MILLISECONDS);
        result.getProps().put("maxLifetime", DataSourceParameter.DEFAULT_MAX_LIFETIME_MILLISECONDS);
        result.getProps().put("maximumPoolSize", DataSourceParameter.DEFAULT_MAX_POOL_SIZE);
        result.getProps().put("minimumIdle", DataSourceParameter.DEFAULT_MIN_POOL_SIZE);
        result.getProps().put("readOnly", DataSourceParameter.DEFAULT_READ_ONLY);
        if (null != segment.getProperties()) {
            result.getCustomPoolProps().putAll(segment.getProperties());
        }
        return result;
    }
    
    private static String getURL(final DatabaseType databaseType, final DataSourceSegment dataSourceSegment) {
        if (null != dataSourceSegment.getUrl()) {
            return dataSourceSegment.getUrl();
        }
        return String.format("%s//%s:%s/%s", databaseType.getJdbcUrlPrefixes().iterator().next(), dataSourceSegment.getHostName(), dataSourceSegment.getPort(), dataSourceSegment.getDb());
    }
}
