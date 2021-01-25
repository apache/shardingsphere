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

package org.apache.shardingsphere.spring.boot.datasource.prop.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spring.boot.datasource.prop.DataSourcePropertiesSetter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Datasource properties setter holder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataSourcePropertiesSetterHolder {
    
    private static final Map<String, DataSourcePropertiesSetter> DATA_SOURCE_PROPERTIES_SETTER_MAP = new HashMap<>();
    
    static {
        ShardingSphereServiceLoader.register(DataSourcePropertiesSetter.class);
        for (DataSourcePropertiesSetter each : ShardingSphereServiceLoader.newServiceInstances(DataSourcePropertiesSetter.class)) {
            DATA_SOURCE_PROPERTIES_SETTER_MAP.put(each.getType(), each);
        }
    }
    
    /**
     * Get data source properties setter by type.
     *
     * @param type data source type
     * @return data source properties setter
     */
    public static Optional<DataSourcePropertiesSetter> getDataSourcePropertiesSetterByType(final String type) {
        return DATA_SOURCE_PROPERTIES_SETTER_MAP.containsKey(type) ? Optional.of(DATA_SOURCE_PROPERTIES_SETTER_MAP.get(type)) : Optional.empty();
    }
}
