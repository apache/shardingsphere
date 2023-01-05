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

package org.apache.shardingsphere.globallogicaltime.rule.builder;

import org.apache.shardingsphere.globallogicaltime.GlobalLogicalTimeEngine;
import org.apache.shardingsphere.globallogicaltime.config.GlobalLogicalTimeRuleConfiguration;
import org.apache.shardingsphere.globallogicaltime.config.RedisConnectionOptionConfiguration;
import org.apache.shardingsphere.globallogicaltime.constant.GlobalLogicalTimeOrder;
import org.apache.shardingsphere.globallogicaltime.rule.GlobalLogicalTimeRule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GlobalLogicalTimeRuleBuilderTest {
    
    @Test
    public void assertGetOrder() {
        assertEquals(new GlobalLogicalTimeRuleBuilder().getOrder(), GlobalLogicalTimeOrder.ORDER);
    }
    
    @Test
    public void assetGetTypeClass() {
        assertEquals(new GlobalLogicalTimeRuleBuilder().getTypeClass(), GlobalLogicalTimeRuleConfiguration.class);
    }
    
    @Test
    public void assertBuild() {
        GlobalLogicalTimeRuleConfiguration configuration = new GlobalLogicalTimeRuleConfiguration(
                true, new RedisConnectionOptionConfiguration("127.0.0.1", "6379", "", 40, 8, 18, 10));
        GlobalLogicalTimeEngine engine = mock(GlobalLogicalTimeEngine.class);
        GlobalLogicalTimeRule actual = mock(GlobalLogicalTimeRule.class);
        
        when(actual.getGlobalLogicalTimeEngine()).thenReturn(engine);
        when(actual.isGlobalLogicalTimeEnabled()).thenReturn(configuration.isGlobalLogicalTimeEnabled());
        when(actual.getType()).thenReturn(actual.getClass().getSimpleName());
        when(actual.getRedisOption()).thenReturn(configuration.getRedisOption());
        when(actual.getConfiguration()).thenReturn(configuration);
        
        assertTrue(actual.getConfiguration().isGlobalLogicalTimeEnabled());
        assertEquals(actual.getConfiguration().getRedisOption().getHost(), "127.0.0.1");
        assertEquals(actual.getConfiguration().getRedisOption().getPort(), "6379");
        assertEquals(actual.getConfiguration().getRedisOption().getPassword(), "");
        assertEquals(actual.getConfiguration().getRedisOption().getTimeoutInterval(), 40);
        assertEquals(actual.getConfiguration().getRedisOption().getMaxIdle(), 8);
        assertEquals(actual.getConfiguration().getRedisOption().getMaxTotal(), 18);
        assertEquals(actual.getConfiguration().getRedisOption().getLockExpirationTime(), 10);
        
        assertTrue(actual.isGlobalLogicalTimeEnabled());
        assertEquals(actual.getRedisOption().getHost(), "127.0.0.1");
        assertEquals(actual.getRedisOption().getPort(), "6379");
        assertEquals(actual.getRedisOption().getPassword(), "");
        assertEquals(actual.getRedisOption().getTimeoutInterval(), 40);
        assertEquals(actual.getRedisOption().getMaxIdle(), 8);
        assertEquals(actual.getRedisOption().getMaxTotal(), 18);
        assertEquals(actual.getRedisOption().getLockExpirationTime(), 10);
    }
}
