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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.test.integration.env.container.atomic.adapter.config.AdaptorContainerConfiguration;
import org.apache.shardingsphere.test.integration.env.container.atomic.util.DatabaseTypeUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Scaling proxy cluster container configuration.
 */
@RequiredArgsConstructor
public final class ScalingProxyClusterContainerConfiguration implements AdaptorContainerConfiguration {
    
    private final DatabaseType databaseType;
    
    private final String dockerImageName;
    
    @Override
    public Map<String, String> getWaitStrategyInfo() {
        return Collections.singletonMap("dataSourceName", DatabaseTypeUtil.isPostgreSQL(databaseType) ? "postgres" : "");
    }
    
    @Override
    public Map<String, String> getResourceMappings(final String scenario, final DatabaseType databaseType) {
        Map<String, String> result = new HashMap<>(2, 1);
        result.put("/env/logback.xml", "/opt/shardingsphere-proxy/conf/logback.xml");
        if (DatabaseTypeUtil.isMySQL(databaseType)) {
            String majorVersion = DatabaseTypeUtil.parseMajorVersion(dockerImageName);
            result.put(String.format("/env/%s/server-%s.yaml", databaseType.getType().toLowerCase(), majorVersion), "/opt/shardingsphere-proxy/conf/server.yaml");
        } else {
            result.put(String.format("/env/%s/server.yaml", databaseType.getType().toLowerCase()), "/opt/shardingsphere-proxy/conf/server.yaml");
        }
        return result;
    }
}
