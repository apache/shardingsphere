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
import lombok.Getter;
import org.apache.shardingsphere.mask.algorithm.MaskAlgorithmUtil;
import org.apache.shardingsphere.mask.spi.MaskAlgorithm;

import java.util.Properties;

/**
 * Keep from x to y algorithm.
 */
public final class KeepFromXToYMaskAlgorithm implements MaskAlgorithm<Object, String> {
    
    private static final String FROM_X = "from-x";
    
    private static final String TO_Y = "to-y";
    
    private static final String REPLACE_CHAR = "replace-char";
    
    private Integer fromX;
    
    private Integer toY;
    
    private Character replaceChar;
    
    @Getter
    private Properties props;
    
    @Override
    public void init(final Properties props) {
        this.props = props;
        this.fromX = createFromX(props);
        this.toY = createToY(props);
        this.replaceChar = createReplaceChar(props);
    }
    
    private Integer createFromX(final Properties props) {
        MaskAlgorithmUtil.checkIntegerTypeConfig(props, FROM_X, getType());
        return Integer.parseInt(props.getProperty(FROM_X));
    }
    
    private Integer createToY(final Properties props) {
        MaskAlgorithmUtil.checkIntegerTypeConfig(props, TO_Y, getType());
        return Integer.parseInt(props.getProperty(TO_Y));
    }
    
    private Character createReplaceChar(final Properties props) {
        MaskAlgorithmUtil.checkSingleCharConfig(props, REPLACE_CHAR, getType());
        return props.getProperty(REPLACE_CHAR).charAt(0);
    }
    
    @Override
    public String mask(final Object plainValue) {
        String result = null == plainValue ? null : String.valueOf(plainValue);
        if (Strings.isNullOrEmpty(result)) {
            return result;
        }
        if (result.length() <= fromX || toY <= fromX) {
            return result;
        }
        char[] chars = result.toCharArray();
        for (int i = 0; i < fromX; i++) {
            chars[i] = replaceChar;
        }
        for (int i = toY + 1; i < chars.length; i++) {
            chars[i] = replaceChar;
        }
        return new String(chars);
    }
    
    @Override
    public String getType() {
        return "KEEP_FROM_X_TO_Y";
    }
}
