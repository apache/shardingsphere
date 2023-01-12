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

import net.bytebuddy.utility.RandomString;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class PluginContextTest {

    private static final String PLUGIN_ENABLED_KEY = "AGENT_PLUGINS_ENABLED";

    @Test
    public void assertKeyNotExistsReturnFalse() {
        assertFalse(PluginContext.isPluginEnabled());
    }

    @Test
    public void assertPropertyValueIsAnyOtherStringBut0ReturnTrue() {
        String randomString;
        while (true) {
            randomString = RandomString.make(new Random().nextInt(20));
            if (!"0".equals(randomString)) {
                break;
            }
        }
        System.setProperty(PLUGIN_ENABLED_KEY, randomString);
        assertTrue(PluginContext.isPluginEnabled());
    }

    @Test
    public void assertPropertyValueIs0ReturnFalse() {
        System.setProperty(PLUGIN_ENABLED_KEY, "0");
        assertFalse(PluginContext.isPluginEnabled());
    }

    @Test
    public void assertPropertyValueIsLowercaseTrueReturnTrue() {
        System.setProperty(PLUGIN_ENABLED_KEY, Boolean.TRUE.toString().toLowerCase());
        assertTrue(PluginContext.isPluginEnabled());
    }

    @Test
    public void assertPropertyValueIsUppercaseTrueReturnTrue() {
        System.setProperty(PLUGIN_ENABLED_KEY, Boolean.TRUE.toString().toUpperCase());
        assertTrue(PluginContext.isPluginEnabled());
    }

    @Test
    public void assertPropertyValueIsMixedCaseTrueReturnTrue() {
        System.setProperty(PLUGIN_ENABLED_KEY, "True");
        assertTrue(PluginContext.isPluginEnabled());
    }

    @Test
    public void assertPropertyValueIsLowercaseFalseReturnFalse() {
        System.setProperty(PLUGIN_ENABLED_KEY, Boolean.FALSE.toString().toLowerCase());
        assertFalse(PluginContext.isPluginEnabled());
    }

    @Test
    public void assertPropertyValueIsUppercaseFalseReturnFalse() {
        System.setProperty(PLUGIN_ENABLED_KEY, Boolean.FALSE.toString().toUpperCase());
        assertFalse(PluginContext.isPluginEnabled());
    }

    @Test
    public void assertPropertyValueIsMixedCaseFalseReturnFalse() {
        System.setProperty(PLUGIN_ENABLED_KEY, "False");
        assertFalse(PluginContext.isPluginEnabled());
    }
}
