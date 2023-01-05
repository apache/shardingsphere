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

package org.apache.shardingsphere.globallogicaltime.yaml.swapper;

import org.apache.shardingsphere.globallogicaltime.config.RedisConnectionOptionConfiguration;
import org.apache.shardingsphere.globallogicaltime.yaml.config.YamlRedisConnectionOptionConfiguration;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class YamlRedisConnectionOptionConfigurationSwapperTest {
    
    @Test
    public void assertSwapToYamlConfiguration() {
        RedisConnectionOptionConfiguration redisConnectionOptionConfiguration = new RedisConnectionOptionConfiguration("127.0.0.1", "6379", "", 40, 8, 18, 10);
        YamlRedisConnectionOptionConfiguration actual = new YamlRedisConnectionOptionConfigurationSwapper().swapToYamlConfiguration(redisConnectionOptionConfiguration);
        assertEquals(actual.getHost(), "127.0.0.1");
        assertEquals(actual.getPort(), "6379");
        assertEquals(actual.getPassword(), "");
        assertEquals(actual.getTimeoutInterval(), 40);
        assertEquals(actual.getMaxIdle(), 8);
        assertEquals(actual.getMaxTotal(), 18);
        assertEquals(actual.getLockExpirationTime(), 10);
    }
    
    @Test
    public void assertSwapToObject() {
        YamlRedisConnectionOptionConfiguration yamlConfig = new YamlRedisConnectionOptionConfiguration();
        yamlConfig.setHost("127.0.0.1");
        yamlConfig.setPort("6379");
        yamlConfig.setPassword("");
        yamlConfig.setTimeoutInterval(40);
        yamlConfig.setMaxIdle(8);
        yamlConfig.setMaxTotal(18);
        yamlConfig.setLockExpirationTime(10);
        RedisConnectionOptionConfiguration actual = new YamlRedisConnectionOptionConfigurationSwapper().swapToObject(yamlConfig);
        assertEquals(actual.getHost(), "127.0.0.1");
        assertEquals(actual.getPort(), "6379");
        assertEquals(actual.getPassword(), "");
        assertEquals(actual.getTimeoutInterval(), 40);
        assertEquals(actual.getMaxIdle(), 8);
        assertEquals(actual.getMaxTotal(), 18);
        assertEquals(actual.getLockExpirationTime(), 10);
    }
}
