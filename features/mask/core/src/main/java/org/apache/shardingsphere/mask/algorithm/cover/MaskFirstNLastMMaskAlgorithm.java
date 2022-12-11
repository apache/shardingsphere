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

import com.google.common.base.Preconditions;
import lombok.Getter;
import org.apache.shardingsphere.mask.spi.MaskAlgorithm;

import java.util.Properties;

/**
 * MASK_FIRST_N_LAST_M.
 */
public final class MaskFirstNLastMMaskAlgorithm implements MaskAlgorithm<Object, String> {
    
    private static final String N = "n";
    
    private static final String M = "m";
    
    private static final String REPLACE_CHAR = "replace-char";
    
    @Getter
    private Properties props;
    
    private Integer n;
    
    private Integer m;
    
    private Character replaceChar;
    
    @Override
    public String mask(final Object plainValue) {
        String value = plainValue == null ? "" : plainValue.toString();
        if ("".equals(value)) {
            return value;
        }
        char[] chars = value.toCharArray();
        for (int i = 0, len = Math.min(n, chars.length); i < len; i++) {
            chars[i] = replaceChar;
        }
        for (int i = chars.length - Math.min(m, chars.length); i < chars.length; i++) {
            chars[i] = replaceChar;
        }
        return new String(chars);
    }
    
    @Override
    public void init(final Properties props) {
        this.props = props;
        this.n = getN(props);
        this.m = getM(props);
        this.replaceChar = getReplaceChar(props);
    }
    
    private Integer getN(final Properties props) {
        Preconditions.checkArgument(props.containsKey(N), "%s can not be null.", N);
        return Integer.parseInt(props.getProperty(N));
    }
    
    private Integer getM(final Properties props) {
        Preconditions.checkArgument(props.containsKey(M), "%s can not be null.", M);
        return Integer.parseInt(props.getProperty(M));
    }
    
    private Character getReplaceChar(final Properties props) {
        Preconditions.checkArgument(props.containsKey(REPLACE_CHAR), "%s can not be null.", REPLACE_CHAR);
        Preconditions.checkArgument(props.getProperty(REPLACE_CHAR).length() == 1, "%s length must be 1.", REPLACE_CHAR);
        return props.getProperty(REPLACE_CHAR).charAt(0);
    }
    
    @Override
    public String getType() {
        return "MASK_FIRST_N_LAST_M";
    }
}
