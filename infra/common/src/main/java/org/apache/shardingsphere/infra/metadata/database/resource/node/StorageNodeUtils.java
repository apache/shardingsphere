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

package org.apache.shardingsphere.infra.metadata.database.resource.node;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Storage node utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StorageNodeUtils {
    
    /**
     * Get storage node data sources.
     *
     * @param dataSources data sources
     * @return storage node data sources
     */
    public static Map<StorageNodeName, DataSource> getStorageNodeDataSources(final Map<String, DataSource> dataSources) {
        return dataSources.entrySet().stream().collect(
                Collectors.toMap(entry -> new StorageNodeName(entry.getKey()), Entry::getValue, (oldValue, currentValue) -> currentValue, () -> new LinkedHashMap<>(dataSources.size(), 1F)));
    }
}
