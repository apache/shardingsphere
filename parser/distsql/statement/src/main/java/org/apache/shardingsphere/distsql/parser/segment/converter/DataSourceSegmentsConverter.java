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

package org.apache.shardingsphere.distsql.parser.segment.converter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.parser.segment.DataSourceSegment;
import org.apache.shardingsphere.distsql.parser.segment.HostnameAndPortBasedDataSourceSegment;
import org.apache.shardingsphere.distsql.parser.segment.URLBasedDataSourceSegment;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Data source segments converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataSourceSegmentsConverter {
    
    /**
     * Convert data source segments to data source properties map.
     *
     * @param databaseType database type
     * @param dataSourceSegments data source segments
     * @return data source properties map
     */
    public static Map<String, DataSourceProperties> convert(final DatabaseType databaseType, final Collection<DataSourceSegment> dataSourceSegments) {
        Map<String, DataSourceProperties> result = new LinkedHashMap<>(dataSourceSegments.size(), 1F);
        for (DataSourceSegment each : dataSourceSegments) {
            result.put(each.getName(), new DataSourceProperties("com.zaxxer.hikari.HikariDataSource", createProperties(databaseType, each)));
        }
        return result;
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Map<String, Object> createProperties(final DatabaseType databaseType, final DataSourceSegment segment) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("jdbcUrl", getURL(databaseType, segment));
        result.put("username", segment.getUser());
        result.put("password", segment.getPassword());
        if (null != segment.getProps()) {
            result.putAll((Map) segment.getProps());
        }
        return result;
    }
    
    private static String getURL(final DatabaseType databaseType, final DataSourceSegment segment) {
        String result = null;
        if (segment instanceof URLBasedDataSourceSegment) {
            result = ((URLBasedDataSourceSegment) segment).getUrl();
        }
        if (segment instanceof HostnameAndPortBasedDataSourceSegment) {
            HostnameAndPortBasedDataSourceSegment actualSegment = (HostnameAndPortBasedDataSourceSegment) segment;
            result = String.format("%s//%s:%s/%s", databaseType.getJdbcUrlPrefixes().iterator().next(), actualSegment.getHostname(), actualSegment.getPort(), actualSegment.getDatabase());
        }
        return result;
    }
}
