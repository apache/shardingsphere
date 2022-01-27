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
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
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
     * Convert resource segments to data source properties map.
     *
     * @param databaseType database type
     * @param resources data source segments
     * @return data source properties map
     */
    public static Map<String, DataSourceProperties> convert(final DatabaseType databaseType, final Collection<DataSourceSegment> resources) {
        Map<String, DataSourceProperties> result = new LinkedHashMap<>(resources.size(), 1);
        for (DataSourceSegment each : resources) {
            result.put(each.getName(), new DataSourceProperties(HikariDataSource.class.getName(), createProperties(databaseType, each)));
        }
        return result;
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Map<String, Object> createProperties(final DatabaseType databaseType, final DataSourceSegment segment) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("jdbcUrl", getURL(databaseType, segment));
        result.put("username", segment.getUser());
        result.put("password", segment.getPassword());
        if (null != segment.getProperties()) {
            result.putAll((Map) segment.getProperties());
        }
        return result;
    }
    
    private static String getURL(final DatabaseType databaseType, final DataSourceSegment segment) {
        if (null != segment.getUrl()) {
            return segment.getUrl();
        }
        return String.format("%s//%s:%s/%s", databaseType.getJdbcUrlPrefixes().iterator().next(), segment.getHostname(), segment.getPort(), segment.getDatabase());
    }
}
