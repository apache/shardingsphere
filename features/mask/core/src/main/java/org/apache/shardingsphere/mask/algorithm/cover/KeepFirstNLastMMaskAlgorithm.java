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
public class KeepFirstNLastMMaskAlgorithm implements MaskAlgorithm<Object, String> {
    
    private static final String START_INDEX = "start-index";
    
    private static final String STOP_INDEX = "stop-index";
    
    private static final String REPLACE_CHAR = "replace-char";
    
    private Integer startIndex;
    
    private Integer stopIndex;
    
    private String replaceChar;
    
    @Getter
    private Properties props;
    
    @Override
    public String mask(final Object plainValue) {
        String value = Optional.ofNullable(plainValue).orElse("").toString();
        if ("".equals(value) || value.length() <= startIndex + stopIndex) {
            return value;
        }
        StringBuilder sb = new StringBuilder(value);
        sb.replace(startIndex, value.length() - stopIndex, replaceChar);
        return sb.toString();
    }
    
    private Integer initStartIndex(final Properties props) {
        Preconditions.checkArgument(props.containsKey(START_INDEX), "%s can not be null.", START_INDEX);
        return Integer.parseInt(props.getProperty(START_INDEX));
    }
    
    private Integer initStopIndex(final Properties props) {
        Preconditions.checkArgument(props.containsKey(STOP_INDEX), "%s can not be null.", STOP_INDEX);
        return Integer.parseInt(props.getProperty(STOP_INDEX));
    }
    
    private String initReplaceChar(final Properties props) {
        Preconditions.checkArgument(props.containsKey(REPLACE_CHAR), "%s can not be null.", REPLACE_CHAR);
        return props.getProperty(REPLACE_CHAR);
    }
    
    @Override
    public void init(final Properties props) {
        this.props = props;
        this.startIndex = initStartIndex(props);
        this.stopIndex = initStopIndex(props);
        this.replaceChar = initReplaceChar(props);
    }
    
    @Override
    public String getType() {
        return "KEEP_FIRST_N_LAST_M";
    }
}
