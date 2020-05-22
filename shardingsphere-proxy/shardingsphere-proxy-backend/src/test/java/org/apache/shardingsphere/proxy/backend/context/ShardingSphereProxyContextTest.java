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

package org.apache.shardingsphere.proxy.backend.context;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.auth.ProxyUser;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.kernal.context.SchemaContext;
import org.apache.shardingsphere.kernal.context.SchemaContexts;
import org.apache.shardingsphere.orchestration.core.common.event.AuthenticationChangedEvent;
import org.apache.shardingsphere.orchestration.core.common.event.PropertiesChangedEvent;
import org.apache.shardingsphere.orchestration.core.common.eventbus.ShardingOrchestrationEventBus;
import org.apache.shardingsphere.orchestration.core.registrycenter.event.CircuitStateChangedEvent;
import org.apache.shardingsphere.proxy.backend.schema.ProxySchemaContexts;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class ShardingSphereProxyContextTest {
    
    @Before
    @SneakyThrows(ReflectiveOperationException.class)
    public void setUp() {
        Field schemaContexts = ProxySchemaContexts.getInstance().getClass().getDeclaredField("schemaContexts");
        schemaContexts.setAccessible(true);
        schemaContexts.set(ProxySchemaContexts.getInstance(), new SchemaContexts(getSchemaContextMap(), new ConfigurationProperties(new Properties()), new Authentication()));
    }
    
    private Map<String, SchemaContext> getSchemaContextMap() {
        Map<String, SchemaContext> result = new HashMap<>(10);
        for (int i = 0; i < 10; i++) {
            result.put("schema_" + i, mock(SchemaContext.class));
        }
        return result;
    }
    
    @Test
    public void assertRenewProperties() {
        assertTrue(ProxySchemaContexts.getInstance().getSchemaContexts().getProperties().getProps().isEmpty());
        Properties props = new Properties();
        props.setProperty(ConfigurationPropertyKey.SQL_SHOW.getKey(), Boolean.TRUE.toString());
        ShardingOrchestrationEventBus.getInstance().post(new PropertiesChangedEvent(props));
        assertFalse(ProxySchemaContexts.getInstance().getSchemaContexts().getProperties().getProps().isEmpty());
    }
    
    @Test
    public void assertRenewAuthentication() {
        ProxyUser proxyUser = new ProxyUser("root", Collections.singleton("db1"));
        Authentication authentication = new Authentication();
        authentication.getUsers().put("root", proxyUser);
        ShardingOrchestrationEventBus.getInstance().post(new AuthenticationChangedEvent(authentication));
        assertThat(ProxySchemaContexts.getInstance().getSchemaContexts().getAuthentication().getUsers().keySet().iterator().next(), is("root"));
        assertThat(ProxySchemaContexts.getInstance().getSchemaContexts().getAuthentication().getUsers().get("root").getPassword(), is("root"));
        assertThat(ProxySchemaContexts.getInstance().getSchemaContexts().getAuthentication().getUsers().get("root").getAuthorizedSchemas().iterator().next(), is("db1"));
    }
    
    @Test
    public void assertRenewCircuitState() {
        assertFalse(ProxySchemaContexts.getInstance().isCircuitBreak());
        ShardingOrchestrationEventBus.getInstance().post(new CircuitStateChangedEvent(true));
        assertTrue(ProxySchemaContexts.getInstance().isCircuitBreak());
        ShardingOrchestrationEventBus.getInstance().post(new CircuitStateChangedEvent(false));
    }
}
