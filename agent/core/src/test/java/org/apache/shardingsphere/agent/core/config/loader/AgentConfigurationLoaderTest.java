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

package org.apache.shardingsphere.agent.core.config.loader;

import org.apache.shardingsphere.agent.core.config.path.AgentPathBuilder;
import org.apache.shardingsphere.infra.util.reflect.ReflectiveUtil;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;

import static org.junit.Assert.assertNotNull;

public final class AgentConfigurationLoaderTest {
    
    private static final String DEFAULT_CONFIG_PATH = "/conf/agent.yaml";
    
    @Test
    public void assertLoad() throws IOException {
        ReflectiveUtil.setStaticField(AgentPathBuilder.class, "agentPath", new File(getResourceUrl()));
        assertNotNull(AgentConfigurationLoader.load());
    }
    
    private String getResourceUrl() {
        URL url = AgentConfigurationLoader.class.getClassLoader().getResource("");
        return null == url ? DEFAULT_CONFIG_PATH : URLDecoder.decode(url.getFile());
    }
}
