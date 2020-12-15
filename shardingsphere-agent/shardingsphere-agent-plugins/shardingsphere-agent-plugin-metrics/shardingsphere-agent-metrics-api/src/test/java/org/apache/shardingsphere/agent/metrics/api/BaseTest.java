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

package org.apache.shardingsphere.agent.metrics.api;

import java.io.IOException;
import java.net.URL;
import org.apache.shardingsphere.agent.core.config.AgentConfiguration;
import org.apache.shardingsphere.agent.core.config.AgentConfigurationLoader;
import org.apache.shardingsphere.agent.core.utils.SingletonHolder;
import org.junit.Before;

public class BaseTest {
    
    private static final String DEFAULT_CONFIG_PATH = "/conf/agent.yaml";
    
    @Before
    public void assertLoad() throws IOException {
        System.setProperty("agent-path", getResourceUrl());
        AgentConfiguration configuration = AgentConfigurationLoader.load();
        SingletonHolder.INSTANCE.put(configuration);
    }
    
    private static String getResourceUrl() {
        URL url = AgentConfigurationLoader.class.getResource(DEFAULT_CONFIG_PATH);
        if (null != url) {
            return url.getFile();
        }
        return DEFAULT_CONFIG_PATH;
    }
}
