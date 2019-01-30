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

package org.apache.shardingsphere.core.routing.strategy;

import org.apache.shardingsphere.core.encrypt.encryptor.ShardingEncryptor;
import org.apache.shardingsphere.core.exception.ShardingConfigurationException;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class ShardingEncryptorStrategyTest {
    
    private ShardingEncryptorStrategy shardingEncryptorStrategy;
    
    @BeforeClass
    public void setUp() {
        ShardingEncryptor shardingEncryptor = mock(ShardingEncryptor.class);
        shardingEncryptorStrategy = new ShardingEncryptorStrategy(Arrays.asList("pwd1", "pwd2"), shardingEncryptor);
    }    
    
    @Test
    public void assertGetColumns() {
        assertThat(shardingEncryptorStrategy.getColumns().iterator().next(), is("pwd1"));
    }
    
    @Test
    public void assertGetAssistedQueryColumns() {
        assertThat(shardingEncryptorStrategy.getAssistedQueryColumns().size(), is(0));
    }
    
    @Test
    public void assertGetShardingEncryptor() {
        assertThat(shardingEncryptorStrategy.getShardingEncryptor(), instanceOf(ShardingEncryptor.class));
    }
    
    @Test(expected = ShardingConfigurationException.class)
    public void assertNewShardingEncryptorStrategy() {
        ShardingEncryptor shardingEncryptor = mock(ShardingEncryptor.class);
        shardingEncryptorStrategy = new ShardingEncryptorStrategy(Arrays.asList("pwd1", "pwd2"), Collections.singletonList("pwd1_index"), shardingEncryptor);
    }
}
