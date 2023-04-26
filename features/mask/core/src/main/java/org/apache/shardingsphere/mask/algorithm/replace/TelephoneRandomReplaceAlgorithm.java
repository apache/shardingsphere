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

package org.apache.shardingsphere.mask.algorithm.replace;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import lombok.SneakyThrows;
import org.apache.shardingsphere.mask.exception.algorithm.MaskAlgorithmInitializationException;
import org.apache.shardingsphere.mask.spi.MaskAlgorithm;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * Telephone random replace algorithm.
 */
public final class TelephoneRandomReplaceAlgorithm implements MaskAlgorithm<Object, String> {
    
    private static final String NETWORK_NUMBERS = "network-numbers";
    
    private final Random random = new SecureRandom();
    
    private List<String> networkNumbers;
    
    @Override
    public void init(final Properties props) {
        networkNumbers = createNetworkNumbers(props);
    }
    
    private List<String> createNetworkNumbers(final Properties props) {
        String networkNumbers = props.containsKey(NETWORK_NUMBERS) && !Strings.isNullOrEmpty(props.getProperty(NETWORK_NUMBERS)) ? props.getProperty(NETWORK_NUMBERS) : initDefaultNetworkNumbers();
        return Splitter.on(",").trimResults().splitToList(networkNumbers).stream().map(this::getNetworkNumber).distinct().collect(Collectors.toList());
    }
    
    @SneakyThrows(IOException.class)
    private String initDefaultNetworkNumbers() {
        StringBuilder result = new StringBuilder();
        try (
                InputStream inputStream = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResourceAsStream("algorithm/replace/chinese_network_numbers.dict"));
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
    
    private String getNetworkNumber(final String networkNumber) {
        try {
            Integer.parseInt(networkNumber);
            return networkNumber;
        } catch (final NumberFormatException ignored) {
            throw new MaskAlgorithmInitializationException(getType(), String.format("network-number %s can only be integer number", networkNumber));
        }
    }
    
    @Override
    public String mask(final Object plainValue) {
        String result = null == plainValue ? null : String.valueOf(plainValue);
        if (Strings.isNullOrEmpty(result)) {
            return result;
        }
        return networkNumbers.stream().filter(result::startsWith).findFirst().map(each -> createRandValue(result, each)).orElse(result);
    }
    
    private String createRandValue(final String plainValue, final String networkNumber) {
        StringBuilder result = new StringBuilder();
        result.append(networkNumbers.get(random.nextInt(networkNumbers.size())));
        for (int i = networkNumber.length(); i < plainValue.length(); i++) {
            result.append(Character.forDigit(random.nextInt(10), 10));
        }
        return result.toString();
    }
    
    @Override
    public String getType() {
        return "TELEPHONE_RANDOM_REPLACE";
    }
}
