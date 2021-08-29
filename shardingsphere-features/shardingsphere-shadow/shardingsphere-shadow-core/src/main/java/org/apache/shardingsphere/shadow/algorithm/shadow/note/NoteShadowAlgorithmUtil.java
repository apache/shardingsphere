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

package org.apache.shardingsphere.shadow.algorithm.shadow.note;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Note shadow algorithm util.
 */
public final class NoteShadowAlgorithmUtil {
    
    private static final String SIMPLE_SQL_NOTE_SPACE = ",";
    
    private static final String SIMPLE_SQL_NOTE_ELEMENT_SPACE = "=";
    
    private static final String SIMPLE_SQL_NOTE_PREFIX = "/*";
    
    private static final String SIMPLE_SQL_NOTE_SUFFIX = "*/";
    
    /**
     * Parse simple SQL note.
     *
     * @param sqlNoteValue SQL note value
     * @return note map
     */
    public static Optional<Map<String, String>> parseSimpleSQLNote(final String sqlNoteValue) {
        String noteValue = sqlNoteValue.trim();
        if (noteValue.startsWith(SIMPLE_SQL_NOTE_PREFIX)) {
            noteValue = removePrefix(noteValue);
        }
        if (noteValue.endsWith(SIMPLE_SQL_NOTE_SUFFIX)) {
            noteValue = removeSuffix(noteValue);
        }
        if (isBlank(noteValue)) {
            return Optional.empty();
        } else {
            noteValue = noteValue.trim();
            String[] noteElements = noteValue.split(SIMPLE_SQL_NOTE_SPACE);
            Map<String, String> result = new HashMap<>(noteElements.length);
            for (String each : noteElements) {
                String temp = each;
                temp = temp.trim();
                String[] split = temp.split(SIMPLE_SQL_NOTE_ELEMENT_SPACE);
                result.put(split[0].trim(), split[1].trim());
            }
            return Optional.of(result);
        }
    }
    
    private static String removePrefix(final String input) {
        return input.substring(SIMPLE_SQL_NOTE_PREFIX.length());
    }
    
    private static String removeSuffix(final String input) {
        return input.substring(0, input.length() - SIMPLE_SQL_NOTE_SUFFIX.length());
    }
    
    private static boolean isBlank(final String noteValue) {
        final int strLen = noteValue == null ? 0 : noteValue.length();
        if (strLen == 0) {
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
