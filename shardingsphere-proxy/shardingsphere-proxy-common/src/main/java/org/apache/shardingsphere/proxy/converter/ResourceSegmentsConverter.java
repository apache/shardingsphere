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

package org.apache.shardingsphere.proxy.converter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.parser.segment.DataSourceSegment;
import org.apache.shardingsphere.infra.config.datasource.DataSourceParameter;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.proxy.config.yaml.YamlDataSourceParameter;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Resource segments converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResourceSegmentsConverter {
    
    /**
     * Convert resource segments to YAML data source parameter map.
     *
     * @param databaseType database type
     * @param resources data source segments
     * @return YAML data source parameter map
     */
    public static Map<String, YamlDataSourceParameter> convert(final DatabaseType databaseType, final Collection<DataSourceSegment> resources) {
        Map<String, YamlDataSourceParameter> result = new LinkedHashMap<>(resources.size(), 1);
        for (DataSourceSegment each : resources) {
            DataSourceParameter parameter = new DataSourceParameter();
            YamlDataSourceParameter dataSource = new YamlDataSourceParameter();
            dataSource.setUrl(getURL(databaseType, each));
            dataSource.setUsername(each.getUser());
            dataSource.setPassword(each.getPassword());
            dataSource.setMinPoolSize(parameter.getMinPoolSize());
            dataSource.setMaxPoolSize(parameter.getMaxPoolSize());
            dataSource.setConnectionTimeoutMilliseconds(parameter.getConnectionTimeoutMilliseconds());
            dataSource.setIdleTimeoutMilliseconds(parameter.getIdleTimeoutMilliseconds());
            dataSource.setMaxLifetimeMilliseconds(parameter.getMaxLifetimeMilliseconds());
            dataSource.setCustomPoolProps(each.getProperties());
            result.put(each.getName(), dataSource);
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
