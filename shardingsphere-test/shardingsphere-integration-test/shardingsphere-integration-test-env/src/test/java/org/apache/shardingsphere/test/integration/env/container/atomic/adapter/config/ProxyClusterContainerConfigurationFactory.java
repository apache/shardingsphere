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

package org.apache.shardingsphere.test.integration.env.container.atomic.adapter.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.Map;

/**
 * Proxy cluster container configuration factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProxyClusterContainerConfigurationFactory {
    
    /**
     * Create new instance of adaptor container configuration.
     *
     * @return created instance
     */
    public static AdaptorContainerConfiguration newInstance() {
        return new AdaptorContainerConfiguration("", getMountedResources());
    }
    
    private static Map<String, String> getMountedResources() {
        return Collections.singletonMap("/env/logback.xml", "/opt/shardingsphere-proxy/conf/logback.xml");
    }
}
