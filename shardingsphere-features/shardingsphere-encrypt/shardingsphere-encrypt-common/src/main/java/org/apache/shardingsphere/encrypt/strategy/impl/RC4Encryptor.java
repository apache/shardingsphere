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

package org.apache.shardingsphere.encrypt.strategy.impl;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.shardingsphere.encrypt.strategy.spi.Encryptor;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Properties;

/**
 * RC4 encryptor.
 */
@Getter
@Setter
public final class RC4Encryptor implements Encryptor {
    
    private static final String RC4_KEY = "rc4.key.value";
    
    private static final int SBOX_LENGTH = 256;
    
    private static final int KEY_MIN_LENGTH = 5;
    
    private byte[] key = new byte[SBOX_LENGTH - 1];
    
    private int[] sbox = new int[SBOX_LENGTH];
    
    private Properties properties = new Properties();
    
    @Override
    public String getType() {
        return "RC4";
    }
    
    @Override
    @SneakyThrows
    public void init() {
        reset();
        setKey(StringUtils.getBytesUtf8(properties.get(RC4_KEY).toString()));
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
        byte[] msg = crypt(data);
        reset();
        return msg;
    }
    
    private void reset() {
        Arrays.fill(key, (byte) 0);
        Arrays.fill(sbox, 0);
    }
    
    /**
     * Crypt given byte array. Be aware, that you must init key, before using.
     * @param msg array to be crypt
     * @return byte array
     * @see <a href="http://en.wikipedia.org/wiki/RC4#Pseudo-random_generation_algorithm_.28PRGA.29">Pseudo-random generation algorithm</a>
     */
    private byte[] crypt(final byte[] msg) {
        sbox = initSBox(key);
        byte[] code = new byte[msg.length];
        int i = 0;
        int j = 0;
        for (int n = 0; n < msg.length; n++) {
            i = (i + 1) % SBOX_LENGTH;
            j = (j + sbox[i]) % SBOX_LENGTH;
            swap(i, j, sbox);
            int rand = sbox[(sbox[i] + sbox[j]) % SBOX_LENGTH];
            code[n] = (byte) (rand ^ msg[n]);
        }
        return code;
    }
    
    /**
     * Initialize SBOX with given key, Key-scheduling algorithm.
     *
     * @param key key
     * @return sbox int array
     * @see <a href="http://en.wikipedia.org/wiki/RC4#Key-scheduling_algorithm_.28KSA.29">Wikipedia. Init sbox</a>
     */
    private int[] initSBox(final byte[] key) {
        int[] sbox = new int[SBOX_LENGTH];
        int j = 0;
        for (int i = 0; i < SBOX_LENGTH; i++) {
            sbox[i] = i;
        }
        for (int i = 0; i < SBOX_LENGTH; i++) {
            j = (j + sbox[i] + (key[i % key.length]) & 0xFF) % SBOX_LENGTH;
            swap(i, j, sbox);
        }
        return sbox;
    }
    
    private void swap(final int i, final int j, final int[] sbox) {
        int temp = sbox[i];
        sbox[i] = sbox[j];
        sbox[j] = temp;
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
}

