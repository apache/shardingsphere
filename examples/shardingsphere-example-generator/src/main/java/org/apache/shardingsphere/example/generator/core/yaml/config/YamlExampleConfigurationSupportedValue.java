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

import java.util.Set;

import com.google.common.collect.Sets;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Configuration values supported by yaml sample.
 */
@Getter
@AllArgsConstructor
public enum YamlExampleConfigurationSupportedValue {

    PRODUCTS("products", Sets.newHashSet("jdbc", "proxy")),

    MODES("modes", Sets.newHashSet("memory", "proxy", "cluster-zookeeper", "cluster-etcd", "standalone-file")),

    TRANSACTIONS("transactions", Sets.newHashSet("local")),

    FEATURES("features", Sets.newHashSet("shadow", "sharding", "readwrite-splitting", "encrypt", "db-discovery")),

    FRAMEWORKS("frameworks", Sets.newHashSet("jdbc", "spring-boot-starter-jdbc", "spring-boot-starter-jpa", "spring-boot-starter-mybatis", "spring-namespace-jdbc", "spring-namespace-jpa", "spring-namespace-mybatis"));

    private String configItem;

    private Set<String> supportedValues;

    public static YamlExampleConfigurationSupportedValue of(String configItem) {
        for (YamlExampleConfigurationSupportedValue exampleSupportedValues : values()) {
            if (exampleSupportedValues.getConfigItem().equals(configItem)) {
                return exampleSupportedValues;
            }
        }
        return null;
    }
}
