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

package org.apache.shardingsphere.infra.util.regex;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.regex.Pattern;

/**
 * Regex utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RegexUtils {
    
    private static final Pattern SINGLE_CHARACTER_PATTERN = Pattern.compile("([^\\\\])_|^_");
    
    private static final Pattern SINGLE_CHARACTER_ESCAPE_PATTERN = Pattern.compile("\\\\_");
    
    private static final Pattern ANY_CHARACTER_PATTERN = Pattern.compile("([^\\\\])%|^%");
    
    private static final Pattern ANY_CHARACTER_ESCAPE_PATTERN = Pattern.compile("\\\\%");
    
    /**
     * Convert like pattern to regex.
     *
     * @param pattern like pattern
     * @return regex
     */
    public static String convertLikePatternToRegex(final String pattern) {
        String result = pattern;
        if (pattern.contains("_")) {
            result = SINGLE_CHARACTER_PATTERN.matcher(result).replaceAll("$1.");
            result = SINGLE_CHARACTER_ESCAPE_PATTERN.matcher(result).replaceAll("_");
        }
        if (pattern.contains("%")) {
            result = ANY_CHARACTER_PATTERN.matcher(result).replaceAll("$1.*");
            result = ANY_CHARACTER_ESCAPE_PATTERN.matcher(result).replaceAll("%");
        }
        return result;
    }
}
