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

package org.apache.shardingsphere.core.keygen;

import org.apache.shardingsphere.core.keygen.impl.SnowflakeShardingKeyGenerator;
import org.apache.shardingsphere.core.keygen.impl.UUIDShardingKeyGenerator;
import org.apache.shardingsphere.core.spi.algorithm.keygen.ShardingKeyGeneratorFactory;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

public final class ShardingKeyGeneratorFactoryTest {
    
    @Test
    public void assertNewSnowflakeKeyGenerator() {
        assertThat(ShardingKeyGeneratorFactory.getInstance().newAlgorithm("SNOWFLAKE", new Properties()), instanceOf(SnowflakeShardingKeyGenerator.class));
    }
    
    @Test
    public void assertNewUUIDKeyGenerator() {
        assertThat(ShardingKeyGeneratorFactory.getInstance().newAlgorithm("UUID", new Properties()), instanceOf(UUIDShardingKeyGenerator.class));
    }
    
    @Test
    public void assertNewDefaultKeyGenerator() {
        assertThat(ShardingKeyGeneratorFactory.getInstance().newAlgorithm(), instanceOf(SnowflakeShardingKeyGenerator.class));
    }
}
