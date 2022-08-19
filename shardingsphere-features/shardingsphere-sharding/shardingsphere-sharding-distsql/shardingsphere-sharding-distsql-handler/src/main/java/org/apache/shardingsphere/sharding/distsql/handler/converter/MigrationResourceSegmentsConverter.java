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

package org.apache.shardingsphere.sharding.distsql.handler.converter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.parser.segment.DataSourceSegment;
import org.apache.shardingsphere.distsql.parser.segment.HostnameAndPortBasedDataSourceSegment;
import org.apache.shardingsphere.distsql.parser.segment.URLBasedDataSourceSegment;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Migration resource segments converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MigrationResourceSegmentsConverter {
    
    
    /**
     * Convert resource segments to data source properties map.
     *
     * @param resources data source segments
     * @return data source properties map
     */
    public static Map<String, DataSourceProperties> convert(final Collection<DataSourceSegment> resources) {
        Map<String, DataSourceProperties> result = new LinkedHashMap<>(resources.size(), 1);
        for (DataSourceSegment each : resources) {
            result.put(each.getName(), new DataSourceProperties("com.zaxxer.hikari.HikariDataSource", createProperties(each)));
        }
        return result;
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Map<String, Object> createProperties(final DataSourceSegment segment) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("jdbcUrl", getURL(segment));
        result.put("username", segment.getUser());
        result.put("password", segment.getPassword());
        if (null != segment.getProps()) {
            result.putAll((Map) segment.getProps());
        }
        return result;
    }
    
    /**
     * Get URL.
     *
     * @param segment data source segment
     * @return jdbc url
     */
    public static String getURL(final DataSourceSegment segment) {
        String result = null;
        if (segment instanceof URLBasedDataSourceSegment) {
            result = ((URLBasedDataSourceSegment) segment).getUrl();
        }
        if (segment instanceof HostnameAndPortBasedDataSourceSegment) {
            throw new UnsupportedOperationException("Not currently support add hostname and port, please use url");
        }
        return result;
    }
}
