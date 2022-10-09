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

package org.apache.shardingsphere.integration.data.pipeline.framework.container.config.proxy;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.test.integration.env.container.atomic.adapter.config.AdaptorContainerConfiguration;
import org.apache.shardingsphere.test.integration.env.container.atomic.adapter.config.ProxyClusterContainerConfigurationFactory;
import org.apache.shardingsphere.test.integration.env.container.atomic.constants.ProxyContainerConstants;
import org.apache.shardingsphere.test.integration.env.container.atomic.util.AdapterContainerUtil;
import org.apache.shardingsphere.test.integration.env.container.atomic.util.DatabaseTypeUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Scaling proxy cluster container configuration factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ScalingProxyClusterContainerConfigurationFactory {
    
    /**
     * Create instance of adaptor container configuration.
     * 
     * @param databaseType database type
     * @param storageContainerImage storage container image
     * @return created instance
     */
    public static AdaptorContainerConfiguration newInstance(final DatabaseType databaseType, final String storageContainerImage) {
        return new AdaptorContainerConfiguration(getProxyDatasourceName(databaseType), getMountedResource(databaseType, storageContainerImage), AdapterContainerUtil.getAdapterContainerImage());
    }
    
    private static String getProxyDatasourceName(final DatabaseType databaseType) {
        return (DatabaseTypeUtil.isPostgreSQL(databaseType) || DatabaseTypeUtil.isOpenGauss(databaseType)) ? "postgres" : "";
    }
    
    private static Map<String, String> getMountedResource(final DatabaseType databaseType, final String storageContainerImage) {
        Map<String, String> result = new HashMap<>(2, 1);
        result.putAll(ProxyClusterContainerConfigurationFactory.newInstance().getMountedResources());
        if (DatabaseTypeUtil.isMySQL(databaseType)) {
            String majorVersion = DatabaseTypeUtil.parseMajorVersion(storageContainerImage);
            result.put(String.format("/env/%s/server-%s.yaml", databaseType.getType().toLowerCase(), majorVersion), ProxyContainerConstants.CONFIG_PATH_IN_CONTAINER + "server.yaml");
        } else {
            result.put(String.format("/env/%s/server.yaml", databaseType.getType().toLowerCase()), ProxyContainerConstants.CONFIG_PATH_IN_CONTAINER + "server.yaml");
        }
        return result;
    }
}
