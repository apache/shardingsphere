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

package org.apache.shardingsphere.infra.security;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

/**
 * this is Aes Security Utils for encrypt or decrypt password
 * NOTICE:
 * in encrypt, we random get iv and key in 16-bytes to encrypt password, but in return value, we combain
 * iv + key + encrypt(passwd) as a string, this is not SAFE!!! iv and key must save other security file or database!
 * if you need more security, please implements AlgorithmSecure youself.
 * in decrypt, we first extract iv and key in 16 bytes, then use it to decrypt password.
 */
public class AesAlgorithmSecure implements AlgorithmSecure {
    // the iv and key lenght
    private static final int KEY_PART_LENGTH = 16;

    // the default encrypt / decrypt algorithm
    private static final String DEFAULT_ALGORITHM = "AES/CBC/PKCS5PADDING";

    // the default key generator algorithm
    private static final String DEFAULT_KEY_ALGORITHM = "AES";

    @Override
    public String encrypt(final byte[] content) throws Exception {
        byte[] ivBytes = getRandom();
        byte[] secretKeyBytes = getRandom();
        byte[] encryptBytes = runAesAlgorithm(content,
                ivBytes,
                secretKeyBytes,
                Cipher.ENCRYPT_MODE);
        byte[] results = mergeAllBytes(ivBytes, secretKeyBytes, encryptBytes);
        return Base64.getEncoder().encodeToString(results);
    }

    @Override
    public String decrypt(final String input, final String charsetName) throws Exception {
        byte[] base64Bytes = Base64.getDecoder().decode(input);
        List<byte[]> splitTotalBytes = splitInputBytes(base64Bytes, KEY_PART_LENGTH, KEY_PART_LENGTH);
        byte[] ivBytes = splitTotalBytes.get(0);
        byte[] secretBytes = splitTotalBytes.get(1);
        byte[] content = splitTotalBytes.get(2);
        byte[] decrypt = runAesAlgorithm(content, ivBytes, secretBytes, Cipher.DECRYPT_MODE);
        return new String(decrypt, charsetName);
    }

    @Override
    public String getType() {
        return "AES";
    }

    private byte[] runAesAlgorithm(final byte[] content, final byte[] iv, final byte[] key, final int mode) throws Exception {
        Cipher cipher = Cipher.getInstance(DEFAULT_ALGORITHM);
        cipher.init(mode,
                new SecretKeySpec(key, DEFAULT_KEY_ALGORITHM),
                new IvParameterSpec(iv));
        return cipher.doFinal(content);
    }

    private static byte[] getRandom() {
        byte[] output = new byte[KEY_PART_LENGTH];
        SecureRandom random = new SecureRandom();
        random.setSeed(System.nanoTime());
        random.nextBytes(output);
        return output;
    }

    private byte[] mergeAllBytes(final byte[]... args) {
        int total = Arrays.stream(args).mapToInt(arr -> arr.length).sum();
        byte[] results = new byte[total];
        int curPos = 0;
        for (byte[] arr: args) {
            System.arraycopy(arr, 0, results, curPos, arr.length);
            curPos += arr.length;
        }
        return results;
    }

    private List<byte[]> splitInputBytes(final byte[] input, final int... args) {
        List<byte[]> result = new ArrayList<>(args.length + 1);
        int curPos = 0;
        for (int len: args) {
            result.add(Arrays.copyOfRange(input, curPos, curPos + len));
            curPos += len;
        }
        if (curPos != input.length) {
            result.add(Arrays.copyOfRange(input, curPos, input.length));
        }
        return result;
    }
}
