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

import org.apache.shardingsphere.mode.manager.ContextManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import java.lang.reflect.Field;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

class ProxyContextTest {
    
    private ContextManager originalContextManager;
    
    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        Field contextManagerField = ProxyContext.class.getDeclaredField("contextManager");
        originalContextManager = (ContextManager) Plugins.getMemberAccessor().get(contextManagerField, ProxyContext.getInstance());
        Plugins.getMemberAccessor().set(contextManagerField, ProxyContext.getInstance(), null);
    }
    
    @AfterEach
    void cleanUp() throws ReflectiveOperationException {
        Field contextManagerField = ProxyContext.class.getDeclaredField("contextManager");
        Plugins.getMemberAccessor().set(contextManagerField, ProxyContext.getInstance(), originalContextManager);
    }
    
    @Test
    void assertInit() {
        ProxyContext actualProxyContext = ProxyContext.getInstance();
        ContextManager expectedContextManager = mock(ContextManager.class);
        ProxyContext.init(expectedContextManager);
        assertThat(actualProxyContext.getContextManager(), is(expectedContextManager));
    }
}
