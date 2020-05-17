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

package org.apache.shardingsphere.proxy.context;

import org.apache.shardingsphere.orchestration.core.common.event.AuthenticationChangedEvent;
import org.apache.shardingsphere.orchestration.core.common.event.PropertiesChangedEvent;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.auth.ProxyUser;
import org.apache.shardingsphere.orchestration.core.common.eventbus.ShardingOrchestrationEventBus;
import org.apache.shardingsphere.orchestration.core.registrycenter.event.CircuitStateChangedEvent;
import org.junit.Test;

import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ShardingSphereProxyContextTest {
    
    @Test
    public void assertInit() {
        ProxyUser proxyUser = new ProxyUser("root", Collections.singleton("db1"));
        Authentication authentication = new Authentication();
        authentication.getUsers().put("root", proxyUser);
        Properties props = new Properties();
        props.setProperty(ConfigurationPropertyKey.SQL_SHOW.getKey(), Boolean.TRUE.toString());
        props.setProperty(ConfigurationPropertyKey.PROXY_TRANSACTION_TYPE.getKey(), "BASE");
        ShardingSphereProxyContext.getInstance().init(authentication, props);
        assertThat(ShardingSphereProxyContext.getInstance().getAuthentication(), is(authentication));
        assertTrue(ShardingSphereProxyContext.getInstance().getProperties().<Boolean>getValue(ConfigurationPropertyKey.SQL_SHOW));
        assertThat(ShardingSphereProxyContext.getInstance().getProperties().getValue(ConfigurationPropertyKey.PROXY_TRANSACTION_TYPE), is("BASE"));
    }
    
    @Test
    public void assertRenewProperties() {
        ProxyUser proxyUser = new ProxyUser("root", Collections.singleton("db1"));
        Authentication authentication = new Authentication();
        authentication.getUsers().put("root", proxyUser);
        ShardingSphereProxyContext.getInstance().init(authentication, new Properties());
        assertTrue(ShardingSphereProxyContext.getInstance().getProperties().getProps().isEmpty());
        Properties props = new Properties();
        props.setProperty(ConfigurationPropertyKey.SQL_SHOW.getKey(), Boolean.TRUE.toString());
        ShardingOrchestrationEventBus.getInstance().post(new PropertiesChangedEvent(props));
        assertFalse(ShardingSphereProxyContext.getInstance().getProperties().getProps().isEmpty());
    }
    
    @Test
    public void assertRenewAuthentication() {
        ProxyUser proxyUser = new ProxyUser("root", Collections.singleton("db1"));
        Authentication authentication = new Authentication();
        authentication.getUsers().put("root", proxyUser);
        ShardingSphereProxyContext.getInstance().init(authentication, new Properties());
        ShardingOrchestrationEventBus.getInstance().post(new AuthenticationChangedEvent(authentication));
        assertThat(ShardingSphereProxyContext.getInstance().getAuthentication().getUsers().keySet().iterator().next(), is("root"));
        assertThat(ShardingSphereProxyContext.getInstance().getAuthentication().getUsers().get("root").getPassword(), is("root"));
        assertThat(ShardingSphereProxyContext.getInstance().getAuthentication().getUsers().get("root").getAuthorizedSchemas().iterator().next(), is("db1"));
    }
    
    @Test
    public void assertRenewCircuitState() {
        assertFalse(ShardingSphereProxyContext.getInstance().isCircuitBreak());
        ShardingOrchestrationEventBus.getInstance().post(new CircuitStateChangedEvent(true));
        assertTrue(ShardingSphereProxyContext.getInstance().isCircuitBreak());
        ShardingOrchestrationEventBus.getInstance().post(new CircuitStateChangedEvent(false));
    }
}
