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

package org.apache.shardingsphere.infra.algorithm.cryptographic.aes;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.algorithm.cryptographic.spi.CryptographicAlgorithm;
import org.apache.shardingsphere.infra.algorithm.cryptographic.spi.CryptographicPropertiesProvider;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.Properties;

/**
 * AES cryptographic algorithm.
 */
public final class AESCryptographicAlgorithm implements CryptographicAlgorithm {
    
    private CryptographicPropertiesProvider propsProvider;
    
    @Override
    public void init(final Properties props) {
        propsProvider = TypedSPILoader.getService(CryptographicPropertiesProvider.class, "DEFAULT", props);
    }
    
    @HighFrequencyInvocation
    @SneakyThrows(GeneralSecurityException.class)
    @Override
    public String encrypt(final Object plainValue) {
        if (null == plainValue) {
            return null;
        }
        byte[] result = getCipher(Cipher.ENCRYPT_MODE).doFinal(String.valueOf(plainValue).getBytes(StandardCharsets.UTF_8));
        return encode(result);
    }
    
    @HighFrequencyInvocation
    private String encode(final byte[] value) {
        return Base64.getEncoder().encodeToString(value);
    }
    
    @HighFrequencyInvocation
    @SneakyThrows(GeneralSecurityException.class)
    @Override
    public Object decrypt(final Object cipherValue) {
        if (null == cipherValue) {
            return null;
        }
        byte[] result = getCipher(Cipher.DECRYPT_MODE).doFinal(decode(cipherValue.toString().trim()));
        return new String(result, StandardCharsets.UTF_8);
    }
    
    @HighFrequencyInvocation
    private byte[] decode(final String value) {
        return Base64.getDecoder().decode(value);
    }
    
    @HighFrequencyInvocation
    private Cipher getCipher(final int decryptMode) throws GeneralSecurityException {
        Cipher result = Cipher.getInstance(getType());
        result.init(decryptMode, new SecretKeySpec(propsProvider.getSecretKey(), getType()));
        return result;
    }
    
    @Override
    public String getType() {
        return "AES";
    }
}
