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
import org.apache.shardingsphere.infra.algorithm.core.exception.AlgorithmInitializationException;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.mask.algorithm.MaskAlgorithmPropertiesChecker;
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
    
    @Override
    public void init(final Properties props) {
        fromX = createFromX(props);
        toY = createToY(props);
        replaceChar = createReplaceChar(props);
        ShardingSpherePreconditions.checkState(fromX <= toY, () -> new AlgorithmInitializationException(this, "fromX must be less than or equal to toY"));
    }
    
    private Integer createFromX(final Properties props) {
        MaskAlgorithmPropertiesChecker.checkPositiveInteger(props, FROM_X, this);
        return Integer.parseInt(props.getProperty(FROM_X));
    }
    
    private Integer createToY(final Properties props) {
        MaskAlgorithmPropertiesChecker.checkPositiveInteger(props, TO_Y, this);
        return Integer.parseInt(props.getProperty(TO_Y));
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
        char[] chars = result.toCharArray();
        for (int i = 0; i < Math.min(fromX, result.length()); i++) {
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
