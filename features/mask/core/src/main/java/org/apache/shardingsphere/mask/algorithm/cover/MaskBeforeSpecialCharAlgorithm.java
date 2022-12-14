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
import com.google.common.base.Strings;
import lombok.Getter;
import org.apache.shardingsphere.mask.spi.MaskAlgorithm;

import java.util.Properties;

/**
 * Mask before special char algorithm.
 */
public final class MaskBeforeSpecialCharAlgorithm implements MaskAlgorithm<Object, String> {
    
    private static final String SPECIAL_CHARACTERS = "special-characters";
    
    private String specialCharacters;
    
    @Getter
    private Properties props;
    
    @Override
    public void init(final Properties props) {
        this.props = props;
        this.specialCharacters = createSpecialCharacters(props);
    }
    
    private String createSpecialCharacters(final Properties props) {
        Preconditions.checkArgument(props.containsKey(SPECIAL_CHARACTERS), "%s can not be null.", SPECIAL_CHARACTERS);
        Preconditions.checkArgument(props.getProperty(SPECIAL_CHARACTERS).length() > 0, "%s is not empty.", SPECIAL_CHARACTERS);
        return props.getProperty(SPECIAL_CHARACTERS);
    }
    
    @Override
    public String mask(final Object plainValue) {
        String result = null == plainValue ? null : String.valueOf(plainValue);
        if (Strings.isNullOrEmpty(result)) {
            return result;
        }
        int index = result.indexOf(specialCharacters);
        char[] chars = result.toCharArray();
        for (int i = 0; i < index; i++) {
            chars[i] = '*';
        }
        return new String(chars);
    }
    
    @Override
    public String getType() {
        return "MASK_BEFORE_SPECIAL_CHAR";
    }
}
