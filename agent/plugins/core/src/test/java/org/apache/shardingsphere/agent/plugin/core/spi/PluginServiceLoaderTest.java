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

package org.apache.shardingsphere.agent.plugin.core.spi;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.internal.configuration.plugins.Plugins;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class PluginServiceLoaderTest {
    
    @AfterEach
    void reset() throws ReflectiveOperationException {
        ((Map<?, ?>) Plugins.getMemberAccessor().get(PluginServiceLoader.class.getDeclaredField("LOADERS"), null)).clear();
    }
    
    @SuppressWarnings("rawtypes")
    @Test
    void assertGetServiceLoader() {
        PluginTypedSPI service = mock(PluginTypedSPI.class);
        when(service.getType()).thenReturn("FIXTURE");
        try (MockedStatic<ServiceLoader> ignored = mockServiceLoader(service)) {
            assertThat(PluginServiceLoader.getServiceLoader(PluginTypedSPI.class).getService("fixture").getType(), is("FIXTURE"));
        }
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private MockedStatic<ServiceLoader> mockServiceLoader(final PluginTypedSPI... services) {
        ServiceLoader<PluginTypedSPI> serviceLoader = mock(ServiceLoader.class);
        Iterator<PluginTypedSPI> iterator = services.length == 0 ? Collections.emptyIterator() : Arrays.asList(services).iterator();
        when(serviceLoader.iterator()).thenReturn(iterator);
        return mockStatic(ServiceLoader.class, invocation -> {
            if ("load".equals(invocation.getMethod().getName())) {
                return serviceLoader;
            }
            return invocation.callRealMethod();
        });
    }
}
