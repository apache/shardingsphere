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

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.shadow.api.shadow.note.NoteShadowAlgorithm;
import org.apache.shardingsphere.shadow.api.shadow.note.PreciseNoteShadowValue;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

/**
 * Simple note shadow algorithm.
 */
@Getter
@Setter
public final class SimpleSQLNoteShadowAlgorithm implements NoteShadowAlgorithm<String> {
    
    private static final String NOTE_SPACE = ",";
    
    private static final String NOTE_ELEMENT_SPACE = "=";
    
    private static final String NOTE_PREFIX = "/*";
    
    private static final String NOTE_SUFFIX = "*/";
    
    private Properties props = new Properties();
    
    @Override
    public String getType() {
        return "SIMPLE_NOTE";
    }
    
    @Override
    public void init() {
        checkPropsSize();
    }
    
    private void checkPropsSize() {
        Preconditions.checkState(!props.isEmpty(), "Simple note shadow algorithm props cannot be empty.");
    }
    
    @Override
    public boolean isShadow(final Collection<String> shadowTableNames, final PreciseNoteShadowValue<String> noteShadowValue) {
        if (!shadowTableNames.contains(noteShadowValue.getLogicTableName())) {
            return false;
        }
        Optional<Map<String, String>> noteOptional = parseNote(noteShadowValue.getSqlNoteValue());
        return noteOptional.filter(stringStringMap -> props.entrySet().stream().allMatch(entry -> Objects.equals(entry.getValue(), stringStringMap.get(String.valueOf(entry.getKey()))))).isPresent();
    }
    
    private Optional<Map<String, String>> parseNote(final String sqlNoteValue) {
        String noteValue = sqlNoteValue.trim();
        if (noteValue.startsWith(NOTE_PREFIX)) {
            noteValue = removePrefix(noteValue);
        }
        if (noteValue.endsWith(NOTE_SUFFIX)) {
            noteValue = removeSuffix(noteValue);
        }
        if (isBlank(noteValue)) {
            return Optional.empty();
        } else {
            noteValue = noteValue.trim();
            String[] noteElements = noteValue.split(NOTE_SPACE);
            Map<String, String> result = new HashMap<>(noteElements.length);
            for (String each : noteElements) {
                String temp = each;
                temp = temp.trim();
                String[] split = temp.split(NOTE_ELEMENT_SPACE);
                result.put(split[0].trim(), split[1].trim());
            }
            return Optional.of(result);
        }
    }
    
    private static String removePrefix(final String input) {
        return input.substring(NOTE_PREFIX.length());
    }
    
    private static String removeSuffix(final String input) {
        return input.substring(0, input.length() - NOTE_SUFFIX.length());
    }
    
    private boolean isBlank(final String noteValue) {
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
