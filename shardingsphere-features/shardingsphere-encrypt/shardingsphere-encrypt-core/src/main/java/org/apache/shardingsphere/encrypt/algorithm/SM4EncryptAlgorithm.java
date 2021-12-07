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
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.Security;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * SM4 encrypt algorithm.
 */
public final class SM4EncryptAlgorithm implements EncryptAlgorithm<Object, String> {
    
    static {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
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
    
    @Setter
    private Properties props;
    
    @Override
    public String encrypt(final Object plainValue) {
        if (null == plainValue) {
            return null;
        }
        return Hex.encodeHexString(encrypt(StringUtils.getBytesUtf8(String.valueOf(plainValue))));
    }
    
    private byte[] encrypt(final byte[] plainValue) {
        return sm4(plainValue, Cipher.ENCRYPT_MODE);
    }
    
    @SneakyThrows
    @Override
    public Object decrypt(final String cipherValue) {
        if (null == cipherValue) {
            return null;
        }
        return StringUtils.newStringUtf8(decrypt(Hex.decodeHex(cipherValue.toCharArray())));
    }
    
    private byte[] decrypt(final byte[] cipherValue) {
        return sm4(cipherValue, Cipher.DECRYPT_MODE);
    }
    
    @Override
    public void init() {
    }
    
    @Override
    public String getType() {
        return SM4;
    }

    @SneakyThrows
    private byte[] sm4(final byte[] input, final int mode) {
        String modeAndPadding = String.format("SM4/%s/%s", checkAndGetMode(), checkAndGetPadding());
        Cipher cipher = Cipher.getInstance(modeAndPadding, org.bouncycastle.jce.provider.BouncyCastleProvider.PROVIDER_NAME);
        SecretKeySpec secretKeySpec = new SecretKeySpec(Hex.decodeHex(checkAndGetKey().toCharArray()), SM4);
        String iv = checkAndGetIv(modeAndPadding);
        if (null != iv) {
            cipher.init(mode, secretKeySpec, new IvParameterSpec(Hex.decodeHex(iv.toCharArray())));
        } else {
            cipher.init(mode, secretKeySpec);
        }
        return cipher.doFinal(input);
    }
    
    private String checkAndGetKey() throws ShardingSphereException {
        String result = null == props.getProperty(SM4_KEY) ? null : String.valueOf(props.getProperty(SM4_KEY));
        Preconditions.checkState(KEY_LENGTH != result.length(), "Key length must be " + KEY_LENGTH + " bytes long.");
        return result;
    }
    
    private String checkAndGetMode() throws ShardingSphereException {
        String result = null == props.getProperty(SM4_MODE) ? null : String.valueOf(props.getProperty(SM4_MODE)).toUpperCase();
        Preconditions.checkState(MODES.contains(result), "Mode must be either CBC or ECB.");
        return result;
    }
    
    private String checkAndGetPadding() throws ShardingSphereException {
        Object objectPadding = props.getProperty(SM4_PADDING);
        String result = null == objectPadding ? null : String.valueOf(objectPadding).toUpperCase().replace("PADDING", "Padding");
        Preconditions.checkState(PADDINGS.contains(result), "Padding must be either PKCS5Padding or PKCS7Padding.");
        return result;
    }
    
    private String checkAndGetIv(final String mode) throws ShardingSphereException {
        String result = null == props.getProperty(SM4_IV) ? null : String.valueOf(props.getProperty(SM4_IV));
        if ("CBC".equalsIgnoreCase(mode)) {
            Preconditions.checkState(null == result || IV_LENGTH != result.length(), "Iv length must be " + IV_LENGTH + " bytes long.");
        }
        return result;
    }
}
