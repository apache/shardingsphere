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

import org.junit.Test;

import static org.junit.Assert.assertThrows;

public final class PluginConfigurationTest {
    
    @Test
    public void assertValidateWhenHostIsEmpty() {
        assertThrows("Hostname of foo_host is required", IllegalArgumentException.class, () -> new PluginConfiguration("", 8080, "pwd", null).validate("foo_host"));
    }
    
    @Test
    public void assertValidateWhenHostIsNull() {
        assertThrows("Hostname of foo_host is required", IllegalArgumentException.class, () -> new PluginConfiguration(null, 8080, "pwd", null).validate("foo_host"));
    }
    
    @Test
    public void assertValidateWhenPortLessThanOne() {
        assertThrows("Port `0` of foo_host must be a positive number", IllegalArgumentException.class, () -> new PluginConfiguration("localhost", 0, "pwd", null).validate("foo_host"));
    }
}
