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

package org.apache.shardingsphere.test.e2e.container.config.proxy;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.test.e2e.env.container.atomic.adapter.config.AdaptorContainerConfiguration;
import org.apache.shardingsphere.test.e2e.env.container.atomic.adapter.config.ProxyClusterContainerConfigurationFactory;
import org.apache.shardingsphere.test.e2e.env.container.atomic.constants.ProxyContainerConstants;
import org.apache.shardingsphere.test.e2e.env.container.atomic.util.AdapterContainerUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Pipeline proxy cluster container configuration factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProxyContainerConfigurationFactory {
    
    /**
     * Create instance of adaptor container configuration.
     *
     * @return created instance
     */
    public static AdaptorContainerConfiguration newInstance() {
        return new AdaptorContainerConfiguration("", getMountedResource(), AdapterContainerUtils.getAdapterContainerImage());
    }
    
    private static Map<String, String> getMountedResource() {
        Map<String, String> result = new HashMap<>(2, 1);
        result.putAll(ProxyClusterContainerConfigurationFactory.newInstance().getMountedResources());
        result.put("/env/mysql/server.yaml", ProxyContainerConstants.CONFIG_PATH_IN_CONTAINER + "server.yaml");
        result.put("/env/logback.xml", ProxyContainerConstants.CONFIG_PATH_IN_CONTAINER + "logback.xml");
        return result;
    }
}
