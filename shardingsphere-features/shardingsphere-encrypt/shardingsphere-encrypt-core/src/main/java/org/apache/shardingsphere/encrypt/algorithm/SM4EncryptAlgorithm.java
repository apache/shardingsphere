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
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.encrypt.spi.context.EncryptContext;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.Security;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

/**
 * SM4 encrypt algorithm.
 */
@Getter
@Setter
public final class SM4EncryptAlgorithm implements EncryptAlgorithm<Object, String> {
    
    static {
        Security.addProvider(new BouncyCastleProvider());
    }
    
    private static final String SM4 = "SM4";
    
    private static final String SM4_KEY = "sm4-key";
    
    private static final String SM4_IV = "sm4-iv";
    
    private static final String SM4_MODE = "sm4-mode";
    
    private static final String SM4_PADDING = "sm4-padding";
    
    private static final int KEY_LENGTH = 16;
    
    private static final int IV_LENGTH = 16;
    
    private static final Set<String> MODES = new HashSet<>(Arrays.asList("ECB", "CBC"));
    
    private static final Set<String> PADDINGS = new HashSet<>(Arrays.asList("PKCS5Padding", "PKCS7Padding"));
    
    private Properties props = new Properties();
    
    private byte[] sm4Key;
    
    private byte[] sm4Iv;
    
    private String sm4ModePadding;
    
    @Override
    public void init() {
        String sm4Mode = createSm4Mode();
        String sm4Padding = createSm4Padding();
        sm4ModePadding = "SM4/" + sm4Mode + "/" + sm4Padding;
        sm4Key = createSm4Key();
        sm4Iv = createSm4Iv(sm4Mode);
    }
    
    private String createSm4Mode() {
        Preconditions.checkArgument(props.containsKey(SM4_MODE), "%s can not be null.", SM4_MODE);
        String result = String.valueOf(props.getProperty(SM4_MODE)).toUpperCase();
        Preconditions.checkState(MODES.contains(result), "Mode must be either CBC or ECB.");
        return result;
    }
    
    private byte[] createSm4Key() {
        Preconditions.checkArgument(props.containsKey(SM4_KEY), "%s can not be null.", SM4_KEY);
        String sm4KeyValue = String.valueOf(props.getProperty(SM4_KEY));
        byte[] sm4KeyBytes = ByteUtils.fromHexString(sm4KeyValue);
        Preconditions.checkState(KEY_LENGTH == sm4KeyBytes.length, "Key length must be " + KEY_LENGTH + " bytes long.");
        return sm4KeyBytes;
    }
    
    private byte[] createSm4Iv(final String sm4Mode) {
        if ("CBC".equalsIgnoreCase(sm4Mode)) {
            Preconditions.checkArgument(props.containsKey(SM4_IV), "%s can not be null.", SM4_IV);
            String sm4IvValue = String.valueOf(props.getProperty(SM4_IV));
            byte[] sm4IvBytes = ByteUtils.fromHexString(sm4IvValue);
            Preconditions.checkState(IV_LENGTH == sm4IvBytes.length, "Iv length must be " + IV_LENGTH + " bytes long.");
        }
        return null;
    }
    
    private String createSm4Padding() {
        Preconditions.checkArgument(props.containsKey(SM4_PADDING), "%s can not be null.", SM4_PADDING);
        String result = String.valueOf(props.get(SM4_PADDING)).toUpperCase().replace("PADDING", "Padding");
        Preconditions.checkState(PADDINGS.contains(result), "Padding must be either PKCS5Padding or PKCS7Padding.");
        return result;
    }
    
    @Override
    public String encrypt(final Object plainValue, final EncryptContext encryptContext) {
        if (null == plainValue) {
            return null;
        }
        return ByteUtils.toHexString(encrypt(StringUtils.getBytesUtf8(String.valueOf(plainValue))));
    }
    
    private byte[] encrypt(final byte[] plainValue) {
        return handle(plainValue, Cipher.ENCRYPT_MODE);
    }
    
    @SneakyThrows
    @Override
    public Object decrypt(final String cipherValue, final EncryptContext encryptContext) {
        if (null == cipherValue) {
            return null;
        }
        return StringUtils.newStringUtf8(decrypt(ByteUtils.fromHexString(cipherValue)));
    }
    
    private byte[] decrypt(final byte[] cipherValue) {
        return handle(cipherValue, Cipher.DECRYPT_MODE);
    }

    @SneakyThrows
    private byte[] handle(final byte[] input, final int mode) {
        Cipher cipher = Cipher.getInstance(sm4ModePadding, BouncyCastleProvider.PROVIDER_NAME);
        SecretKeySpec secretKeySpec = new SecretKeySpec(sm4Key, SM4);
        Optional<byte[]> sm4Iv = getSm4Iv();
        if (sm4Iv.isPresent()) {
            cipher.init(mode, secretKeySpec, new IvParameterSpec(sm4Iv.get()));
        } else {
            cipher.init(mode, secretKeySpec);
        }
        return cipher.doFinal(input);
    }
    
    private Optional<byte[]> getSm4Iv() {
        return Optional.ofNullable(sm4Iv);
    }
    
    @Override
    public String getType() {
        return SM4;
    }
}
