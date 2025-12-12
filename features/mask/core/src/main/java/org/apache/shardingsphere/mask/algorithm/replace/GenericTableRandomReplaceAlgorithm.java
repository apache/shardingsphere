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
import org.apache.shardingsphere.infra.algorithm.core.exception.AlgorithmInitializationException;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.mask.spi.MaskAlgorithm;

import java.security.SecureRandom;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Generic table random replace algorithm.
 */
public final class GenericTableRandomReplaceAlgorithm implements MaskAlgorithm<Object, String> {
    
    private static final String UPPERCASE_LETTER_CODES = "uppercase-letter-codes";
    
    private static final String LOWERCASE_LETTER_CODES = "lowercase-letter-codes";
    
    private static final String DIGITAL_CODES = "digital-codes";
    
    private static final String SPECIAL_CODES = "special-codes";
    
    private static final String DEFAULT_UPPERCASE_LETTER_CODES = "A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z";
    
    private static final String DEFAULT_LOWERCASE_LETTER_CODES = "a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z";
    
    private static final String DEFAULT_DIGITAL_CODES = "0,1,2,3,4,5,6,7,8,9";
    
    private static final String DEFAULT_SPECIAL_CODES = "~,!,@,#,$,%,^,&,*,:,<,>,|";
    
    private final Random random = new SecureRandom();
    
    private List<Character> uppercaseLetterCodes;
    
    private List<Character> lowercaseLetterCodes;
    
    private List<Character> digitalCodes;
    
    private List<Character> specialCodes;
    
    @Override
    public void init(final Properties props) {
        uppercaseLetterCodes = splitPropsToList(props.getProperty(UPPERCASE_LETTER_CODES, DEFAULT_UPPERCASE_LETTER_CODES));
        lowercaseLetterCodes = splitPropsToList(props.getProperty(LOWERCASE_LETTER_CODES, DEFAULT_LOWERCASE_LETTER_CODES));
        digitalCodes = splitPropsToList(props.getProperty(DIGITAL_CODES, DEFAULT_DIGITAL_CODES));
        ShardingSpherePreconditions.checkNotEmpty(digitalCodes, () -> new AlgorithmInitializationException(this, "'%s' must be not empty", DIGITAL_CODES));
        specialCodes = splitPropsToList(props.getProperty(SPECIAL_CODES, DEFAULT_SPECIAL_CODES));
        ShardingSpherePreconditions.checkNotEmpty(specialCodes, () -> new AlgorithmInitializationException(this, "'%s' must be not empty", SPECIAL_CODES));
    }
    
    private List<Character> splitPropsToList(final String props) {
        return Splitter.on(",").trimResults().splitToList(props).stream().map(each -> each.charAt(0)).collect(Collectors.toList());
    }
    
    @HighFrequencyInvocation
    @Override
    public String mask(final Object plainValue) {
        String result = null == plainValue ? null : String.valueOf(plainValue);
        if (Strings.isNullOrEmpty(result)) {
            return result;
        }
        char[] chars = result.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c >= 'A' && c <= 'Z') {
                chars[i] = uppercaseLetterCodes.get(random.nextInt(uppercaseLetterCodes.size()));
            } else if (c >= 'a' && c <= 'z') {
                chars[i] = lowercaseLetterCodes.get(random.nextInt(lowercaseLetterCodes.size()));
            } else if (c >= '0' && c <= '9') {
                chars[i] = digitalCodes.get(random.nextInt(digitalCodes.size()));
            } else {
                chars[i] = specialCodes.get(random.nextInt(specialCodes.size()));
            }
        }
        return new String(chars);
    }
    
    @Override
    public String getType() {
        return "GENERIC_TABLE_RANDOM_REPLACE";
    }
}
