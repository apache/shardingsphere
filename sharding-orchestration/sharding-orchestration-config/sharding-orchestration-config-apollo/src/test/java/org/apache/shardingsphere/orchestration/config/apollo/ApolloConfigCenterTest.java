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

package org.apache.shardingsphere.orchestration.config.apollo;

import com.ctrip.framework.apollo.mockserver.EmbeddedApollo;
import org.apache.shardingsphere.orchestration.config.api.ConfigCenter;
import org.apache.shardingsphere.orchestration.config.api.ConfigCenterConfiguration;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ApolloConfigCenterTest {
    
    static {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }
    
    @ClassRule
    public static EmbeddedApollo embeddedApollo = new EmbeddedApollo();
    
    private static ConfigCenter configCenter = new ApolloConfigCenter();
    
    @BeforeClass
    public static void init() {
        ConfigCenterConfiguration configuration = new ConfigCenterConfiguration(configCenter.getType(), new Properties());
        configuration.setServerLists("http://config-service-url");
        configuration.setNamespace("orchestration");
        configCenter.init(configuration);
    }
    
    @Test
    public void assertGet() {
        assertThat(configCenter.get("key1"), is("value1"));
    }
    
    @Test
    public void assertGetDirectly() {
        assertThat(configCenter.getDirectly("key2"), is("value2"));
    }
    
    @Test
    public void assertIsExisted() {
        assertThat(configCenter.isExisted("key1"), is(true));
    }
}
