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
 * personal identity number random replace
 * random replacement of the first 6 bits
 */
public class PersonalIdentityNumberRandomReplaceAlgorithm implements MaskAlgorithm<Object, String> {
    
    private static final int MIN_PERSONAL_IDENTITY_DIGIT = 15;
    
    private final Random random = new Random();
    
    @Getter
    private Properties props;
    
    @Override
    public String mask(Object plainValue) {
        String result = null == plainValue ? null : String.valueOf(plainValue);
        if (Strings.isNullOrEmpty(result)) {
            return result;
        }
        if (result.length() < MIN_PERSONAL_IDENTITY_DIGIT) {
            return result;
        }
        char[] chars = result.toCharArray();
        for (int i = 0; i < 6; i++) {
            chars[i] = Character.forDigit(random.nextInt(10), 10);
        }
        return new String(chars);
    }
    
    @Override
    public void init(Properties props) {
    }
    
    @Override
    public String getType() {
        return "PERSONAL_IDENTITY_NUMBER_RANDOM_REPLACE";
    }
}
