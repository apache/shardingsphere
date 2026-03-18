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

package org.apache.shardingsphere.test.it.rewriter.fixture.encrypt;

import com.google.common.base.Strings;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithmMetaData;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.context.AlgorithmSQLContext;
import org.apache.shardingsphere.infra.algorithm.core.exception.AlgorithmInitializationException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class RewriteEncryptLikeAlgorithmFixture implements EncryptAlgorithm {
    
    private static final String DELTA_KEY = "delta";
    
    private static final String MASK_KEY = "mask";
    
    private static final String START_KEY = "start";
    
    private static final String DICT_KEY = "dict";
    
    private static final int DEFAULT_DELTA = 1;
    
    private static final int DEFAULT_MASK = 0b1111_0111_1101;
    
    private static final int DEFAULT_START = 0x4e00;
    
    private static final int MAX_NUMERIC_LETTER_CHAR = 255;
    
    @Getter
    private final EncryptAlgorithmMetaData metaData = new EncryptAlgorithmMetaData(false, true, true);
    
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
        if (props.containsKey(DELTA_KEY)) {
            try {
                return Integer.parseInt(props.getProperty(DELTA_KEY));
            } catch (final NumberFormatException ignored) {
                throw new AlgorithmInitializationException(this, "delta can only be a decimal number");
            }
        }
        return DEFAULT_DELTA;
    }
    
    private int createMask(final Properties props) {
        if (props.containsKey(MASK_KEY)) {
            try {
                return Integer.parseInt(props.getProperty(MASK_KEY));
            } catch (final NumberFormatException ignored) {
                throw new AlgorithmInitializationException(this, "mask can only be a decimal number");
            }
        }
        return DEFAULT_MASK;
    }
    
    private int createStart(final Properties props) {
        if (props.containsKey(START_KEY)) {
            try {
                return Integer.parseInt(props.getProperty(START_KEY));
            } catch (final NumberFormatException ignored) {
                throw new AlgorithmInitializationException(this, "start can only be a decimal number");
            }
        }
        return DEFAULT_START;
    }
    
    private Map<Character, Integer> createCharIndexes(final Properties props) {
        String dictContent = props.containsKey(DICT_KEY) && !Strings.isNullOrEmpty(props.getProperty(DICT_KEY)) ? props.getProperty(DICT_KEY) : initDefaultDict();
        return IntStream.range(0, dictContent.length()).boxed().collect(Collectors.toMap(dictContent::charAt, index -> index, (oldValue, currentValue) -> oldValue));
    }
    
    @SneakyThrows(IOException.class)
    private String initDefaultDict() {
        StringBuilder result = new StringBuilder();
        try (
                InputStream inputStream = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResourceAsStream("algorithm/like/common_chinese_character.dict"));
                Scanner scanner = new Scanner(inputStream)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    result.append(line);
                }
            }
        }
        return result.toString();
    }
    
    @Override
    public String encrypt(final Object plainValue, final AlgorithmSQLContext algorithmSQLContext) {
        return null == plainValue ? null : digest(String.valueOf(plainValue));
    }
    
    @Override
    public Object decrypt(final Object cipherValue, final AlgorithmSQLContext algorithmSQLContext) {
        throw new UnsupportedOperationException(String.format("Algorithm `%s` is unsupported to decrypt", getType()));
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
    public AlgorithmConfiguration toConfiguration() {
        return new AlgorithmConfiguration(getType(), new Properties());
    }
    
    @Override
    public String getType() {
        return "IT.ENCRYPT.LIKE.FIXTURE";
    }
}
