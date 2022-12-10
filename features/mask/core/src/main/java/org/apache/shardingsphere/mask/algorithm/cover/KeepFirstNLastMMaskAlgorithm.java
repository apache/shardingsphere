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

import java.util.Optional;
import java.util.Properties;

/**
 * KEEP_FIRST_N_LAST_M Algorithm.
 */
public final class KeepFirstNLastMMaskAlgorithm implements MaskAlgorithm<Object, String> {
    
    private static final String N = "n";
    
    private static final String M = "m";
    
    private static final String REPLACE_CHAR = "replace-char";
    
    private Integer n;
    
    private Integer m;
    
    private Character replaceChar;
    
    @Getter
    private Properties props;
    
    @Override
    public String mask(final Object plainValue) {
        String value = Optional.ofNullable(plainValue).orElse("").toString();
        if ("".equals(value) || value.length() <= n + m) {
            return value;
        }
        char[] chars = value.toCharArray();
        for (int i = n; i < value.length() - m; i++) {
            chars[i] = replaceChar;
        }
        return new String(chars);
    }
    
    private Integer initNIndex(final Properties props) {
        Preconditions.checkArgument(props.containsKey(N), "%s can not be null.", N);
        return Integer.parseInt(props.getProperty(N));
    }
    
    private Integer initMIndex(final Properties props) {
        Preconditions.checkArgument(props.containsKey(M), "%s can not be null.", M);
        return Integer.parseInt(props.getProperty(M));
    }
    
    private Character initReplaceChar(final Properties props) {
        Preconditions.checkArgument(props.containsKey(REPLACE_CHAR), "%s can not be null.", REPLACE_CHAR);
        Preconditions.checkArgument(props.getProperty(REPLACE_CHAR).length() == 1, "%s length must be 1.", REPLACE_CHAR);
        return props.getProperty(REPLACE_CHAR).charAt(0);
    }
    
    @Override
    public void init(final Properties props) {
        this.props = props;
        this.n = initNIndex(props);
        this.m = initMIndex(props);
        this.replaceChar = initReplaceChar(props);
    }
    
    @Override
    public String getType() {
        return "KEEP_FIRST_N_LAST_M";
    }
}
