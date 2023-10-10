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

package org.apache.shardingsphere.example.generator.core.yaml.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Configuration values supported by yaml sample.
 */
@RequiredArgsConstructor
@Getter
public enum YamlExampleConfigurationSupportedValue {
    
    MODES("modes", new HashSet<>(Arrays.asList("memory", "proxy", "cluster-zookeeper", "cluster-etcd", "standalone"))),
    
    TRANSACTIONS("transactions", new HashSet<>(Arrays.asList("local", "xa-atomikos", "xa-narayana", "base-seata"))),
    
    FEATURES("features", new HashSet<>(Arrays.asList("shadow", "sharding", "readwrite-splitting", "encrypt", "db-discovery", "mask"))),
    
    FRAMEWORKS("frameworks", new HashSet<>(Arrays.asList("jdbc", "spring-boot-starter-jdbc", "spring-boot-starter-jpa", "spring-boot-starter-mybatis", "spring-namespace-jdbc", "spring-namespace-jpa", "spring-namespace-mybatis")));
    
    private final String configItem;
    
    private final Set<String> supportedValues;
    
    public static YamlExampleConfigurationSupportedValue of(String configItem) {
        for (YamlExampleConfigurationSupportedValue each : values()) {
            if (each.getConfigItem().equals(configItem)) {
                return each;
            }
        }
        throw new IllegalArgumentException(configItem);
    }
}
