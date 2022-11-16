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

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.io.CharStreams;
import com.google.common.io.LineProcessor;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.shardingsphere.encrypt.exception.algorithm.EncryptAlgorithmInitializationException;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.encrypt.spi.context.EncryptContext;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Char digest like encrypt algorithm.
 */
public final class CharDigestLikeEncryptAlgorithm implements EncryptAlgorithm<Object, String> {
    
    private static final String DELTA = "delta";
    
    private static final String MASK = "mask";
    
    private static final String START = "start";
    
    private static final String DICT = "dict";
    
    private static final int DEFAULT_DELTA = 1;
    
    private static final int DEFAULT_MASK = 0b1111_0111_1101;
    
    private static final int DEFAULT_START = 0x4e00;
    
    private static final int MAX_NUMERIC_LETTER_CHAR = 255;
    
    @Getter
    private Properties props;
    
    private int delta;
    
    private int mask;
    
    private int start;
    
    private Map<Character, Integer> charIndexes;
    
    @Override
    public void init(final Properties props) {
        this.props = props;
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
            } catch (NumberFormatException ex) {
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
            } catch (NumberFormatException ex) {
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
            } catch (NumberFormatException ex) {
                throw new EncryptAlgorithmInitializationException("CHAR_DIGEST_LIKE", "start can only be a decimal number");
            }
        }
        return DEFAULT_START;
    }
    
    private Map<Character, Integer> createCharIndexes(final Properties props) {
        String dictContent = props.containsKey(DICT) && !Strings.isNullOrEmpty(props.getProperty(DICT)) ? props.getProperty(DICT) : initDefaultDict();
        Map<Character, Integer> result = new HashMap<>(dictContent.length(), 1);
        for (int index = 0; index < dictContent.length(); index++) {
            result.put(dictContent.charAt(index), index);
        }
        return result;
    }
    
    @SneakyThrows
    private String initDefaultDict() {
        InputStream inputStream = CharDigestLikeEncryptAlgorithm.class.getClassLoader().getResourceAsStream("like/CommonChineseCharacters.dict");
        LineProcessor<String> lineProcessor = new LineProcessor<String>() {
            
            private StringBuilder builder = new StringBuilder();
            
            @Override
            public boolean processLine(final String line) {
                if (line.startsWith("#") || 0 == line.length()) {
                    return true;
                } else {
                    builder.append(line);
                    return false;
                }
            }
            
            @Override
            public String getResult() {
                return builder.toString();
            }
        };
        return CharStreams.readLines(new InputStreamReader(inputStream, Charsets.UTF_8), lineProcessor);
    }
    
    @Override
    public String encrypt(final Object plainValue, final EncryptContext encryptContext) {
        return null == plainValue ? null : digest(String.valueOf(plainValue));
    }
    
    private String digest(final String plainValue) {
        StringBuilder result = new StringBuilder(plainValue.length());
        for (char each : plainValue.toCharArray()) {
            char maskedChar = getMaskedChar(each);
            if ('%' == maskedChar) {
                result.append(each);
            } else {
                result.append(maskedChar);
            }
        }
        return result.toString();
    }
    
    private char getMaskedChar(final char originalChar) {
        if ('%' == originalChar) {
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
    public String decrypt(final String cipherValue, final EncryptContext encryptContext) {
        return cipherValue;
    }
    
    @Override
    public String getType() {
        return "CHAR_DIGEST_LIKE";
    }
}
