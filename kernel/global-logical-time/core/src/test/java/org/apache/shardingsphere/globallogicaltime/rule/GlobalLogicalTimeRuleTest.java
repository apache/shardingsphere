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

package org.apache.shardingsphere.globallogicaltime.rule;

import org.apache.shardingsphere.globallogicaltime.GlobalLogicalTimeEngine;
import org.apache.shardingsphere.globallogicaltime.config.GlobalLogicalTimeRuleConfiguration;
import org.apache.shardingsphere.globallogicaltime.config.RedisConnectionOptionConfiguration;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class GlobalLogicalTimeRuleTest {
    
    private GlobalLogicalTimeRule globalLogicalTimeRule;
    
    @Before
    public void setup() {
        globalLogicalTimeRule = mockGlobalLogicalTimeRule(new GlobalLogicalTimeRuleConfiguration(
                true, new RedisConnectionOptionConfiguration("127.0.0.1", "6379", "", 40, 8, 18, 10)));
    }
    
    /**
     * Mock global logical time rule.
     *
     * @param configuration configuration
     * @return global logical time rule
     */
    public GlobalLogicalTimeRule mockGlobalLogicalTimeRule(final GlobalLogicalTimeRuleConfiguration configuration) {
        GlobalLogicalTimeEngine engine = mock(GlobalLogicalTimeEngine.class);
        GlobalLogicalTimeRule rule = mock(GlobalLogicalTimeRule.class);
        
        when(rule.getGlobalLogicalTimeEngine()).thenReturn(engine);
        when(rule.isGlobalLogicalTimeEnabled()).thenReturn(configuration.isGlobalLogicalTimeEnabled());
        when(rule.getType()).thenReturn(rule.getClass().getSimpleName());
        when(rule.getRedisOption()).thenReturn(configuration.getRedisOption());
        when(rule.getConfiguration()).thenReturn(configuration);
        return rule;
    }
    
    @Test
    public void assertGetGlobalLogicalTimeEngine() {
        assertNotNull(globalLogicalTimeRule.getGlobalLogicalTimeEngine());
    }
    
    @Test
    public void assertGetType() {
        assertEquals(globalLogicalTimeRule.getType(), GlobalLogicalTimeRule.class.getSimpleName());
    }
    
    @Test
    public void assertFields() {
        // "127.0.0.1", "6379", "", 40, 8, 18, 10
        assertTrue(globalLogicalTimeRule.getConfiguration().isGlobalLogicalTimeEnabled());
        assertEquals(globalLogicalTimeRule.getConfiguration().getRedisOption().getHost(), "127.0.0.1");
        assertEquals(globalLogicalTimeRule.getConfiguration().getRedisOption().getPort(), "6379");
        assertEquals(globalLogicalTimeRule.getConfiguration().getRedisOption().getPassword(), "");
        assertEquals(globalLogicalTimeRule.getConfiguration().getRedisOption().getTimeoutInterval(), 40);
        assertEquals(globalLogicalTimeRule.getConfiguration().getRedisOption().getMaxIdle(), 8);
        assertEquals(globalLogicalTimeRule.getConfiguration().getRedisOption().getMaxTotal(), 18);
        assertEquals(globalLogicalTimeRule.getConfiguration().getRedisOption().getLockExpirationTime(), 10);
        
        assertTrue(globalLogicalTimeRule.isGlobalLogicalTimeEnabled());
        assertEquals(globalLogicalTimeRule.getRedisOption().getHost(), "127.0.0.1");
        assertEquals(globalLogicalTimeRule.getRedisOption().getPort(), "6379");
        assertEquals(globalLogicalTimeRule.getRedisOption().getPassword(), "");
        assertEquals(globalLogicalTimeRule.getRedisOption().getTimeoutInterval(), 40);
        assertEquals(globalLogicalTimeRule.getRedisOption().getMaxIdle(), 8);
        assertEquals(globalLogicalTimeRule.getRedisOption().getMaxTotal(), 18);
        assertEquals(globalLogicalTimeRule.getRedisOption().getLockExpirationTime(), 10);
    }
}
