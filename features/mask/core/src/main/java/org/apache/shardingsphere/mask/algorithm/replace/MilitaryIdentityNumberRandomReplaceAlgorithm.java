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
import java.util.stream.Collectors;

/**
 * Military identity number random replace algorithm.
 */
public final class MilitaryIdentityNumberRandomReplaceAlgorithm implements MaskAlgorithm<Object, String> {
    
    private static final String TYPE_CODE = "type-codes";
    
    private final Random random = new SecureRandom();
    
    private List<Character> typeCodes;
    
    @Override
    public void init(final Properties props) {
        typeCodes = createTypeCodes(props);
    }
    
    private List<Character> createTypeCodes(final Properties props) {
        MaskAlgorithmPropsChecker.checkAtLeastOneCharConfig(props, TYPE_CODE, getType());
        return Splitter.on(",").trimResults().splitToList(props.getProperty(TYPE_CODE)).stream().map(each -> each.charAt(0)).collect(Collectors.toList());
    }
    
    @Override
    public String mask(final Object plainValue) {
        String result = null == plainValue ? null : String.valueOf(plainValue);
        if (Strings.isNullOrEmpty(result)) {
            return result;
        }
        char[] chars = result.toCharArray();
        chars[0] = typeCodes.get(random.nextInt(typeCodes.size()));
        for (int i = 1; i < chars.length; i++) {
            if (Character.isDigit(chars[i])) {
                chars[i] = Character.forDigit(random.nextInt(10), 10);
            }
        }
        return new String(chars);
    }
    
    @Override
    public String getType() {
        return "MILITARY_IDENTITY_NUMBER_RANDOM_REPLACE";
    }
}
