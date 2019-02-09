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

package org.apache.shardingsphere.core.encrypt.encryptor.imp;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.shardingsphere.core.exception.ShardingConfigurationException;
import org.apache.shardingsphere.spi.algorithm.encrypt.ShardingEncryptor;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Properties;

/**
 * AES sharding encryptor.
 *
 * @author panjuan
 */
@Getter
@Setter
public final class AESShardingEncryptor implements ShardingEncryptor {
    
    private Properties properties = new Properties();
    
    @Override
    public String getType() {
        return "AES";
    }
    
    @Override
    @SneakyThrows
    public Object encode(final Object plaintext) {
        if (!hasDesKey()) {
            throw new ShardingConfigurationException("No available secret key for AESShardingEncryptor.");
        }
        getCipher().init(Cipher.ENCRYPT_MODE, generateKey());
        return new String(getCipher().doFinal(String.valueOf(plaintext).getBytes()));
    }
    
    @Override
    @SneakyThrows
    public Object decode(final Object ciphertext) {
        if (!hasDesKey()) {
            throw new ShardingConfigurationException("No available secret key for AESShardingEncryptor.");
        }
        getCipher().init(Cipher.DECRYPT_MODE, generateKey());
        return new String(getCipher().doFinal(String.valueOf(ciphertext).getBytes()));
    }
    
    private boolean hasDesKey() {
        return null != properties.get("aes.key.value");
    }
    
    @SneakyThrows
    private Cipher getCipher() {
        return Cipher.getInstance(getType().toUpperCase());
    }
    
    private Key generateKey() {
        return new SecretKeySpec(properties.getProperty("aes.key.value").getBytes(), getType());
    }
}
