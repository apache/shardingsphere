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

package org.apache.shardingsphere.orchestration.center.instance;

import com.ctrip.framework.apollo.mockserver.EmbeddedApollo;
import com.google.common.util.concurrent.SettableFuture;
import lombok.SneakyThrows;
import org.apache.shardingsphere.orchestration.center.api.ConfigCenter;
import org.apache.shardingsphere.orchestration.center.configuration.InstanceConfiguration;
import org.apache.shardingsphere.orchestration.center.instance.wrapper.ApolloConfigWrapper;
import org.apache.shardingsphere.orchestration.center.listener.DataChangedEvent;
import org.apache.shardingsphere.orchestration.center.listener.DataChangedEventListener;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.internal.util.reflection.FieldSetter;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ApolloInstanceTest {
    
    static {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }
    
    @ClassRule
    public static EmbeddedApollo embeddedApollo = new EmbeddedApollo();
    
    private static ConfigCenter configCenter = new ApolloInstance();
    
    @BeforeClass
    @SneakyThrows
    public static void init() {
        InstanceConfiguration configuration = new InstanceConfiguration("apollo");
        configuration.setServerLists("http://config-service-url");
        configuration.setNamespace("orchestration");
        Properties properties = new Properties();
        ApolloConfigWrapper configWrapper = new ApolloConfigWrapper(configuration, properties);
        FieldSetter.setField(configCenter, ApolloInstance.class.getDeclaredField("configWrapper"), configWrapper);
    }
    
    @Test
    public void assertGet() {
        assertThat(configCenter.get("/test/children/2"), is("value2"));
    }
    
    @Test
    @SneakyThrows
    public void assertWatch() {
        final SettableFuture<DataChangedEvent> future = SettableFuture.create();
        configCenter.watch("/test/children/1", new DataChangedEventListener() {
            
            @Override
            public void onChange(final DataChangedEvent dataChangedEvent) {
                future.set(dataChangedEvent);
            }
        });
        embeddedApollo.addOrModifyProperty("orchestration", "test.children.1", "value3");
        DataChangedEvent changeEvent = future.get(5, TimeUnit.SECONDS);
        assertThat(changeEvent.getKey(), is("/test/children/1"));
        assertThat(changeEvent.getValue(), is("value3"));
        assertThat(changeEvent.getChangedType(), is(DataChangedEvent.ChangedType.UPDATED));
    }
}
