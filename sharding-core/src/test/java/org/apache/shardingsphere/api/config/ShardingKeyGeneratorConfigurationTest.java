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

package org.apache.shardingsphere.api.config;

import org.apache.shardingsphere.core.exception.ShardingConfigurationException;
import org.apache.shardingsphere.core.keygen.generator.impl.SnowflakeShardingKeyGenerator;
import org.apache.shardingsphere.core.keygen.generator.impl.UUIDShardingKeyGenerator;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class ShardingKeyGeneratorConfigurationTest {
    
    @Test
    public void assertGetKeyGeneratorWithAllProperties() {
        KeyGeneratorConfiguration keyGeneratorConfiguration = new KeyGeneratorConfiguration("order_id", "SNOWFLAKE", new Properties());
        assertThat(keyGeneratorConfiguration.getColumn(), is("order_id"));
        assertThat(keyGeneratorConfiguration.getType(), is("SNOWFLAKE"));
        assertThat(keyGeneratorConfiguration.getProps().entrySet().size(), is(0));
    }
    
    @Test
    public void assertGetKeyGeneratorWithSnowflakeType() {
        KeyGeneratorConfiguration keyGeneratorConfiguration = new KeyGeneratorConfiguration("order_id", "SNOWFLAKE", new Properties());
        assertThat(keyGeneratorConfiguration.getKeyGenerator().getClass().getName(), is(SnowflakeShardingKeyGenerator.class.getName()));
    }
        
    @Test
    public void assertGetKeyGeneratorClassNameWithUUID() {
        assertThat(new KeyGeneratorConfiguration(null, "UUID", new Properties()).getKeyGenerator().getClass().getName(), is(UUIDShardingKeyGenerator.class.getName()));
    }
    
    @Test(expected = ShardingConfigurationException.class)
    public void assertGetKeyGeneratorClassNameWithException() {
        new KeyGeneratorConfiguration(null, "DEFAULT", new Properties()).getKeyGenerator();
    }
    
    @Test
    public void assertGetKeyGeneratorVariables() {
        assertTrue(null == new KeyGeneratorConfiguration(null, null, new Properties()).getKeyGenerator());
    }
}
