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

package org.apache.shardingsphere.sql.parser.sql.common.constant;

import com.google.common.base.Strings;
import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Quote character.
 */
@RequiredArgsConstructor
@Getter
public enum QuoteCharacter {
    
    BACK_QUOTE("`", "`"),
    
    SINGLE_QUOTE("'", "'"),
    
    QUOTE("\"", "\""),
    
    BRACKETS("[", "]"),
    
    NONE("", "");
    
    private final String startDelimiter;
    
    private final String endDelimiter;
    
    /**
     * Get quote character.
     * 
     * @param value value to be get quote character
     * @return value of quote character
     */
    public static QuoteCharacter getQuoteCharacter(final String value) {
        if (Strings.isNullOrEmpty(value)) {
            return NONE;
        }
        return Arrays.stream(values()).filter(each -> NONE != each && each.startDelimiter.charAt(0) == value.charAt(0)).findFirst().orElse(NONE);
    }
}
