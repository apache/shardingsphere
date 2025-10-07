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

package org.apache.shardingsphere.test.e2e.operation.transaction.framework.container.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.test.e2e.env.container.adapter.config.AdaptorContainerConfiguration;
import org.apache.shardingsphere.test.e2e.env.container.constants.ProxyContainerConstants;
import org.apache.shardingsphere.test.e2e.env.runtime.E2ETestEnvironment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Transaction proxy container configuration factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TransactionProxyContainerConfigurationFactory {
    
    /**
     * Create instance of adaptor container configuration.
     *
     * @param scenario scenario
     * @param databaseType database type
     * @param portBindings port bindings
     * @return created instance
     */
    public static AdaptorContainerConfiguration newInstance(final String scenario, final DatabaseType databaseType, final List<String> portBindings) {
        return new AdaptorContainerConfiguration(
                getProxyDatasourceName(scenario), portBindings, getMountedResource(scenario, databaseType), E2ETestEnvironment.getInstance().getDockerEnvironment().getProxyImage(), "");
    }
    
    private static String getProxyDatasourceName(final String scenario) {
        return "default".equals(scenario) ? "sharding_db" : scenario;
    }
    
    private static Map<String, String> getMountedResource(final String scenario, final DatabaseType databaseType) {
        Map<String, String> result = new HashMap<>(2, 1F);
        result.put(String.format("/env/%s/global.yaml", databaseType.getType().toLowerCase()), ProxyContainerConstants.CONFIG_PATH_IN_CONTAINER + "global.yaml");
        result.put("/env/scenario/" + scenario + "/proxy/conf/" + databaseType.getType().toLowerCase(), ProxyContainerConstants.CONFIG_PATH_IN_CONTAINER);
        return result;
    }
}
