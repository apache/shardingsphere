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

package org.apache.shardingsphere.encrypt.algorithm.like;

import com.google.common.base.Strings;
import lombok.SneakyThrows;
import org.apache.shardingsphere.encrypt.api.context.EncryptContext;
import org.apache.shardingsphere.encrypt.exception.algorithm.EncryptAlgorithmInitializationException;
import org.apache.shardingsphere.encrypt.spi.LikeEncryptAlgorithm;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Char digest like encrypt algorithm.
 */
public final class CharDigestLikeEncryptAlgorithm implements LikeEncryptAlgorithm<Object, String> {
    
    private static final String DELTA = "delta";
    
    private static final String MASK = "mask";
    
    private static final String START = "start";
    
    private static final String DICT = "dict";
    
    private static final int DEFAULT_DELTA = 1;
    
    private static final int DEFAULT_MASK = 0b1111_0111_1101;
    
    private static final int DEFAULT_START = 0x4e00;
    
    private static final int MAX_NUMERIC_LETTER_CHAR = 255;
    
    private int delta;
    
    private int mask;
    
    private int start;
    
    private Map<Character, Integer> charIndexes;
    
    @Override
    public void init(final Properties props) {
        delta = createDelta(props);
        mask = createMask(props);
        start = createStart(props);
        charIndexes = createCharIndexes(props);
    }
    
    private int createDelta(final Properties props) {
        if (props.containsKey(DELTA)) {
            String delta = props.getProperty(DELTA);
            try {
                return Integer.parseInt(delta);
            } catch (final NumberFormatException ex) {
                throw new EncryptAlgorithmInitializationException("CHAR_DIGEST_LIKE", "delta can only be a decimal number");
            }
        }
        return DEFAULT_DELTA;
    }
    
    private int createMask(final Properties props) {
        if (props.containsKey(MASK)) {
            String mask = props.getProperty(MASK);
            try {
                return Integer.parseInt(mask);
            } catch (final NumberFormatException ex) {
                throw new EncryptAlgorithmInitializationException("CHAR_DIGEST_LIKE", "mask can only be a decimal number");
            }
        }
        return DEFAULT_MASK;
    }
    
    private int createStart(final Properties props) {
        if (props.containsKey(START)) {
            String start = props.getProperty(START);
            try {
                return Integer.parseInt(start);
            } catch (final NumberFormatException ex) {
                throw new EncryptAlgorithmInitializationException("CHAR_DIGEST_LIKE", "start can only be a decimal number");
            }
        }
        return DEFAULT_START;
    }
    
    private Map<Character, Integer> createCharIndexes(final Properties props) {
        String dictContent = props.containsKey(DICT) && !Strings.isNullOrEmpty(props.getProperty(DICT)) ? props.getProperty(DICT) : initDefaultDict();
        return IntStream.range(0, dictContent.length()).boxed().collect(Collectors.toMap(dictContent::charAt, index -> index, (a, b) -> b));
    }
    
    @SneakyThrows(IOException.class)
    private String initDefaultDict() {
        StringBuilder builder = new StringBuilder();
        try (
                InputStream inputStream = Objects.requireNonNull(CharDigestLikeEncryptAlgorithm.class.getClassLoader().getResourceAsStream("algorithm/like/common_chinese_character.dict"));
                Scanner scanner = new Scanner(inputStream)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    builder.append(line);
                }
            }
        }
        return builder.toString();
    }
    
    @Override
    public String encrypt(final Object plainValue, final EncryptContext encryptContext) {
        return null == plainValue ? null : digest(String.valueOf(plainValue));
    }
    
    private String digest(final String plainValue) {
        StringBuilder result = new StringBuilder(plainValue.length());
        for (char each : plainValue.toCharArray()) {
            char maskedChar = getMaskedChar(each);
            if ('%' == maskedChar || '_' == maskedChar) {
                result.append(each);
            } else {
                result.append(maskedChar);
            }
        }
        return result.toString();
    }
    
    private char getMaskedChar(final char originalChar) {
        if ('%' == originalChar || '_' == originalChar) {
            return originalChar;
        }
        if (originalChar <= MAX_NUMERIC_LETTER_CHAR) {
            return (char) ((originalChar + delta) & mask);
        }
        if (charIndexes.containsKey(originalChar)) {
            return (char) (((charIndexes.get(originalChar) + delta) & mask) + start);
        }
        return (char) (((originalChar + delta) & mask) + start);
    }
    
    @Override
    public String getType() {
        return "CHAR_DIGEST_LIKE";
    }
}
