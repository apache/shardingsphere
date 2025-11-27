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

package org.apache.shardingsphere.mode.manager.listener;

import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ShardingSphereServiceLoader.class)
class ContextManagerLifecycleListenerFactoryTest {
    
    @Test
    void assertGetListeners() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getComputeNodeInstanceContext().getModeConfiguration()).thenReturn(new ModeConfiguration("Cluster", mock()));
        ContextManagerLifecycleListener noAnnotationListener = mock(ContextManagerLifecycleListener.class);
        ContextManagerLifecycleListener matchedModeListener = new MatchedModeListener();
        ContextManagerLifecycleListener mismatchedModeListener = new MismatchedModeListener();
        Collection<ContextManagerLifecycleListener> listeners = Arrays.asList(noAnnotationListener, matchedModeListener, mismatchedModeListener);
        when(ShardingSphereServiceLoader.getServiceInstances(ContextManagerLifecycleListener.class)).thenReturn(listeners);
        Collection<ContextManagerLifecycleListener> actual = ContextManagerLifecycleListenerFactory.getListeners(contextManager);
        assertThat(actual, hasSize(2));
        assertTrue(actual.contains(noAnnotationListener));
        assertTrue(actual.contains(matchedModeListener));
        assertFalse(actual.contains(mismatchedModeListener));
    }

    @ContextManagerLifecycleListenerModeRequired("Cluster")
    private static final class MatchedModeListener implements ContextManagerLifecycleListener {
        
        @Override
        public void onInitialized(final ContextManager contextManager) {
        }
        
        @Override
        public void onDestroyed(final ContextManager contextManager) {
        }
    }
    
    @ContextManagerLifecycleListenerModeRequired("Standalone")
    private static final class MismatchedModeListener implements ContextManagerLifecycleListener {
        
        @Override
        public void onInitialized(final ContextManager contextManager) {
        }
        
        @Override
        public void onDestroyed(final ContextManager contextManager) {
        }
    }
}
