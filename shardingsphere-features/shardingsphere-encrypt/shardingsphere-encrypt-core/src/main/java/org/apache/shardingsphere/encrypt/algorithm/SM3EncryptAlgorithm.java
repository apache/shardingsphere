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
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.bouncycastle.crypto.digests.SM3Digest;

import java.security.Security;
import java.util.Properties;

/**
 * SM3 encrypt algorithm.
 */
public final class SM3EncryptAlgorithm implements EncryptAlgorithm<Object, String> {
    
    static {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }
    
    private static final String SM3 = "SM3";
    
    private static final String SM3_SALT = "sm3-salt";
    
    private static final int SALT_LENGTH = 8;
    
    @Setter
    private Properties props;

    @Override
    public String encrypt(final Object plainValue) {
        if (null == plainValue) {
            return null;
        }
        String salt = null == props.getProperty(SM3_SALT) ? "" : String.valueOf(props.getProperty(SM3_SALT));
        Preconditions.checkState(0 == salt.length() || SALT_LENGTH == salt.length(), "Salt should be either blank or better " + SALT_LENGTH + " bytes long.");
        return Hex.encodeHexString(digest(StringUtils.getBytesUtf8(String.valueOf(plainValue)), 0 == salt.length() ? new byte[0] : StringUtils.getBytesUtf8(salt)));
    }
    
    @Override
    public Object decrypt(final String cipherValue) {
        return cipherValue;
    }
    
    @Override
    public String getType() {
        return SM3;
    }
    
    private byte[] digest(final byte[] input, final byte[] salt) {
        SM3Digest sm3Digest = new SM3Digest();
        byte[] updateByte = concat(input, salt);
        sm3Digest.update(updateByte, 0, updateByte.length);
        byte[] result = new byte[sm3Digest.getDigestSize()];
        sm3Digest.doFinal(result, 0);
        return result;
    }
    
    private byte[] concat(final byte[] input, final byte[] salt) {
        int inputLength = input.length;
        int saltLength = salt.length;
        byte[] result = new byte[inputLength + saltLength];
        System.arraycopy(input, 0, result, 0, inputLength);
        System.arraycopy(salt, 0, result, inputLength, saltLength);
        return result;
    }
    
    @Override
    public void init() {
    }
}
