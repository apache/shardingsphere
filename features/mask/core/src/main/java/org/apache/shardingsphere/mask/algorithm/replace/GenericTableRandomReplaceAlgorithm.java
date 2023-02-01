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
import lombok.Getter;
import org.apache.shardingsphere.mask.spi.MaskAlgorithm;

import java.util.Properties;
import java.util.Random;

/**
 * Generic table random replace algorithm.
 */
public final class GenericTableRandomReplaceAlgorithm implements MaskAlgorithm<Object, String> {
    
    private static final String UPPERCASE_LETTER_CODES = "uppercase-letter-codes";
    
    private static final String LOWERCASE_LETTER_CODES = "lowercase-letter-codes";
    
    private static final String DIGITAL_RANDOM_CODES = "digital-random-codes";
    
    private static final String SPECIAL_CODES = "special-codes";
    
    private String uppercaseLetterCodes = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    
    private String lowercaseLetterCodes = "abcdefghijklmnopqrstuvwxyz";
    
    private String digitalRandomCodes = "0123456789";
    
    private String specialCodes = "~!@#$%^&*:<>|";
    
    @Getter
    private Properties props;
    
    @Override
    public void init(final Properties props) {
        this.props = props;
        
        if (isLeastOneCharConfig(props, UPPERCASE_LETTER_CODES)) {
            this.uppercaseLetterCodes = props.getProperty(UPPERCASE_LETTER_CODES);
        }
        if (isLeastOneCharConfig(props, LOWERCASE_LETTER_CODES)) {
            this.lowercaseLetterCodes = props.getProperty(LOWERCASE_LETTER_CODES);
        }
        if (isLeastOneCharConfig(props, DIGITAL_RANDOM_CODES)) {
            this.digitalRandomCodes = props.getProperty(DIGITAL_RANDOM_CODES);
        }
        if (isLeastOneCharConfig(props, SPECIAL_CODES)) {
            this.specialCodes = props.getProperty(SPECIAL_CODES);
        }
    }
    
    private boolean isLeastOneCharConfig(final Properties props, final String atLeastOneCharConfigKey) {
        if (!props.containsKey(atLeastOneCharConfigKey)) {
            return false;
        }
        if (0 == props.getProperty(atLeastOneCharConfigKey).length()) {
            return false;
        }
        return true;
    }
    
    @Override
    public String mask(final Object plainValue) {
        String result = null == plainValue ? null : String.valueOf(plainValue);
        if (Strings.isNullOrEmpty(result)) {
            return result;
        }
        Random random = new Random();
        char[] chars = result.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if ('A' <= c && c <= 'Z') {
                chars[i] = uppercaseLetterCodes.charAt(random.nextInt(uppercaseLetterCodes.length()));
            } else if ('a' <= c && c <= 'z') {
                chars[i] = lowercaseLetterCodes.charAt(random.nextInt(lowercaseLetterCodes.length()));
            } else if ('0' <= c && c <= '9') {
                chars[i] = digitalRandomCodes.charAt(random.nextInt(digitalRandomCodes.length()));
            } else {
                chars[i] = specialCodes.charAt(random.nextInt(specialCodes.length()));
            }
        }
        return new String(chars);
    }
    
    @Override
    public String getType() {
        return "GENERIC_TABLE_RANDOM_REPLACE";
    }
}
