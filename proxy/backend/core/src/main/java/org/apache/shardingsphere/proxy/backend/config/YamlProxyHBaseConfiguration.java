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

package org.apache.shardingsphere.proxy.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.infra.util.yaml.YamlConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlHBaseParameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Rule configuration for YAML.
 */
@Getter
@Setter
public final class YamlProxyHBaseConfiguration implements YamlConfiguration {
    
    private Map<String, Object> dataSourceCommon;
    
    private Map<String, YamlHBaseParameter> dataSources = new HashMap<>();
    
    private Properties props;
    
    /**
     * get ignored tables.
     * 
     * @return a map contains tables.
     */
    public Map<String, List<String>> getIgnoreTables() {
        Map<String, List<String>> result = new HashMap<>(dataSources.size());
        for (Map.Entry<String, YamlHBaseParameter> entry : dataSources.entrySet()) {
            result.put(entry.getKey(), Arrays.stream(entry.getValue().getIgnoreTables().split(",")).map(String::trim).collect(Collectors.toList()));
        }
        return result;
    }
}
