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

package org.apache.shardingsphere.dbdiscovery.factory;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.dbdiscovery.spi.DatabaseDiscoveryProviderAlgorithm;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmFactory;
import org.apache.shardingsphere.infra.util.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPIRegistry;

/**
 * Database discovery provider algorithm factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseDiscoveryProviderAlgorithmFactory {
    
    static {
        ShardingSphereServiceLoader.register(DatabaseDiscoveryProviderAlgorithm.class);
    }
    
    /**
     * Create new instance of database discovery provider algorithm.
     *
     * @param databaseDiscoveryProviderAlgorithmConfig database discovery provider algorithm configuration
     * @return created instance
     */
    public static DatabaseDiscoveryProviderAlgorithm newInstance(final AlgorithmConfiguration databaseDiscoveryProviderAlgorithmConfig) {
        return ShardingSphereAlgorithmFactory.createAlgorithm(databaseDiscoveryProviderAlgorithmConfig, DatabaseDiscoveryProviderAlgorithm.class);
    }
    
    /**
     * Judge whether contains database discovery provider algorithm.
     *
     * @param databaseDiscoveryProviderAlgorithmType database discovery provider algorithm type
     * @return contains database discovery provider algorithm or not
     */
    public static boolean contains(final String databaseDiscoveryProviderAlgorithmType) {
        return TypedSPIRegistry.findRegisteredService(DatabaseDiscoveryProviderAlgorithm.class, databaseDiscoveryProviderAlgorithmType).isPresent();
    }
}
