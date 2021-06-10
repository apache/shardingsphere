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

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Properties;

/**
 * RC4 encrypt algorithm.
 */
public final class RC4EncryptAlgorithm implements EncryptAlgorithm {
    
    private static final String RC4_KEY = "rc4-key-value";
    
    private static final int SBOX_LENGTH = 256;
    
    private static final int KEY_MIN_LENGTH = 5;
    
    private byte[] key = new byte[SBOX_LENGTH - 1];
    
    private int[] sBox = new int[SBOX_LENGTH];
    
    @Getter
    @Setter
    private Properties props = new Properties();
    
    @Override
    public void init() {
        reset();
        setKey(StringUtils.getBytesUtf8(props.getProperty(RC4_KEY)));
    }
    
    @Override
    public String encrypt(final Object plaintext) {
        if (null == plaintext) {
            return null;
        }
        byte[] result = handle(StringUtils.getBytesUtf8(String.valueOf(plaintext)), key);
        return Base64.encodeBase64String(result);
    }
    
    @Override
    public Object decrypt(final String ciphertext) {
        if (null == ciphertext) {
            return null;
        }
        byte[] result = handle(Base64.decodeBase64(ciphertext), key);
        return new String(result, StandardCharsets.UTF_8);
    }
    
    private byte[] handle(final byte[] data, final byte[] key) {
        reset();
        setKey(key);
        byte[] result = crypt(data);
        reset();
        return result;
    }
    
    private void reset() {
        Arrays.fill(key, (byte) 0);
        Arrays.fill(sBox, 0);
    }
    
    /**
     * Crypt given byte array. Be aware, that you must init key, before using.
     * @param message array to be crypt
     * @return byte array
     * @see <a href="http://en.wikipedia.org/wiki/RC4#Pseudo-random_generation_algorithm_.28PRGA.29">Pseudo-random generation algorithm</a>
     */
    private byte[] crypt(final byte[] message) {
        sBox = initSBox(key);
        byte[] result = new byte[message.length];
        int i = 0;
        int j = 0;
        for (int n = 0; n < message.length; n++) {
            i = (i + 1) % SBOX_LENGTH;
            j = (j + sBox[i]) % SBOX_LENGTH;
            swap(i, j, sBox);
            int rand = sBox[(sBox[i] + sBox[j]) % SBOX_LENGTH];
            result[n] = (byte) (rand ^ message[n]);
        }
        return result;
    }
    
    /**
     * Initialize SBOX with given key, Key-scheduling algorithm.
     *
     * @param key key
     * @return sBox int array
     * @see <a href="http://en.wikipedia.org/wiki/RC4#Key-scheduling_algorithm_.28KSA.29">Wikipedia. Init sBox</a>
     */
    private int[] initSBox(final byte[] key) {
        int[] result = new int[SBOX_LENGTH];
        int j = 0;
        for (int i = 0; i < SBOX_LENGTH; i++) {
            result[i] = i;
        }
        for (int i = 0; i < SBOX_LENGTH; i++) {
            j = (j + result[i] + (key[i % key.length]) & 0xFF) % SBOX_LENGTH;
            swap(i, j, result);
        }
        return result;
    }
    
    private void swap(final int i, final int j, final int[] sBox) {
        int temp = sBox[i];
        sBox[i] = sBox[j];
        sBox[j] = temp;
    }
    
    /**
     * Set key.
     *
     * @param key key to be setup
     * @throws ShardingSphereException if key length is smaller than 5 or bigger than 255
     */
    private void setKey(final byte[] key) throws ShardingSphereException {
        if (!(key.length >= KEY_MIN_LENGTH && key.length < SBOX_LENGTH)) {
            throw new ShardingSphereException("Key length has to be between " + KEY_MIN_LENGTH + " and " + (SBOX_LENGTH - 1));
        }
        this.key = key;
    }
    
    @Override
    public String getType() {
        return "RC4";
    }
}

