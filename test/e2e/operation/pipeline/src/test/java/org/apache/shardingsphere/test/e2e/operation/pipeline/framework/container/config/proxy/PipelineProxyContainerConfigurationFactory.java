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

package org.apache.shardingsphere.test.e2e.operation.pipeline.framework.container.config.proxy;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.opengauss.type.OpenGaussDatabaseType;
import org.apache.shardingsphere.database.connector.postgresql.type.PostgreSQLDatabaseType;
import org.apache.shardingsphere.test.e2e.env.container.adapter.config.AdaptorContainerConfiguration;
import org.apache.shardingsphere.test.e2e.env.container.constants.ProxyContainerConstants;
import org.apache.shardingsphere.test.e2e.env.runtime.E2ETestEnvironment;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Pipeline proxy container configuration factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PipelineProxyContainerConfigurationFactory {
    
    /**
     * Create instance of adaptor container configuration.
     *
     * @param databaseType database type
     * @return created instance
     */
    public static AdaptorContainerConfiguration newInstance(final DatabaseType databaseType) {
        String proxyImage = E2ETestEnvironment.getInstance().getDockerEnvironment().getProxyImage();
        return new AdaptorContainerConfiguration(getProxyDatasourceName(databaseType), new LinkedList<>(), getMountedResource(databaseType), proxyImage, "");
    }
    
    private static String getProxyDatasourceName(final DatabaseType databaseType) {
        return (databaseType instanceof PostgreSQLDatabaseType || databaseType instanceof OpenGaussDatabaseType) ? "postgres" : "";
    }
    
    private static Map<String, String> getMountedResource(final DatabaseType databaseType) {
        Map<String, String> result = new HashMap<>(2, 1F);
        result.put(String.format("env/container/proxy/%s/global.yaml", databaseType.getType().toLowerCase()), ProxyContainerConstants.CONFIG_PATH_IN_CONTAINER + "global.yaml");
        result.put("env/logback.xml", ProxyContainerConstants.CONFIG_PATH_IN_CONTAINER + "logback.xml");
        return result;
    }
}
