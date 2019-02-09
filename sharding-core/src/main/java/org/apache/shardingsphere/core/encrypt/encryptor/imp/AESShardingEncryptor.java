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
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.shardingsphere.core.exception.ShardingConfigurationException;
import org.apache.shardingsphere.spi.algorithm.encrypt.ShardingEncryptor;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Arrays;
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
        Cipher cipher = Cipher.getInstance(getType().toUpperCase());
        cipher.init(Cipher.ENCRYPT_MODE, generateKey());
        byte[] result = cipher.doFinal(String.valueOf(plaintext).getBytes("UTF8"));
        return Base64.encodeBase64String(result);
    }
    
    @Override
    @SneakyThrows
    public Object decode(final Object ciphertext) {
        if (!hasDesKey()) {
            throw new ShardingConfigurationException("No available secret key for AESShardingEncryptor.");
        }
        Cipher cipher = Cipher.getInstance(getType().toUpperCase());
        cipher.init(Cipher.DECRYPT_MODE, generateKey());
        byte[] result = Base64.decodeBase64(String.valueOf(ciphertext));
        return new String(cipher.doFinal(result));
    }
    
    private boolean hasDesKey() {
        return null != properties.get("aes.key.value");
    }
    
    @SneakyThrows
    private Key generateKey() {
        byte[] keyValue = String.valueOf(properties.get("aes.key.value")).getBytes("UTF8");
        return new SecretKeySpec(Arrays.copyOf(DigestUtils.sha1(keyValue), 16), getType());
    }
    
    public static void main(String[] args) throws Exception {
        String before = "aaaaa";
        AESShardingEncryptor aesShardingEncryptor = new AESShardingEncryptor();
        Properties properties = new Properties();
        properties.setProperty("aes.key.value", "123456");
        aesShardingEncryptor.setProperties(properties);
        Object after = aesShardingEncryptor.encode(before);
        System.out.println("-----");
        System.out.println(after);
        System.out.println(aesShardingEncryptor.decode(after));
//        byte[] encryptedByteValue = Base64.encodeBase64("aa".getBytes());
//        String encryptedValue = String.valueOf(encryptedByteValue);
//        System.out.println(Base64.decodeBase64(encryptedByteValue).toString());
//        System.out.println("----");
     
    }
}
