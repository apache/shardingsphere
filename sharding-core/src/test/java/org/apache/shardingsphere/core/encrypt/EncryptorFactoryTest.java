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

import org.apache.shardingsphere.core.encrypt.impl.AESEncryptor;
import org.apache.shardingsphere.core.encrypt.impl.MD5Encryptor;
import org.apache.shardingsphere.core.fixture.TestShardingEncryptor;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

public final class EncryptorFactoryTest {
    
    @Test
    public void assertNewMD5Encryptor() {
        assertThat(EncryptorFactory.getInstance().newAlgorithm("MD5", new Properties()), instanceOf(MD5Encryptor.class));
    }
    
    @Test
    public void assertNewAESEncryptor() {
        assertThat(EncryptorFactory.getInstance().newAlgorithm("AES", new Properties()), instanceOf(AESEncryptor.class));
    }
    
    @Test
    public void assertNewDefaultEncryptor() {
        assertThat(EncryptorFactory.getInstance().newAlgorithm(), instanceOf(TestShardingEncryptor.class));
    }
}
