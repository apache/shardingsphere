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

package org.apache.shardingsphere.infra.datasource.mapper;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Data source mapper info.
 */
@Getter
@Setter
@RequiredArgsConstructor
public final class DataSourceMapperInfo {
    
    private final Map<String, Collection<DataSourceRoleInfo>> dataSources;
    
    public DataSourceMapperInfo() {
        dataSources = new LinkedHashMap<>();
    }
    
    /**
     * Get data source mapper.
     * 
     * @return data source mapper
     */
    public Map<String, Collection<String>> getMapper() {
        Map<String, Collection<String>> result = new LinkedHashMap<>(dataSources.size(), 1);
        dataSources.forEach((key, value) -> result.put(key, value.stream().map(DataSourceRoleInfo::getName).collect(Collectors.toList())));
        return result;
    }
}
