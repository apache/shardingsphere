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

package org.apache.shardingsphere.test.e2e.env.container.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.test.e2e.env.container.atomic.adapter.config.AdaptorContainerConfiguration;
import org.apache.shardingsphere.test.e2e.env.container.atomic.constants.ProxyContainerConstants;
import org.apache.shardingsphere.test.e2e.env.container.atomic.util.AdapterContainerUtils;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * SQL E2E Proxy container configuration factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLE2EProxyContainerConfigurationFactory {
    
    /**
     * Create instance of adaptor container configuration.
     *
     * @param scenario scenario
     * @param modeType modeType
     * @param databaseType database type
     * @return created instance
     */
    public static AdaptorContainerConfiguration newInstance(final String scenario, final String modeType, final DatabaseType databaseType) {
        return new AdaptorContainerConfiguration(scenario, new LinkedList<>(), getMountedResources(scenario, modeType, databaseType), AdapterContainerUtils.getAdapterContainerImage(), "");
    }
    
    private static Map<String, String> getMountedResources(final String scenario, final String modeType, final DatabaseType databaseType) {
        Map<String, String> result = new HashMap<>(3, 1F);
        result.put(String.format("/env/common/%s/proxy/conf/logback.xml", modeType), ProxyContainerConstants.CONFIG_PATH_IN_CONTAINER + "logback.xml");
        result.put(String.format("/env/scenario/%s/proxy/conf/%s", scenario, databaseType.getType().toLowerCase()), ProxyContainerConstants.CONFIG_PATH_IN_CONTAINER);
        result.put(getGlobalYamlPath(scenario, modeType, databaseType), ProxyContainerConstants.CONFIG_PATH_IN_CONTAINER + "global.yaml");
        return result;
    }
    
    @NotNull
    private static String getGlobalYamlPath(final String scenario, final String modeType, final DatabaseType databaseType) {
        if (isDialectScenarioGlobalYamlExists(scenario, modeType, databaseType)) {
            return String.format("/env/scenario/%s/proxy/mode/%s/%s/global.yaml", scenario, modeType, databaseType.getType().toLowerCase());
        }
        if (isScenarioGlobalYamlExists(scenario, modeType)) {
            return String.format("/env/scenario/%s/proxy/mode/%s/global.yaml", scenario, modeType);
        }
        return String.format( "/env/common/%s/proxy/conf/global.yaml", modeType);
    }
    
    private static boolean isDialectScenarioGlobalYamlExists(final String scenario, final String modeType, final DatabaseType databaseType) {
        URL url = Thread.currentThread().getContextClassLoader().getResource(String.format("env/scenario/%s/proxy/mode/%s/%s/global.yaml", scenario, modeType, databaseType.getType().toLowerCase()));
        return null != url;
    }
    
    private static boolean isScenarioGlobalYamlExists(final String scenario, final String modeType) {
        URL url = Thread.currentThread().getContextClassLoader().getResource(String.format("env/scenario/%s/proxy/mode/%s/global.yaml", scenario, modeType));
        return null != url;
    }
}
