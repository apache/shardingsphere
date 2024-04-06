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

package org.apache.shardingsphere.encrypt.algorithm.standard;

import com.google.common.base.Strings;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithmMetaData;
import org.apache.shardingsphere.infra.algorithm.core.context.AlgorithmSQLContext;
import org.apache.shardingsphere.infra.algorithm.core.exception.AlgorithmInitializationException;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Properties;

/**
 * AES encrypt algorithm.
 */
@EqualsAndHashCode
public final class AESEncryptAlgorithm implements EncryptAlgorithm {
    
    private static final String AES_KEY = "aes-key-value";
    
    private static final String DIGEST_ALGORITHM_NAME = "digest-algorithm-name";
    
    @Getter
    private final EncryptAlgorithmMetaData metaData = new EncryptAlgorithmMetaData(true, true, false);
    
    private byte[] secretKey;
    
    @Override
    public void init(final Properties props) {
        secretKey = getSecretKey(props);
    }
    
    private byte[] getSecretKey(final Properties props) {
        String aesKey = props.getProperty(AES_KEY);
        ShardingSpherePreconditions.checkState(!Strings.isNullOrEmpty(aesKey), () -> new AlgorithmInitializationException(this, "%s can not be null or empty", AES_KEY));
        String digestAlgorithm = props.getProperty(DIGEST_ALGORITHM_NAME, MessageDigestAlgorithms.SHA_1);
        return Arrays.copyOf(DigestUtils.getDigest(digestAlgorithm.toUpperCase()).digest(aesKey.getBytes(StandardCharsets.UTF_8)), 16);
    }
    
    @SneakyThrows(GeneralSecurityException.class)
    @Override
    public String encrypt(final Object plainValue, final AlgorithmSQLContext algorithmSQLContext) {
        if (null == plainValue) {
            return null;
        }
        byte[] result = getCipher(Cipher.ENCRYPT_MODE).doFinal(String.valueOf(plainValue).getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(result);
    }
    
    @SneakyThrows(GeneralSecurityException.class)
    @Override
    public Object decrypt(final Object cipherValue, final AlgorithmSQLContext algorithmSQLContext) {
        if (null == cipherValue) {
            return null;
        }
        byte[] result = getCipher(Cipher.DECRYPT_MODE).doFinal(Base64.getDecoder().decode(cipherValue.toString().trim()));
        return new String(result, StandardCharsets.UTF_8);
    }
    
    private Cipher getCipher(final int decryptMode) throws GeneralSecurityException {
        Cipher result = Cipher.getInstance(getType());
        result.init(decryptMode, new SecretKeySpec(secretKey, getType()));
        return result;
    }
    
    @Override
    public String getType() {
        return "AES";
    }
}
