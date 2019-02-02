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

import org.apache.shardingsphere.api.config.sharding.KeyGeneratorConfiguration;
import org.apache.shardingsphere.core.exception.ShardingConfigurationException;
import org.apache.shardingsphere.core.keygen.generator.ShardingKeyGenerator;
import org.apache.shardingsphere.core.keygen.generator.impl.SnowflakeShardingKeyGenerator;
import org.apache.shardingsphere.core.keygen.generator.impl.UUIDShardingKeyGenerator;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class KeyGeneratorConfigurationTest {
    
    @Test
    public void assertGetKeyGeneratorWithSnowflake() {
        assertGetKeyGenerator("SNOWFLAKE", SnowflakeShardingKeyGenerator.class);
    }
    
    @Test
    public void assertGetKeyGeneratorWithUUID() {
        assertGetKeyGenerator("UUID", UUIDShardingKeyGenerator.class);
    }
    
    private void assertGetKeyGenerator(final String type, final Class<? extends ShardingKeyGenerator> classType) {
        KeyGeneratorConfiguration actual = new KeyGeneratorConfiguration("id", type, new Properties());
        assertThat(actual.getColumn(), is("id"));
        assertThat(actual.getType(), is(type));
        assertThat(actual.getProps(), is(new Properties()));
        assertTrue(actual.getKeyGenerator().isPresent());
        assertThat(actual.getKeyGenerator().get(), instanceOf(classType));
    }
    
    @Test
    public void assertGetKeyGeneratorWithoutType() {
        KeyGeneratorConfiguration actual = new KeyGeneratorConfiguration("id", null, new Properties());
        assertThat(actual.getColumn(), is("id"));
        assertNull(actual.getType());
        assertThat(actual.getProps(), is(new Properties()));
        assertFalse(actual.getKeyGenerator().isPresent());
    }
    
    @Test(expected = ShardingConfigurationException.class)
    public void assertGetKeyGeneratorWithInvalidType() {
        new KeyGeneratorConfiguration("id", "INVALID", new Properties()).getKeyGenerator();
    }
}
