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

package org.apache.shardingsphere.core.strategy.encrypt;

import org.apache.shardingsphere.core.spi.algorithm.encrypt.ShardingEncryptorServiceLoader;
import org.apache.shardingsphere.core.strategy.encrypt.fixture.TestShardingEncryptor;
import org.apache.shardingsphere.core.strategy.encrypt.impl.AESShardingEncryptor;
import org.apache.shardingsphere.core.strategy.encrypt.impl.MD5ShardingEncryptor;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

public final class ShardingEncryptorServiceLoaderTest {
    
    private ShardingEncryptorServiceLoader serviceLoader = new ShardingEncryptorServiceLoader();
    
    @Test
    public void assertNewMD5Encryptor() {
        assertThat(serviceLoader.newService("MD5", new Properties()), instanceOf(MD5ShardingEncryptor.class));
    }
    
    @Test
    public void assertNewAESEncryptor() {
        assertThat(serviceLoader.newService("AES", new Properties()), instanceOf(AESShardingEncryptor.class));
    }
    
    @Test
    public void assertNewDefaultEncryptor() {
        assertThat(serviceLoader.newService(), instanceOf(TestShardingEncryptor.class));
    }
}
