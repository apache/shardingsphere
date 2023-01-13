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

package org.apache.shardingsphere.agent.core.plugin;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class PluginContextTest {
    
    @Test
    public void assertNotPluginEnabledKey() {
        assertFalse(PluginContext.isPluginEnabled());
    }
    
    @Test
    public void assertPluginEnabledKeyIsFalse() {
        System.setProperty("AGENT_PLUGINS_ENABLED", "false");
        assertFalse(PluginContext.isPluginEnabled());
    }
    
    @Test
    public void assertPluginEnabledKeyIsZero() {
        System.setProperty("AGENT_PLUGINS_ENABLED", "0");
        assertFalse(PluginContext.isPluginEnabled());
    }
    
    @Test
    public void assertPluginEnabled() {
        System.setProperty("AGENT_PLUGINS_ENABLED", "1");
        assertTrue(PluginContext.isPluginEnabled());
        System.setProperty("AGENT_PLUGINS_ENABLED", "true");
        assertTrue(PluginContext.isPluginEnabled());
    }
}
