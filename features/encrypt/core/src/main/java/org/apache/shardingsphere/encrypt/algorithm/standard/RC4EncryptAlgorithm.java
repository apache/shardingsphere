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

import org.apache.commons.codec.binary.Base64;
import org.apache.shardingsphere.encrypt.api.context.EncryptContext;
import org.apache.shardingsphere.encrypt.api.encrypt.standard.StandardEncryptAlgorithm;
import org.apache.shardingsphere.encrypt.exception.algorithm.EncryptAlgorithmInitializationException;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;

import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * RC4 encrypt algorithm.
 */
public final class RC4EncryptAlgorithm implements StandardEncryptAlgorithm<Object, String> {
    
    private static final String RC4_KEY = "rc4-key-value";
    
    private static final int KEY_MIN_LENGTH = 5;
    
    private static final int SBOX_LENGTH = 256;
    
    private final AtomicReferenceArray<Byte> key = new AtomicReferenceArray<>(SBOX_LENGTH - 1);
    
    private final AtomicReferenceArray<Integer> sBox = new AtomicReferenceArray<>(SBOX_LENGTH);
    
    @Override
    public void init(final Properties props) {
        byte[] keyBytes = getKey(props);
        for (int i = 0; i < key.length(); i++) {
            if (i < keyBytes.length) {
                key.set(i, keyBytes[i]);
            } else {
                key.set(i, (byte) 0);
            }
        }
    }
    
    private byte[] getKey(final Properties props) {
        byte[] result = props.getProperty(RC4_KEY, "").getBytes(StandardCharsets.UTF_8);
        ShardingSpherePreconditions.checkState(KEY_MIN_LENGTH <= result.length && SBOX_LENGTH > result.length,
                () -> new EncryptAlgorithmInitializationException(getType(), "Key length has to be between " + KEY_MIN_LENGTH + " and " + (SBOX_LENGTH - 1)));
        return result;
    }
    
    @Override
    public String encrypt(final Object plainValue, final EncryptContext encryptContext) {
        return null == plainValue ? null : Base64.encodeBase64String(crypt(String.valueOf(plainValue).getBytes(StandardCharsets.UTF_8)));
    }
    
    @Override
    public Object decrypt(final String cipherValue, final EncryptContext encryptContext) {
        return null == cipherValue ? null : new String(crypt(Base64.decodeBase64(cipherValue)), StandardCharsets.UTF_8);
    }
    
    /*
     * @see <a href="http://en.wikipedia.org/wiki/RC4#Pseudo-random_generation_algorithm_.28PRGA.29">Pseudo-random generation algorithm</a>
     */
    private byte[] crypt(final byte[] message) {
        fillSBox();
        byte[] result = new byte[message.length];
        int i = 0;
        int j = 0;
        for (int n = 0; n < message.length; n++) {
            i = (i + 1) % SBOX_LENGTH;
            j = (j + sBox.get(i)) % SBOX_LENGTH;
            swapSBox(i, j);
            int rand = sBox.get((sBox.get(i) + sBox.get(j)) % SBOX_LENGTH);
            result[n] = (byte) (rand ^ message[n]);
        }
        return result;
    }
    
    /*
     * @see <a href="http://en.wikipedia.org/wiki/RC4#Key-scheduling_algorithm_.28KSA.29">Wikipedia. Init sBox</a>
     */
    private void fillSBox() {
        int j = 0;
        for (int i = 0; i < SBOX_LENGTH; i++) {
            sBox.set(i, i);
        }
        for (int i = 0; i < SBOX_LENGTH; i++) {
            System.out.println(i % key.length());
            System.out.println(key);
            System.out.println();
            j = (j + sBox.get(i) + (key.get(i % key.length())) & 0xFF) % SBOX_LENGTH;
            swapSBox(i, j);
        }
    }
    
    private void swapSBox(final int i, final int j) {
        int temp = sBox.get(i);
        sBox.set(i, sBox.get(j));
        sBox.set(j, temp);
    }
    
    @Override
    public String getType() {
        return "RC4";
    }
}
