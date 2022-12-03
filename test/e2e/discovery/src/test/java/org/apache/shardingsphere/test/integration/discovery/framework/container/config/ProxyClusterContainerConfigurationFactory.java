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

package org.apache.shardingsphere.test.integration.discovery.framework.container.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.test.integration.env.container.atomic.adapter.config.AdaptorContainerConfiguration;
import org.apache.shardingsphere.test.integration.env.container.atomic.constants.ProxyContainerConstants;
import org.apache.shardingsphere.test.integration.env.container.atomic.util.AdapterContainerUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Proxy cluster container configuration factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProxyClusterContainerConfigurationFactory {
    
    /**
     * Create new instance of adaptor container configuration.
     * 
     * @param scenario scenario
     * @return created instance
     */
    public static AdaptorContainerConfiguration newInstance(final String scenario) {
        return new AdaptorContainerConfiguration("", getMountedResources(scenario), AdapterContainerUtil.getAdapterContainerImage());
    }
    
    private static Map<String, String> getMountedResources(final String scenario) {
        Map<String, String> result = new HashMap<>(2, 1);
        result.put("logback-test.xml", ProxyContainerConstants.CONFIG_PATH_IN_CONTAINER + "logback.xml");
        result.put(String.format("env/scenario/%s/proxy/server.yaml", scenario), ProxyContainerConstants.CONFIG_PATH_IN_CONTAINER + "server.yaml");
        return result;
    }
}
