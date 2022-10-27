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

package org.apache.shardingsphere.agent.config;

import static org.junit.Assert.assertThrows;

import org.junit.Test;

public final class PluginConfigurationTest {
    
    private PluginConfiguration pluginConfiguration;
    
    @Test
    public void assertValidateWhenHostIsNullOrEmpty() {
        pluginConfiguration = new PluginConfiguration("", 8080, "passkey", null);
        assertThrows("Hostname of host1 is required", IllegalArgumentException.class, () -> pluginConfiguration.validate("host1"));
        pluginConfiguration = new PluginConfiguration(null, 8080, "passkey", null);
        assertThrows("Hostname of host2 is required", IllegalArgumentException.class, () -> pluginConfiguration.validate("host2"));
    }
    
    @Test
    public void assertValidateWhenPortLessThanOne() {
        pluginConfiguration = new PluginConfiguration("localhost", 0, "passkey", null);
        assertThrows("Port `0` of host1 must be a positive number", IllegalArgumentException.class, () -> pluginConfiguration.validate("host1"));
        pluginConfiguration = new PluginConfiguration("localhost", -1, "passkey", null);
        assertThrows("Port `-1` of host2 must be a positive number", IllegalArgumentException.class, () -> pluginConfiguration.validate("host2"));
    }
}
