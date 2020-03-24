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
import org.apache.shardingsphere.orchestration.center.ConfigCenterRepository;
import org.apache.shardingsphere.orchestration.center.instance.wrapper.ApolloConfigWrapper;
import org.apache.shardingsphere.orchestration.center.instance.wrapper.ApolloOpenApiWrapper;
import org.apache.shardingsphere.orchestration.center.listener.DataChangedEvent;
import org.apache.shardingsphere.orchestration.center.util.ConfigKeyUtils;
import org.apache.shardingsphere.orchestration.center.config.CenterConfiguration;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.internal.util.reflection.FieldSetter;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public final class ApolloCenterRepositoryTest {
    
    static {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }
    
    @ClassRule
    public static EmbeddedApollo embeddedApollo = new EmbeddedApollo();
    
    private static ConfigCenterRepository configCenterRepository = new ApolloCenterRepository();
    
    private static ApolloOpenApiWrapper openApiWrapper = mock(ApolloOpenApiWrapper.class);
    
    @BeforeClass
    @SneakyThrows
    public static void init() {
        CenterConfiguration configuration = new CenterConfiguration("apollo");
        configuration.setServerLists("http://config-service-url");
        configuration.setNamespace("orchestration");
        Properties properties = new Properties();
        configCenterRepository.setProperties(properties);
        ApolloConfigWrapper configWrapper = new ApolloConfigWrapper(configuration, new ApolloProperties(properties));
        FieldSetter.setField(configCenterRepository, ApolloCenterRepository.class.getDeclaredField("configWrapper"), configWrapper);
        FieldSetter.setField(configCenterRepository, ApolloCenterRepository.class.getDeclaredField("openApiWrapper"), openApiWrapper);
    }
    
    @Test
    public void assertGet() {
        assertThat(configCenterRepository.get("/test/children/2"), is("value2"));
    }
    
    @Test
    @SneakyThrows
    public void assertWatch() {
        assertWatchUpdateChangedType("/test/children/1", "value3");
    }
    
    @Test
    @SneakyThrows
    public void assertGetWithNonExistentKey() {
        assertNull(configCenterRepository.get("/test/nonExistentKey"));
    }
    
    @Test
    @SneakyThrows
    public void assertWatchUpdateChangedTypeWithExistedKey() {
        assertWatchUpdateChangedType("/test/children/1", "newValue1");
        assertThat(configCenterRepository.get("/test/children/1"), is("newValue1"));
    }
    
    @Test
    @SneakyThrows
    public void assertWatchDeletedChangedType() {
        final SettableFuture<DataChangedEvent> future = SettableFuture.create();
        configCenterRepository.watch("/test/children/1", future::set);
        embeddedApollo.deleteProperty("orchestration", "test.children.1");
        DataChangedEvent changeEvent = future.get(5, TimeUnit.SECONDS);
        assertThat(changeEvent.getKey(), is("/test/children/1"));
        assertNull(changeEvent.getValue());
        assertThat(changeEvent.getChangedType(), is(DataChangedEvent.ChangedType.DELETED));
        assertNull(configCenterRepository.get("/test/children/1"));
    }
    
    @Test
    @SneakyThrows
    public void assertWatchUpdateChangedTypeWithNotExistedKey() {
        assertWatchUpdateChangedType("/test/children/newKey", "newVaule");
    }
    
    @SneakyThrows
    private void assertWatchUpdateChangedType(final String key, final String newVaule) {
        final SettableFuture<DataChangedEvent> future = SettableFuture.create();
        configCenterRepository.watch(key, future::set);
        embeddedApollo.addOrModifyProperty("orchestration", ConfigKeyUtils.path2Key(key), newVaule);
        DataChangedEvent changeEvent = future.get(5, TimeUnit.SECONDS);
        assertThat(changeEvent.getKey(), is(key));
        assertThat(changeEvent.getValue(), is(newVaule));
        assertThat(changeEvent.getChangedType(), is(DataChangedEvent.ChangedType.UPDATED));
    }
}
