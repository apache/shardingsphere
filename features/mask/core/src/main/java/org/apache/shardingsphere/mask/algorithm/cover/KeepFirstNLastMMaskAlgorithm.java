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

package org.apache.shardingsphere.mask.algorithm.cover;

import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.mask.algorithm.MaskAlgorithmPropertiesChecker;
import org.apache.shardingsphere.mask.spi.MaskAlgorithm;

import java.util.Properties;

/**
 * Keep first n last m algorithm.
 */
public final class KeepFirstNLastMMaskAlgorithm implements MaskAlgorithm<Object, String> {
    
    private static final String FIRST_N = "first-n";
    
    private static final String LAST_M = "last-m";
    
    private static final String REPLACE_CHAR = "replace-char";
    
    private Integer firstN;
    
    private Integer lastM;
    
    private Character replaceChar;
    
    @Override
    public void init(final Properties props) {
        firstN = createFirstN(props);
        lastM = createLastM(props);
        replaceChar = createReplaceChar(props);
    }
    
    private Integer createFirstN(final Properties props) {
        MaskAlgorithmPropertiesChecker.checkPositiveInteger(props, FIRST_N, this);
        return Integer.parseInt(props.getProperty(FIRST_N));
    }
    
    private Integer createLastM(final Properties props) {
        MaskAlgorithmPropertiesChecker.checkPositiveInteger(props, LAST_M, this);
        return Integer.parseInt(props.getProperty(LAST_M));
    }
    
    private Character createReplaceChar(final Properties props) {
        MaskAlgorithmPropertiesChecker.checkSingleChar(props, REPLACE_CHAR, this);
        return props.getProperty(REPLACE_CHAR).charAt(0);
    }
    
    @HighFrequencyInvocation
    @Override
    public String mask(final Object plainValue) {
        String result = null == plainValue ? null : String.valueOf(plainValue);
        if (Strings.isNullOrEmpty(result)) {
            return result;
        }
        if (result.length() < firstN + lastM) {
            return result;
        }
        char[] chars = result.toCharArray();
        for (int i = firstN; i < result.length() - lastM; i++) {
            chars[i] = replaceChar;
        }
        return new String(chars);
    }
    
    @Override
    public String getType() {
        return "KEEP_FIRST_N_LAST_M";
    }
}
