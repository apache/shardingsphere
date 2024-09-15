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

package org.apache.shardingsphere.mode.manager.cluster.event.subscriber.dispatch;

import org.apache.shardingsphere.mode.event.dispatch.config.AlterPropertiesEvent;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Properties;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PropertiesEventSubscriberTest {
    
    private PropertiesEventSubscriber subscriber;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ContextManager contextManager;
    
    @BeforeEach
    void setUp() {
        subscriber = new PropertiesEventSubscriber(contextManager);
    }
    
    @Test
    void assertRenew() {
        when(contextManager.getPersistServiceFacade().getMetaDataPersistService().getMetaDataVersionPersistService().getActiveVersionByFullPath("key")).thenReturn("value");
        when(contextManager.getPersistServiceFacade().getMetaDataPersistService().getPropsService().load()).thenReturn(new Properties());
        subscriber.renew(new AlterPropertiesEvent("key", "value"));
        verify(contextManager.getMetaDataContextManager().getGlobalConfigurationManager()).alterProperties(new Properties());
    }
}
