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
import org.apache.shardingsphere.mask.algorithm.MaskAlgorithmPropsChecker;
import org.apache.shardingsphere.mask.spi.MaskAlgorithm;

import java.security.SecureRandom;
import java.util.List;
import java.util.Properties;
import java.util.Random;

/**
 * Landline number replace algorithm.
 */
public final class LandlineNumberRandomAlgorithm implements MaskAlgorithm<Object, String> {
    
    private static final String LANDLINE_NUMBERS = "landline-numbers";
    
    private final Random random = new SecureRandom();
    
    private List<String> landLineNumbers;
    
    @Override
    public void init(final Properties props) {
        landLineNumbers = createLandLineNumbers(props);
    }
    
    private List<String> createLandLineNumbers(final Properties props) {
        MaskAlgorithmPropsChecker.checkAtLeastOneCharConfig(props, LANDLINE_NUMBERS, getType());
        return Splitter.on(",").trimResults().splitToList(props.getProperty(LANDLINE_NUMBERS));
    }
    
    @Override
    public String mask(final Object plainValue) {
        String result = null == plainValue ? null : String.valueOf(plainValue);
        if (Strings.isNullOrEmpty(result)) {
            return result;
        }
        return landLineNumbers.stream().filter(result::startsWith).findFirst().map(each -> createRandValue(result, each)).orElse(result);
    }
    
    private String createRandValue(final String plainValue, final String landLineNumber) {
        StringBuilder result = new StringBuilder();
        result.append(landLineNumbers.get(random.nextInt(landLineNumbers.size())));
        for (int i = landLineNumber.length(); i < plainValue.length(); i++) {
            result.append(Character.forDigit(random.nextInt(10), 10));
        }
        return result.toString();
    }
    
    @Override
    public String getType() {
        return "LANDLINE_NUMBER_RANDOM_REPLACE";
    }
}
