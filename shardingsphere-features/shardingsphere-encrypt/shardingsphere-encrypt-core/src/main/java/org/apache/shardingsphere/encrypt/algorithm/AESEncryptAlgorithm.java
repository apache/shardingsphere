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

package org.apache.shardingsphere.encrypt.algorithm;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.encrypt.spi.context.EncryptContext;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Properties;

/**
 * AES encrypt algorithm.
 */
public final class AESEncryptAlgorithm implements EncryptAlgorithm<Object, String> {
    
    private static final String AES_KEY = "aes-key-value";
    
    @Getter
    private Properties props;
    
    private byte[] secretKey;
    
    @Override
    public void init(final Properties props) {
        this.props = props;
        secretKey = createSecretKey(props);
    }
    
    private byte[] createSecretKey(final Properties props) {
        Preconditions.checkArgument(props.containsKey(AES_KEY), "%s can not be null", AES_KEY);
        return Arrays.copyOf(DigestUtils.sha1(props.getProperty(AES_KEY)), 16);
    }
    
    @SneakyThrows(GeneralSecurityException.class)
    @Override
    public String encrypt(final Object plainValue, final EncryptContext encryptContext) {
        if (null == plainValue) {
            return null;
        }
        byte[] result = getCipher(Cipher.ENCRYPT_MODE).doFinal(String.valueOf(plainValue).getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(result);
    }
    
    @SneakyThrows(GeneralSecurityException.class)
    @Override
    public Object decrypt(final String cipherValue, final EncryptContext encryptContext) {
        if (null == cipherValue) {
            return null;
        }
        byte[] result = getCipher(Cipher.DECRYPT_MODE).doFinal(Base64.getDecoder().decode(cipherValue));
        return new String(result, StandardCharsets.UTF_8);
    }
    
    private Cipher getCipher(final int decryptMode) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        Cipher result = Cipher.getInstance(getType());
        result.init(decryptMode, new SecretKeySpec(secretKey, getType()));
        return result;
    }
    
    @Override
    public String getType() {
        return "AES";
    }
}
