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

package org.apache.shardingsphere.orchestration.repository.apollo.wrapper;

import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.enums.PropertyChangeType;
import com.ctrip.framework.apollo.mockserver.EmbeddedApollo;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.google.common.util.concurrent.SettableFuture;
import org.apache.shardingsphere.orchestration.repository.api.config.OrchestrationCenterConfiguration;
import org.apache.shardingsphere.orchestration.repository.apollo.ApolloProperties;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ApolloConfigWrapperTest {
    
    static {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }
    
    @ClassRule
    public static EmbeddedApollo embeddedApollo = new EmbeddedApollo();
    
    private ApolloConfigWrapper configWrapper;
    
    @Before
    public void setup() {
        configWrapper = new ApolloConfigWrapper(
                "orchestration", new OrchestrationCenterConfiguration("Apollo", "http://config-service-url", new Properties()), new ApolloProperties(new Properties()));
    }
    
    @Test
    public void assertGetProperty() {
        assertThat(configWrapper.getProperty("test.children.1"), is("value1"));
    }
    
    @Test
    public void assertAddChangeListener() throws InterruptedException, TimeoutException, ExecutionException {
        SettableFuture<ConfigChangeEvent> future = SettableFuture.create();
        ConfigChangeListener listener = future::set;
        configWrapper.addChangeListener(listener, Collections.singleton("test.children.2"));
        embeddedApollo.addOrModifyProperty("orchestration", "test.children.2", "value3");
        ConfigChangeEvent changeEvent = future.get(5, TimeUnit.SECONDS);
        assertTrue(changeEvent.isChanged("test.children.2"));
        assertThat(changeEvent.getChange("test.children.2").getOldValue(), is("value2"));
        assertThat(changeEvent.getChange("test.children.2").getNewValue(), is("value3"));
        assertThat(changeEvent.getChange("test.children.2").getChangeType(), is(PropertyChangeType.MODIFIED));
    }
    
    @Test
    public void assertAddChangeListenerWithInterestedKeyPrefixes() throws InterruptedException, TimeoutException, ExecutionException {
        SettableFuture<ConfigChangeEvent> future = SettableFuture.create();
        ConfigChangeListener listener = future::set;
        configWrapper.addChangeListener(listener, Collections.singleton("test.children.1"), Collections.singleton("test.children.2"));
        embeddedApollo.addOrModifyProperty("orchestration", "test.children.2.1", "value4");
        ConfigChangeEvent changeEvent = future.get(5, TimeUnit.SECONDS);
        assertTrue(changeEvent.isChanged("test.children.2.1"));
        assertThat(changeEvent.getChange("test.children.2.1").getNewValue(), is("value4"));
        assertThat(changeEvent.getChange("test.children.2.1").getChangeType(), is(PropertyChangeType.ADDED));
    }
}
