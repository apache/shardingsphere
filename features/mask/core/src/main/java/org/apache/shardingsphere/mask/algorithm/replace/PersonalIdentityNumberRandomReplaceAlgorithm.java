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

import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.mask.exception.algorithm.MaskAlgorithmInitializationException;
import org.apache.shardingsphere.mask.spi.MaskAlgorithm;

import java.security.SecureRandom;
import java.util.Properties;
import java.util.Random;

/**
 * Personal identity number random replace algorithm.
 */
public final class PersonalIdentityNumberRandomReplaceAlgorithm implements MaskAlgorithm<Object, String> {
    
    private static final String ALPHA_TWO_COUNTRY_AREA_CODE = "alpha-two-country-area-code";
    
    private final Random random = new SecureRandom();
    
    private String alphaTwoCountryAreaCode;
    
    @Override
    public void init(final Properties props) {
        alphaTwoCountryAreaCode = props.getProperty(ALPHA_TWO_COUNTRY_AREA_CODE, "CN");
        ShardingSpherePreconditions.checkState(!Strings.isNullOrEmpty(alphaTwoCountryAreaCode),
                () -> new MaskAlgorithmInitializationException(getType(), String.format("%s can not be empty", ALPHA_TWO_COUNTRY_AREA_CODE)));
    }
    
    @Override
    public String mask(final Object plainValue) {
        String result = null == plainValue ? null : String.valueOf(plainValue);
        if (Strings.isNullOrEmpty(result)) {
            return result;
        }
        if ("CN".equals(alphaTwoCountryAreaCode)) {
            return randomReplaceForChinesePersonalIdentityNumber(result);
        }
        return result;
    }
    
    private String randomReplaceForChinesePersonalIdentityNumber(final String result) {
        switch (result.length()) {
            case 15:
                return randomReplaceNumber(result, 6, 12);
            case 18:
                return randomReplaceNumber(result, 6, 14);
            default:
        }
        return result;
    }
    
    private String randomReplaceNumber(final String result, final int from, final int to) {
        char[] chars = result.toCharArray();
        for (int i = from; i < to; i++) {
            chars[i] = Character.forDigit(random.nextInt(10), 10);
        }
        return new String(chars);
    }
    
    @Override
    public String getType() {
        return "PERSONAL_IDENTITY_NUMBER_RANDOM_REPLACE";
    }
}
