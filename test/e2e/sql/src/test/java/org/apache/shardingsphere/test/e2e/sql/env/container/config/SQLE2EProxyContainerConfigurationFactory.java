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

package org.apache.shardingsphere.test.e2e.sql.env.container.config;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.test.e2e.env.container.adapter.config.AdaptorContainerConfiguration;
import org.apache.shardingsphere.test.e2e.env.container.constants.ProxyContainerConstants;
import org.apache.shardingsphere.test.e2e.env.runtime.E2ETestEnvironment;

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
     * @param mode mode
     * @param databaseType database type
     * @return created instance
     */
    public static AdaptorContainerConfiguration newInstance(final String scenario, final String mode, final DatabaseType databaseType) {
        return new AdaptorContainerConfiguration(
                scenario, new LinkedList<>(), getMountedResources(scenario, mode, databaseType), E2ETestEnvironment.getInstance().getDockerEnvironment().getProxyImage(), "");
    }
    
    private static Map<String, String> getMountedResources(final String scenario, final String mode, final DatabaseType databaseType) {
        Map<String, String> result = new HashMap<>(3, 1F);
        result.put(getGlobalYamlPath(scenario, mode, databaseType), ProxyContainerConstants.CONFIG_PATH_IN_CONTAINER + "global.yaml");
        result.put(String.format("/env/scenario/%s/proxy/conf/%s", scenario, databaseType.getType().toLowerCase()), ProxyContainerConstants.CONFIG_PATH_IN_CONTAINER);
        result.put("/env/common/logback.xml", ProxyContainerConstants.CONFIG_PATH_IN_CONTAINER + "logback.xml");
        return result;
    }
    
    private static String getGlobalYamlPath(final String scenario, final String mode, final DatabaseType databaseType) {
        String regCenterType = getRegistryCenterType(mode);
        if (isDialectScenarioGlobalYamlExists(scenario, mode, regCenterType, databaseType)) {
            return String.format("/env/scenario/%s/proxy/mode/%s/%s/%s/global.yaml", scenario, mode, databaseType.getType().toLowerCase(), regCenterType);
        }
        if (isRegistryCenterGlobalYamlExists(scenario, mode, regCenterType)) {
            return String.format("/env/scenario/%s/proxy/mode/%s/%s/global.yaml", scenario, mode, regCenterType);
        }
        return String.format("/env/common/%s/proxy/conf/%s/global.yaml", mode, regCenterType);
    }
    
    private static String getRegistryCenterType(final String mode) {
        String regCenterType = E2ETestEnvironment.getInstance().getArtifactEnvironment().getRegCenterType();
        if (Strings.isNullOrEmpty(regCenterType)) {
            return "cluster".equals(mode) ? "zookeeper" : "memory";
        }
        return regCenterType.toLowerCase();
    }
    
    private static boolean isDialectScenarioGlobalYamlExists(final String scenario, final String mode, final String regCenterType, final DatabaseType databaseType) {
        URL url = Thread.currentThread().getContextClassLoader().getResource(
                String.format("env/scenario/%s/proxy/mode/%s/%s/%s/global.yaml", scenario, mode, databaseType.getType().toLowerCase(), regCenterType));
        return null != url;
    }
    
    private static boolean isRegistryCenterGlobalYamlExists(final String scenario, final String mode, final String regCenterType) {
        URL url = Thread.currentThread().getContextClassLoader().getResource(String.format("env/scenario/%s/proxy/mode/%s/%s/global.yaml", scenario, mode, regCenterType));
        return null != url;
    }
}
