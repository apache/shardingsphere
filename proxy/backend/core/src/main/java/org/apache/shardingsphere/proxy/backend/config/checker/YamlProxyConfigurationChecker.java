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

package org.apache.shardingsphere.proxy.backend.config.checker;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.kernel.metadata.resource.storageunit.DuplicateStorageUnitException;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyDataSourceConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyDatabaseConfiguration;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * YAML configuration checker for ShardingSphere-Proxy.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class YamlProxyConfigurationChecker {
    
    /**
     * Check data sources.
     *
     * @param globalDataSources global data sources
     * @param databaseConfigs database configurations
     */
    public static void checkDataSources(final Map<String, YamlProxyDataSourceConfiguration> globalDataSources, final Collection<YamlProxyDatabaseConfiguration> databaseConfigs) {
        databaseConfigs.forEach(each -> checkDataSources(globalDataSources, each.getDataSources(), each.getDatabaseName()));
    }
    
    private static void checkDataSources(final Map<String, YamlProxyDataSourceConfiguration> globalDataSources,
                                         final Map<String, YamlProxyDataSourceConfiguration> databaseDataSources, final String databaseName) {
        Collection<String> duplicatedDataSourceNames = globalDataSources.keySet().stream().filter(databaseDataSources.keySet()::contains).collect(Collectors.toSet());
        ShardingSpherePreconditions.checkMustEmpty(duplicatedDataSourceNames, () -> new DuplicateStorageUnitException(databaseName, duplicatedDataSourceNames));
    }
}
