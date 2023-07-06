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

package org.apache.shardingsphere.infra.datasource.storage;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.datasource.props.DataSourcePropertiesCreator;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Storage utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StorageUtils {
    
    /**
     * Get storage units from provided data sources.
     *
     * @param dataSources data sources
     * @return storage units
     */
    public static Map<String, StorageUnit> getStorageUnits(final Map<String, DataSource> dataSources) {
        Map<String, StorageUnit> result = new LinkedHashMap<>(dataSources.size(), 1F);
        for (Entry<String, DataSource> entry : dataSources.entrySet()) {
            DataSourceProperties dataSourceProperties = DataSourcePropertiesCreator.create(entry.getValue());
            String url = dataSourceProperties.getConnectionPropertySynonyms().getStandardProperties().get("url").toString();
            result.put(entry.getKey(), new StorageUnit(entry.getKey(), entry.getKey(), url));
        }
        return result;
    }
}
