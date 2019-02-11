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

package org.apache.shardingsphere.core.encrypt;

import org.apache.shardingsphere.core.encrypt.encryptor.impl.AESShardingEncryptor;
import org.apache.shardingsphere.core.encrypt.encryptor.impl.MD5ShardingEncryptor;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

public final class ShardingEncryptorFactoryTest {
    
    @Test
    public void assertNewMD5Encryptor() {
        assertThat(ShardingEncryptorFactory.getInstance().newAlgorithm("MD5", new Properties()), instanceOf(MD5ShardingEncryptor.class));
    }
    
    @Test
    public void assertNewAESEncryptor() {
        assertThat(ShardingEncryptorFactory.getInstance().newAlgorithm("AES", new Properties()), instanceOf(AESShardingEncryptor.class));
    }
    
    @Test
    public void assertNewDefaultEncryptor() {
        assertThat(ShardingEncryptorFactory.getInstance().newAlgorithm(), instanceOf(MD5ShardingEncryptor.class));
    }
}
