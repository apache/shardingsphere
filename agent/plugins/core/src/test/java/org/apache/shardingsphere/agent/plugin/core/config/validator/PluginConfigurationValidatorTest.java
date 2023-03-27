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

package org.apache.shardingsphere.agent.plugin.core.config.validator;

import org.apache.shardingsphere.agent.api.PluginConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class PluginConfigurationValidatorTest {
    
    @Test
    void assertValidateHostAndPortSuccess() {
        PluginConfigurationValidator.validateHostAndPort("foo_type", new PluginConfiguration("localhost", 8080, "pwd", null));
    }
    
    @Test
    void assertValidateHostAndPortWhenHostIsEmpty() {
        assertThrows(IllegalArgumentException.class, () -> PluginConfigurationValidator.validateHostAndPort("foo_type", new PluginConfiguration("", 8080, "pwd", null)));
    }
    
    @Test
    void assertValidateHostAndPortWhenHostIsNull() {
        assertThrows(IllegalArgumentException.class, () -> PluginConfigurationValidator.validateHostAndPort("foo_type", new PluginConfiguration(null, 8080, "pwd", null)));
    }
    
    @Test
    void assertValidateHostAndPortWhenPortLessThanOne() {
        assertThrows(IllegalArgumentException.class, () -> PluginConfigurationValidator.validateHostAndPort("foo_type", new PluginConfiguration("localhost", 0, "pwd", null)));
    }
}
