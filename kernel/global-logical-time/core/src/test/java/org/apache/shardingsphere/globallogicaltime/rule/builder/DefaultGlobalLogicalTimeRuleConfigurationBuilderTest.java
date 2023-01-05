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

import org.apache.shardingsphere.globallogicaltime.config.GlobalLogicalTimeRuleConfiguration;
import org.apache.shardingsphere.globallogicaltime.constant.GlobalLogicalTimeOrder;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class DefaultGlobalLogicalTimeRuleConfigurationBuilderTest {
    
    @Test
    public void assertBuild() {
        GlobalLogicalTimeRuleConfiguration actual = new DefaultGlobalLogicalTimeRuleConfigurationBuilder().build();
        assertFalse(actual.isGlobalLogicalTimeEnabled());
        assertEquals(actual.getRedisOption().getHost(), "127.0.0.1");
        assertEquals(actual.getRedisOption().getPort(), "6379");
        assertEquals(actual.getRedisOption().getPassword(), "");
        assertEquals(actual.getRedisOption().getTimeoutInterval(), 40000);
        assertEquals(actual.getRedisOption().getMaxIdle(), 8);
        assertEquals(actual.getRedisOption().getMaxTotal(), 18);
        assertEquals(actual.getRedisOption().getLockExpirationTime(), 10);
    }
    
    @Test
    public void assertGetOrder() {
        assertEquals(new DefaultGlobalLogicalTimeRuleConfigurationBuilder().getOrder(), GlobalLogicalTimeOrder.ORDER);
    }
    
    @Test
    public void assertGetTypeClass() {
        assertEquals(new DefaultGlobalLogicalTimeRuleConfigurationBuilder().getTypeClass(), GlobalLogicalTimeRuleBuilder.class);
    }
}
