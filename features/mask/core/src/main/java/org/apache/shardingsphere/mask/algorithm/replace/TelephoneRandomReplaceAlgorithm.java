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
import lombok.Getter;
import org.apache.shardingsphere.mask.algorithm.MaskAlgorithmUtil;
import org.apache.shardingsphere.mask.exception.algorithm.MaskAlgorithmInitializationException;
import org.apache.shardingsphere.mask.spi.MaskAlgorithm;

import java.util.Collection;
import java.util.Comparator;
import java.util.Properties;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Telephone random replace algorithm.
 */
public final class TelephoneRandomReplaceAlgorithm implements MaskAlgorithm<Object, String> {
    
    private static final String NETWORK_NUMBERS = "network-numbers";
    
    private Collection<String> networkNumbers;
    
    private Collection<Integer> reverseOrderedNetworkNumberLengths;
    
    @Getter
    private Properties props;
    
    @Override
    public void init(final Properties props) {
        this.props = props;
        this.networkNumbers = createNetworkNumbers(props);
        this.reverseOrderedNetworkNumberLengths = createReverseOrderedNetworkNumberLengths(networkNumbers);
    }
    
    private Collection<String> createNetworkNumbers(final Properties props) {
        MaskAlgorithmUtil.checkAtLeastOneCharConfig(props, NETWORK_NUMBERS, getType());
        return Splitter.on(",").trimResults().splitToList(props.getProperty(NETWORK_NUMBERS)).stream().map(this::getNetworkNumber).collect(Collectors.toSet());
    }
    
    private String getNetworkNumber(final String networkNumber) {
        try {
            Integer.parseInt(networkNumber);
            return networkNumber;
        } catch (final NumberFormatException ex) {
            throw new MaskAlgorithmInitializationException(getType(), String.format("network-number %s can only be integer number.", networkNumber));
        }
    }
    
    private Collection<Integer> createReverseOrderedNetworkNumberLengths(final Collection<String> networkNumbers) {
        return networkNumbers.stream().map(String::length).distinct().sorted(Comparator.reverseOrder()).collect(Collectors.toList());
    }
    
    @Override
    public String mask(final Object plainValue) {
        String result = null == plainValue ? null : String.valueOf(plainValue);
        if (Strings.isNullOrEmpty(result)) {
            return result;
        }
        Random random = new Random();
        char[] chars = result.toCharArray();
        for (Integer each : reverseOrderedNetworkNumberLengths) {
            if (networkNumbers.contains(result.substring(0, each))) {
                for (int i = each; i < chars.length; i++) {
                    chars[i] = Character.forDigit(random.nextInt(10), 10);
                }
                break;
            }
        }
        return new String(chars);
    }
    
    @Override
    public String getType() {
        return "TELEPHONE_RANDOM_REPLACE";
    }
}
