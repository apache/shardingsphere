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

package org.apache.shardingsphere.shadow.algorithm.shadow.hint;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Hint shadow algorithm util.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShadowHintExtractor {
    
    private static final String SHADOW_HINT_SPACE = ",";
    
    private static final String SHADOW_HINT_ELEMENT_SPACE = ":";
    
    private static final String SQL_COMMENT_PREFIX = "/*";
    
    private static final String SQL_COMMENT_SUFFIX = "*/";
    
    private static final String SQL_COMMENT_TRACE_SPAN = "@TRACE_CONTEXT@";
    
    /**
     * Extract simple hint map from SQL comment.
     *
     * @param sqlComment SQL comment
     * @return simple hint map
     */
    public static Optional<Map<String, String>> extractSimpleHint(final String sqlComment) {
        String sqlCommentValue = trim(sqlComment);
        if (isBlank(sqlCommentValue)) {
            return Optional.empty();
        }
        return extractElements(sqlCommentValue);
    }
    
    private static String trim(final String sqlComment) {
        String result = sqlComment.trim();
        if (result.startsWith(SQL_COMMENT_PREFIX)) {
            result = removePrefix(result);
        }
        if (result.endsWith(SQL_COMMENT_SUFFIX)) {
            result = removeSuffix(result);
        }
        result = result.trim();
        return trimTrace(result);
    }
    
    private static String removePrefix(final String input) {
        return input.substring(SQL_COMMENT_PREFIX.length());
    }
    
    private static String removeSuffix(final String input) {
        return input.substring(0, input.length() - SQL_COMMENT_SUFFIX.length());
    }
    
    private static String trimTrace(final String sqlComment) {
        int startIndex = sqlComment.indexOf(SQL_COMMENT_TRACE_SPAN);
        if (startIndex == -1) {
            return sqlComment;
        }
        int traceLen = SQL_COMMENT_TRACE_SPAN.length();
        int fromIndex = startIndex + traceLen;
        int endIndex = sqlComment.indexOf(SQL_COMMENT_TRACE_SPAN, fromIndex);
        if (endIndex == -1) {
            return sqlComment;
        }
        String result = sqlComment.substring(0, startIndex) + sqlComment.substring(endIndex + traceLen);
        return result.trim();
    }
    
    private static Optional<Map<String, String>> extractElements(final String sqlComment) {
        String[] noteElements = sqlComment.split(SHADOW_HINT_SPACE);
        Map<String, String> result = new HashMap<>(noteElements.length);
        for (String each : noteElements) {
            String temp = each;
            temp = temp.trim();
            String[] split = temp.split(SHADOW_HINT_ELEMENT_SPACE);
            if (2 == split.length) {
                result.put(split[0].trim(), split[1].trim());
            }
        }
        return Optional.of(result);
    }
    
    private static boolean isBlank(final String noteValue) {
        final int strLen = null == noteValue ? 0 : noteValue.length();
        if (0 ==strLen) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(noteValue.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
