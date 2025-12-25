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

package org.apache.shardingsphere.agent.plugin.logging.file;

import lombok.SneakyThrows;
import org.apache.shardingsphere.agent.api.PluginConfiguration;
import org.apache.shardingsphere.agent.plugin.core.context.PluginContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileLoggingPluginLifecycleServiceTest {
    
    @AfterEach
    void resetContext() throws ReflectiveOperationException {
        Plugins.getMemberAccessor().set(PluginContext.class.getDeclaredField("isEnhancedForProxy"), PluginContext.getInstance(), false);
    }
    
    @Test
    void assertStart() {
        try (FileLoggingPluginLifecycleService lifecycleService = new FileLoggingPluginLifecycleService()) {
            lifecycleService.start(new PluginConfiguration("127.0.0.1", 9000, "pwd", new Properties()), true);
            assertTrue(getEnhancedForProxy());
            lifecycleService.start(new PluginConfiguration("127.0.0.1", 9000, "pwd", new Properties()), false);
            assertFalse(getEnhancedForProxy());
            assertThat(lifecycleService.getType(), is("File"));
        }
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private boolean getEnhancedForProxy() {
        return (boolean) Plugins.getMemberAccessor().get(PluginContext.class.getDeclaredField("isEnhancedForProxy"), PluginContext.getInstance());
        
    }
}
